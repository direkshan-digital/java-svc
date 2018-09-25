/*
 * Copyright (C) 2018 Marcus Hirt
 *                    www.hirt.se
 *
 * This software is free:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESSED OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright (C) Marcus Hirt, 2018
 */
package se.hirt.examples.svc.attach.util;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import sun.management.counter.perf.InstrumentationException;

/**
 * Various utility functions.
 * 
 * @author Marcus Hirt
 */
@SuppressWarnings("restriction")
public class AttachUtils {
	private static final Logger LOGGER = Logger.getLogger("se.hirt.examples.svc.attach"); //$NON-NLS-1$
	private static final boolean IS_LOCAL_ATTACH_AVAILABLE;
	private static final Class<?> CONNECTOR_ADDRESS_LINK;

	static {
		boolean available;
		Class<?> cal = null;
		try {
			Class.forName("com.sun.tools.attach.VirtualMachine"); //$NON-NLS-1$
			Class.forName("sun.tools.attach.HotSpotVirtualMachine"); //$NON-NLS-1$
			Class.forName("sun.jvmstat.monitor.MonitorException"); //$NON-NLS-1$
			cal = lookupConnectorAddressLink();
			available = true;
		} catch (Throwable t) {
			available = false;
			LOGGER.log(Level.FINE, "Cannot enable attach. Exiting...", t);
			System.exit(4711);
		}
		IS_LOCAL_ATTACH_AVAILABLE = available;
		CONNECTOR_ADDRESS_LINK = cal;
	}

	/**
	 * Parses out the PID from the first argument, printing a friendly error message and exiting if
	 * no PID argument is provided. Here to keep the example code simple.
	 * 
	 * @param args
	 *            the main method arguments.
	 * @return the PID, if provided. Exiting process with error message, if not.
	 */
	public static String checkPid(String[] args) {
		if (args.length < 1) {
			System.out.println(
					"This command requires the first argument to be the PID of the process you want to attach to!\nExiting!");
			System.exit(2);
		}
		System.out.println("======" + "PID: " + args[0] + "======");
		return args[0];
	}


	/**
	 * Importing the JMXServiceURL connector address from another process.
	 * 
	 * @param pid
	 *            the pid to import
	 * @return
	 */
	public static String importFromPid(Integer pid) {
		try {
			Method importFrom = CONNECTOR_ADDRESS_LINK.getMethod("importFrom", int.class);
			return (String) importFrom.invoke(null, pid);
		} catch (NullPointerException e) {
			// JVM possibly died during call
		} catch (InstrumentationException e) {
			// Connecting to a 1.4 Sun JVM.
		} catch (Throwable t) {
			LOGGER.log(Level.WARNING, "Could not get connector address for pid", t);
		}
		return null;
	}

	/**
	 * @return if we have local attachment capabilities available.
	 */
	public static boolean isLocalAttachAvailable() {
		return IS_LOCAL_ATTACH_AVAILABLE;
	}

	private static Class<?> lookupConnectorAddressLink() throws Throwable {
		try {
			return Class.forName("jdk.internal.agent.ConnectorAddressLink");
		} catch (Throwable t) {
			// Assuming because Java 7/8, try the equivalent old sun.management class...
		}
		return Class.forName("sun.management.ConnectorAddressLink");
	}
	
	public static void printJVMVersion() {
		System.out.println("JVM: " + Runtime.version());
	}
}
