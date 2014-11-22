/*******************************************************************************
 * Copyright (c) 2014 Remain Software and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wim.Jongman@remainsofwtare.com
 *******************************************************************************/
package com.remainsoftware.egg.ui.parts;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.ecf.raspberrypi.gpio.IGPIOPinOutput;
import org.eclipse.ecf.raspberrypi.gpio.pi4j.adc.IAnalogService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.remainsoftware.egg.core.OSGiUtil;
import com.remainsoftware.egg.core.ServiceManager;

public class EggCookPart implements
		ServiceTrackerCustomizer<IGPIOPinOutput, IGPIOPinOutput> {

	@Inject
	UISynchronize sync;

	private ServiceRegistration<?> fTempService;
	private EggCookComposite fEggCookComposite;


	@Inject
	public EggCookPart() {

	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		registerTemperatureService();
		parent.setLayout(new FillLayout());
		fEggCookComposite = new EggCookComposite(parent, SWT.NONE);
		new ServiceManager(this).open();
	}

	private void registerTemperatureService() {

		if (fTempService != null) {
			return;
		}

		System.out.println("Registering temperature service");

		fTempService = OSGiUtil.RegisterService(IAnalogService.class, new IAnalogService() {
			private double fTemp;

			@Override
			public void setValue(String pHost, String pDevice, double pValue) {
				System.out.println("Temperature " + pValue);
				fTemp = pValue;
				sync.syncExec(new Runnable() {
					@Override
					public void run() {
						fEggCookComposite.setTemperature(fTemp);
					}
				});

			}
		}, this);

	}

	@PreDestroy
	private void unRegisterServiceAsync() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				fTempService.unregister();
				fTempService = null;
			}
		});
		thread.setName("Unregistering service (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=448466)");
		thread.setDaemon(true);
		thread.start();
	}

	private void setPinInUI(Integer pPin, IGPIOPinOutput pService) {
		sync.syncExec(new Runnable() {
			@Override
			public void run() {
				if (pPin == 0) {
					fEggCookComposite.setCookPin(pService);
				}
				if (pPin == 1) {
					fEggCookComposite.setLightPin(pService);
				}
			}
		});
	}

	@Override
	public IGPIOPinOutput addingService(
			ServiceReference<IGPIOPinOutput> pReference) {
		Integer pin = ServiceManager.getPinFromServiceRef(pReference);
		IGPIOPinOutput service = OSGiUtil.getService(pReference, this);
		setPinInUI(pin, service);
		return service;
	}

	@Override
	public void modifiedService(ServiceReference<IGPIOPinOutput> reference,
			IGPIOPinOutput service) {
	}

	@Override
	public void removedService(ServiceReference<IGPIOPinOutput> pReference,
			IGPIOPinOutput service) {
		Integer pin = ServiceManager.getPinFromServiceRef(pReference);
		setPinInUI(pin, null);
	}
}