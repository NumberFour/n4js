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

import eu.numberfour.n4js.N4JSInjectorProviderWithSuppressedValidator
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Here we test constraints like <code>⟨ G&lt;B> <: G&lt;α> ⟩</code>.
 */
@RunWith(XtextRunner)
@InjectWith(N4JSInjectorProviderWithSuppressedValidator)
class InferenceContext_GenericsTest extends AbstractInferenceContextTest {


	@Test
	def void test_base1() {
		script.assertSolution(
			#[
				constraint(G.of(B),'<:',G.of(alpha))
			],
			alpha -> B.ref
		)
	}


	@Test
	def void test_base2() {
		script.assertSolution(
			#[
				constraint(G.of(alpha),'<:',G.of(B))
			],
			alpha -> B.ref
		)
	}


	@Test
	def void test_rhs_wildcard() {
		script.assertSolution(
			#[
				constraint(G.of(alpha),'<:',G.of(wildcard)) // ⟨ G<α> <: G<?> ⟩
			],
			alpha -> any.ref
		)
	}
	@Test
	def void test_rhs_wildcard_upperBound() {
		script.assertSolution(
			#[
				constraint(G.of(alpha),'<:',G.of(wildcardExtends(B))) // ⟨ G<α> <: G<? extends B> ⟩
			],
			alpha -> B.ref
		)
		script.assertSolution(
			#[
				constraint(G.of(alpha),'<:',G.of(wildcardExtends(B))), // ⟨ G<α> <: G<? extends B> ⟩
				constraint(alpha,'<:',C)
			],
			alpha -> intersection(B,C) // just C would be nicer
		)
	}
	@Test
	def void test_rhs_wildcard_lowerBound() {
		script.assertSolution(
			#[
				constraint(G.of(alpha),'<:',G.of(wildcardSuper(B))) // ⟨ G<α> <: G<? super B> ⟩
			],
			alpha -> B.ref
		)
		script.assertSolution(
			#[
				constraint(G.of(alpha),'<:',G.of(wildcardSuper(B))), // ⟨ G<α> <: G<? super B> ⟩
				constraint(alpha,':>',A)
			],
			alpha -> union(A,B) // just A would be nicer
		)
	}


	@Test
	def void test_lhs_wildcard() {
		script.assertSolution(
			#[
				constraint(G.of(wildcard),'<:',G.of(alpha)) // ⟨ G<?> <: G<α> ⟩
			],
			alpha -> any.ref
		)
	}
	@Ignore("IDE-1653") // TODO IDE-1653
	@Test
	def void test_lhs_wildcard_upperBound() {
		// should fail because we cannot find a non-wildcard α such that G<α> is a super type of G<? extends B>
		script.assertNoSolution(
			#[
				constraint(G.of(wildcardExtends(B)),'<:',G.of(alpha)) // ⟨ G<? extends B> <: G<α> ⟩
			],
			alpha
		)
	}
	@Ignore("IDE-1653") // TODO IDE-1653
	@Test
	def void test_lhs_wildcard_lowerBound() {
		// should fail because we cannot find a non-wildcard α such that G<α> is a super type of G<? super B>
		script.assertNoSolution(
			#[
				constraint(G.of(wildcardSuper(B)),'<:',G.of(alpha)) // ⟨ G<? super B> <: G<α> ⟩
			],
			alpha
		)
	}


	@Test
	def void test_both_wildcard_upperBound() {
		// we can fix the case #test_lhs_wildcard_upperBound() by using a wildcard also on rhs:
		script.assertSolution(
			#[
				constraint(G.of(wildcardExtends(B)),'<:',G.of(wildcardExtends(alpha)))
			],
			alpha -> B.ref
		)
// TODO IDE-1653
//		script.assertSolution(
//			#[
//				constraint(G.of(wildcardExtends(B)),'<:',G.of(wildcardExtends(alpha))),
//				constraint(alpha,':>',A)
//			],
//			alpha -> union(A,B)
//		)
	}
	@Test
	def void test_both_wildcard_lowerBound() {
		// we can fix the case #test_lhs_wildcard_lowerBound() by using a wildcard also on rhs:
		script.assertSolution(
			#[
				constraint(G.of(wildcardSuper(B)),'<:',G.of(wildcardSuper(alpha)))
			],
			alpha -> B.ref
		)
// TODO IDE-1653
//		script.assertSolution(
//			#[
//				constraint(G.of(wildcardSuper(B)),'<:',G.of(wildcardSuper(alpha))),
//				constraint(alpha,'<:',C)
//			],
//			alpha -> intersection(B,C)
//		)
	}
}
