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
package eu.numberfour.n4js.n4JS;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Continue Statement</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link eu.numberfour.n4js.n4JS.ContinueStatement#getLabel <em>Label</em>}</li>
 * </ul>
 *
 * @see eu.numberfour.n4js.n4JS.N4JSPackage#getContinueStatement()
 * @model
 * @generated
 */
public interface ContinueStatement extends Statement {
	/**
	 * Returns the value of the '<em><b>Label</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Label</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Label</em>' reference.
	 * @see #setLabel(LabelledStatement)
	 * @see eu.numberfour.n4js.n4JS.N4JSPackage#getContinueStatement_Label()
	 * @model
	 * @generated
	 */
	LabelledStatement getLabel();

	/**
	 * Sets the value of the '{@link eu.numberfour.n4js.n4JS.ContinueStatement#getLabel <em>Label</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Label</em>' reference.
	 * @see #getLabel()
	 * @generated
	 */
	void setLabel(LabelledStatement value);

} // ContinueStatement
