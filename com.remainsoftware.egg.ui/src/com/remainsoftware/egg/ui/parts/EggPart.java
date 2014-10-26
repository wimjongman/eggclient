/*******************************************************************************
 * Copyright (c) 2010 - 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <lars.Vogel@gmail.com> - Bug 419770
 *******************************************************************************/
package com.remainsoftware.egg.ui.parts;

import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.ecf.raspberrypi.gpio.IGPIOPinOutput;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.remainsoftware.egg.core.OSGiUtil;
import com.remainsoftware.egg.core.ServiceManager;

public class EggPart implements
		ServiceTrackerCustomizer<IGPIOPinOutput, IGPIOPinOutput> {

	@Inject
	UISynchronize fSyncer;

	HashMap<String, CTabItem> fTabs = new HashMap<String, CTabItem>();

	private CTabFolder fTabFolder;

	private CTabItem fMainTab;

	ServiceManager fServiceManager = new ServiceManager(this);

	@PostConstruct
	public void createComposite(Composite pParent) {
		System.out.println(fSyncer);

		fTabFolder = new CTabFolder(pParent, SWT.CLOSE | SWT.FLAT | SWT.BOTTOM);
		fTabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));

		fMainTab = new CTabItem(fTabFolder, SWT.NONE);
		fMainTab.setText("Main");

		Composite composite = new Composite(fTabFolder, SWT.NONE);
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		fMainTab.setControl(composite);
		composite.setLayout(new GridLayout(1, false));
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		ProgressBar progressBar = new ProgressBar(composite, SWT.INDETERMINATE);
		progressBar.setMaximum(10);
		progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		new Label(composite, SWT.NONE);

		Label lblScanning = new Label(composite, SWT.NONE);
		lblScanning.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		lblScanning.setForeground(SWTResourceManager
				.getColor(SWT.COLOR_DARK_GREEN));
		lblScanning.setFont(SWTResourceManager
				.getFont("Terminal", 13, SWT.BOLD));
		lblScanning.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1));
		lblScanning.setText("Scanning ...");

		fTabFolder.setSelection(fMainTab);

		// Open service tracker to get IGPIOPinOutput instances
		fServiceManager.open();
	}

	@Override
	public IGPIOPinOutput addingService(
			ServiceReference<IGPIOPinOutput> pReference) {
		fSyncer.syncExec(new Runnable() {

			@Override
			public void run() {
				System.out.println("Service found");
				CTabItem tabItem = getTabItem(pReference);
				ServiceControlComposite control = (ServiceControlComposite) tabItem
						.getControl();
				control.addPinService(pReference);
				tabItem.setText(getHostName(pReference));
				fTabFolder.setSelection(tabItem);
			}

		});
		return OSGiUtil.getService(pReference, this);
	}

	private CTabItem getTabItem(ServiceReference<IGPIOPinOutput> pReference) {
		CTabItem tabItem = fTabs.get(getHostName(pReference));
		if (tabItem == null) {
			tabItem = ServiceControlComposite.createTabItem(fTabFolder);
			fTabs.put(getHostName(pReference), tabItem);
		}
		return tabItem;
	}

	private String getHostName(ServiceReference<IGPIOPinOutput> pReference) {
		return pReference.getProperty("ecf.generic.server.hostname").toString();
	}

	@Override
	public void modifiedService(ServiceReference<IGPIOPinOutput> pReference,
			IGPIOPinOutput pService) {

	}

	@Override
	public void removedService(ServiceReference<IGPIOPinOutput> pReference,
			IGPIOPinOutput pService) {
		fSyncer.syncExec(new Runnable() {
			@Override
			public void run() {
				System.out.println("Service lost");
				CTabItem tabItem = fTabs.get(getHostName(pReference));
				ServiceControlComposite control = (ServiceControlComposite) tabItem
						.getControl();
				control.removeService(pReference);
				if (control.getServiceCount() == 0) {
					tabItem.getControl().dispose();
					tabItem.dispose();
					fTabs.remove(getHostName(pReference));
					fTabFolder.setSelection(fMainTab);
				}
			}
		});
	}
}