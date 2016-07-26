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
package eu.numberfour.n4js.issues;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.validation.CheckType;
import org.eclipse.xtext.validation.Issue;

/**
 * Matches expectations against an instance of {@link Issue}. The expectations are specified using a simple builder
 * syntax.
 */
public class IssueMatcher {
	private final List<IssuePropertyMatcher> propertyMatchers = new LinkedList<>();

	/**
	 * Adds an expectation an issue's severity.
	 *
	 * @see Issue#getSeverity()
	 * @see Severity
	 *
	 * @param expectedSeverity
	 *            the expected severity
	 * @return this issue matcher
	 */
	public IssueMatcher severity(Severity expectedSeverity) {
		return addEqualsMatcher(expectedSeverity, (Issue issue) -> issue.getSeverity());
	}

	/**
	 * Creates a builder for an issue's message.
	 *
	 * @see Issue#getMessage()
	 *
	 * @return an instance of {@link StringPropertyMatcher} that can be used to specify the actual expectation
	 */
	public StringPropertyMatcherBuilder message() {
		return new StringPropertyMatcherBuilder(this, (Issue issue) -> issue.getMessage());
	}

	/**
	 * Adds an expectation for an issue's message.
	 *
	 * @see Issue#getMessage()
	 *
	 * @param expectedMessage
	 *            the expected message
	 * @return this issue matcher
	 */
	public IssueMatcher message(String expectedMessage) {
		return message().equals(expectedMessage);
	}

	/**
	 * Creates a builder for an issue's code.
	 *
	 * @see Issue#getCode()
	 *
	 * @return an instance of {@link StringPropertyMatcher} that can be used to specify the actual expectation
	 */
	public StringPropertyMatcherBuilder code() {
		return new StringPropertyMatcherBuilder(this, (Issue issue) -> issue.getCode());
	}

	/**
	 * Adds an expectation for an issue's code.
	 *
	 * @see Issue#getCode()
	 *
	 * @param expectedCode
	 *            the expected code
	 * @return this issue matcher
	 */
	public IssueMatcher code(String expectedCode) {
		return code().equals(expectedCode);
	}

	/**
	 * Adds an expectation for an issue's type.
	 *
	 * @see Issue#getCode()
	 * @see CheckType
	 *
	 * @param expectedType
	 *            the expected type
	 * @return this issue matcher
	 */
	public IssueMatcher type(CheckType expectedType) {
		return addEqualsMatcher(expectedType, (Issue issue) -> issue.getType());
	}

	/**
	 * Creates a builder for an issue's URI.
	 *
	 * @see Issue#getUriToProblem()
	 *
	 * @return an instance of {@link URIPropertyMatcher} that can be used to specify the actual expectation
	 */
	public URIPropertyMatcherBuilder uri() {
		return new URIPropertyMatcherBuilder(this, (Issue issue) -> issue.getUriToProblem());
	}

	/**
	 * Adds an expectation for an issue's line number.
	 *
	 * @see Issue#getLineNumber()
	 *
	 * @param expectedLineNumber
	 *            the expected line number
	 * @return this issue matcher
	 */
	public IssueMatcher lineNumber(int expectedLineNumber) {
		return addEqualsMatcher(expectedLineNumber, (Issue issue) -> issue.getLineNumber());
	}

	/**
	 * Adds an expectation for an issue's column.
	 *
	 * @see Issue#getColumn()
	 *
	 * @param expectedColumn
	 *            the expected column
	 * @return this issue matcher
	 */
	public IssueMatcher column(int expectedColumn) {
		return addEqualsMatcher(expectedColumn, (Issue issue) -> issue.getColumn());
	}

	/**
	 * Adds an expectation for an issue's line number and column.
	 *
	 * @see Issue#getLineNumber()
	 * @see Issue#getColumn()
	 *
	 * @param expectedLineNumber
	 *            the expected line number
	 * @param expectedColumn
	 *            the expected column
	 * @return this issue matcher
	 */
	public IssueMatcher at(int expectedLineNumber, int expectedColumn) {
		return lineNumber(expectedLineNumber).column(expectedColumn);
	}

	/**
	 * Adds an expectation for an issue's URI, line number and column.
	 *
	 * @see Issue#getUriToProblem()
	 * @see Issue#getLineNumber()
	 * @see Issue#getColumn()
	 *
	 * @param expectedURISuffix
	 *            the expected suffix of the URI
	 * @param expectedLineNumber
	 *            the expected line number
	 * @param expectedColumn
	 *            the expected column
	 * @return this issue matcher
	 */
	public IssueMatcher at(URI expectedURISuffix, int expectedLineNumber, int expectedColumn) {
		return lineNumber(expectedLineNumber).column(expectedColumn).uri().endsWith(expectedURISuffix);
	}

	/**
	 * Adds an expectation for an issue's URI, line number and column.
	 *
	 * @see Issue#getUriToProblem()
	 * @see Issue#getLineNumber()
	 * @see Issue#getColumn()
	 *
	 * @param expectedURISuffix
	 *            the expected suffix of the URI
	 * @param expectedLineNumber
	 *            the expected line number
	 * @param expectedColumn
	 *            the expected column
	 * @return this issue matcher
	 */
	public IssueMatcher at(String expectedURISuffix, int expectedLineNumber, int expectedColumn) {
		return at(URI.createFileURI(expectedURISuffix), expectedLineNumber, expectedColumn);
	}

	/**
	 * Adds an expectation for an issue's offset.
	 *
	 * @see Issue#getOffset()
	 *
	 * @param expectedOffset
	 *            the expected offset
	 * @return this issue matcher
	 */
	public IssueMatcher offset(int expectedOffset) {
		return addEqualsMatcher(expectedOffset, (Issue issue) -> issue.getOffset());
	}

	/**
	 * Adds an expectation for an issue's length.
	 *
	 * @see Issue#getLength()
	 *
	 * @param expectedLength
	 *            the expected length
	 * @return this issue matcher
	 */
	public IssueMatcher length(int expectedLength) {
		return addEqualsMatcher(expectedLength, (Issue issue) -> issue.getLength());
	}

	/**
	 * Adds an expectation for whether an issue is a syntax error.
	 *
	 * @see Issue#isSyntaxError()
	 *
	 * @param syntaxError
	 *            the expected value
	 * @return this issue matcher
	 */
	public IssueMatcher syntaxError(boolean syntaxError) {
		return addEqualsMatcher(syntaxError, (Issue issue) -> issue.isSyntaxError());
	}

	private <T> IssueMatcher addEqualsMatcher(T expectedValue, Function<Issue, T> getActualValue) {
		return addPropertyMatcher(new IssuePropertyEqualsMatcher<>(expectedValue, getActualValue));
	}

	IssueMatcher addPropertyMatcher(IssuePropertyMatcher propertyMatcher) {
		propertyMatchers.add(Objects.requireNonNull(propertyMatcher));
		return this;
	}

	/**
	 * Matches this issue matcher against the given issue.
	 *
	 * @param issue
	 *            the issue to match against
	 * @return <code>true</code> if the expectations in this matcher match the given issue and <code>false</code>
	 *         otherwise
	 */
	public boolean matches(Issue issue) {
		Objects.requireNonNull(issue);
		for (IssuePropertyMatcher propertyMatcher : propertyMatchers) {
			if (!propertyMatcher.matches(issue))
				return false;
		}
		return true;
	}
}
