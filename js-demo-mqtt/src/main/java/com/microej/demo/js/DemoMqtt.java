/*
 * Java
 *
 * Copyright 2021 MicroEJ Corp. All rights reserved.
 * This library is provided in source code for use, modification and test, subject to license terms.
 * Any modification of the source code will break MicroEJ Corp. warranties on the whole library.
 */
package com.microej.demo.js;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.microej.js.JsCode;
import com.microej.js.JsRuntime;

import android.net.ConnectivityManager;
import ej.components.dependencyinjection.ServiceLoaderFactory;
import ej.microui.MicroUI;
import ej.net.util.connectivity.ConnectivityUtil;
import ej.net.util.connectivity.SimpleNetworkCallback;
import ej.net.util.connectivity.SimpleNetworkCallbackAdapter;
import ej.wadapps.app.BackgroundService;

/**
 * Main class.
 */
public class DemoMqtt implements BackgroundService, SimpleNetworkCallback {

	public static final Logger LOGGER = Logger.getLogger("[MQTT Demo]");

	/**
	 * Starts the application.
	 */
	@Override
	public void onStart() {
		MicroUI.start();
		JsBridge.initialize();

		try {
			JsCode.initJs();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "An error occured during JavaScript initialization", e);
		}

		LOGGER.info("Waiting for Internet connection...");
		ConnectivityManager service = ServiceLoaderFactory.getServiceLoader().getService(ConnectivityManager.class);
		ConnectivityUtil.registerAndCall(service, new SimpleNetworkCallbackAdapter(this));
	}

	@Override
	public void onStop() {
		JsRuntime.ENGINE.stop();
	}

	@Override
	public void onInternet(boolean hasInternet) {
		if (hasInternet) {
			LOGGER.info("Internet connection is up");

			new Thread(new Runnable() {
				@Override
				public void run() {
					JsRuntime.ENGINE.run();
				}
			}).start();
		}
	}

	@Override
	public void onConnectivity(boolean isConnected) {
		// Connected to the network. Waiting for Internet connection.
	}

}
