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

import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import eu.numberfour.n4js.N4JSInjectorProviderWithIssueSuppression

/*
 * Tests for {@link TypeSystemHelper#join(RuleEnvironment, TypeRef...)} method with union types.
 */
@RunWith(XtextRunner)
@InjectWith(N4JSInjectorProviderWithIssueSuppression)
class JoinComputer_UnionTypesTest extends AbstractTypeSystemHelperTests {

	@Before
	def void prepareTypeDefs() {
		setDefaultTypeDefinitions()
	}

	/*
	 * Test some assumptions.
	 */
	@Test
	def void testJoinAsumptions() {
		assertJoin("G<? extends A>", "G<A>", "G<B>");
		assertJoin("A", "A", "B");

		// G is instanceof of N4OBject ;-)
		assertJoin("N4Object", "G<A>", "A");
	}

	@Test
	def void testJoinSimpleWithUnions() {
		assertJoin("union{A,B}", "A", "union{A,B}");
		assertJoin("union{A,B}", "union{A,B}", "A");
		assertJoin("union{A,B}", "A", "union{B,A}");
	}

	@Test
	def void testJoinUnionWithUnions() {
		assertJoin("union{A,B,C}", "union{A,B}", "union{B,C}");
		assertJoin("union{B,C}", "C", "union{B,C}");
	}



	@Test
	def void testJoinWithUnionsAndGenerics() {

		// this is not G<? extends A>: G<union{A,B}> != union{G<A>, G<B>} !!!!!
		// upper A v union{A,B} = union{A,B}
		// lower A ^ union{A,B} = B
		// union{A,B} ... B
		// TODO: why not G<A>?
		assertJoin("G<? extends union{A,B}>", "G<A>", "G<union{A,B}>");

		assertJoin("union{A,B,G<A>}", "G<A>", "union{A,B}");
	}

}
