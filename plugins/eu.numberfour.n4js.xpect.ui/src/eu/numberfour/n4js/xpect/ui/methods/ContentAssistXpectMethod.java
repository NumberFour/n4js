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
package eu.numberfour.n4js.xpect.ui.methods;

import static eu.numberfour.n4js.xpect.ui.methods.QuickFixTestHelper.asString2;
import static eu.numberfour.n4js.xpect.ui.methods.QuickFixTestHelper.extractSingleChangedLine;
import static eu.numberfour.n4js.xpect.ui.methods.QuickFixTestHelper.separateOnCommaAndQuote;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.xpect.XpectImport;
import org.xpect.expectation.CommaSeparatedValuesExpectation;
import org.xpect.expectation.ICommaSeparatedValuesExpectation;
import org.xpect.expectation.IExpectationRegion;
import org.xpect.expectation.IStringExpectation;
import org.xpect.expectation.impl.AbstractExpectation;
import org.xpect.expectation.impl.CommaSeparatedValuesExpectationImpl;
import org.xpect.expectation.impl.ExpectationCollection;
import org.xpect.parameter.ParameterParser;
import org.xpect.runner.Xpect;
import org.xpect.setup.ISetupInitializer;
import org.xpect.xtext.lib.setup.ThisResource;
import org.xpect.xtext.lib.tests.ValidationTestModuleSetup;
import org.xpect.xtext.lib.tests.ValidationTestModuleSetup.ConsumedIssues;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import eu.numberfour.n4js.xpect.common.N4JSOffsetAdapter;
import eu.numberfour.n4js.xpect.config.Config;
import eu.numberfour.n4js.xpect.config.VarDef;
import eu.numberfour.n4js.xpect.config.XpEnvironmentData;
import eu.numberfour.n4js.xpect.ui.methods.QuickFixTestHelper.ChangeInfo;
import junit.framework.AssertionFailedError;
import junit.framework.ComparisonFailure;

/**
 * Provides XPECT test methods for content assist
 */
@SuppressWarnings("restriction")
@XpectImport({ N4JSOffsetAdapter.class, XpEnvironmentData.class, VarDef.class, Config.class,
		ValidationTestModuleSetup.class })
public class ContentAssistXpectMethod {
	@Inject
	private N4ContentAssistProcessorTestBuilderHelper n4ContentAssistProcessorTestBuilderHelper;

	private static Logger logger = Logger.getLogger(ContentAssistXpectMethod.class);

	/*-
	 contentAssist              at ’a.<|>methodA’        ’methodA2’           --> a.methodA2() <|>methodA
	 contentAssist              at ’a.<|><[>methodA<]>’  ’methodA2’ override  --> a.methodA2()<|>
	 contentAssist              at ’a.methodA’           ’methodA2’           --> a.methodA2() <|>methodA
	 contentAssist              at ’var A<|> a’          ’A - import’         ---
	 contentAssist              at 'a.<|>methodA'       'methodA2'            --> a.methodA2()<|>methodA
	 contentAssist              at 'a.<|>methodA'       'methodA2'   insert   --> a.methodA2()<|>methodA
	 contentAssist              at 'a.<|>methodA'       'methodA2'   override --> a.methodA2()<|>
	 contentAssist kind 'smart' at '<a|>methodA'        'methodA2'            ---
	              kind             offset               selected    mode
	              arg3             arg2                 arg4        arg5
	 */
	/**
	 * Application of a content assist. The right hand side describes the expected modifications.
	 *
	 * Up to now only single-line changes are supported.
	 *
	 * @param expectation
	 *            injected Applicaiton
	 * @param resource
	 *            injected xtext resource
	 * @param offset
	 *            parsed arg2 - Cursorposition and Selection
	 * @param kind
	 *            parsed arg3 - named kind of assistence-provider 'recommenders',...
	 * @param selected
	 *            parsed arg4 - string of things to select in proposal list.
	 * @param mode
	 *            parsed arg5 - mode modifier
	 * @throws Exception
	 *             in junit-error case
	 */
	@Xpect
	@ParameterParser(syntax = "( ('kind' arg3=STRING)? 'at' (arg2=STRING  ('apply' arg4=STRING)?  (arg5=ID)? )? )?")
	@ConsumedIssues({ Severity.INFO, Severity.ERROR, Severity.WARNING })
	public void contentAssist(
			IStringExpectation expectation, // arg0
			@ThisResource XtextResource resource, // arg1
			RegionWithCursor offset, // arg2 //@ThisOffset is obsolete
			String kind, // arg3
			String selected, // arg4
			String mode // arg5
	) throws Exception {

		N4ContentAssistProcessorTestBuilder fixture = n4ContentAssistProcessorTestBuilderHelper
				.createTestBuilderForResource(resource);
		ICompletionProposal proposal = exactlyMatchingProposal(offset, fixture, selected);
		String before = resource.getParseResult().getRootNode().getText();

		// apply:
		// so working with the fixture cannot get any selection information since these are mocked away.
		IXtextDocument document = fixture.getDocument(
				XtextResourceCleanUtil.cleanXtextResource(resource), before);

		Optional<String> optionalMode = Optional.ofNullable(mode);
		if (optionalMode.isPresent() && optionalMode.get().trim() == "override") {

			// TODO mode override: needs to have a real selectionprovider in the fixture.
			// currently there is a mockup which doesn't support selection
			// see org.eclipse.xtext.junit4.ui.ContentAssistProcessorTestBuilder.getSourceViewer(String, IXtextDocument)

			// val sv = fixture.getSourceViewer(null, document)

			// sv.selectionProvider.selection = new TextSelection(..)
		}

		String after = applyProposal(proposal, document);

		// Expecting change only in single line:
		ChangeInfo changedLines = extractSingleChangedLine(before, after);
		if (changedLines.isMoreThanOne()) {
			throw new AssertionError("more then one line changed: " + changedLines.asString());
		} else if (changedLines.isEmpty()) {
			throw new AssertionError("Nothing changed.");
		}
		String exp = changedLines.first().getAfter();

		Point selection = proposal.getSelection(document);
		if (selection != null) {
			IExpectationRegion region = ((AbstractExpectation) expectation).getRegion();
			if (CursorMarkerHelper.exists(region.getRegionText(), CursorMarkerHelper.markerCursor)) {
				int newPos = selection.x - changedLines.first().getAfterOffset();
				exp = new StringBuilder(exp).insert(newPos, CursorMarkerHelper.markerCursor).toString();
			}
		}
		// Single changed line:
		expectation.assertEquals(exp);

		// TODO up to now this only removes the locations for the cursor but doesn't check the location.
		// TODO multilines must be supported.
		// // before = replaceXPECT(before);
		// // after = replaceXPECT(after);
		// expectation.assertEquals(after)
		// expectation.assertDiffEquals("a", "b - not implemented yet")
	}

	/*-
	 contentAssistList              at ’a.<|>methodA’       display    ’methodA2’           --> ’methodA2(): any - A’
	 contentAssistList              at 'a.<|>methodA'       proposals                       --> <$objectProposals>, methodA2
	 contentAssistList              at 'a.<|>methodA'       proposals                       --> <$objectProposals>, methodA2
	 contentAssistList              at 'a.<|>methodA'       proposals                       --> methodA, methodA2
	 contentAssistList              at 'a.<|>methodA'       proposals             contains  --> methodA2
	 contentAssistList              at 'a.<|>methodA'       proposals             exactly   --> methodA, methodA2
	 contentAssistList              at 'a.<|>methodA'       proposals             not       --> methodB
	 contentAssistList              at 'a.<|>methodA'       proposals             ordered   --> methodA, methodA2
	 contentAssistList              at 'a.<|>methodA'       proposals             unordered --> methodA2, methodA
	 contentAssistList              at 'a.<|>methodA'       display   'methodA2'            --> 'methodA2(): any - A'
	 contentAssistList kind 'smart' at 'a.<|>methodA'       display   'methodA2'            --> 'methodA2(): any - A'
	
	                    kind        offset                  checkType  selected    mode
	                    arg4        arg2                    arg3       arg5        arg6
	 */
	/**
	 * Compares the expected list of expected display strings with the actual computed one by the proposal provider.
	 *
	 * @param expect
	 *            injected from right hand side - a list expected display strings
	 * @param resource
	 *            injected - the resource under test
	 * @param offset
	 *            arg2 - the offset of where to invoke content assist given as string matching next line with cursor
	 *            position
	 * @param checkType
	 *            arg3 - one of {proposals | display }
	 * @param kind
	 *            arg4 - contentAssist - cycling: current kind like 'n4js'(default) or 'recommenders'
	 * @param selected
	 *            arg5 - chosen selection form the proposal list
	 * @param mode
	 *            arg6 - depending on checkTye: apply->{insert(default)|override}, proposals->{contains,
	 *            exactly(default), not}
	 * @param orderMod
	 *            arg7 - for proposal-modes contains/exactly there are two mode available{ordered, unordered(default)}
	 * @throws Exception
	 *             some exception
	 */
	@Xpect
	@ParameterParser(syntax = "( ('kind' arg4=STRING)? 'at' (arg2=STRING (arg3=ID  (arg5=STRING)?  (arg6=ID (arg7=ID)? )? )? )? )?")
	@ConsumedIssues({ Severity.INFO, Severity.ERROR, Severity.WARNING })
	public void contentAssistList(
			@CommaSeparatedValuesExpectation(quoted = true) ICommaSeparatedValuesExpectation expect, // arg0
			@ThisResource XtextResource resource, // arg1
			RegionWithCursor offset, // arg2 //@ThisOffset is obsolete
			String checkType, // arg3
			String kind, // arg4
			String selected, // arg5
			String mode, // arg6
			String orderMod, // arg7
			ISetupInitializer<XpEnvironmentData> uiTestRunInit) throws Exception {

		XpEnvironmentData xpEnvData = new XpEnvironmentData();
		uiTestRunInit.initialize(xpEnvData);
		xpEnvData.setResourceUnderTest(resource);

		// Expansion of Variables. This changes the original expectation:
		CommaSeparatedValuesExpectationImpl csvE = (CommaSeparatedValuesExpectationImpl) expect;
		Pair<CommaSeparatedValuesExpectationImpl, CharSequence> exptectationAndText = expandVariables(csvE, xpEnvData);

		CommaSeparatedValuesExpectationImpl expectation = exptectationAndText.getKey();
		CharSequence expectedText = exptectationAndText.getValue();

		// System.out.println("---|" + expectedText + "|---");
		List<String> proposals = getProposalDisplayStrings(resource, offset, kind);

		if (("display").equals(checkType)) {

			// TODO check original code, seems like errors wehn porting

			// expectation exactly one string:
			// pick the proposal, test that only one is Picked && verify with expectation.
			List<String> candidates = proposals.stream().filter(p -> p.contains(selected)).collect(Collectors.toList());
			if (candidates.size() > 1) {
				StringBuilder sb = new StringBuilder();
				sb.append("more then one proposal matches the selection '").append(selected).append("' matches:[");
				candidates.forEach(m -> sb.append(m).append(","));
				sb.append("]");
				throw new RuntimeException(sb.toString());
			}
			if (candidates.size() == 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("nothing matches the selection '").append(selected).append("' available are [");
				candidates.forEach(m -> sb.append(m).append(","));
				sb.append("]");
				throw new RuntimeException(sb.toString());
			}
			// exactly one:
			expectation.assertEquals(candidates);
			return;
		} else if ("proposals".equals(checkType)) {

			// order-mode, default is 'unordered'
			boolean ordered = ("ordered" == orderMod || "ordered" == mode);

			if (mode == null) {
				if (ordered) {
					assertExactlyOrdered(proposals, separateOnCommaAndQuote(expectedText), expectation);
				} else {
					assertExactly(proposals,
							separateOnCommaAndQuote(expectedText),
							expectation);
				}
				return;
			}

			switch (mode) {
			case "":
			case "ordered": // just in case mode is default, then orderMod gets assigned to mode
			case "unordered": // just in case mode is default, then orderMod gets assigned to mode
			case "exactly": // default case.
				if (ordered) {
					assertExactlyOrdered(proposals, separateOnCommaAndQuote(expectedText), expectation);
				} else {
					assertExactly(proposals,
							separateOnCommaAndQuote(expectedText),
							expectation);
				}
				return;

			case "contains": {
				if (ordered) {
					assertContainingMatchAllOrdered(proposals, separateOnCommaAndQuote(expectedText), expectation);
				} else {
					assertContainingMatchAll(proposals, separateOnCommaAndQuote(expectedText), expectation);
				}
				return;
			}
			case "not":

				// ordered / unordered doesn't apply here
				assertNoMatch(proposals, separateOnCommaAndQuote(expectedText), expectation);
				return;

			default:
				throw new RuntimeException("unrecognized mode for proposal-test : '" + mode + "'");
			}

		} else {
			throw new UnsupportedOperationException("unrecognized checktype: '" + checkType + "'");
		}
	}

	private static class VarSubstCommaSeparatedValuesExpectationImpl extends CommaSeparatedValuesExpectationImpl {

		/***/
		public VarSubstCommaSeparatedValuesExpectationImpl(CommaSeparatedValuesExpectationImpl original,
				CommaSeparatedValuesExpectation annotation) {
			super(original, annotation);
		}

		@Override
		protected ExpectationCollection createExpectationCollection() {
			CommaSeparatedValuesExpectation annotation = getAnnotation();
			ExpectationCollection exp = new VarSubstExpectationCollection(
					((CommaSeparatedValuesExpectationCfg) annotation).getData());
			exp.setCaseSensitive(annotation.caseSensitive());
			exp.setOrdered(annotation.ordered());
			exp.setQuoted(annotation.quoted());
			exp.setSeparator(',');
			exp.setWhitespaceSensitive(annotation.whitespaceSensitive());
			exp.init(getExpectation());
			return exp;
		}

	}

	private Pair<CommaSeparatedValuesExpectationImpl, CharSequence> expandVariables(
			CommaSeparatedValuesExpectationImpl csvE,
			XpEnvironmentData data) {

		// val CommaSeparatedValuesExpectationImpl csvE = (expectation as CommaSeparatedValuesExpectationImpl);
		IExpectationRegion region = csvE.getRegion();
		CharSequence doc = region.getDocument();
		CharSequence expectedText = null;
		if (region.getLength() < 0) {
			expectedText = "";
		} else {
			expectedText = doc.subSequence(region.getOffset(), region.getOffset() + region.getLength());
		}

		CommaSeparatedValuesExpectationCfg cfg = new CommaSeparatedValuesExpectationCfg(csvE.getAnnotation());
		cfg.setData(data);
		VarSubstCommaSeparatedValuesExpectationImpl csvRet = new VarSubstCommaSeparatedValuesExpectationImpl(csvE,
				cfg);
		VarSubstExpectationCollection vseColl = new VarSubstExpectationCollection(data);
		vseColl.init(expectedText.toString());
		String expandedExpectedText = IteratorExtensions.join(vseColl.iterator(), ",");

		return new Pair<>(csvRet, expandedExpectedText);
	}

	private void assertNoMatch(List<String> proposals, List<String> forbidden,
			ICommaSeparatedValuesExpectation expectation) {
		List<String> matched = computeMatches(proposals, forbidden);
		if (!matched.isEmpty()) {

			// at least one match.
			// val unmatched = forbidden.filter[ ! matched.contains(it)]
			// expectation.assertEquals(unmatched)
			expectation.assertEquals(new Predicate<String>() {

				@Override
				public boolean apply(String input) {
					return !matched.contains(input);
				}
			});
		}
	}

	private void assertExactlyOrdered(List<String> proposals, List<String> required,
			CommaSeparatedValuesExpectationImpl expectation) {
		assertContainingMatchAllOrdered(proposals, required, expectation);

		// assert same length:
		if (proposals.size() != required.size())
			throw new ComparisonFailure(
					"Ambiguity: All required proposal (right side) could match the ones the system provides." +
							" But, at least two required labels matched the same proposal." +
							" Your requirement on the right side is to sloppy. Please provide more specific labels." +
							" See the full proposal display strings in the comparison",
					required.stream().collect(
							Collectors.joining(",")),
					proposals.stream().collect(Collectors.joining(",")));
	}

	/** Unordered comparison: same number of required and proposed */
	private void assertExactly(List<String> proposals, List<String> required,
			ICommaSeparatedValuesExpectation expectation) {

		// ensure, that there are not more proposals then required/expected
		// assert same length:
		if (proposals.size() != required.size())
			throw new ComparisonFailure(
					"System provides " + proposals.size() + " proposals, expected have been " + required.size() + ".",
					required.stream().collect(Collectors.joining(",")), proposals.stream().collect(
							Collectors.joining(",")));

		// ensure, that all required match a proposal.
		assertContainingMatchAll(proposals, required, expectation);
	}

	private void assertContainingMatchAll(List<String> proposals, List<String> required,
			ICommaSeparatedValuesExpectation exp) {
		List<String> matched = computeMatches(proposals, required);
		exp.assertEquals(matched);
	}

	/**
	 * Collect all strings from required, that are a partial string of at least one of the strings in proposals. Retain
	 * order of required in the resulting list.
	 *
	 * @param proposals
	 *            list of long strings
	 * @param required
	 *            list of partial strings
	 * @return subsequence of required, where each element is a true substring of at least on element of proposals
	 */
	private List<String> computeMatches(List<String> proposals, List<String> required) {
		List<String> matched = Lists.newArrayList();
		required.forEach(r -> {
			if (proposals.stream().anyMatch(p -> p.contains(r))) {
				matched.add(r);
			}
		});
		return matched;
	}

	private void assertContainingMatchAllOrdered(List<String> proposals, List<String> required,
			CommaSeparatedValuesExpectationImpl exp) {

		List<String> matched = Lists.newArrayList();
		HashMap<String, Integer> match2index = new HashMap<>();

		// TODO review if needed
		// boolean orderBroken = false;
		int highestIdx = 0;
		for (int i = 0; i < required.size(); i++) {
			String r = required.get(i);
			Optional<String> prop = proposals.stream().filter(p -> p.contains(r)).findFirst();
			if (prop.isPresent()) {
				int idx = proposals.indexOf(prop.get());
				if (highestIdx >= idx) {
					// orderBroken = true;
				}
				matched.add(r);
				match2index.put(r, idx);
				highestIdx = Math.max(highestIdx, idx);
			}
		}

		// create the matchedOrder:
		List<String> matchedOrder = match2index.entrySet().stream()
				.sorted(new Comparator<Map.Entry<String, Integer>>() {

					@Override
					public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
						return e1.getValue().compareTo(e2.getValue());
					}
				}).map(sme -> sme.getKey()).collect(Collectors.toList());

		// change config to ordered:
		CommaSeparatedValuesExpectationCfg cfg = new CommaSeparatedValuesExpectationCfg(exp.getAnnotation());
		cfg.setOrdered(true);
		CommaSeparatedValuesExpectationImpl expNCSV = new CommaSeparatedValuesExpectationImpl(exp, cfg);
		expNCSV.assertEquals(matchedOrder);
	}

	/**
	 * Searches for the proposal matching to selected.
	 *
	 * Throws exception if there are more then one or no proposals matching 'selected' found.
	 */
	private ICompletionProposal exactlyMatchingProposal(RegionWithCursor offset,
			N4ContentAssistProcessorTestBuilder fixture,
			String selected) {
		ICompletionProposal[] computeCompletionProposals = allProposalsAt(offset, fixture);
		List<ICompletionProposal> candidates = Arrays.stream(computeCompletionProposals)
				.filter(proposal -> proposal.getDisplayString().contains(selected)).collect(Collectors.toList());

		if (candidates.size() > 1) {
			throw new AssertionFailedError(
					"The selection of contentassist is not precise enough more then one assist matched the selection '"
							+
							selected + "': " + asString2(candidates) + " Please be more precise.");
		} else if (candidates.size() < 1) {
			throw new AssertionFailedError(
					"No content assist matching the selection '" + selected + "' found. Available are " +
							asString2(Arrays.asList(computeCompletionProposals)));
		}

		ICompletionProposal proposal = candidates.get(0);
		return proposal;
	}

	/**
	 * Helper for display-strings
	 *
	 * @param resource
	 *            resource under test
	 * @param offset
	 *            selection & cursor position
	 * @param kind
	 *            selection-cycling mode.
	 * @return List of proposal display-strings.
	 * @throws Exception
	 *             in case of errors.
	 */
	private List<String> getProposalDisplayStrings(XtextResource resource, RegionWithCursor offset, String kind)
			throws Exception {

		if (kind != null && kind != "") {
			throw new UnsupportedOperationException("Proposals of a special kind=" + kind + " are not yet queried.");
		}
		ICompletionProposal[] computeCompletionProposals = allProposalsAt(offset,
				n4ContentAssistProcessorTestBuilderHelper.createTestBuilderForResource(resource));
		List<String> result = Lists.newArrayList();
		for (ICompletionProposal iCompletionProposal : computeCompletionProposals) {
			result.add(iCompletionProposal.getDisplayString());
		}
		return result;
	}

	private ICompletionProposal[] allProposalsAt(RegionWithCursor offset, N4ContentAssistProcessorTestBuilder fixture) {
		AtomicReference<ICompletionProposal[]> w = new AtomicReference<>();

		Display.getDefault().syncExec(() -> {

			try {
				w.set(fixture.computeCompletionProposals(offset
						.getGlobalCursorOffset()));
			} catch (Exception e) {
				logger.warn("Cannot compute Completion Proposals", e);
			}
		});

		return w.get();
	}

	private String applyProposal(ICompletionProposal proposal, IXtextDocument document) {
		return document.modify(
				state -> {
					state.setValidationDisabled(false);
					if (!(proposal instanceof TemplateProposal)) {
						proposal.apply(document);
					}
					return document.get();
				});
	}

}
