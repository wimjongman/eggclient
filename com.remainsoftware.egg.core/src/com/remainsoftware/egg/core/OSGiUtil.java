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
	 * Returns an <code>InetAddress</code> object encapsulating what is most likely the machine's LAN IP address.
	 * <p/>
	 * This method is intended for use as a replacement of JDK method <code>InetAddress.getLocalHost</code>, because
	 * that method is ambiguous on Linux systems. Linux systems enumerate the loopback network interface the same
	 * way as regular LAN network interfaces, but the JDK <code>InetAddress.getLocalHost</code> method does not
	 * specify the algorithm used to select the address returned under such circumstances, and will often return the
	 * loopback address, which is not valid for network communication. Details
	 * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037">here</a>.
	 * <p/>
	 * This method will scan all IP addresses on all network interfaces on the host machine to determine the IP address
	 * most likely to be the machine's LAN address. If the machine has multiple IP addresses, this method will prefer
	 * a site-local IP address (e.g. 192.168.x.x or 10.10.x.x, usually IPv4) if the machine has one (and will return the
	 * first site-local address if the machine has more than one), but if the machine does not hold a site-local
	 * address, this method will return simply the first non-loopback address found (IPv4 or IPv6).
	 * <p/>
	 * If this method cannot find a non-loopback address using this selection algorithm, it will fall back to
	 * calling and returning the result of JDK method <code>InetAddress.getLocalHost</code>.
	 * <p/>
	 *
	 * @throws UnknownHostException If the LAN address of the machine cannot be found.
	 */
	public static String getFirstInterface()  {
	    try {
	        InetAddress candidateAddress = null;
	        // Iterate all NICs (network interface cards)...
	        for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
	            NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
	            // Iterate all IP addresses assigned to each card...
	            for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
	                InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
	                if (!inetAddr.isLoopbackAddress()) {

	                    if (inetAddr.isSiteLocalAddress()) {
	                        // Found non-loopback site-local address. Return it immediately...
	                        return inetAddr.getHostAddress();
	                    }
	                    else if (candidateAddress == null) {
	                        // Found non-loopback address, but not necessarily site-local.
	                        // Store it as a candidate to be returned if site-local address is not subsequently found...
	                        candidateAddress = inetAddr;
	                        // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
	                        // only the first. For subsequent iterations, candidate will be non-null.
	                    }
	                }
	            }
	        }
	        if (candidateAddress != null) {
	            // We did not find a site-local address, but we found some other non-loopback address.
	            // Server might have a non-site-local address assigned to its NIC (or it might be running
	            // IPv6 which deprecates the "site-local" concept).
	            // Return this non-loopback candidate address...
	            return candidateAddress.getHostAddress();
	        }
	        // At this point, we did not find a non-loopback address.
	        // Fall back to returning whatever InetAddress.getLocalHost() returns...
	        InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
	        if (jdkSuppliedAddress == null) {
	            throw new UnknownHostException("The JDK InetAddress.getLocalHost() method returned null.");
	        }
	        return jdkSuppliedAddress.getHostAddress();
	    }
	    catch (Exception e) {
	        return null;
	    }
	}
}
