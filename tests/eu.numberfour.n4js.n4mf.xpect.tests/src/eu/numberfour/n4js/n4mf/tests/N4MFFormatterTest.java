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
package eu.numberfour.n4js.n4mf.tests;

import org.eclipse.xtext.formatting.INodeModelFormatter;
import org.eclipse.xtext.formatting.INodeModelFormatter.IFormattedRegion;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.resource.XtextResource;
import org.junit.runner.RunWith;
import org.xpect.XpectImport;
import org.xpect.expectation.IStringExpectation;
import org.xpect.expectation.StringExpectation;
import org.xpect.parameter.ParameterParser;
import org.xpect.runner.Xpect;
import org.xpect.runner.XpectRunner;
import org.xpect.runner.XpectTestFiles;
import org.xpect.runner.XpectTestFiles.FileRoot;
import org.xpect.setup.XpectSetupFactory;
import org.xpect.xtext.lib.setup.ThisOffset;
import org.xpect.xtext.lib.setup.ThisResource;
import org.xpect.xtext.lib.setup.XtextStandaloneSetup;
import org.xpect.xtext.lib.setup.XtextValidatingSetup;

import com.google.inject.Inject;

import eu.numberfour.n4js.n4mf.tests.N4MFFormatterTest.NullValidatorSetup;

/**
 */
@RunWith(XpectRunner.class)
@XpectTestFiles(relativeTo = FileRoot.PROJECT, baseDir = "model/formatter", fileExtensions = "n4mf")
@XpectImport({ XtextStandaloneSetup.class, NullValidatorSetup.class })
public class N4MFFormatterTest {

	/**
	 * Disable validation for formatter test so most combinations can be tested during formatting without the need to
	 * handle validation issues.
	 */
	@XpectSetupFactory
	public static class NullValidatorSetup extends XtextValidatingSetup {
		/***/
		public NullValidatorSetup(@ThisResource XtextResource resource) {
			super(resource);
		}

		@Override
		public void validate() {
			// do nothing
		}
	}

	@Inject
	private INodeModelFormatter formatter;

	/**
	 * @param expectation
	 *            the expected formatted code
	 * @param resource
	 *            the Xtext resource to be formatted
	 * @param offset
	 *            the optional start offset from where formatting should be applied
	 * @param to
	 *            the optional to offset to which formatting should be applied
	 */
	@ParameterParser(syntax = "('from' offset=OFFSET 'to' to=OFFSET)?")
	@Xpect
	public void formatted(@StringExpectation(whitespaceSensitive = true) IStringExpectation expectation,
			@ThisResource XtextResource resource, @ThisOffset int offset, @ThisOffset int to) {
		ICompositeNode rootNode = resource.getParseResult().getRootNode();
		IFormattedRegion r = null;
		if (offset >= 0 && to > offset) {
			r = formatter.format(rootNode, offset, to - offset);
		} else {
			r = formatter.format(rootNode, rootNode.getOffset(), rootNode.getTotalLength());
		}
		String formatted = r.getFormattedText();
		if (isUnixEnding()) {
			formatted = formatted.replaceAll("\r\n", "\n");
		} else if (isWindowsEnding()) {
			if (!rootNode.getText().contains("\r\n")) {
				formatted = formatted.replaceAll("\r\n", "\n");
			} else {
				formatted = formatted.replaceAll("(!\r)\n", "\r\n");
			}
		}
		expectation.assertEquals(formatted);
	}

	private static boolean isWindowsEnding() {
		String ls = System.getProperty("line.separator");
		return "\r\n".equals(ls);
	}

	private static boolean isUnixEnding() {
		String ls = System.getProperty("line.separator");
		return "\n".equals(ls);
	}
}
