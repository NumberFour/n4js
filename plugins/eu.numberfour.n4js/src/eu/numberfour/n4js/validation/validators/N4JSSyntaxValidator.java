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
package eu.numberfour.n4js.validation.validators;

import static eu.numberfour.n4js.validation.IssueCodes.AST_CATCH_VAR_TYPED;
import static eu.numberfour.n4js.validation.IssueCodes.SYN_KW_EXTENDS_IMPLEMENTS_MIXED_UP;
import static eu.numberfour.n4js.validation.IssueCodes.SYN_KW_INSTEAD_OF_COMMA_WARN;
import static eu.numberfour.n4js.validation.IssueCodes.getMessageForAST_CATCH_VAR_TYPED;
import static eu.numberfour.n4js.validation.IssueCodes.getMessageForSYN_KW_EXTENDS_IMPLEMENTS_MIXED_UP;
import static eu.numberfour.n4js.validation.IssueCodes.getMessageForSYN_KW_INSTEAD_OF_COMMA_WARN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.Alternatives;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.nodemodel.BidiTreeIterator;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

import com.google.common.base.Joiner;

import eu.numberfour.n4js.n4JS.CatchVariable;
import eu.numberfour.n4js.n4JS.ModifiableElement;
import eu.numberfour.n4js.n4JS.ModifierUtils;
import eu.numberfour.n4js.n4JS.N4ClassDefinition;
import eu.numberfour.n4js.n4JS.N4InterfaceDeclaration;
import eu.numberfour.n4js.n4JS.N4JSPackage;
import eu.numberfour.n4js.n4JS.N4Modifier;
import eu.numberfour.n4js.ts.typeRefs.ParameterizedTypeRef;
import eu.numberfour.n4js.ts.types.IdentifiableElement;
import eu.numberfour.n4js.ts.types.TClass;
import eu.numberfour.n4js.ts.types.TInterface;
import eu.numberfour.n4js.ts.types.Type;
import eu.numberfour.n4js.validation.AbstractN4JSDeclarativeValidator;
import eu.numberfour.n4js.validation.IssueCodes;

/**
 * Validates syntax of N4JS not already checked by the parser. The parser is designed to accept some invalid constructs
 * in order to be able to create more user-friendly error messages here.
 */
public class N4JSSyntaxValidator extends AbstractN4JSDeclarativeValidator {

	/**
	 * NEEDED
	 *
	 * when removed check methods will be called twice once by N4JSValidator, and once by
	 * AbstractDeclarativeN4JSValidator
	 */
	@Override
	public void register(EValidatorRegistrar registrar) {
		// nop
	}

	/**
	 * Checks modifiers.
	 */
	@Check
	private boolean checkModifiers(ModifiableElement elem) {
		return holdsNoInvalidOrDuplicateModifiers(elem)
				&& holdsNotMoreThanOneAccessModifier(elem)
				&& holdsCorrectOrder(elem);
	}

	/**
	 * Check for invalid modifiers (e.g. ABSTRACT on a field) and duplicates.
	 */
	private boolean holdsNoInvalidOrDuplicateModifiers(ModifiableElement elem) {
		boolean hasIssue = false;
		final Set<N4Modifier> checked = new HashSet<>();
		for (int idx = 0; idx < elem.getDeclaredModifiers().size(); idx++) {
			final N4Modifier mod = elem.getDeclaredModifiers().get(idx);
			final boolean duplicate = !checked.add(mod);
			if (!ModifierUtils.isValid(elem.eClass(), mod)) {
				final ILeafNode node = ModifierUtils.getNodeForModifier(elem, idx);
				addIssue(IssueCodes.getMessageForSYN_MODIFIER_INVALID(mod.getName(), keywordProvider.keyword(elem)),
						elem, node.getOffset(), node.getLength(),
						IssueCodes.SYN_MODIFIER_INVALID);
				hasIssue = true;
			} else if (duplicate) {
				final ILeafNode node = ModifierUtils.getNodeForModifier(elem, idx);
				addIssue(IssueCodes.getMessageForSYN_MODIFIER_DUPLICATE(mod.getName()),
						elem, node.getOffset(), node.getLength(),
						IssueCodes.SYN_MODIFIER_DUPLICATE);
				hasIssue = true;
			}
		}
		return !hasIssue;
	}

	/**
	 * Check that not more than one access modifier is given. Access modifiers are those for which
	 * {@link ModifierUtils#isAccessModifier(N4Modifier)} returns <code>true</code>.
	 */
	private boolean holdsNotMoreThanOneAccessModifier(ModifiableElement elem) {
		boolean hasIssue = false;
		boolean hasAccessModifier = false;
		for (int idx = 0; idx < elem.getDeclaredModifiers().size(); idx++) {
			final N4Modifier mod = elem.getDeclaredModifiers().get(idx);
			final boolean isAccessModifier = ModifierUtils.isAccessModifier(mod);
			if (hasAccessModifier && isAccessModifier) {
				final ILeafNode node = ModifierUtils.getNodeForModifier(elem, idx);
				addIssue(IssueCodes.getMessageForSYN_MODIFIER_ACCESS_SEVERAL(),
						elem, node.getOffset(), node.getLength(),
						IssueCodes.SYN_MODIFIER_ACCESS_SEVERAL);
				hasIssue = true;
			}
			hasAccessModifier |= isAccessModifier;
		}
		return !hasIssue;
	}

	/**
	 * Check for correct ordering of access modifiers.
	 */
	private boolean holdsCorrectOrder(ModifiableElement elem) {
		boolean isOrderMessedUp = false;
		int lastValue = -1;
		for (N4Modifier mod : elem.getDeclaredModifiers()) {
			final int currValue = mod.getValue();
			if (currValue < lastValue) {
				isOrderMessedUp = true;
				break;
			}
			lastValue = currValue;
		}
		if (isOrderMessedUp) {
			final List<N4Modifier> modifiers = ModifierUtils.getSortedModifiers(elem.getDeclaredModifiers());
			final String modifiersStr = Joiner.on(' ').join(modifiers.iterator());
			final ILeafNode nodeFirst = ModifierUtils.getNodeForModifier(elem, 0);
			final ILeafNode nodeLast = ModifierUtils.getNodeForModifier(elem,
					elem.getDeclaredModifiers().size() - 1);
			addIssue(IssueCodes.getMessageForSYN_MODIFIER_BAD_ORDER(modifiersStr),
					elem, nodeFirst.getOffset(),
					nodeLast.getOffset() - nodeFirst.getOffset() + nodeLast.getLength(),
					IssueCodes.SYN_MODIFIER_BAD_ORDER);
			return false;
		}
		return true;
	}

	/**
	 * Checks that no "with" is used and that list of implemented interfaces is separated with commas and not with
	 * keywords. These checks (with some warnings created instead of errors) should help the transition from roles to
	 * interfaces. However, they may be useful later on as well, e.g., if an interface is manually refactored into a
	 * class or vice versa.
	 * <p>
	 * Note that "with" is used in Dart for roles, so maybe it is useful to have a user-friendly message instead of a
	 * parser error.
	 */
	@Check
	public void checkClassDefinition(N4ClassDefinition n4ClassDefinition) {
		holdsNoKeywordInsteadOfComma(n4ClassDefinition);

		ICompositeNode node = NodeModelUtils.findActualNodeFor(n4ClassDefinition);
		ILeafNode keywordNode = findSecondLeafWithKeyword(n4ClassDefinition, "{", node, "extends", false);
		if (keywordNode != null) {
			TClass tclass = n4ClassDefinition.getDefinedTypeAsClass();
			if (tclass == null) {
				return; // avoid consequential errors
			}
			if (StreamSupport.stream(tclass.getImplementedInterfaceRefs().spliterator(), false).allMatch(
					superTypeRef -> superTypeRef.getDeclaredType() instanceof TInterface)) {
				List<? extends IdentifiableElement> interfaces = StreamSupport.stream(
						tclass.getImplementedInterfaceRefs().spliterator(), false)
						.map(ref -> (TInterface) (ref.getDeclaredType())).collect(Collectors.toList());
				String message = getMessageForSYN_KW_EXTENDS_IMPLEMENTS_MIXED_UP(
						validatorMessageHelper.description(tclass), "extend",
						"interface" + (interfaces.size() > 1 ? "s " : " ") + validatorMessageHelper.names(interfaces),
						"implements");
				addIssue(message, n4ClassDefinition, keywordNode.getTotalOffset(),
						keywordNode.getLength(), SYN_KW_EXTENDS_IMPLEMENTS_MIXED_UP);
			}

		}
	}

	/**
	 * Checks that no "with" or "role" is used and that list of implemented interfaces is separated with commas and not
	 * with keywords. These checks (with some warnings created instead of errors) should help the transition from roles
	 * to interfaces. However, they may be useful later on as well, e.g., if an interface is manually refactored into a
	 * class or vice versa.
	 * <p>
	 * Note that "with" is used in Dart for roles, so maybe it is useful to have a user-friendly message instead of a
	 * parser error.
	 * <p>
	 * "role" will be removed in grammar.
	 */
	@Check
	public void checkInterfaceDeclaration(N4InterfaceDeclaration n4InterfaceDecl) {

		holdsNoKeywordInsteadOfComma(n4InterfaceDecl);

		ICompositeNode node = NodeModelUtils.findActualNodeFor(n4InterfaceDecl);
		ILeafNode keywordNode;

		keywordNode = findLeafWithKeyword(n4InterfaceDecl, "{", node, "implements", false);
		if (keywordNode != null) {
			TInterface tinterface = n4InterfaceDecl.getDefinedTypeAsInterface();
			if (tinterface == null) {
				return; // avoid consequential errors
			}
			if (tinterface.getSuperInterfaceRefs().isEmpty()) {
				return; // ok
			}
			if (tinterface.getSuperInterfaceRefs().stream().allMatch(
					superTypeRef -> superTypeRef.getDeclaredType() instanceof TInterface)) {
				List<? extends IdentifiableElement> interfaces = tinterface.getSuperInterfaceRefs()
						.stream()
						.flatMap((ParameterizedTypeRef ref) -> {
							Type declaredType = ref.getDeclaredType();
							if (declaredType instanceof TInterface) {
								return Stream.of((TInterface) declaredType);
							}
							return Stream.empty();
						})
						.collect(Collectors.toList());
				String message = getMessageForSYN_KW_EXTENDS_IMPLEMENTS_MIXED_UP(
						validatorMessageHelper.description(tinterface), "implement",
						"interface" + (interfaces.size() > 1 ? "s" : "") + validatorMessageHelper.names(interfaces),
						"extends");
				addIssue(message, n4InterfaceDecl, keywordNode.getTotalOffset(),
						keywordNode.getLength(), SYN_KW_EXTENDS_IMPLEMENTS_MIXED_UP);
			}

		}

	}

	private boolean holdsNoKeywordInsteadOfComma(EObject semanticElement) {
		ICompositeNode node = NodeModelUtils.findActualNodeFor(semanticElement);
		List<ILeafNode> commaAlternatives = filterLeafsWithKeywordInsteadOfComma(semanticElement, "{", node, "extends",
				"implements",
				"with");
		boolean result = true;
		for (ILeafNode n : commaAlternatives) {
			addIssue(getMessageForSYN_KW_INSTEAD_OF_COMMA_WARN(n.getText()), semanticElement, n.getTotalOffset(),
					n.getLength(), SYN_KW_INSTEAD_OF_COMMA_WARN);
			result = false;
		}
		return result;
	}

	private ILeafNode findLeafWithKeyword(EObject semanticElement, String stopAtKeyword, ICompositeNode node,
			String keyWord,
			boolean commaAlternative) {
		return doFindLeafWithKeyword(semanticElement, stopAtKeyword, node, keyWord, commaAlternative, 1);
	}

	private ILeafNode findSecondLeafWithKeyword(EObject semanticElement, String stopAtKeyword, ICompositeNode node,
			String keyWord,
			boolean commaAlternative) {
		return doFindLeafWithKeyword(semanticElement, stopAtKeyword, node, keyWord, commaAlternative, 2);

	}

	/**
	 * Returns the first keyword with the given value, or null if no such keyword is found.
	 */
	private ILeafNode doFindLeafWithKeyword(EObject semanticElement, String stopAtKeyword, ICompositeNode node,
			String keyWord,
			boolean commaAlternative, int hitNumber) {
		EObject grammarElement;
		int foundHits = 0;

		for (BidiTreeIterator<INode> iter = node.getAsTreeIterable().iterator(); iter.hasNext();) {
			INode child = iter.next();
			EObject childSemElement = child.getSemanticElement();
			if (child != node && childSemElement != null && childSemElement != semanticElement) {
				iter.prune();
			} else if (child instanceof ILeafNode) {
				ILeafNode leaf = (ILeafNode) child;
				grammarElement = leaf.getGrammarElement();
				if (grammarElement instanceof Keyword) {
					String value = ((Keyword) grammarElement).getValue();
					if (stopAtKeyword.equals(value)) {
						return null;
					}
					if (keyWord.equals(value)) {
						if (grammarElement.eContainer() instanceof Alternatives) {
							AbstractElement first = ((Alternatives) (grammarElement.eContainer())).getElements().get(0);
							boolean inCommaAlternative = (first instanceof Keyword && ",".equals(((Keyword) first)
									.getValue()));
							if (inCommaAlternative == commaAlternative) {
								foundHits++;
								if (foundHits >= hitNumber) {
									return leaf;
								}
							}
						} else {
							if (!commaAlternative) {
								foundHits++;
								if (foundHits >= hitNumber) {
									return leaf;
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns nodes which represent keywords and specified in keywords.
	 *
	 * @param keywords
	 *            keywords in natural order used in Arrays#s
	 */
	protected List<ILeafNode> filterLeafsWithKeywordInsteadOfComma(EObject semanticElement, String stopAtKeyword,
			ICompositeNode node,
			final String... keywords) {
		List<ILeafNode> filteredLeaves = null;
		for (BidiTreeIterator<INode> iter = node.getAsTreeIterable().iterator(); iter.hasNext();) {
			INode child = iter.next();
			EObject childSemElement = child.getSemanticElement();
			if (child != node && childSemElement != null && childSemElement != semanticElement) {
				iter.prune();
			} else if (child instanceof ILeafNode) {
				ILeafNode leaf = (ILeafNode) child;
				EObject grammarElement = leaf.getGrammarElement();
				if (grammarElement instanceof Keyword) {
					String value = ((Keyword) grammarElement).getValue();
					if (stopAtKeyword.equals(value)) {
						break;
					}
					if (Arrays.binarySearch(keywords, value) >= 0) {
						if (grammarElement.eContainer() instanceof Alternatives) {
							AbstractElement first = ((Alternatives) (grammarElement.eContainer())).getElements().get(0);
							boolean inCommaAlternative = (first instanceof Keyword && ",".equals(((Keyword) first)
									.getValue()));
							if (inCommaAlternative) {
								if (filteredLeaves == null) {
									filteredLeaves = new ArrayList<>(5);
								}
								filteredLeaves.add(leaf);
							}
						}
					}
				}
			}
		}
		return filteredLeaves == null ? Collections.emptyList() : filteredLeaves;
	}

	/**
	 * Ensures that a catch variable has not type annotation. This is supported by the parser to enable better error
	 * messages.
	 *
	 * @see "Spec, 9.1.8"
	 * @see <a href="https://github.com/NumberFour/n4js/issues/179">GH-179</a>
	 */
	@Check
	public void checkCatchVariable(CatchVariable catchVariable) {
		if (catchVariable.getDeclaredTypeRef() != null) {
			addIssue(getMessageForAST_CATCH_VAR_TYPED(), catchVariable,
					N4JSPackage.eINSTANCE.getTypedElement_DeclaredTypeRef(),
					AST_CATCH_VAR_TYPED);
		}
	}

}
