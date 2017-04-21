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
package eu.numberfour.n4js.smith.graph;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	/**
	 * The plug-in ID.
	 * <p>
	 * IMPORTANT: when changing the view or plugin ID, you need to update method takeSnapshotInTestView() in class
	 * UtilN4!
	 */
	public static final String PLUGIN_ID = "eu.numberfour.n4js.smith.graph"; //$NON-NLS-1$
	private static final String ICON_FOLDER = "icons/";

	// The shared instance
	private static Activator plugin;

	// The view instance
	private ASTGraphView view;

	@SuppressWarnings("javadoc")
	public final ImageDescriptor ICON_SNAPSHOT = imageDescriptorFromPlugin(PLUGIN_ID, ICON_FOLDER + "snapshot.png");
	@SuppressWarnings("javadoc")
	public final ImageDescriptor ICON_PAUSE = imageDescriptorFromPlugin(PLUGIN_ID, ICON_FOLDER + "pause.gif");
	@SuppressWarnings("javadoc")
	public final ImageDescriptor ICON_GRAPH = imageDescriptorFromPlugin(PLUGIN_ID, ICON_FOLDER + "graph.gif");

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getInstance() {
		return plugin;
	}

	/* package */void setViewInstance(ASTGraphView view) {
		this.view = view;
	}

	/**
	 * Returns the shared view instance
	 *
	 * @return the shared view instance
	 */
	public ASTGraphView getViewInstance() {
		return view;
	}
}
