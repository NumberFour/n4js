/**
 * Copyright (c) 2017 NumberFour AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   NumberFour AG - Initial API and implementation
 */
package eu.numberfour.n4js.scoping.members;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.xbase.lib.Pair;

import com.google.common.base.Joiner;

import eu.numberfour.n4js.ts.typeRefs.TypeRef;
import eu.numberfour.n4js.ts.typeRefs.UnknownTypeRef;
import eu.numberfour.n4js.ts.types.MemberAccessModifier;
import eu.numberfour.n4js.ts.types.MemberType;
import eu.numberfour.n4js.ts.types.TField;
import eu.numberfour.n4js.ts.types.TFormalParameter;
import eu.numberfour.n4js.ts.types.TMember;
import eu.numberfour.n4js.ts.types.TMethod;
import eu.numberfour.n4js.ts.types.TSetter;
import eu.numberfour.n4js.ts.types.VoidType;
import eu.numberfour.n4js.ts.utils.TypeUtils;
import eu.numberfour.n4js.typesystem.N4JSTypeSystem;
import it.xsemantics.runtime.RuleEnvironment;

/**
 * The purpose of the classes and methods in this file is threefold:
 * <ol>
 * <li>Provide methods to aggregate all existing members on which a new composed member is based upon. The existing
 * members are also called siblings.</li>
 * <li>Interpret all sibling members to derive general information which is later used when creating the composed
 * member.</li>
 * <li>Provide the derived information using data holder objects. The information is later used to create a new composed
 * {@link TMember} out of its existing siblings.</li>
 * </ol>
 * The aggregation methods are static and the method {@link #get()} returns an aggregation object. Their life cycle is
 * as follows:
 *
 * <pre>
 * Start -> init() -> addMember()* -> get() -> End.
 *                       ^             |
 *                       |_____________|
 * </pre>
 *
 * The aggregation object returned by {@link #get()} provides several data providing methods such as
 * {@link #getAccessabilityMax()}. Their values are computed lazily using the init methods.
 * <p>
 * <i> Please note that this class provides <b>general information</b> about composed types only. That is, information
 * here should be named without referring explicitly to union or intersection members.</i>
 */
public class ComposedMemberAggregate {

	/////////////////////////// Static Part ///////////////////////////

	private static ComposedMemberAggregate currCMA;

	/**
	 * Initializes the static methods. (Also refer to the life cycle mentioned above.)
	 */
	public static void init(boolean writeAccess, Resource resource, N4JSTypeSystem ts) {
		Objects.isNull(currCMA);
		currCMA = new ComposedMemberAggregate(writeAccess, resource, ts);
	}

	/**
	 * Adds a sibling member on which a new composed member is based upon. (Also refer to the life cycle mentioned
	 * above.)
	 */
	public static void addMember(TMember member, RuleEnvironment G) {
		Objects.nonNull(currCMA);
		Pair<TMember, RuleEnvironment> pair = null;
		if (member != null) {
			pair = new Pair<>(member, G);
		}
		currCMA.siblings.add(pair); // adds null to indicate missing members
	}

	/**
	 * Returns a data object that provides all aggregated information to create a new composed {@link TMember}. This
	 * method also performs an initialization for adding members to the next data object. (Also refer to the life cycle
	 * mentioned above.)
	 */
	public static ComposedMemberAggregate get() {
		ComposedMemberAggregate lastCMA = currCMA;
		currCMA = new ComposedMemberAggregate(lastCMA.isWriteAccess, lastCMA.resource, lastCMA.ts);
		return lastCMA;
	}

	/////////////////////////// Non-Static Part ///////////////////////////

	// finals get set in constructor
	private final boolean isWriteAccess;
	private final Resource resource;
	private final N4JSTypeSystem ts;
	private final List<Pair<TMember, RuleEnvironment>> siblings;

	// non-finals are set in initMemberAggregate()
	private boolean isInitialized = false;
	private boolean isEmpty = true;
	private boolean isSiblingMissing = false;
	private boolean hasMultipleMemberTypes = false;
	private boolean hasFieldMemberType = false;
	private boolean hasGetterMemberType = false;
	private boolean hasSetterMemberType = false;
	private boolean hasMethodMemberType = false;
	private boolean hasNonMethodMemberType = false;
	private boolean onlyMethodMemberTypes = true;
	private boolean onlyFieldMemberTypes = true;
	private boolean onlyGetterMemberTypes = true;
	private boolean onlySetterMemberTypes = true;
	private boolean hasReadOnlyField = false;
	private boolean onlyReadOnlyFields = true;
	private final boolean hasValidationProblem = false;
	private MemberAccessModifier accessibilityMin = MemberAccessModifier.PUBLIC;
	private MemberAccessModifier accessibilityMax = MemberAccessModifier.PRIVATE;
	private final Map<MemberType, List<TypeRef>> typeRefsMap = new HashMap<>();
	private final List<TypeRef> typeRefs = new ArrayList<>();
	private final List<TypeRef> methodTypeRefsVoid = new ArrayList<>();
	private final List<TypeRef> methodTypeRefsNonVoid = new ArrayList<>();
	private final Map<TypeRef, RuleEnvironment> typeRef2G = new HashMap<>();

	private final List<FParAggregate> fParameters = new ArrayList<>();
	private boolean isVariadicButLastFParIsDifferent = false;

	/**
	 * Aggregates all necessary information for composing a new formal parameter out of its existing siblings.
	 */
	public static class FParAggregate {
		// is set in initMemberAggregate()
		private final List<Pair<TFormalParameter, RuleEnvironment>> fpSiblings = new ArrayList<>();

		// is set in initFParAggregate()
		private final List<String> names = new LinkedList<>();
		private boolean allOptional = true;
		private boolean allNonOptional = true;
		private boolean hasValidationProblem = false;
		private final List<TypeRef> typeRefs = new ArrayList<>();
		private final List<TypeRef> typeRefsVariadic = new ArrayList<>();
		private final List<TypeRef> typeRefsVariadicAccumulated = new ArrayList<>();

		/** Returns the name of the formal parameter. */
		public String getName() {
			return Joiner.on("_").join(names);
		}

		/** Returns true iff all siblings are optional. */
		public boolean allOptional() {
			return allOptional;
		}

		/** Returns true iff all siblings are other than optional. */
		public boolean allNonOptional() {
			return allNonOptional;
		}

		/** Returns true iff one of the sibling formal parameter has a validation problem. */
		public boolean hasValidationProblem() {
			return hasValidationProblem;
		}

		/** Returns the {@link TypeRef}s of all siblings. */
		public List<TypeRef> getTypeRefs() {
			return typeRefs;
		}

		/** Returns the {@link TypeRef}s of all variadic siblings. */
		public List<TypeRef> getTypeRefsVariadic() {
			return typeRefsVariadic;
		}

		/** Returns the set of {@link #getTypeRefsVariadic()} of this formal parameter and all its predecessors. */
		public List<TypeRef> getTypeRefsVariadicAccumulated() {
			return typeRefsVariadicAccumulated;
		}
	}

	private ComposedMemberAggregate(boolean writeAccess, Resource resource, N4JSTypeSystem ts) {
		this.isWriteAccess = writeAccess;
		this.resource = resource;
		this.ts = ts;
		this.siblings = new LinkedList<>();
	}

	/////////////////////////// Init Methods ///////////////////////////

	synchronized private void initMemberAggregate() {
		if (isInitialized)
			return;

		this.isSiblingMissing = siblings.contains(null);

		MemberType lastMType = null;
		for (Pair<TMember, RuleEnvironment> pair : siblings) {
			if (pair == null)
				continue;
			this.isEmpty = false;

			TMember member = pair.getKey();
			RuleEnvironment G = pair.getValue();

			lastMType = handleMemberTypes(lastMType, member);
			handleReadOnlyField(member);
			handleAccessibility(member);
			handleTypeRefLists(member, G);
			handleFParameters(member, G);
		}

		// init: fParameters
		List<TypeRef> currVariadicAccumulated = new LinkedList<>();
		for (FParAggregate fpAggr : fParameters) {
			initFParAggregate(fpAggr);

			// handle: typeRefsVariadicAccumulated
			currVariadicAccumulated.addAll(fpAggr.typeRefsVariadic);
			fpAggr.typeRefsVariadicAccumulated.addAll(currVariadicAccumulated);
		}

		handleIsVariadicButLastFParIsDifferent();

		handleValidationProblems();

		this.isInitialized = true;
	}

	private void initFParAggregate(FParAggregate fpAggr) {
		for (Pair<TFormalParameter, RuleEnvironment> fpPair : fpAggr.fpSiblings) {
			TFormalParameter tFpar = fpPair.getKey();
			RuleEnvironment G = fpPair.getValue();

			// handle: name
			final String nextName = tFpar.getName();
			if (nextName != null && !fpAggr.names.contains(nextName)) {
				fpAggr.names.add(nextName);
			}

			// handle: typeRef lists
			TypeRef typeRefSubst = ts.substTypeVariablesInTypeRef(G, tFpar.getTypeRef());
			if (typeRefSubst != null && !(typeRefSubst instanceof UnknownTypeRef)) {
				TypeRef typeRefCopy = TypeUtils.copyIfContained(typeRefSubst);
				fpAggr.typeRefs.add(typeRefCopy);
				if (tFpar.isVariadic()) {
					fpAggr.typeRefsVariadic.add(typeRefCopy);
				}
			}

			// handle: optional
			fpAggr.allOptional &= tFpar.isOptional();
			fpAggr.allNonOptional &= !tFpar.isOptional();
		}
	}

	/////////////////////////// Helper Init Methods ///////////////////////////

	private MemberType handleMemberTypes(MemberType lastMType, TMember member) {
		MemberType currMType = member.getMemberType();
		lastMType = (lastMType == null) ? currMType : lastMType;
		hasMultipleMemberTypes |= currMType != lastMType;
		hasFieldMemberType |= currMType == MemberType.FIELD;
		hasGetterMemberType |= currMType == MemberType.GETTER;
		hasSetterMemberType |= currMType == MemberType.SETTER;
		hasMethodMemberType |= currMType == MemberType.METHOD;
		hasNonMethodMemberType |= currMType != MemberType.METHOD;
		onlyMethodMemberTypes &= currMType == MemberType.METHOD;
		onlyFieldMemberTypes &= currMType == MemberType.FIELD;
		onlyGetterMemberTypes &= currMType == MemberType.GETTER;
		onlySetterMemberTypes &= currMType == MemberType.SETTER;
		lastMType = currMType;
		return lastMType;
	}

	private void handleReadOnlyField(TMember member) {
		boolean isReadOnly = false;
		if (member instanceof TField) {
			TField tField = (TField) member;
			isReadOnly = !tField.isWriteable();
		}
		hasReadOnlyField |= isReadOnly;
		onlyReadOnlyFields &= isReadOnly && (member instanceof TField);
	}

	private void handleAccessibility(TMember member) {
		MemberAccessModifier currAccessibility = member.getMemberAccessModifier();
		if (accessibilityMax.getValue() < currAccessibility.getValue())
			accessibilityMax = currAccessibility;
		if (accessibilityMin.getValue() > currAccessibility.getValue())
			accessibilityMin = currAccessibility;
	}

	private void handleTypeRefLists(TMember member, RuleEnvironment G) {
		TypeRef typeRef = TypeUtils.getMemberTypeRef(member);
		TypeRef typeRefSubst = ts.substTypeVariablesInTypeRef(G, typeRef);
		if (typeRefSubst != null && !(typeRefSubst instanceof UnknownTypeRef)) {
			TypeRef typeRefCopy = TypeUtils.copyIfContained(typeRefSubst);
			typeRefs.add(typeRefCopy);
			typeRef2G.put(typeRefCopy, G);
			if (member.getMemberType() == MemberType.METHOD) {
				if (TypeUtils.isVoid(typeRefCopy)) {
					methodTypeRefsVoid.add(typeRefCopy);
				} else {
					methodTypeRefsNonVoid.add(typeRefCopy);
				}
			}
			MemberType currMType = member.getMemberType();
			if (!typeRefsMap.containsKey(currMType)) {
				typeRefsMap.put(currMType, new LinkedList<>());
			}
			List<TypeRef> typeRefsOfMemberType = typeRefsMap.get(currMType);
			typeRefsOfMemberType.add(typeRefCopy);
		}
	}

	private void handleFParameters(TMember member, RuleEnvironment G) {
		EList<TFormalParameter> fpars = null;
		if (member instanceof TMethod) {
			TMethod method = (TMethod) member;
			fpars = method.getFpars();
		}
		if (member instanceof TSetter) {
			TSetter setter = (TSetter) member;
			fpars = new BasicEList<>();
			fpars.add(setter.getFpar());
		}
		if (fpars != null) {
			for (int i = 0; i < fpars.size(); i++) {
				TFormalParameter fpar = fpars.get(i);
				if (fParameters.size() <= i) {
					fParameters.add(new FParAggregate());
				}
				FParAggregate fpAggr = fParameters.get(i);
				Pair<TFormalParameter, RuleEnvironment> fpPair = new Pair<>(fpar, G);
				fpAggr.fpSiblings.add(fpPair);
			}
		}
	}

	private void handleIsVariadicButLastFParIsDifferent() {
		boolean result = false;
		if (!fParameters.isEmpty()) {
			FParAggregate lastFpar = fParameters.get(fParameters.size() - 1);
			List<TypeRef> variadics = lastFpar.getTypeRefsVariadicAccumulated();

			// case: the last fpar is not everywhere optional, but variadics exist
			if (!lastFpar.allOptional() && !variadics.isEmpty())
				result = true;

			// case: the last fpar has a different type than the variadics
			for (TypeRef lfpTypeRef : lastFpar.getTypeRefs()) {
				for (TypeRef variTypeRef : variadics) {
					if (variTypeRef.getDeclaredType() != lfpTypeRef.getDeclaredType()) {
						result = true;
						break;
					}
				}
			}
		}

		isVariadicButLastFParIsDifferent = result;
	}

	private void handleValidationProblems() {
		for (Pair<TMember, RuleEnvironment> pair : siblings) {
			if (pair == null)
				continue;

			TMember member = pair.getKey();
			if (member instanceof TMethod) {
				TMethod tMethod = (TMethod) member;

				for (int i = 0; i < tMethod.getFpars().size(); i++) {
					TFormalParameter currFP = tMethod.getFpars().get(i);
					if (currFP.isVariadic() && tMethod.getFpars().size() > i + 1) {
						FParAggregate currFPA = fParameters.get(i);
						currFPA.hasValidationProblem = true;
						return;
					}
				}
			}
		}
	}

	/////////////////////////// Access Methods ///////////////////////////

	/** Returns a reference to {@link N4JSTypeSystem}. */
	public N4JSTypeSystem getTypeSystem() {
		return ts;
	}

	/** Returns a reference to the {@link Resource} of the composed member. */
	public Resource getResource() {
		return resource;
	}

	/** Returns true iff no sibling exits. */
	public boolean isEmpty() {
		initMemberAggregate();
		return isEmpty;
	}

	/** Returns true iff an accessor or field has write access. */
	public boolean isWriteAccess() {
		return isWriteAccess;
	}

	/** Returns true iff a sibling is missing. */
	public boolean isSiblingMissing() {
		initMemberAggregate();
		return isSiblingMissing;
	}

	/** Returns true iff the siblings are of different {@link MemberType}. */
	public boolean hasMultipleMemberTypes() {
		initMemberAggregate();
		return hasMultipleMemberTypes;
	}

	/** Returns true iff one or more siblings are of {@link MemberType} {@code MemberType.FIELD} */
	public boolean hasFieldMemberType() {
		initMemberAggregate();
		return hasFieldMemberType;
	}

	/** Returns true iff one or more siblings are of {@link MemberType} {@code MemberType.GETTER} */
	public boolean hasGetterMemberType() {
		initMemberAggregate();
		return hasGetterMemberType;
	}

	/** Returns true iff one or more siblings are of {@link MemberType} {@code MemberType.SETTER} */
	public boolean hasSetterMemberType() {
		initMemberAggregate();
		return hasSetterMemberType;
	}

	/** Returns true iff one or more siblings are of {@link MemberType} {@code MemberType.METHOD} */
	public boolean hasMethodMemberType() {
		initMemberAggregate();
		return hasMethodMemberType;
	}

	/** Returns true iff one or more siblings are of other {@link MemberType} than {@code MemberType.METHOD} */
	public boolean hasNonMethodMemberType() {
		initMemberAggregate();
		return hasNonMethodMemberType;
	}

	/** Returns true iff all siblings are of {@link MemberType} {@code MemberType.METHOD} */
	public boolean onlyMethodMemberTypes() {
		initMemberAggregate();
		return onlyMethodMemberTypes;
	}

	/** Returns true iff all siblings are of {@link MemberType} {@code MemberType.FIELD} */
	public boolean onlyFieldMemberTypes() {
		initMemberAggregate();
		return onlyFieldMemberTypes;
	}

	/** Returns true iff all siblings are of {@link MemberType} {@code MemberType.GETTER} */
	public boolean onlyGetterMemberTypes() {
		initMemberAggregate();
		return onlyGetterMemberTypes;
	}

	/** Returns true iff all siblings are of {@link MemberType} {@code MemberType.SETTER} */
	public boolean onlySetterMemberTypes() {
		initMemberAggregate();
		return onlySetterMemberTypes;
	}

	/** Returns true iff one or more siblings are {@link MemberType} {@code MemberType.FIELD} and readable. */
	public boolean hasReadOnlyField() {
		initMemberAggregate();
		return hasReadOnlyField;
	}

	/** Returns true iff all siblings are {@link MemberType} {@code MemberType.FIELD} and readable. */
	public boolean onlyReadOnlyFields() {
		initMemberAggregate();
		return onlyReadOnlyFields;
	}

	/** Returns the min accessibility of all siblings. */
	public MemberAccessModifier getAccessabilityMin() {
		initMemberAggregate();
		return accessibilityMin;
	}

	/** Returns the max accessibility of all siblings. */
	public MemberAccessModifier getAccessabilityMax() {
		initMemberAggregate();
		return accessibilityMax;
	}

	/** Returns true iff one of the siblings has a validation problem. */
	public boolean hasValidationProblem() {
		initMemberAggregate();
		return hasValidationProblem;
	}

	/**
	 * Returns a list of all return {@link TypeRef}s of the given {@link MemberType}s. If no {@link MemberType} is
	 * given, all {@link TypeRef}s are returned.
	 */
	public List<TypeRef> getTypeRefsOfMemberType(MemberType... memberTypes) {
		initMemberAggregate();
		List<TypeRef> resultTypeRefs = new LinkedList<>();
		if (memberTypes == null) {
			for (List<TypeRef> franzLiszt : typeRefsMap.values()) {
				resultTypeRefs.addAll(franzLiszt);
			}
			return resultTypeRefs;
		}

		for (MemberType memberType : memberTypes) {
			if (typeRefsMap.containsKey(memberType)) {
				resultTypeRefs.addAll(typeRefsMap.get(memberType));
			}
		}
		return resultTypeRefs;
	}

	/**
	 * Returns all non {@link VoidType} return {@link TypeRef}s of siblings which are of {@link MemberType}
	 * {@code MemberType.METHOD}.
	 */
	public List<TypeRef> getMethodTypeRefsNonVoid() {
		initMemberAggregate();
		return methodTypeRefsNonVoid;
	}

	/**
	 * Returns all {@link VoidType} return {@link TypeRef}s of siblings which are of {@link MemberType}
	 * {@code MemberType.METHOD}.
	 */
	public List<TypeRef> getMethodTypeRefsVoid() {
		initMemberAggregate();
		return methodTypeRefsVoid;
	}

	/** Returns the {@link RuleEnvironment} of the given return {@link TypeRef}. */
	public RuleEnvironment getRuleEnvironmentForTypeRef(TypeRef typeRef) {
		initMemberAggregate();
		return typeRef2G.get(typeRef);
	}

	/** Returns a list of all aggregates of formal parameters. */
	public List<FParAggregate> getFParAggregates() {
		initMemberAggregate();
		return fParameters;
	}

	/**
	 * Returns true iff there exist variadic formal parameters and iff the last formal parameter is non optional or is
	 * of another type than the variadic {@link TypeRef}s.
	 */
	public boolean isVariadicButLastFParIsDifferent() {
		initMemberAggregate();
		return isVariadicButLastFParIsDifferent;
	}

}
