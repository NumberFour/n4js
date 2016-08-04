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
package eu.numberfour.n4js.typesystem

import eu.numberfour.n4js.N4JSInjectorProvider
import eu.numberfour.n4js.validation.IssueCodes
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/*
 * Tests for {@link TypeSystemHelper#meet(RuleEnvironment, TypeRef...)} method.
 */
@RunWith(XtextRunner)
@InjectWith(N4JSInjectorProvider)
class MeetComputer_UnionTypesTest extends AbstractTypeSystemHelperTests {

	@Before
	def void prepareTypeDefs() {
		setDefaultTypeDefinitions()
	}

	@Test
	def void testMeetWithUnion() {

		assertMeet(#[IssueCodes.UNI_REDUNDANT_SUBTYPE], "B", "A", "union{A,B}");
		assertMeet(#[IssueCodes.UNI_REDUNDANT_SUBTYPE], "B", "B", "union{A,B}");
		assertMeet(#[IssueCodes.UNI_REDUNDANT_SUBTYPE], "C", "A", "union{B,C}");
		assertMeet(#[IssueCodes.UNI_REDUNDANT_SUBTYPE], "C", "C", "union{B,C}");
	}

}
