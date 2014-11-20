/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/
package com.remainsoftware.egg.ui;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessRemovals;

import com.remainsoftware.egg.core.OSGiUtil;

/**
 * This is a stub implementation containing e4 LifeCycle annotated methods.<br />
 * There is a corresponding entry in <em>plugin.xml</em> (under the
 * <em>org.eclipse.core.runtime.products' extension point</em>) that references
 * this class.
 **/
@SuppressWarnings("restriction")
public class E4LifeCycle {

	private static final String NET_MDNS_INTERFACE = "net.mdns.interface";
	private static final String ECF_GENERIC_SERVER_HOSTNAME = "ecf.generic.server.hostname";

	@PostContextCreate
	void postContextCreate(IEclipseContext workbenchContext) {
		String ip = OSGiUtil.getFirstInterface();
		setECFProperty(ip);
		setMDNSProperty(ip); // too late, jmdns already started
	}

	private void setECFProperty(String ip) {
		if (ip != null) {
			setProperty(ip, ECF_GENERIC_SERVER_HOSTNAME);
		}
	}

	private void setMDNSProperty(String ip) {
		if (ip != null) {
			setProperty(ip, NET_MDNS_INTERFACE);
		}
	}

	private void setProperty(String ip, String pProp) {
		String prop = System.getProperty(pProp);
		if (prop == null) {
			System.setProperty(pProp, ip);
		}
	}

	@PreSave
	void preSave(IEclipseContext workbenchContext) {
	}

	@ProcessAdditions
	void processAdditions(IEclipseContext workbenchContext) {
	}

	@ProcessRemovals
	void processRemovals(IEclipseContext workbenchContext) {
	}
}
