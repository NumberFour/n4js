/**
 * Copyright (c) 2017 NumberFour AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   NumberFour AG - Initial API and implementation
 */
package eu.numberfour.n4js.ui.organize.imports;

import java.util.function.Predicate;

/**
 * Interface for predicates used to filter unresolved cross proxy references.
 */
public interface IReferenceFilter extends Predicate<ReferenceProxyInfo> {
	// nop
}
