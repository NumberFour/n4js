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
 * A representation of the model object '<em><b>Additive Expression</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link eu.numberfour.n4js.n4JS.AdditiveExpression#getLhs <em>Lhs</em>}</li>
 *   <li>{@link eu.numberfour.n4js.n4JS.AdditiveExpression#getOp <em>Op</em>}</li>
 *   <li>{@link eu.numberfour.n4js.n4JS.AdditiveExpression#getRhs <em>Rhs</em>}</li>
 * </ul>
 *
 * @see eu.numberfour.n4js.n4JS.N4JSPackage#getAdditiveExpression()
 * @model
 * @generated
 */
public interface AdditiveExpression extends Expression {
	/**
	 * Returns the value of the '<em><b>Lhs</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Lhs</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Lhs</em>' containment reference.
	 * @see #setLhs(Expression)
	 * @see eu.numberfour.n4js.n4JS.N4JSPackage#getAdditiveExpression_Lhs()
	 * @model containment="true"
	 * @generated
	 */
	Expression getLhs();

	/**
	 * Sets the value of the '{@link eu.numberfour.n4js.n4JS.AdditiveExpression#getLhs <em>Lhs</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Lhs</em>' containment reference.
	 * @see #getLhs()
	 * @generated
	 */
	void setLhs(Expression value);

	/**
	 * Returns the value of the '<em><b>Op</b></em>' attribute.
	 * The literals are from the enumeration {@link eu.numberfour.n4js.n4JS.AdditiveOperator}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Op</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Op</em>' attribute.
	 * @see eu.numberfour.n4js.n4JS.AdditiveOperator
	 * @see #setOp(AdditiveOperator)
	 * @see eu.numberfour.n4js.n4JS.N4JSPackage#getAdditiveExpression_Op()
	 * @model unique="false"
	 * @generated
	 */
	AdditiveOperator getOp();

	/**
	 * Sets the value of the '{@link eu.numberfour.n4js.n4JS.AdditiveExpression#getOp <em>Op</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Op</em>' attribute.
	 * @see eu.numberfour.n4js.n4JS.AdditiveOperator
	 * @see #getOp()
	 * @generated
	 */
	void setOp(AdditiveOperator value);

	/**
	 * Returns the value of the '<em><b>Rhs</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Rhs</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Rhs</em>' containment reference.
	 * @see #setRhs(Expression)
	 * @see eu.numberfour.n4js.n4JS.N4JSPackage#getAdditiveExpression_Rhs()
	 * @model containment="true"
	 * @generated
	 */
	Expression getRhs();

	/**
	 * Sets the value of the '{@link eu.numberfour.n4js.n4JS.AdditiveExpression#getRhs <em>Rhs</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Rhs</em>' containment reference.
	 * @see #getRhs()
	 * @generated
	 */
	void setRhs(Expression value);

} // AdditiveExpression
