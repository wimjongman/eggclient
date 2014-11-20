package com.remainsoftware.egg.core;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

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

	public static ServiceRegistration<?> RegisterService(Class<?> serviceClass,
			Object service, Object requester) {

		Dictionary<String, Object> pinProps = new Hashtable<String, Object>();
		pinProps.put("service.exported.interfaces", "*");
		pinProps.put("service.exported.configs", "ecf.generic.server");
		pinProps.put("ecf.generic.server.port", "3288");
		try {
			pinProps.put("ecf.generic.server.hostname", InetAddress
					.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			System.out.println("Cannot find host");
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

	/**
	 * @return the first network address that is not in the loopback range or
	 *         null if it could not be found.
	 */
	public static String getFirstInterface() {
		try {
			Enumeration<NetworkInterface> e = NetworkInterface
					.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				NetworkInterface n = e.nextElement();
				Enumeration<InetAddress> ee = n.getInetAddresses();
				while (ee.hasMoreElements()) {
					InetAddress i = (InetAddress) ee.nextElement();
					if (!(i.getHostAddress().startsWith("127"))) {
						return i.getHostAddress();
					}
				}
			}
		} catch (Exception e) {
		}
		return null;
	}
}
