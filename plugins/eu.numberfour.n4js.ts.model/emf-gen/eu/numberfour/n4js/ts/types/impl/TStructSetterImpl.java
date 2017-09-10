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
package eu.numberfour.n4js.ts.types.impl;

import eu.numberfour.n4js.ts.types.MemberAccessModifier;
import eu.numberfour.n4js.ts.types.TMember;
import eu.numberfour.n4js.ts.types.TMemberWithAccessModifier;
import eu.numberfour.n4js.ts.types.TSetter;
import eu.numberfour.n4js.ts.types.TStructMember;
import eu.numberfour.n4js.ts.types.TStructSetter;
import eu.numberfour.n4js.ts.types.TypesPackage;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>TStruct Setter</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link eu.numberfour.n4js.ts.types.impl.TStructSetterImpl#getDefinedMember <em>Defined Member</em>}</li>
 * </ul>
 *
 * @generated
 */
public class TStructSetterImpl extends TSetterImpl implements TStructSetter {
	/**
	 * The cached value of the '{@link #getDefinedMember() <em>Defined Member</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDefinedMember()
	 * @generated
	 * @ordered
	 */
	protected TStructMember definedMember;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TStructSetterImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TypesPackage.Literals.TSTRUCT_SETTER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TStructMember getDefinedMember() {
		if (definedMember != null && definedMember.eIsProxy()) {
			InternalEObject oldDefinedMember = (InternalEObject)definedMember;
			definedMember = (TStructMember)eResolveProxy(oldDefinedMember);
			if (definedMember != oldDefinedMember) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, TypesPackage.TSTRUCT_SETTER__DEFINED_MEMBER, oldDefinedMember, definedMember));
			}
		}
		return definedMember;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TStructMember basicGetDefinedMember() {
		return definedMember;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDefinedMember(TStructMember newDefinedMember) {
		TStructMember oldDefinedMember = definedMember;
		definedMember = newDefinedMember;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TypesPackage.TSTRUCT_SETTER__DEFINED_MEMBER, oldDefinedMember, definedMember));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isWriteable() {
		return true;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MemberAccessModifier getDefaultMemberAccessModifier() {
		return MemberAccessModifier.PUBLIC;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isStatic() {
		return false;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MemberAccessModifier getMemberAccessModifier() {
		return MemberAccessModifier.PUBLIC;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TypesPackage.TSTRUCT_SETTER__DEFINED_MEMBER:
				if (resolve) return getDefinedMember();
				return basicGetDefinedMember();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case TypesPackage.TSTRUCT_SETTER__DEFINED_MEMBER:
				setDefinedMember((TStructMember)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case TypesPackage.TSTRUCT_SETTER__DEFINED_MEMBER:
				setDefinedMember((TStructMember)null);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case TypesPackage.TSTRUCT_SETTER__DEFINED_MEMBER:
				return definedMember != null;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
		if (baseClass == TStructMember.class) {
			switch (derivedFeatureID) {
				case TypesPackage.TSTRUCT_SETTER__DEFINED_MEMBER: return TypesPackage.TSTRUCT_MEMBER__DEFINED_MEMBER;
				default: return -1;
			}
		}
		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
		if (baseClass == TStructMember.class) {
			switch (baseFeatureID) {
				case TypesPackage.TSTRUCT_MEMBER__DEFINED_MEMBER: return TypesPackage.TSTRUCT_SETTER__DEFINED_MEMBER;
				default: return -1;
			}
		}
		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eDerivedOperationID(int baseOperationID, Class<?> baseClass) {
		if (baseClass == TMember.class) {
			switch (baseOperationID) {
				case TypesPackage.TMEMBER___GET_MEMBER_ACCESS_MODIFIER: return TypesPackage.TSTRUCT_SETTER___GET_MEMBER_ACCESS_MODIFIER;
				case TypesPackage.TMEMBER___IS_WRITEABLE: return TypesPackage.TSTRUCT_SETTER___IS_WRITEABLE;
				case TypesPackage.TMEMBER___IS_STATIC: return TypesPackage.TSTRUCT_SETTER___IS_STATIC;
				default: return super.eDerivedOperationID(baseOperationID, baseClass);
			}
		}
		if (baseClass == TMemberWithAccessModifier.class) {
			switch (baseOperationID) {
				case TypesPackage.TMEMBER_WITH_ACCESS_MODIFIER___GET_MEMBER_ACCESS_MODIFIER: return TypesPackage.TSTRUCT_SETTER___GET_MEMBER_ACCESS_MODIFIER;
				default: return super.eDerivedOperationID(baseOperationID, baseClass);
			}
		}
		if (baseClass == TSetter.class) {
			switch (baseOperationID) {
				case TypesPackage.TSETTER___IS_WRITEABLE: return TypesPackage.TSTRUCT_SETTER___IS_WRITEABLE;
				default: return super.eDerivedOperationID(baseOperationID, baseClass);
			}
		}
		if (baseClass == TStructMember.class) {
			switch (baseOperationID) {
				case TypesPackage.TSTRUCT_MEMBER___GET_DEFAULT_MEMBER_ACCESS_MODIFIER: return TypesPackage.TSTRUCT_SETTER___GET_DEFAULT_MEMBER_ACCESS_MODIFIER;
				case TypesPackage.TSTRUCT_MEMBER___IS_STATIC: return TypesPackage.TSTRUCT_SETTER___IS_STATIC;
				case TypesPackage.TSTRUCT_MEMBER___GET_MEMBER_ACCESS_MODIFIER: return TypesPackage.TSTRUCT_SETTER___GET_MEMBER_ACCESS_MODIFIER;
				default: return -1;
			}
		}
		return super.eDerivedOperationID(baseOperationID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
			case TypesPackage.TSTRUCT_SETTER___IS_WRITEABLE:
				return isWriteable();
			case TypesPackage.TSTRUCT_SETTER___GET_DEFAULT_MEMBER_ACCESS_MODIFIER:
				return getDefaultMemberAccessModifier();
			case TypesPackage.TSTRUCT_SETTER___IS_STATIC:
				return isStatic();
			case TypesPackage.TSTRUCT_SETTER___GET_MEMBER_ACCESS_MODIFIER:
				return getMemberAccessModifier();
		}
		return super.eInvoke(operationID, arguments);
	}

} //TStructSetterImpl