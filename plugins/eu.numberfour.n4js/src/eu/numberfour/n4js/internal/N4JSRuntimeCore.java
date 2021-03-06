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
package eu.numberfour.n4js.internal;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Boolean.TRUE;
import static org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider.PERSISTED_DESCRIPTIONS;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsData;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import eu.numberfour.n4js.N4JSGlobals;
import eu.numberfour.n4js.projectModel.IN4JSProject;
import eu.numberfour.n4js.projectModel.IN4JSRuntimeCore;
import eu.numberfour.n4js.projectModel.IN4JSSourceContainer;
import eu.numberfour.n4js.resource.OrderedResourceDescriptionsData;
import eu.numberfour.n4js.utils.ResourceType;

/**
 */
@Singleton
public class N4JSRuntimeCore extends AbstractN4JSCore implements IN4JSRuntimeCore {

	private final FileBasedWorkspace workspace;

	private final N4JSModel model;

	@Inject
	private Provider<XtextResourceSet> resourceSetProvider;

	@Inject
	private ResourceDescriptionsProvider resourceDescriptionsProvider;

	@Inject
	private IResourceServiceProvider.Registry resourceServiceProviderRegistry;

	/**
	 * Public for testing purpose.
	 */
	@Inject
	public N4JSRuntimeCore(FileBasedWorkspace workspace, N4JSModel model) {
		this.workspace = workspace;
		this.model = model;
	}

	@Override
	public IN4JSProject create(URI location) {
		if (location == null) {
			return null;
		}
		return model.getN4JSProject(location);
	}

	@Override
	public Optional<? extends IN4JSProject> findProject(URI nestedLocation) {
		if (nestedLocation == null) {
			return Optional.absent();
		}
		IN4JSProject result = model.findProjectWith(nestedLocation);
		return Optional.fromNullable(result);
	}

	@Override
	public Iterable<IN4JSProject> findAllProjects() {
		List<IN4JSProject> projects = new ArrayList<>();
		this.workspace.getAllProjectsLocations().forEachRemaining(
				location -> projects.add(model.getN4JSProject(location)));
		return projects;
	}

	@Override
	public Optional<? extends IN4JSSourceContainer> findN4JSSourceContainer(URI nestedLocation) {
		if (nestedLocation == null) {
			return Optional.absent();
		} else {
			return model.findN4JSSourceContainer(nestedLocation);
		}
	}

	@Override
	public void registerProject(File file) {
		if (file.isDirectory()) {
			URI uri = URI.createURI(file.toURI().toString()).trimSegments(1);
			workspace.registerProject(uri);
		} else {
			throw new IllegalArgumentException(file.getAbsolutePath() + " is not a valid project location");
		}
	}

	@Override
	public ResourceSet createResourceSet(Optional<IN4JSProject> contextProject) {
		final ResourceSet resourceSet = resourceSetProvider.get();
		resourceSet.getLoadOptions().put(PERSISTED_DESCRIPTIONS, TRUE);
		createAllResourcesWorkspace(resourceSet);
		attachResourceDescriptionsData(resourceSet);
		return resourceSet;
	}

	@Override
	public IResourceDescriptions getXtextIndex(ResourceSet resourceSet) {
		return resourceDescriptionsProvider.getResourceDescriptions(resourceSet);
	}

	private void createAllResourcesWorkspace(ResourceSet resourceSet) {
		final Set<URI> uris = newHashSet();
		for (IN4JSProject project : findAllProjects()) {
			project.getSourceContainers().forEach(sc -> {
				for (URI sourceFile : sc) {
					if (isN4File(sourceFile) && uris.add(sourceFile)) {
						resourceSet.createResource(sourceFile);
					}
				}
			});
		}
	}

	/**
	 * Return true if the URI is a recognized N4 file. Sub-languages should override this method to provide additional
	 * file extensions!
	 */
	protected boolean isN4File(final URI uri) {
		final String ext = uri != null ? uri.fileExtension() : null;
		return N4JSGlobals.ALL_N4_FILE_EXTENSIONS.contains(ext);
	}

	private void attachResourceDescriptionsData(ResourceSet resourceSet) {
		installIndex(resourceSet);
	}

	private void installIndex(ResourceSet resourceSet) {
		// Fill index
		ResourceDescriptionsData index = new OrderedResourceDescriptionsData(
				Collections.<IResourceDescription> emptyList());
		List<Resource> resources = Lists.newArrayList(resourceSet.getResources());
		for (Resource resource : resources) {
			index(resource, resource.getURI(), index);
		}
		Adapter existing = EcoreUtil.getAdapter(resourceSet.eAdapters(), ResourceDescriptionsData.class);
		if (existing != null) {
			resourceSet.eAdapters().remove(existing);
		}
		ResourceDescriptionsData.ResourceSetAdapter.installResourceDescriptionsData(resourceSet, index);
	}

	/**
	 * Installing the ResourceDescription of a resource into the index. Raw JS-files will not be indexed.
	 */
	private void index(Resource resource, URI uri, ResourceDescriptionsData index) {

		if (isJsFile(uri)) {
			IN4JSSourceContainer sourceContainer = findN4JSSourceContainer(uri).orNull();
			if (null == sourceContainer) {
				return; // We do not want to index resources that are not in source containers.
			}
		}

		IResourceServiceProvider serviceProvider = resourceServiceProviderRegistry.getResourceServiceProvider(uri);
		if (serviceProvider != null) {
			IResourceDescription resourceDescription = serviceProvider.getResourceDescriptionManager()
					.getResourceDescription(resource);
			if (resourceDescription != null) {
				index.addDescription(uri, resourceDescription);
			}
		}
	}

	/**
	 * Check for raw JS-files
	 *
	 * @param uri
	 *            to test
	 * @boolean if ends in .js or .js.xt
	 */
	protected boolean isJsFile(URI uri) {
		ResourceType resourceType = ResourceType.getResourceType(uri);
		return resourceType.equals(ResourceType.JS);
	}
}
