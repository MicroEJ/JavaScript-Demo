# Overview

This example project demonstrates the JavaScript capabilities of MicroEJ.

- `src/main/java` contains a generic Main class that starts the JavaScript runtime.
- `src/main/js` contains a set of JavaScript files.

The main class is [Main.java](/src/main/java/com/microej/demo/js/Main.java).

# Usage

## Environment Setup

A platform or a virtual device is required to run the examples.

A virtual device can be downloaded [here](https://repository.microej.com/packages/blue/2.0.1/vd/STM32F746GDISCO/BLUE-STM32F746GDISCO-CXLQH-2.0.1.vde).
More information about installation and configuration can be found on [MicroEJ Developers' Site](https://developer.microej.com/get-started/).

## Enable an Example

The JavaScript source files are located in the folder `src/main/js`.

- `adder.js` creates a closure to add values.
- `array_like.js` creates, fills and prints an array-like object.
- `demo_builtin_objects.js` uses all the JavaScript built-in objects.
- `demo_usecase_mqtt.js` parses a JSON object and applies operations on it.
- `fibo.js` prints the Fibonacci sequence.
- `func.js` creates and uses a function.
- `ref.js` uses properties by index and by reference.
- `simple_array.js` creates, prints and modify an array.
- `simple_test.js` calls a function that prints something.
- `sum.js` computes and prints the sum of the entries of an array.
- `try.js` uses try/catch/finally. 
- `validation.js` shows compilation errors created by JavaScript processor.

Only the files ending with `.js` are enabled and executed.
By default all the files are disabled (ending with `.js_`) except `demo_usecase_mqtt.js`.
To enable others demo Javascript sources, remove the `_` character.

## Running an Example from MMM Command Line Interface

- Refer to the [public documentation](https://docs.microej.com/en/latest/ApplicationDeveloperGuide/mmm.html#command-line-interface) to install and configure the MMM CLI.
- Open a terminal at the root of `js-demo` project.
- Remove the `_` suffix from one of the example files.
- Build the project with `mmm build -D"platform-loader.target.platform.file"="<path to platform or virtual device>"`.
- Run the example with `mmm run -D"platform-loader.target.platform.file"="<path to platform or virtual device>"`.

# Requirements

N/A.

# Dependencies

_All dependencies are retrieved transitively by MicroEJ Module Manager_.

# Source

N/A.

# Restrictions

None.

---
_Markdown_  
_Copyright 2020-2021 MicroEJ Corp. All rights reserved._  
_Use of this source code is governed by a BSD-style license that can be found with this software._  
