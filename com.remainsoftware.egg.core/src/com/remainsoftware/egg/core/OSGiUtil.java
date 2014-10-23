package com.remainsoftware.egg.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.eclipse.ecf.raspberrypi.gpio.ILM35;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class OSGiUtil {

	/**
	 * @param reference
	 * @param requester
	 * @return the service from the passed service reference
	 * @throws NullPointerException
	 *             if the provided class does not refer to a bundle context.
	 */
	public static <T> T getService(ServiceReference<T> reference,
			Object requester) {
		return getContext(requester).getService(reference);
	}

	private static BundleContext getContext(Object requester) {
		return FrameworkUtil.getBundle(requester.getClass()).getBundleContext();
	}

	public static ServiceRegistration<?> RegisterService(Class<?> serviceClass, Object service, Object requester) {

		Dictionary<String, Object> pinProps = new Hashtable<String, Object>();
		pinProps.put("service.exported.interfaces", "*");
		pinProps.put("service.exported.configs", "ecf.generic.server");
		pinProps.put("ecf.generic.server.port", "3288");
		try {
			pinProps.put("ecf.generic.server.hostname", InetAddress
					.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
		}
		pinProps.put("ecf.exported.async.interfaces", "*");
		Properties systemProps = System.getProperties();
		for (Object pn : systemProps.keySet()) {
			String propName = (String) pn;
			if (propName.startsWith("service.") || propName.startsWith("ecf."))
				pinProps.put(propName, systemProps.get(propName));
		}

		return getContext(requester).registerService(serviceClass.getName(),
				service, pinProps);

	}
}
