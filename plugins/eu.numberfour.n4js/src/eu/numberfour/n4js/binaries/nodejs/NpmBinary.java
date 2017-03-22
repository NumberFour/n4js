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
package eu.numberfour.n4js.binaries.nodejs;

import static java.util.Collections.singletonList;

import java.io.File;
import java.net.URI;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;

import com.google.inject.Inject;
import com.google.inject.Provider;

import eu.numberfour.n4js.binaries.BinariesPreferenceStore;
import eu.numberfour.n4js.binaries.BinariesValidator;
import eu.numberfour.n4js.binaries.Binary;
import eu.numberfour.n4js.utils.Version;

/**
 * Representation of a {@code npm} binary.
 */
public class NpmBinary implements Binary {

	@Inject
	private BinariesValidator validator;

	@Inject
	private Provider<NodeJsBinary> nodeJsBinaryProvider;

	@Inject
	private Provider<NpmrcBinary> npmrcBinaryProvider;

	@Inject
	private BinariesPreferenceStore preferenceStore;

	@Override
	public String getId() {
		return NpmBinary.class.getName();
	}

	@Override
	public String getLabel() {
		return NodeBinariesConstants.NPM_LABEL;
	}

	@Override
	public String getDescription() {
		return "Configuration of the folder location of the npm library "
				+ "can be provided here. If not given, then the location will be resolved by used Node.js\u00AE. The required minimum version npm is '"
				+ NodeBinariesConstants.NPM_MIN_VERSION + "'.";
	}

	@Override
	public Version getMinimumVersion() {
		return NodeBinariesConstants.NPM_MIN_VERSION;
	}

	@Override
	public String getBinaryAbsolutePath() {
		final NodeJsBinary nodeJsBinary = nodeJsBinaryProvider.get();
		return nodeJsBinary.getUserNodePathOrDefault() + File.separator + NodeBinariesConstants.NPM_BINARY_NAME;
	}

	@Override
	public String getVersionArgument() {
		return NodeBinariesConstants.VERSION_ARGUMENT;
	}

	@Override
	public Binary getParent() {
		return nodeJsBinaryProvider.get();
	}

	@Override
	public Iterable<Binary> getChildren() {
		return singletonList(npmrcBinaryProvider.get());
	}

	@Override
	public Map<String, String> updateEnvironment(final Map<String, String> environment) {
		final Binary parent = getParent();
		if (null != parent) {
			parent.updateEnvironment(environment);
		}
		return environment;
	}

	@Override
	public URI getUserConfiguredLocation() {
		return preferenceStore.getPath(this);
	}

	@Override
	public IStatus validate() {
		final Binary parent = getParent();
		if (null != parent) {
			final IStatus parentStatus = parent.validate();
			if (!parentStatus.isOK()) {
				return parentStatus;
			}
		}
		return validator.validate(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof NpmBinary)) {
			return false;
		}
		final NpmBinary other = (NpmBinary) obj;
		if (getId() == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (!getId().equals(other.getId())) {
			return false;
		}
		return true;
	}

}
