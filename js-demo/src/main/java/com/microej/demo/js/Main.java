/*
 * Java
 *
 * Copyright 2020 MicroEJ Corp. All rights reserved.
 * This library is provided in source code for use, modification and test, subject to license terms.
 * Any modification of the source code will break MicroEJ Corp. warranties on the whole library.
 */
package com.microej.demo.js;

import com.microej.js.JsCode;
import com.microej.js.JsRuntime;

/**
 * Main class.
 */
public class Main {

	/**
	 * Starts the JavaScript runtime.
	 *
	 * @param args
	 *            unused.
	 * @throws Exception
	 *             if an exception is thrown while initializing the JS code.
	 */
	public static void main(String[] args) throws Exception {
		JsCode.initJs();
		JsRuntime.ENGINE.runOneJob();
		JsRuntime.stop();
	}

}
