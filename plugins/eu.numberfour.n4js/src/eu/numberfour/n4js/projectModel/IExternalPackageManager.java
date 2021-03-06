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
package eu.numberfour.n4js.projectModel;

import org.eclipse.emf.common.util.URI;

import eu.numberfour.n4js.n4mf.ProjectDescription;

/**
 */
public interface IExternalPackageManager {

	/**
	 * Loads the N4 manifest content and returns with a project description instance which actually the representation
	 * of the manifest content.
	 *
	 * @param manifest
	 *            location of the external package manifest.
	 * @return the project description instance for the external library.
	 */
	ProjectDescription loadManifest(URI manifest);

}
