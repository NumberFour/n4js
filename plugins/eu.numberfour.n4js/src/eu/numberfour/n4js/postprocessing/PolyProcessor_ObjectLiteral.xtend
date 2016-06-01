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

import com.google.inject.Inject
import eu.numberfour.n4js.n4JS.ObjectLiteral
import eu.numberfour.n4js.n4JS.PropertyGetterDeclaration
import eu.numberfour.n4js.n4JS.PropertyMethodDeclaration
import eu.numberfour.n4js.n4JS.PropertyNameValuePair
import eu.numberfour.n4js.n4JS.PropertySetterDeclaration
import eu.numberfour.n4js.ts.typeRefs.DeferredTypeRef
import eu.numberfour.n4js.ts.typeRefs.TypeRef
import eu.numberfour.n4js.ts.types.ContainerType
import eu.numberfour.n4js.ts.types.FieldAccessor
import eu.numberfour.n4js.ts.types.TGetter
import eu.numberfour.n4js.ts.types.TMethod
import eu.numberfour.n4js.ts.types.TStructGetter
import eu.numberfour.n4js.ts.types.TStructMember
import eu.numberfour.n4js.ts.types.TStructuralType
import eu.numberfour.n4js.ts.types.TypingStrategy
import eu.numberfour.n4js.ts.types.util.Variance
import eu.numberfour.n4js.ts.utils.TypeUtils
import eu.numberfour.n4js.typesystem.TypeSystemHelper
import eu.numberfour.n4js.typesystem.constraints.InferenceContext
import eu.numberfour.n4js.utils.EcoreUtilN4
import eu.numberfour.n4js.xsemantics.N4JSTypeSystem
import it.xsemantics.runtime.RuleEnvironment

import static extension eu.numberfour.n4js.typesystem.RuleEnvironmentExtensions.*

/**
 */
class PolyProcessor_ObjectLiteral extends AbstractPolyProcessor {

	@Inject
	private PolyProcessor polyProcessor;

	@Inject
	private N4JSTypeSystem ts;
	@Inject
	private TypeSystemHelper tsh;


	def package TypeRef processObjectLiteral(RuleEnvironment G, InferenceContext infCtx, ObjectLiteral objLit, TypeRef expectedTypeRef) {

		if(!objLit.isPoly) {
			val result = ts.type(G, objLit).getValue();
			// do not store in cache (TypingASTWalker responsible for storing types of non-poly expressions in cache)
			return result;
		}

		val haveUsableExpectedType = expectedTypeRef!==null
				&& (expectedTypeRef.useSiteStructuralTyping || expectedTypeRef.defSiteStructuralTyping); // FIXME reconsider
		if(!haveUsableExpectedType) {
			return getTypeForObjectLiteralWithoutExpectation(G, infCtx, objLit);
		}

		// create the members for the structural result type reference
		val tMembers = <TStructMember>newArrayList;
		val mapInfVar2PropNameValuePair = newHashMap;
		val mapInfVar2ExpressionTypeRef = newHashMap;
		for(pa : objLit.propertyAssignments) {
			if(pa!==null) {
				if(pa.isPoly) {
					val tMember = TypeUtils.copy(pa.definedMember);
					if(tMember!==null) {
						tMembers += tMember;
						if(!(tMember instanceof TMethod)) {
							val originalMemberType = tMember.typeOfMember;
							assertTrueIfRigid("type of "+pa.eClass.name+" in TModule should be a DeferredTypeRef", originalMemberType instanceof DeferredTypeRef);
							if(originalMemberType instanceof DeferredTypeRef) {
								// create new inference variable for type of this property
								val iv = infCtx.newInferenceVariable;
								mapInfVar2PropNameValuePair.put(iv,pa);
								// set it as type in our copy
								tMember.typeOfMember = TypeUtils.createTypeRef(iv);
								// add a constraint for the initializer expression (if any)
								if(pa instanceof PropertyNameValuePair) {
									if(pa.expression!==null) {
										val exprTypeRef = polyProcessor.processExpr(G, infCtx, pa.expression, null);
										mapInfVar2ExpressionTypeRef.put(iv, exprTypeRef); // will be copied below when taken out of the map!
										infCtx.addConstraint(exprTypeRef, TypeUtils.createTypeRef(iv), Variance.CO); // exprTypeRef <: iv
									}
								}
							}
						}
					}
				} else {
					// pa is not poly
					// -> simply use a copy of the member generated by the types builder
					tMembers += TypeUtils.copy(pa.definedMember);
				}
			}
		}
		// add a constraint for each getter/setter pair reflecting the relation between the getter's and setter's type
		// (required to make the getter obtain its implicit type from the corresponding setter, and vice versa)
		for(tMember : tMembers) {
			if(tMember instanceof TStructGetter) {
				val tOtherInPair = findOtherAccessorInPair(tMember);
				if(tOtherInPair!==null) {
					val typeGetter = tMember.typeOfMember;
					val typeSetter = tOtherInPair.typeOfMember;
					if(infCtx.isInferenceVariable(typeGetter) || infCtx.isInferenceVariable(typeSetter)) {
						infCtx.addConstraint(typeGetter, typeSetter, Variance.CO);
					} else {
						// do not add a constraint if both types were explicitly declared
						// (then this constraint does not apply!!)
					}
				}
			}
		}

		val result = TypeUtils.createParameterizedTypeRefStructural(G.objectType, TypingStrategy.STRUCTURAL, tMembers);

		infCtx.onSolved[solution|
			for(e : mapInfVar2PropNameValuePair.entrySet) {
				val pa = e.value;
				val memberInTModule = pa.definedMember;
				val memberType = if(solution.present) {
					// success case:
					val fromSolution = solution.get.get(e.key);
					if(pa instanceof PropertyNameValuePair) {
						val fromCache = if(pa.expression instanceof ObjectLiteral) getFinalResultTypeOfNestedPolyExpression(pa.expression) else null;
						if(fromCache!==null && ts.equaltypeSucceeded(G, fromCache, fromSolution)) {
							// tweak for nested ObjectLiterals in initializer expression of PropertyNameValuePairs:
							// the solution from the infCtx will be a StructuralTypeRef with 'genStructuralMembers'
							// but the result of the nested poly computation (via the cache) will give us a much more
							// efficient StructuralTypeRef with 'structuralType' pointing to the TStructuralType in the TModule
							fromCache
						} else {
							fromSolution
						}
					} else {
						fromSolution
					}
				} else {
					// failure case:
//					mapInfVar2ExpressionTypeRef.get(e.key)
					val ttt = if(pa instanceof PropertyNameValuePair) {
						getFinalResultTypeOfNestedPolyExpression(pa.expression)
					} else {
						G.anyTypeRef
					};
					ttt
				};
				val memberTypeSane = tsh.sanitizeTypeOfVariableFieldProperty(G, memberType); // FIXME ok to also apply this to getter/setter???
				EcoreUtilN4.doWithDeliver(false, [
					memberInTModule.typeOfMember = TypeUtils.copy(memberTypeSane);
				], memberInTModule);
			}
			val resultFinal = TypeUtils.createParameterizedTypeRefStructural(G.objectType, TypingStrategy.STRUCTURAL,
					objLit.definedType as TStructuralType);
			storeInCache(objLit, resultFinal);
			for(currAss : objLit.propertyAssignments) {
				if(currAss instanceof PropertyMethodDeclaration) {
					storeInCache(currAss, TypeUtils.createTypeRef(currAss.definedMember));
				} else {
					storeInCache(currAss, TypeUtils.copy(currAss.definedMember.typeOfMember));
				}
			}
		];

		return result;
	}

	def private TypeRef getTypeForObjectLiteralWithoutExpectation(RuleEnvironment G, InferenceContext infCtx, ObjectLiteral objLit) {


		// create the members for the structural result type reference
		val tMembers = <TStructMember>newArrayList;
		val mapPA2FallbackType = newHashMap;
		for(pa : objLit.propertyAssignments) {
			if(pa!==null) {
				if(pa.isPoly) {
					val tMember = TypeUtils.copy(pa.definedMember);
					if(tMember!==null) {
						tMembers += tMember;
						if(!(tMember instanceof TMethod)) {
							val originalMemberType = tMember.typeOfMember;
							assertTrueIfRigid("type of "+pa.eClass.name+" in TModule should be a DeferredTypeRef", originalMemberType instanceof DeferredTypeRef);
							if(originalMemberType instanceof DeferredTypeRef) {
								// no usable expected type
								val fallbackType = switch(pa) {
									PropertyNameValuePair case pa.expression!==null:
										polyProcessor.processExpr(G, infCtx, pa.expression, null)
									PropertyGetterDeclaration:
										pa.declaredTypeOfOtherAccessorInPair ?: G.anyTypeRef
									PropertySetterDeclaration:
										pa.declaredTypeOfOtherAccessorInPair ?: G.anyTypeRef
									default:
										G.anyTypeRef
								};
								tMember.typeOfMember = TypeUtils.copy(fallbackType);
								mapPA2FallbackType.put(pa, fallbackType);
							}
						}
					}
				} else {
					// pa is not poly
					// -> simply use a copy of the member generated by the types builder
					tMembers += TypeUtils.copy(pa.definedMember);
				}
			}
		}

		val result = TypeUtils.createParameterizedTypeRefStructural(G.objectType, TypingStrategy.STRUCTURAL, tMembers);

		infCtx.onSolved[solution|
			for(e : mapPA2FallbackType.entrySet) {
				val pa = e.key;
				val memberInTModule = pa.definedMember;
				val memberType = if(solution.present) {
					// success case:
					val ttt = if(pa instanceof PropertyNameValuePair) {
						getFinalResultTypeOfNestedPolyExpression(pa.expression)
					} else {
						TypeUtils.copy(e.value).applySolution(G, solution.get);
					}
					ttt
				} else {
					// failure case (unsolvable constraint system):
					val ttt = if(pa instanceof PropertyNameValuePair) {
						getFinalResultTypeOfNestedPolyExpression(pa.expression)
					} else {
						G.anyTypeRef
					};
					ttt
				};
				val memberTypeSane = tsh.sanitizeTypeOfVariableFieldProperty(G, memberType); // FIXME ok to also apply this to getter/setter???
				EcoreUtilN4.doWithDeliver(false, [
					memberInTModule.typeOfMember = TypeUtils.copy(memberTypeSane);
				], memberInTModule);
			}
			val resultFinal = TypeUtils.createParameterizedTypeRefStructural(G.objectType, TypingStrategy.STRUCTURAL,
					objLit.definedType as TStructuralType);
			storeInCache(objLit, resultFinal);
			for(currAss : objLit.propertyAssignments) {
				if(currAss instanceof PropertyMethodDeclaration) {
					storeInCache(currAss, TypeUtils.createTypeRef(currAss.definedMember));
				} else {
					storeInCache(currAss, TypeUtils.copy(currAss.definedMember.typeOfMember));
				}
			}
		];

		return result;
	}

	def private TypeRef getDeclaredTypeOfOtherAccessorInPair(eu.numberfour.n4js.n4JS.FieldAccessor accAST) {
		return (
			findOtherAccessorInPair(accAST.definedAccessor)?.astElement as eu.numberfour.n4js.n4JS.FieldAccessor
		)?.declaredTypeRef;
	}

	def private FieldAccessor findOtherAccessorInPair(FieldAccessor acc) {
		if(acc?.name!==null) {
			val type = acc.eContainer;
			if(type instanceof ContainerType<?>) {
				val lookForWriteAccess = acc instanceof TGetter;
				val result = type.findOwnedMember(acc.name, lookForWriteAccess, acc.static);
				if(result instanceof FieldAccessor) {
					return result;
				}
			}
		}
		return null;
	}
}
