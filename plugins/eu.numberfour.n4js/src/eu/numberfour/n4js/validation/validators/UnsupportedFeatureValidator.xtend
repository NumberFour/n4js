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
package eu.numberfour.n4js.validation.validators

import eu.numberfour.n4js.n4JS.Argument
import eu.numberfour.n4js.n4JS.BindingPattern
import eu.numberfour.n4js.n4JS.ExportDeclaration
import eu.numberfour.n4js.n4JS.ExportableElement
import eu.numberfour.n4js.n4JS.FormalParameter
import eu.numberfour.n4js.n4JS.ImportDeclaration
import eu.numberfour.n4js.n4JS.N4ClassDefinition
import eu.numberfour.n4js.n4JS.N4ClassExpression
import eu.numberfour.n4js.n4JS.N4JSPackage
import eu.numberfour.n4js.n4JS.NamedElement
import eu.numberfour.n4js.n4JS.NewTarget
import eu.numberfour.n4js.n4JS.TaggedTemplateString
import eu.numberfour.n4js.validation.ASTStructureValidator
import eu.numberfour.n4js.validation.AbstractN4JSDeclarativeValidator
import eu.numberfour.n4js.validation.IssueCodes
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.validation.Check
import org.eclipse.xtext.validation.EValidatorRegistrar

/**
 * Validations to show an error for unsupported language features, mostly ECMAScript6 features.
 * These validations will be removed over time once the corresponding features are implemented.
 */
class UnsupportedFeatureValidator extends AbstractN4JSDeclarativeValidator {

	/**
	 * NEEEDED
	 *
	 * when removed check methods will be called twice once by N4JSValidator, and once by
	 * AbstractDeclarativeN4JSValidator
	 */
	override register(EValidatorRegistrar registrar) {
		// nop
	}


	@Check
	def void checkEmptyImport(ImportDeclaration importDecl) {
		if(importDecl.importSpecifiers.empty) {
			unsupported("empty import", importDecl);
		}
	}


	@Check
	def void checkAnonymousDefaultExport(ExportableElement decl) {
		if(decl instanceof NamedElement) {
			if(decl.name===null && decl.exportedAsDefault) {
				unsupported("anonymous default export", decl);
			}
		}
	}
	@Check
	def void checkSeparateExport(ExportDeclaration exportDecl) {
		if(exportDecl.exportedElement===null) {
			if(exportDecl.defaultExport && exportDecl.defaultExportedExpression!==null) {
				unsupported(
					"exporting values (only declared classes, interfaces, enums, functions and variables can be exported)",
					exportDecl);
			} else {
				unsupported(
					"separate export statements (add keyword 'export' directly before a class, interface, enum, function or variable declaration)",
					exportDecl);
			}
		}
	}


	@Check
	def void checkTaggedTemplateLiteral(TaggedTemplateString tts) {
		unsupported("tagged template literals", tts, N4JSPackage.eINSTANCE.taggedTemplateString_Target);
	}


	// TODO when removing this method, remove flag 'classExpressionsAreAllowed' as well!
	@Check
	def void checkClassExpression(N4ClassExpression classExpr) {
		if(!classExpressionsAreAllowed) {
			if(classExpr.name!==null) {
				unsupported("class expressions", classExpr, N4JSPackage.eINSTANCE.n4ClassExpression_Name);
			} else {
				unsupported("class expressions", classExpr, NodeModelUtils.getNode(classExpr).offset, 5); // the first 5 characters are always the 'class' keyword
			}
		}
	}
	private static boolean classExpressionsAreAllowed = false;


	@Check
	def void checkNewTarget(NewTarget newTarget) {
		unsupported("new.target", newTarget);
	}


	@Check
	def void checkExtendsExpression(N4ClassDefinition classDef) {
		if(classDef.superClassExpression!==null) {
			unsupported("extends <expression>", classDef, N4JSPackage.eINSTANCE.n4ClassDefinition_SuperClassExpression);
		}
	}


	@Check
	def void checkBindingPatternAsFpar(BindingPattern pattern) {
		if(pattern.eContainer instanceof FormalParameter) {
			unsupported("destructuring patterns as formal parameter", pattern);
		}
	}


	/**
	 * NOTE: in addition to the errors produced by this method, spread operator in <em>array literals</em> is also
	 * unsupported; but that is checked in
	 * {@link ASTStructureValidator#validateSpreadInArrayLiteral(eu.numberfour.n4js.n4JS.ArrayElement,
	 * eu.numberfour.n4js.validation.ASTStructureDiagnosticProducer)}
	 */
	@Check
	def void checkSpreadOperatorInNewAndCallExpressions(Argument argument) {
		if(argument.spread) {
			unsupported("spread operator in new and call expressions (only allowed in array destructuring patterns)",
				argument, N4JSPackage.eINSTANCE.argument_Spread);
		}
	}


	// ----------------------------------------------------------------------------------------------------------------
	// UTILITY METHODS

	def private void unsupported(String msg, EObject source) {
		unsupported(msg, source, null);
	}
	def private void unsupported(String msg, EObject source, EStructuralFeature feature) {
		addIssue(
			IssueCodes.getMessageForUNSUPPORTED(msg),
			source, feature,
			IssueCodes.UNSUPPORTED);
	}
	def private void unsupported(String msg, EObject source, int offset, int length) {
		addIssue(
			IssueCodes.getMessageForUNSUPPORTED(msg),
			source,
			offset, length,
			IssueCodes.UNSUPPORTED);
	}

	/**
	 * Turns off unsupported feature validation for class expressions, invokes given function, and turns validation
	 * back on (for testing of class expressions).
	 */
	def public static void allowClassExpressions(()=>void r) {
		try {
			classExpressionsAreAllowed = true;
			r.apply();
		}
		finally {
			classExpressionsAreAllowed = false;
		}
	}
}
