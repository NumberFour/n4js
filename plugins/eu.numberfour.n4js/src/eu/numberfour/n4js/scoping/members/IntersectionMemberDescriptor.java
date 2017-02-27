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

import static eu.numberfour.n4js.ts.types.MemberType.FIELD;
import static eu.numberfour.n4js.ts.types.MemberType.GETTER;
import static eu.numberfour.n4js.ts.types.MemberType.METHOD;
import static eu.numberfour.n4js.ts.types.MemberType.SETTER;

import eu.numberfour.n4js.ts.types.MemberType;

/**
 *
 */
public class IntersectionMemberDescriptor extends ComposedMemberDescriptor {

	IntersectionMemberDescriptor(ComposedMemberAggregate cma) {
		super(cma);
	}

	@Override
	protected ComposedMemberCreator getSpecialMemberDescriptor() {
		switch (memberType) {
		case METHOD:
			return new MethodDescriptor.IntersectionMethod(cma);
		case FIELD:
			return new FieldDescriptor.IntersectionField(cma);
		case GETTER:
			return new GetterDescriptor.IntersectionGetter(cma);
		case SETTER:
			return new SetterDescriptor.IntersectionSetter(cma);
		}
		return null;
	}

	@Override
	protected MemberType getNewMemberType() {
		// mix of all memberTypes
		if (cma.hasMethodMemberType() && cma.hasNonMethodMemberType()) {
			return null; // inValid
		}
		if (cma.onlyMethodMemberTypes()) {
			return METHOD;
		}
		// mix of all non-method memberTypes
		if (cma.onlyGetterMemberTypes() && !cma.isWriteAccess()) {
			return GETTER;
		}
		if (cma.onlySetterMemberTypes() && cma.isWriteAccess()) {
			return SETTER;
		}
		if (allTypeRefAreEqual()) {
			return FIELD;
		}
		// mix of all non-method memberTypes AND different return types
		if (cma.isWriteAccess()) {
			if (!cma.hasGetterMemberType()) {
				// return MemberType.FIELD;
			}
			return SETTER;
		}
		if (!cma.isWriteAccess()) {
			if (!cma.hasSetterMemberType()) {
				// return MemberType.FIELD;
			}
			return GETTER;
		}
		return null; // inValid
	}

	@Override
	public boolean isValid() {
		if (specialMemberDescriptor == null)
			return false;
		return specialMemberDescriptor.isValid();
	}

}
