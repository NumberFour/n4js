/**
 * Copyright (c) 2016 NumberFour AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package eu.numberfour.n4js.ts.types;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>TAnnotation Type Ref Argument</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see eu.numberfour.n4js.ts.types.TypesPackage#getTAnnotationTypeRefArgument()
 * @model
 * @generated
 */
public interface TAnnotationTypeRefArgument extends TAnnotationArgument, TypeReferenceContainer {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation" unique="false"
	 *        annotation="http://www.eclipse.org/emf/2002/GenModel body='<%eu.numberfour.n4js.ts.typeRefs.TypeRef%> _typeRef = this.getTypeRef();\nreturn _typeRef.getTypeRefAsString();'"
	 * @generated
	 */
	String getArgAsString();

} // TAnnotationTypeRefArgument
