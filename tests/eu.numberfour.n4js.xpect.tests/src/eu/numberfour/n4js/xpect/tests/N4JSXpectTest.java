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
package eu.numberfour.n4js.xpect.tests;

import org.junit.runner.RunWith;
import org.xpect.XpectImport;
import org.xpect.runner.XpectRunner;
import org.xpect.runner.XpectSuiteClasses;
import org.xpect.runner.XpectTestFiles;
import org.xpect.runner.XpectTestFiles.FileRoot;
import org.xpect.xtext.lib.tests.LinkingTest;
import org.xpect.xtext.lib.tests.ResourceDescriptionTest;
import org.xpect.xtext.lib.tests.ValidationTest;

import eu.numberfour.n4js.xpect.methods.AccessModifierXpectMethod;
import eu.numberfour.n4js.xpect.methods.ElementKeywordXpectMethod;
import eu.numberfour.n4js.xpect.methods.FindReferencesXpectMethod;
import eu.numberfour.n4js.xpect.methods.FormatterXpectMethod;
import eu.numberfour.n4js.xpect.methods.NoerrorsXpectMethod;
import eu.numberfour.n4js.xpect.methods.ReturnXpectMethod;
import eu.numberfour.n4js.xpect.methods.TypeXpectMethod;
import eu.numberfour.n4js.xpect.methods.scoping.ScopeXpectMethod;
import eu.numberfour.n4js.xpect.validation.suppression.SuppressIssuesSetup;

/**
 * Xpect test class, also configured in fragment.xml, executes all Xpect tests found in model folder.
 */
@XpectSuiteClasses({
		AccessModifierXpectMethod.class,
		LinkingTest.class,
		ReturnXpectMethod.class,
		TypeXpectMethod.class,
		ScopeXpectMethod.class,
		NoerrorsXpectMethod.class,
		ResourceDescriptionTest.class,
		ValidationTest.class,
		FormatterXpectMethod.class,
		FindReferencesXpectMethod.class,
		ElementKeywordXpectMethod.class
})
@RunWith(XpectRunner.class)
@XpectTestFiles(relativeTo = FileRoot.PROJECT, baseDir = "model", fileExtensions = { "xt" })
@XpectImport(SuppressIssuesSetup.class)
public class N4JSXpectTest {
	//
}
