/*
 * Java
 *
 * Copyright 2021 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.demo.js;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microej.js.JsClosure;
import com.microej.js.JsErrorWrapper;
import com.microej.js.JsRuntime;
import com.microej.js.host.Engine.Job;
import com.microej.js.objects.JsObject;
import com.microej.js.objects.JsObjectArrayBuffer;
import com.microej.js.objects.property.DataPropertyDescriptor;
import com.microej.js.objects.property.PropertyDescriptor;

import ej.annotation.Nullable;
import ej.util.concurrent.SingleThreadExecutor;
import ej.websocket.ConnectionStates;
import ej.websocket.Endpoint;
import ej.websocket.ReasonForClosure;
import ej.websocket.WebSocket;
import ej.websocket.WebSocketException;
import ej.websocket.WebSocketURI;

/**
 * JavaScript Host Object for WebSocket API.
 * <p>
 * This object is needed by Paho, which connects to the broker through a websocket.
 */
public class WebSocketHostObject extends JsObject {

	private static final Logger LOGGER = Logger.getLogger("[WebSocket Host Object]");

	private static final String READY_STATE = "readyState"; //$NON-NLS-1$

	private static final int CONNECTING = 0;

	private static final int OPEN = 1;

	private static final int CLOSING = 2;

	private static final int CLOSED = 3;

	private Object thisBinding;

	private final WebSocket webSocket;

	/**
	 * The {@link Executor} used to handle blocking operations. When a blocking Java operation needs to be done, it can
	 * be scheduled in this executor and return immediately to continue JavaScript execution.
	 */
	private final SingleThreadExecutor executor;

	/**
	 * Constructor.
	 *
	 * @param url
	 *            the URL to connect to
	 * @throws URISyntaxException
	 *             when the given URL string is not a valid URL
	 */
	public WebSocketHostObject(String url) throws URISyntaxException {
		this.executor = new SingleThreadExecutor();

		this.put("send", new DataPropertyDescriptor(JsRuntime.createFunction(new JsClosure() { //$NON-NLS-1$
			@Override
			@Nullable
			public Object invoke(@Nullable Object thisBinding, int argsLength, Object... arguments) {
				JsObjectArrayBuffer data = (JsObjectArrayBuffer) arguments[0];
				asyncSend(data.arrayBufferData);
				return null;
			}
		})));

		this.put("close", new DataPropertyDescriptor(JsRuntime.createFunction(new JsClosure() { //$NON-NLS-1$
			@Override
			@Nullable
			public Object invoke(@Nullable Object thisBinding, int argsLength, Object... arguments) {
				try {
					WebSocketHostObject.this.webSocket.close();
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, "Closing error", e); //$NON-NLS-1$
				}
				return null;
			}
		})));

		URI uri = new URI(url);
		WebSocketURI wsUri = new WebSocketURI(uri.getHost(), uri.getPort(), uri.getPath());
		this.webSocket = new WebSocket(wsUri, new Endpoint() {

			@Override
			public String onTextMessage(WebSocket ws, String message) {
				callJsListener("onmessage", message); //$NON-NLS-1$
				return null;
			}

			@Override
			public byte[] onBinaryMessage(WebSocket ws, byte[] message) {
				JsObject event = new JsObject();
				JsObjectArrayBuffer data = new JsObjectArrayBuffer(message);
				event.put("data", data, false); //$NON-NLS-1$
				callJsListener("onmessage", event); //$NON-NLS-1$
				return null;
			}

			@Override
			public void onPong(byte[] data) {
				// not implemented
			}

			@Override
			public void onPing(byte[] data) {
				// not implemented
			}

			@Override
			public void onOpen(WebSocket ws) {
				callJsListener("onopen"); //$NON-NLS-1$
			}

			@Override
			public void onError(WebSocket ws, Throwable thr) {
				LOGGER.log(Level.SEVERE, "WebSocket error", thr);
				callJsListener("onerror"); //$NON-NLS-1$
			}

			@Override
			public void onClose(WebSocket ws, ReasonForClosure closeReason) {
				callJsListener("onclose"); //$NON-NLS-1$
			}

			/**
			 * Calls the given JavaScript listener.
			 * <p>
			 * A new JS {@link Job} is created for the given function and registered on the engine so it can be managed
			 * by the JS application thread.
			 * <p>
			 * If the given name does not correspond to a JavaScript function, it is just ignored.
			 *
			 * @param name
			 *            the name of the JS listener to call
			 * @param arguments
			 *            the arguments of the listener
			 */
			private void callJsListener(String name, Object... arguments) {
				Object functionObject = WebSocketHostObject.this.get(name);
				JsRuntime.ENGINE.addJob(functionObject, WebSocketHostObject.this.thisBinding, arguments);
			}
		});

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					WebSocketHostObject.this.webSocket.connect();
				} catch (IllegalArgumentException | WebSocketException e) {
					LOGGER.log(Level.SEVERE, "Connection error", e); //$NON-NLS-1$
				}
			}
		}).start();

	}

	@Override
	@Nullable
	public PropertyDescriptor getOwnProperty(String property) {
		if (READY_STATE.equals(property)) {
			ConnectionStates state = this.webSocket.getCurrentState();
			switch (state) {
			case CONNECTING:
				return new DataPropertyDescriptor(new Integer(CONNECTING), false, false, false);
			case OPEN:
				return new DataPropertyDescriptor(new Integer(OPEN), false, false, false);
			case CLOSING:
				return new DataPropertyDescriptor(new Integer(CLOSING), false, false, false);
			case CLOSED:
				return new DataPropertyDescriptor(new Integer(CLOSED), false, false, false);
			default:
				throw new JsErrorWrapper("Unsupported WebSocket state: " + state); //$NON-NLS-1$
			}
		}
		return super.getOwnProperty(property);
	}

	/**
	 * Sends the given binary data through websocket.
	 * <p>
	 * This method is non-blocking. Each call to this method adds a send task to a FIFO which will be executed ass soon
	 * as possible.
	 *
	 * @param data
	 *            the binary data to send
	 */
	private void asyncSend(final byte[] data) {
		this.executor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					if (WebSocketHostObject.this.webSocket.getCurrentState() == ConnectionStates.OPEN) {
						WebSocketHostObject.this.webSocket.sendBinary(data);
					} else {
						LOGGER.severe("[Host object] Sending error: websocket not opened"); //$NON-NLS-1$
					}
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, "[Host object] Sending error", e); //$NON-NLS-1$
				}

			}
		});
	}
}
