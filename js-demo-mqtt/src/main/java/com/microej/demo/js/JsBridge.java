/*
 * Java
 *
 * Copyright 2021 MicroEJ Corp. All rights reserved.
 * This library is provided in source code for use, modification and test, subject to license terms.
 * Any modification of the source code will break MicroEJ Corp. warranties on the whole library.
 */
package com.microej.demo.js;

import java.net.URISyntaxException;

import com.microej.js.JsClosure;
import com.microej.js.JsErrorWrapper;
import com.microej.js.JsRuntime;
import com.microej.js.objects.JsObjectFunction;

import ej.annotation.Nullable;
import ej.rest.web.Resty;

/**
 * Bridge to expose Java APIs used in JavaScript files.
 */
public class JsBridge {

	/**
	 * Initializes the JavaScript host objects.
	 */
	public static void initialize() {
		JsRuntime.JS_GLOBAL_OBJECT.put("global", JsRuntime.JS_GLOBAL_OBJECT, false);
		JsRuntime.JS_GLOBAL_OBJECT.put("self", JsRuntime.JS_GLOBAL_OBJECT, false);
		JsRuntime.JS_GLOBAL_OBJECT.put("WebSocket", JsRuntime.createFunction(new WebSocketClosure()), false);

		// Exposes Rusty constructor to JavaScript. It cannot be done automatically as FFI does not handle varargs for
		// now.
		JsRuntime.JS_GLOBAL_OBJECT.put("Resty", new JsObjectFunction() {
			@Override
			@Nullable
			public String call(@Nullable Object thisBinding, Object... arguments) {
				throw new JsErrorWrapper("Resty is not a function");
			}

			@Override
			public Object construct(Object... arguments) {
				// We only manage no args constructor for simplicity reasons.
				return new Resty();
			}
		}, false);
	}

	private static final class WebSocketClosure extends JsClosure {
		@Override
		@Nullable
		public Object invoke(@Nullable Object thisBinding, int argsLength, Object... arguments) {
			if (arguments.length == 0) {
				throw new JsErrorWrapper("WebSocket constructor: At least 1 argument required, but only 0 passed");
			}

			String url = (String) arguments[0];
			try {
				return new WebSocketHostObject(url);
			} catch (URISyntaxException e) {
				throw new JsErrorWrapper("WebSocket constructor: invalid url '" + url + "'");
			}
		}
	}
}
