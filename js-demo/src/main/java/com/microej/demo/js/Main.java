/*
 * Java
 *
 * Copyright 2020-2021 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
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
