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

import eu.numberfour.n4js.ts.types.TypesPackage;
import eu.numberfour.n4js.ts.types.UndefinedType;

import org.eclipse.emf.ecore.EClass;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Undefined Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * @generated
 */
public class UndefinedTypeImpl extends BuiltInTypeImpl implements UndefinedType {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected UndefinedTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TypesPackage.Literals.UNDEFINED_TYPE;
	}

} //UndefinedTypeImpl