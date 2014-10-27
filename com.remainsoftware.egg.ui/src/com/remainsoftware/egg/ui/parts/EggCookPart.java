package com.remainsoftware.egg.ui.parts;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.ecf.raspberrypi.gpio.IGPIOPinOutput;
import org.eclipse.ecf.raspberrypi.gpio.ILM35;
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

	private ServiceManager fServiceManager;

	@Inject
	public EggCookPart() {

	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		registerTemperatureService();
		parent.setLayout(new FillLayout());
		fEggCookComposite = new EggCookComposite(parent, SWT.NONE);
		fServiceManager = new ServiceManager(this);
	}

	private void registerTemperatureService() {

		if (fTempService != null) {
			return;
		}

		System.out.println("Registering temperature service");

		fTempService = OSGiUtil.RegisterService(ILM35.class, new ILM35() {
			private double fTemp;

			@Override
			public void setTemperature(String pHost, double pTemperature) {
				System.out.println("Temperature " + pTemperature);
				fTemp = pTemperature;
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
		Integer pin = fServiceManager.getPinFromServiceRef(pReference);
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
		Integer pin = fServiceManager.getPinFromServiceRef(pReference);
		setPinInUI(pin, null);
	}
}