package com.remainsoftware.egg.core;

import org.eclipse.ecf.raspberrypi.gpio.IGPIOPin;
import org.eclipse.ecf.raspberrypi.gpio.IGPIOPinOutput;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ServiceManager {

	private ServiceTracker<IGPIOPinOutput, IGPIOPinOutput> fPinTracker;

	public ServiceManager(
			ServiceTrackerCustomizer<IGPIOPinOutput, IGPIOPinOutput> pTrackerCustomizer) {
		System.out.println("Init tracker");
		fPinTracker = new ServiceTracker<IGPIOPinOutput, IGPIOPinOutput>(
				FrameworkUtil.getBundle(getClass()).getBundleContext(),
				IGPIOPinOutput.class, pTrackerCustomizer);
		fPinTracker.open();
	}

	public boolean hasPin(int pin) {
		ServiceReference<IGPIOPinOutput>[] references = fPinTracker
				.getServiceReferences();
		for (ServiceReference<IGPIOPinOutput> ref : references) {
			if (pin == getPinFromServiceRef(ref)) {
				return true;
			}
		}
		return false;
	}

	public static Integer getPinFromServiceRef(
			ServiceReference<IGPIOPinOutput> pServiceRef) {
		Integer pin = Integer.valueOf((String) pServiceRef
				.getProperty(IGPIOPin.PIN_ID_PROP));
		return pin;
	}

	public void open() {
		//fPinTracker.open();
	}

}
