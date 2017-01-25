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
package eu.numberfour.n4jsx.transpiler.utils;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.resource.IContainer;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.scoping.impl.DefaultGlobalScopeProvider;

import com.google.common.base.Optional;
import com.google.inject.Inject;

import eu.numberfour.n4js.N4JSGlobals;
import eu.numberfour.n4js.n4JS.ImportDeclaration;
import eu.numberfour.n4js.n4JS.ImportSpecifier;
import eu.numberfour.n4js.naming.ModuleNameComputer;
import eu.numberfour.n4js.projectModel.IN4JSCore;
import eu.numberfour.n4js.projectModel.IN4JSProject;
import eu.numberfour.n4js.projectModel.ProjectUtils;
import eu.numberfour.n4js.ts.types.TModule;
import eu.numberfour.n4jsx.N4JSXGlobals;

/**
 * Helper for working with JSX backends, e.g. Ract, Preact, etc. Internally it supports only React, but API wise should
 * work for other backends once their support is added.
 */
public final class JSXBackendHelper {

	/**
	 * This is hacky scope. Unlike normal scopes it is not using any {@link EReference} or {@link EClass} to obtain
	 * proper scope for that element, instead it just accessing internals of the {@link DefaultGlobalScopeProvider} to
	 * find all {@link IContainer}s to get all resources visible from given resource.
	 */
	private final static class JSXBackendsScopeProvider extends DefaultGlobalScopeProvider {
		public final Stream<IResourceDescription> visibleResourceDescriptions(Resource resource) {
			return getVisibleContainers(resource).parallelStream()
					.flatMap(ic -> StreamSupport.stream(ic.getResourceDescriptions().spliterator(), true));
		}
	}

	/**
	 * Local cache of JSX backends.
	 *
	 * We don't bother with proper caching, due to the way this helper is currently used in the transpiler.
	 */
	private final Map<String, IResourceDescription> jsxBackends = new ConcurrentHashMap<>();

	@Inject
	private JSXBackendsScopeProvider jsxGlobalScope;
	@Inject
	private IQualifiedNameConverter qualifiedNameConverter;
	@Inject
	private ModuleNameComputer nameComputer;
	@Inject
	private IN4JSCore n4jsCore;
	@Inject
	ProjectUtils projectUtils;

	private final static String JSX_BACKEND_MODULE_NAME = "react";
	private final static String JSX_BACKEND_FACADE_NAME = "React";
	private final static String JSX_BACKEND_ELEMENT_FACTORY_NAME = "createElement";

	/** @return name of the JSX backend module, i.e. "react" */
	public String getBackendModuleName() {
		return JSX_BACKEND_MODULE_NAME;
	}

	/** @return name of the JSX backend facade, i.e "React" */
	public String getBackendFacadeName() {
		return JSX_BACKEND_FACADE_NAME;
	}

	/** @return name of the JSX element factory name, i.e "createElement" */
	public String getBackendElementFactoryMethodName() {
		return JSX_BACKEND_ELEMENT_FACTORY_NAME;
	}

	/** Checks if given module looks like JSX backend module, e.g. "react" */
	public static boolean isJsxBackendModule(TModule module) {
		if (module == null) {
			return false;
		}
		return module.getQualifiedName().endsWith(JSX_BACKEND_MODULE_NAME);
	}

	/** Checks if given import declaration looks like JSX backend import, e.g. "(...) from "react" */
	public static boolean isJsxBackendImportDeclaration(ImportDeclaration declaration) {
		return isJsxBackendModule(declaration.getModule());
	}

	/** Checks if given import specifier looks like JSX backend import, e.g. "import * as React from "react" */
	public static boolean isJsxBackendImportSpecifier(ImportSpecifier specifier) {
		return isJsxBackendImportDeclaration((ImportDeclaration) specifier.eContainer());
	}

	/**
	 * Similar to {@link eu.numberfour.n4js.naming.QualifiedNameComputer#getCompleteModuleSpecifier(TModule)} but for
	 * artificial modules that were patched in by the transpiler for JSX backend.
	 */
	public String jsxBackendModuleSpecifier(TModule module, Resource resource) {
		URI uri = getOrFindJSXBackend(resource, module.getQualifiedName());

		Optional<? extends IN4JSProject> optionalProject = n4jsCore.findProject(uri);
		if (!optionalProject.isPresent()) {
			throw new RuntimeException(
					"Cannot handle resource without containing project. Resource URI was: " + uri);
		}
		return ProjectUtils.formatDescriptor(optionalProject.get(),
				module.getModuleSpecifier(), "-", ".", "/", false);
	}

	/**
	 * Similar to
	 * {@link eu.numberfour.n4js.naming.QualifiedNameComputer#getCompleteModuleSpecifierAsIdentifier(TModule)} but for
	 * artificial modules that were patched in by the transpiler for JSX backend.
	 */
	public String getJsxBackendCompleteModuleSpecifierAsIdentifier(TModule module) {
		URI uri = getOrFindJSXBackend(module.eResource(), module.getQualifiedName());

		IN4JSProject resolveProject = projectUtils.resolveProject(uri);

		return projectUtils.getValidJavascriptIdentifierName(projectUtils.formatDescriptorAsIdentifier(
				resolveProject, module.getModuleSpecifier(), "_", "_", "_", false));
	}

	/**
	 * Looks up JSX backend for a provided resource. If more than one available, picks one at <b>random</b>. When no
	 * backend is available throws {@link NoSuchElementException}.
	 *
	 * @return qualified name of the selected JSX backend module
	 */
	public String jsxBackendModuleQualifiedName(Resource resource) {
		Objects.requireNonNull(resource);

		// maybe this is first call to the helper, populate cached backends
		if (jsxBackends.isEmpty()) {
			populateBeckendsCache(resource);
		}

		// since we have no info about which backend to use, use any
		return getAnyBackend();
	}

	/**
	 * Selects JSX backend at random from {@link #jsxBackends} cache. If we get exception below that means that either:
	 * method was called before cache was populated, or it was not populated correctly. Latter is unusual and means that
	 * either resource validation is broken and did not put error marker on compiled resource (error should say that
	 * there is no JSX backend available), or custom scope used to populate cache is broken and is not finding any JSX
	 * backend.
	 */
	private final String getAnyBackend() {
		return jsxBackends.keySet().stream().findAny().get();
	}

	/**
	 * Populates {@link #jsxBackends} with backends visible from provided resource.
	 */
	private final void populateBeckendsCache(Resource resource) {
		jsxBackends.putAll(
				jsxGlobalScope.visibleResourceDescriptions(resource)
						.filter(JSXBackendHelper::looksLikeReactFile)
						.collect(Collectors.toConcurrentMap(
								ird -> qualifiedNameConverter.toString(nameComputer.getQualifiedModuleName(ird)),
								Function.identity(),
								// IDE-2505
								JSXBackendHelper::stubMerger)));
	}

	/**
	 * Helper function that allows to deal with duplicate resources with that have the same FQN in the same scope. In
	 * general this is configuration error and, normally, should not happen.Due to lack of proper validations, it can
	 * happen. Validations preventing this situation are expected to be added with IDE-2505. Once that is done this
	 * merger should be removed.
	 *
	 * For reasons described above, we provide just some merging function, without much thought about its internals.
	 *
	 */
	private static IResourceDescription stubMerger(IResourceDescription first, IResourceDescription second) {
		if (first.getURI().lastSegment().compareToIgnoreCase(second.getURI().lastSegment()) > 0)
			return first;
		return second;
	}

	/**
	 * Provides URI for JSX Backend. URI is cached in local map {@link JSXBackendHelper#jsxBackends}. If there is no URI
	 * for given QN, performs lookup via scope of the provided resource. and returned.
	 *
	 */
	private final URI getOrFindJSXBackend(Resource resource, String qualifiedName) {
		if (jsxBackends.isEmpty()) {
			populateBeckendsCache(resource);
		}
		IResourceDescription iResourceDescription = jsxBackends.get(qualifiedName);
		if (iResourceDescription == null) {
			iResourceDescription = jsxBackends.get(getAnyBackend());
		}
		return iResourceDescription.getURI();
	}

	/** @return <code>true</code> if provided resourceDescription looks like JSX backend file. */
	private final static boolean looksLikeReactFile(IResourceDescription ird) {
		if (ird == null)
			return false;

		URI uri = ird.getURI();
		if (uri == null)
			return false;

		String sqn = uri.toString();
		if (sqn == null)
			return false;

		return sqn.endsWith(JSX_BACKEND_MODULE_NAME + "." + N4JSGlobals.N4JSD_FILE_EXTENSION) // e.g. react.n4jsd
				|| sqn.endsWith(JSX_BACKEND_MODULE_NAME + "." + N4JSGlobals.N4JS_FILE_EXTENSION) // e.g. react.n4js
				|| sqn.endsWith(JSX_BACKEND_MODULE_NAME + "." + N4JSGlobals.JS_FILE_EXTENSION) // e.g. react.js
				|| sqn.endsWith(JSX_BACKEND_MODULE_NAME + "." + N4JSXGlobals.N4JSX_FILE_EXTENSION) // e.g. rect.n4jsx
				|| sqn.endsWith(JSX_BACKEND_MODULE_NAME + "." + N4JSXGlobals.JSX_FILE_EXTENSION);// e.g. react.jsx
	}
}
