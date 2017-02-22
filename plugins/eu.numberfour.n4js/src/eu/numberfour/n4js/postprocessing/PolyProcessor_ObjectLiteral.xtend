/**
 * Copyright (c) 2016 NumberFour AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   NumberFour AG - Initial API and implementation
 */
package eu.numberfour.n4js.postprocessing

import com.google.common.base.Optional
import com.google.inject.Inject
import com.google.inject.Singleton
import eu.numberfour.n4js.n4JS.ObjectLiteral
import eu.numberfour.n4js.n4JS.PropertyAssignment
import eu.numberfour.n4js.n4JS.PropertyGetterDeclaration
import eu.numberfour.n4js.n4JS.PropertyMethodDeclaration
import eu.numberfour.n4js.n4JS.PropertyNameValuePair
import eu.numberfour.n4js.n4JS.PropertySetterDeclaration
import eu.numberfour.n4js.ts.typeRefs.DeferredTypeRef
import eu.numberfour.n4js.ts.typeRefs.TypeRef
import eu.numberfour.n4js.ts.typeRefs.TypeRefsFactory
import eu.numberfour.n4js.ts.typeRefs.Versionable
import eu.numberfour.n4js.ts.types.ContainerType
import eu.numberfour.n4js.ts.types.FieldAccessor
import eu.numberfour.n4js.ts.types.InferenceVariable
import eu.numberfour.n4js.ts.types.TGetter
import eu.numberfour.n4js.ts.types.TMethod
import eu.numberfour.n4js.ts.types.TStructGetter
import eu.numberfour.n4js.ts.types.TStructMember
import eu.numberfour.n4js.ts.types.TStructuralType
import eu.numberfour.n4js.ts.types.TypingStrategy
import eu.numberfour.n4js.ts.types.util.Variance
import eu.numberfour.n4js.ts.utils.TypeUtils
import eu.numberfour.n4js.typesystem.N4JSTypeSystem
import eu.numberfour.n4js.typesystem.TypeSystemHelper
import eu.numberfour.n4js.typesystem.constraints.InferenceContext
import eu.numberfour.n4js.utils.EcoreUtilN4
import it.xsemantics.runtime.RuleEnvironment
import java.util.List
import java.util.Map

import static extension eu.numberfour.n4js.typesystem.RuleEnvironmentExtensions.*

/**
 * {@link PolyProcessor} delegates here for processing array literals.
 * 
 * @see PolyProcessor#inferType(RuleEnvironment,eu.numberfour.n4js.n4JS.Expression,ASTMetaInfoCache)
 * @see PolyProcessor#processExpr(RuleEnvironment,eu.numberfour.n4js.n4JS.Expression,TypeRef,InferenceContext,ASTMetaInfoCache)
 */
@Singleton
package class PolyProcessor_ObjectLiteral extends AbstractPolyProcessor {

	@Inject
	private PolyProcessor polyProcessor;

	@Inject
	private N4JSTypeSystem ts;
	@Inject
	private TypeSystemHelper tsh;

	/**
	 * BEFORE CHANGING THIS METHOD, READ THIS:
	 * {@link PolyProcessor#processExpr(RuleEnvironment,eu.numberfour.n4js.n4JS.Expression,TypeRef,InferenceContext,ASTMetaInfoCache)}
	 */
	def package TypeRef processObjectLiteral(RuleEnvironment G, ObjectLiteral objLit, TypeRef expectedTypeRef,
		InferenceContext infCtx, ASTMetaInfoCache cache) {

		if (!objLit.isPoly) {
			val result = ts.type(G, objLit).getValue();
			// do not store in cache (TypeProcessor responsible for storing types of non-poly expressions in cache)
			return result;
		}

		// quick mode as a performance tweak:
		val haveUsableExpectedType = expectedTypeRef !== null
				&& (expectedTypeRef.useSiteStructuralTyping || expectedTypeRef.defSiteStructuralTyping); // FIXME reconsider
		val quickMode = !haveUsableExpectedType && !TypeUtils.isInferenceVariable(expectedTypeRef);

		val List<TStructMember> tMembers = newArrayList;
		// in standard mode: the following list will contain pairs from property assignments to inference variables
		// in quick mode: the following list will contain pairs from property assignments to fallback types
		val List<Pair<PropertyAssignment, ? extends Versionable>> props2InfVarOrFallbackType = newArrayList;
		processProperties(G, cache, infCtx, objLit, tMembers, quickMode, props2InfVarOrFallbackType);
		linkGetterSetterPairs(infCtx, tMembers, quickMode);

		// create temporary type (i.e. may contain inference variables)
		val resultTypeRef = TypeUtils.createParameterizedTypeRefStructural(G.objectType, TypingStrategy.STRUCTURAL, tMembers);
		
		// register onSolved handlers to add final types to cache (i.e. may not contain inference variables)
		infCtx.onSolved [ solution | handleOnSolved(G, cache, infCtx, objLit, quickMode, props2InfVarOrFallbackType, solution) ];

		// return temporary type of objLit (i.e. may contain inference variables)
		return resultTypeRef;
	}

	/**
	 * Processes all properties of an object literal.
	 * As of now, methods are not processed.
	 */
	def private void processProperties(RuleEnvironment G, ASTMetaInfoCache cache, InferenceContext infCtx,
		ObjectLiteral objLit, List<TStructMember> tMembers,
		boolean quickMode, List<Pair<PropertyAssignment, ? extends Versionable>> props2InfVarOrFallbackType
	) {
		for (pa : objLit.propertyAssignments) {
			if (pa !== null && pa.definedMember!==null) {
				val tMember = TypeUtils.copy(pa.definedMember);
				if (tMember !== null) {
					tMembers += tMember;
					
					if (pa.isPoly) {
						if (!(tMember instanceof TMethod)) {
							processNonMethodProperties(G, cache, infCtx, tMember, pa,
								quickMode, props2InfVarOrFallbackType);
						}
					}
				}
			}
		}
	}

	/**
	 * 	For each property in the object literal:
	 *  a) introduce a new inference variable representing the property's type (except for methods)
	 *  b) add a constraint: expressionTypeRef <: iv
	 *  c) create a TStructMember to be used in the structural result type reference
	 */
	def private void processNonMethodProperties(RuleEnvironment G, ASTMetaInfoCache cache, InferenceContext infCtx,
		TStructMember tMember, PropertyAssignment propAssignm,
		boolean quickMode, List<Pair<PropertyAssignment, ? extends Versionable>> props2InfVarOrFallbackType
	) {
		val originalMemberType = tMember.typeOfMember;

		assertTrueIfRigid(cache, "type of " + propAssignm.eClass.name + " in TModule should be a DeferredTypeRef",
			originalMemberType instanceof DeferredTypeRef);

		if (originalMemberType instanceof DeferredTypeRef) {
			if (!quickMode) {
				// standard mode:
				// create new inference variable for the to-be-inferred type of this property
				val iv = infCtx.newInferenceVariable;
				// set it as type in tMember
				tMember.typeOfMember = TypeUtils.createTypeRef(iv);
				// add a constraint for the initializer expression (if any)
				if (propAssignm instanceof PropertyNameValuePair) {
					if (propAssignm.expression !== null) {
						val exprTypeRef = polyProcessor.processExpr(G, propAssignm.expression, TypeUtils.createTypeRef(iv), infCtx, cache);
						infCtx.addConstraint(exprTypeRef, TypeUtils.createTypeRef(iv), Variance.CO); // exprTypeRef <: iv
					}
				}
				// remember for later
				props2InfVarOrFallbackType += propAssignm -> iv;
			} else {
				// quick mode:
				// compute a fall-back type
				val fallbackType = switch (propAssignm) {
					PropertyNameValuePair case propAssignm.expression !== null:
						polyProcessor.processExpr(G, propAssignm.expression, null, infCtx, cache)
					PropertyGetterDeclaration:
						propAssignm.declaredTypeOfOtherAccessorInPair ?: G.anyTypeRef
					PropertySetterDeclaration:
						propAssignm.declaredTypeOfOtherAccessorInPair ?: G.anyTypeRef
					default:
						G.anyTypeRef
				};
				// set it as type in tMember
				tMember.typeOfMember = TypeUtils.copy(fallbackType);
				// remember for later
				props2InfVarOrFallbackType += propAssignm -> fallbackType;
			}
		}
	}


	def private TypeRef getDeclaredTypeOfOtherAccessorInPair(eu.numberfour.n4js.n4JS.FieldAccessor accAST) {
		val otherPair = findOtherAccessorInPair(accAST.definedAccessor);
		val otherPairAST = otherPair?.astElement as eu.numberfour.n4js.n4JS.FieldAccessor;
		val declTypeRef = otherPairAST?.declaredTypeRef;
		return declTypeRef;
	}

	def private FieldAccessor findOtherAccessorInPair(FieldAccessor acc) {
		if (acc?.name !== null) {
			val type = acc.eContainer;
			if (type instanceof ContainerType<?>) {
				val lookForWriteAccess = acc instanceof TGetter;
				val result = type.findOwnedMember(acc.name, lookForWriteAccess, acc.static);
				if (result instanceof FieldAccessor) {
					return result;
				}
			}
		}
		return null;
	}
	
	/**
	 * Add a constraint for each getter/setter pair reflecting the relation between the getter's and setter's type.
	 * Required to make the getter obtain its implicit type from the corresponding setter, and vice versa.
	 */
	private def void linkGetterSetterPairs(InferenceContext infCtx, List<TStructMember> tMembers, boolean quickMode) {
		if(!quickMode) { // not in quick mode
			for (tMember : tMembers) {
				if (tMember instanceof TStructGetter) {
					val tOtherInPair = findOtherAccessorInPair(tMember);
					if (tOtherInPair !== null) {
						val typeGetter = tMember.typeOfMember;
						val typeSetter = tOtherInPair.typeOfMember;
						if (TypeUtils.isInferenceVariable(typeGetter) || TypeUtils.isInferenceVariable(typeSetter)) {
							infCtx.addConstraint(typeGetter, typeSetter, Variance.CO);
						} else {
							// do not add a constraint if both types were explicitly declared
							// (then this constraint does not apply!!)
						}
					}
				}
			}
		}
	}
	
	/**
	 * Writes final types to cache
	 */
	private def void handleOnSolved(RuleEnvironment G, ASTMetaInfoCache cache, InferenceContext infCtx, ObjectLiteral objLit,
		boolean quickMode, List<? extends Pair<PropertyAssignment, ? extends Versionable>> props2InfVarOrFallbackType,
		Optional<Map<InferenceVariable, TypeRef>> solution
	) {
		for (propPair : props2InfVarOrFallbackType) {
			val memberType = getMemberType(G, solution, quickMode, propPair);
			val memberTypeSane = tsh.sanitizeTypeOfVariableFieldProperty(G, memberType);
			val propAssignm = propPair.key;
			val memberInTModule = propAssignm.definedMember;
			EcoreUtilN4.doWithDeliver(false, [
				memberInTModule.typeOfMember = TypeUtils.copy(memberTypeSane);
			], memberInTModule);
		}
		
		val resultFinal = TypeUtils.createParameterizedTypeRefStructural(G.objectType, TypingStrategy.STRUCTURAL,
			objLit.definedType as TStructuralType);
		cache.storeType(objLit, resultFinal);
		
		for (currAss : objLit.propertyAssignments) {
			if (currAss.definedMember !== null) {
				if (currAss instanceof PropertyMethodDeclaration) {
					cache.storeType(currAss, TypeUtils.createTypeRef(currAss.definedMember));
				} else {
					cache.storeType(currAss, TypeUtils.copy(currAss.definedMember.typeOfMember));
				}
			} else {
				cache.storeType(currAss, TypeRefsFactory.eINSTANCE.createUnknownTypeRef);
			}
		}
	}
	
	private def TypeRef getMemberType(RuleEnvironment G, Optional<Map<InferenceVariable, TypeRef>> solution,
		boolean quickMode, Pair<PropertyAssignment, ? extends Versionable> prop2InfVarOrFallbackType
	) {
		var TypeRef memberType = null;
		val propAssignm = prop2InfVarOrFallbackType.key;
		if (solution.present) {
			if (quickMode) {
				// success case (quick mode):
				val fallbackType = prop2InfVarOrFallbackType.value as TypeRef; // value is a TypeRef
				if (propAssignm instanceof PropertyNameValuePair) {
					memberType = getFinalResultTypeOfNestedPolyExpression(propAssignm.expression)
				} else {
					memberType = TypeUtils.copy(fallbackType).applySolution(G, solution.get)
				}
				
			} else {
				// success case (standard mode):
				val infVar = prop2InfVarOrFallbackType.value as InferenceVariable; // value is an infVar
				val fromSolution = solution.get.get(infVar);
				
				if (propAssignm instanceof PropertyNameValuePair) {
					val fromCache = if (propAssignm.expression instanceof ObjectLiteral) {
							getFinalResultTypeOfNestedPolyExpression(propAssignm.expression)
						} else {
							null
						};
					if (fromCache !== null && ts.equaltypeSucceeded(G, fromCache, fromSolution)) {
						// tweak for nested ObjectLiterals in initializer expression of PropertyNameValuePairs:
						// the solution from the infCtx will be a StructuralTypeRef with 'genStructuralMembers'
						// but the result of the nested poly computation (via the cache) will give us a much more
						// efficient StructuralTypeRef with 'structuralType' pointing to the TStructuralType in the TModule
						memberType = fromCache
					} else {
						memberType = fromSolution
					}
				} else {
					memberType = fromSolution
				}
			}
		} else {
			// failure case (both modes):
			if (propAssignm instanceof PropertyNameValuePair) {
				memberType = getFinalResultTypeOfNestedPolyExpression(propAssignm.expression)
			} else {
				memberType = G.anyTypeRef
			}
		}
		
		return memberType;
	}
}
