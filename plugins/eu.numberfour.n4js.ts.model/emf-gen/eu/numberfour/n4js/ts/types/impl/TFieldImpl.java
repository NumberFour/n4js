/**
 * Copyright (c) 2016 NumberFour AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package eu.numberfour.n4js.ts.types.impl;

import eu.numberfour.n4js.ts.typeRefs.TypeRef;

import eu.numberfour.n4js.ts.types.MemberType;
import eu.numberfour.n4js.ts.types.TConstableElement;
import eu.numberfour.n4js.ts.types.TField;
import eu.numberfour.n4js.ts.types.TMember;
import eu.numberfour.n4js.ts.types.TTypedElement;
import eu.numberfour.n4js.ts.types.TypesPackage;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>TField</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link eu.numberfour.n4js.ts.types.impl.TFieldImpl#getTypeRef <em>Type Ref</em>}</li>
 *   <li>{@link eu.numberfour.n4js.ts.types.impl.TFieldImpl#isConst <em>Const</em>}</li>
 *   <li>{@link eu.numberfour.n4js.ts.types.impl.TFieldImpl#getCompileTimeValue <em>Compile Time Value</em>}</li>
 *   <li>{@link eu.numberfour.n4js.ts.types.impl.TFieldImpl#isHasExpression <em>Has Expression</em>}</li>
 *   <li>{@link eu.numberfour.n4js.ts.types.impl.TFieldImpl#isOptional_NEW_SYNTAX <em>Optional NEW SYNTAX</em>}</li>
 * </ul>
 *
 * @generated
 */
public class TFieldImpl extends TMemberWithAccessModifierImpl implements TField {
	/**
	 * The cached value of the '{@link #getTypeRef() <em>Type Ref</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTypeRef()
	 * @generated
	 * @ordered
	 */
	protected TypeRef typeRef;

	/**
	 * The default value of the '{@link #isConst() <em>Const</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isConst()
	 * @generated
	 * @ordered
	 */
	protected static final boolean CONST_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isConst() <em>Const</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isConst()
	 * @generated
	 * @ordered
	 */
	protected boolean const_ = CONST_EDEFAULT;

	/**
	 * The default value of the '{@link #getCompileTimeValue() <em>Compile Time Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCompileTimeValue()
	 * @generated
	 * @ordered
	 */
	protected static final String COMPILE_TIME_VALUE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCompileTimeValue() <em>Compile Time Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCompileTimeValue()
	 * @generated
	 * @ordered
	 */
	protected String compileTimeValue = COMPILE_TIME_VALUE_EDEFAULT;

	/**
	 * The default value of the '{@link #isHasExpression() <em>Has Expression</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isHasExpression()
	 * @generated
	 * @ordered
	 */
	protected static final boolean HAS_EXPRESSION_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isHasExpression() <em>Has Expression</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isHasExpression()
	 * @generated
	 * @ordered
	 */
	protected boolean hasExpression = HAS_EXPRESSION_EDEFAULT;

	/**
	 * The default value of the '{@link #isOptional_NEW_SYNTAX() <em>Optional NEW SYNTAX</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isOptional_NEW_SYNTAX()
	 * @generated
	 * @ordered
	 */
	protected static final boolean OPTIONAL_NEW_SYNTAX_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isOptional_NEW_SYNTAX() <em>Optional NEW SYNTAX</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isOptional_NEW_SYNTAX()
	 * @generated
	 * @ordered
	 */
	protected boolean optional_NEW_SYNTAX = OPTIONAL_NEW_SYNTAX_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TFieldImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TypesPackage.Literals.TFIELD;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TypeRef getTypeRef() {
		return typeRef;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetTypeRef(TypeRef newTypeRef, NotificationChain msgs) {
		TypeRef oldTypeRef = typeRef;
		typeRef = newTypeRef;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TypesPackage.TFIELD__TYPE_REF, oldTypeRef, newTypeRef);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTypeRef(TypeRef newTypeRef) {
		if (newTypeRef != typeRef) {
			NotificationChain msgs = null;
			if (typeRef != null)
				msgs = ((InternalEObject)typeRef).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TypesPackage.TFIELD__TYPE_REF, null, msgs);
			if (newTypeRef != null)
				msgs = ((InternalEObject)newTypeRef).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TypesPackage.TFIELD__TYPE_REF, null, msgs);
			msgs = basicSetTypeRef(newTypeRef, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TypesPackage.TFIELD__TYPE_REF, newTypeRef, newTypeRef));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isConst() {
		return const_;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setConst(boolean newConst) {
		boolean oldConst = const_;
		const_ = newConst;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TypesPackage.TFIELD__CONST, oldConst, const_));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getCompileTimeValue() {
		return compileTimeValue;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCompileTimeValue(String newCompileTimeValue) {
		String oldCompileTimeValue = compileTimeValue;
		compileTimeValue = newCompileTimeValue;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TypesPackage.TFIELD__COMPILE_TIME_VALUE, oldCompileTimeValue, compileTimeValue));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isHasExpression() {
		return hasExpression;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setHasExpression(boolean newHasExpression) {
		boolean oldHasExpression = hasExpression;
		hasExpression = newHasExpression;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TypesPackage.TFIELD__HAS_EXPRESSION, oldHasExpression, hasExpression));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isOptional_NEW_SYNTAX() {
		return optional_NEW_SYNTAX;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOptional_NEW_SYNTAX(boolean newOptional_NEW_SYNTAX) {
		boolean oldOptional_NEW_SYNTAX = optional_NEW_SYNTAX;
		optional_NEW_SYNTAX = newOptional_NEW_SYNTAX;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TypesPackage.TFIELD__OPTIONAL_NEW_SYNTAX, oldOptional_NEW_SYNTAX, optional_NEW_SYNTAX));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isOptional() {
		return this.isOptional_NEW_SYNTAX();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isStatic() {
		return (this.isDeclaredStatic() || this.isConst());
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isReadable() {
		return true;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isWriteable() {
		return (!(this.isConst() || this.isFinal()));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MemberType getMemberType() {
		return MemberType.FIELD;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getMemberAsString() {
		final StringBuilder strb = new StringBuilder();
		String _name = this.getName();
		strb.append(_name);
		boolean _isOptional = this.isOptional();
		if (_isOptional) {
			strb.append("?");
		}
		TypeRef _typeRef = this.getTypeRef();
		boolean _tripleNotEquals = (_typeRef != null);
		if (_tripleNotEquals) {
			StringBuilder _append = strb.append(": ");
			TypeRef _typeRef_1 = this.getTypeRef();
			String _typeRefAsString = _typeRef_1.getTypeRefAsString();
			_append.append(_typeRefAsString);
		}
		return strb.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case TypesPackage.TFIELD__TYPE_REF:
				return basicSetTypeRef(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TypesPackage.TFIELD__TYPE_REF:
				return getTypeRef();
			case TypesPackage.TFIELD__CONST:
				return isConst();
			case TypesPackage.TFIELD__COMPILE_TIME_VALUE:
				return getCompileTimeValue();
			case TypesPackage.TFIELD__HAS_EXPRESSION:
				return isHasExpression();
			case TypesPackage.TFIELD__OPTIONAL_NEW_SYNTAX:
				return isOptional_NEW_SYNTAX();
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
			case TypesPackage.TFIELD__TYPE_REF:
				setTypeRef((TypeRef)newValue);
				return;
			case TypesPackage.TFIELD__CONST:
				setConst((Boolean)newValue);
				return;
			case TypesPackage.TFIELD__COMPILE_TIME_VALUE:
				setCompileTimeValue((String)newValue);
				return;
			case TypesPackage.TFIELD__HAS_EXPRESSION:
				setHasExpression((Boolean)newValue);
				return;
			case TypesPackage.TFIELD__OPTIONAL_NEW_SYNTAX:
				setOptional_NEW_SYNTAX((Boolean)newValue);
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
			case TypesPackage.TFIELD__TYPE_REF:
				setTypeRef((TypeRef)null);
				return;
			case TypesPackage.TFIELD__CONST:
				setConst(CONST_EDEFAULT);
				return;
			case TypesPackage.TFIELD__COMPILE_TIME_VALUE:
				setCompileTimeValue(COMPILE_TIME_VALUE_EDEFAULT);
				return;
			case TypesPackage.TFIELD__HAS_EXPRESSION:
				setHasExpression(HAS_EXPRESSION_EDEFAULT);
				return;
			case TypesPackage.TFIELD__OPTIONAL_NEW_SYNTAX:
				setOptional_NEW_SYNTAX(OPTIONAL_NEW_SYNTAX_EDEFAULT);
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
			case TypesPackage.TFIELD__TYPE_REF:
				return typeRef != null;
			case TypesPackage.TFIELD__CONST:
				return const_ != CONST_EDEFAULT;
			case TypesPackage.TFIELD__COMPILE_TIME_VALUE:
				return COMPILE_TIME_VALUE_EDEFAULT == null ? compileTimeValue != null : !COMPILE_TIME_VALUE_EDEFAULT.equals(compileTimeValue);
			case TypesPackage.TFIELD__HAS_EXPRESSION:
				return hasExpression != HAS_EXPRESSION_EDEFAULT;
			case TypesPackage.TFIELD__OPTIONAL_NEW_SYNTAX:
				return optional_NEW_SYNTAX != OPTIONAL_NEW_SYNTAX_EDEFAULT;
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
		if (baseClass == TTypedElement.class) {
			switch (derivedFeatureID) {
				case TypesPackage.TFIELD__TYPE_REF: return TypesPackage.TTYPED_ELEMENT__TYPE_REF;
				default: return -1;
			}
		}
		if (baseClass == TConstableElement.class) {
			switch (derivedFeatureID) {
				case TypesPackage.TFIELD__CONST: return TypesPackage.TCONSTABLE_ELEMENT__CONST;
				case TypesPackage.TFIELD__COMPILE_TIME_VALUE: return TypesPackage.TCONSTABLE_ELEMENT__COMPILE_TIME_VALUE;
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
		if (baseClass == TTypedElement.class) {
			switch (baseFeatureID) {
				case TypesPackage.TTYPED_ELEMENT__TYPE_REF: return TypesPackage.TFIELD__TYPE_REF;
				default: return -1;
			}
		}
		if (baseClass == TConstableElement.class) {
			switch (baseFeatureID) {
				case TypesPackage.TCONSTABLE_ELEMENT__CONST: return TypesPackage.TFIELD__CONST;
				case TypesPackage.TCONSTABLE_ELEMENT__COMPILE_TIME_VALUE: return TypesPackage.TFIELD__COMPILE_TIME_VALUE;
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
				case TypesPackage.TMEMBER___GET_MEMBER_TYPE: return TypesPackage.TFIELD___GET_MEMBER_TYPE;
				case TypesPackage.TMEMBER___IS_OPTIONAL: return TypesPackage.TFIELD___IS_OPTIONAL;
				case TypesPackage.TMEMBER___IS_READABLE: return TypesPackage.TFIELD___IS_READABLE;
				case TypesPackage.TMEMBER___IS_WRITEABLE: return TypesPackage.TFIELD___IS_WRITEABLE;
				case TypesPackage.TMEMBER___GET_MEMBER_AS_STRING: return TypesPackage.TFIELD___GET_MEMBER_AS_STRING;
				case TypesPackage.TMEMBER___IS_STATIC: return TypesPackage.TFIELD___IS_STATIC;
				default: return super.eDerivedOperationID(baseOperationID, baseClass);
			}
		}
		if (baseClass == TTypedElement.class) {
			switch (baseOperationID) {
				default: return -1;
			}
		}
		if (baseClass == TConstableElement.class) {
			switch (baseOperationID) {
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
			case TypesPackage.TFIELD___IS_OPTIONAL:
				return isOptional();
			case TypesPackage.TFIELD___IS_STATIC:
				return isStatic();
			case TypesPackage.TFIELD___IS_READABLE:
				return isReadable();
			case TypesPackage.TFIELD___IS_WRITEABLE:
				return isWriteable();
			case TypesPackage.TFIELD___GET_MEMBER_TYPE:
				return getMemberType();
			case TypesPackage.TFIELD___GET_MEMBER_AS_STRING:
				return getMemberAsString();
		}
		return super.eInvoke(operationID, arguments);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (const: ");
		result.append(const_);
		result.append(", compileTimeValue: ");
		result.append(compileTimeValue);
		result.append(", hasExpression: ");
		result.append(hasExpression);
		result.append(", optional_NEW_SYNTAX: ");
		result.append(optional_NEW_SYNTAX);
		result.append(')');
		return result.toString();
	}

} //TFieldImpl
