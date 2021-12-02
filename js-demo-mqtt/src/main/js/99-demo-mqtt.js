/*
 * JavaScript
 *
 * Copyright 2021 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */

var Logger = JavaImport("java.util.logging.Logger");
var ServiceLoaderFactory = JavaImport("ej.components.dependencyinjection.ServiceLoaderFactory");
var LocationProvider = JavaImport("ej.library.service.location.LocationProvider");
var IPAPILocationProvider = JavaImport("ej.library.service.location.impl.IPAPILocationProvider");
var Util = JavaImport("ej.bon.Util");
var Leds = JavaImport("ej.microui.led.Leds");

// The Java application logger.
var logger = Logger.getLogger("[MQTT Demo]");

logger.info("Starting MicroEJ JavaScript MQTT demo");

// MQTT configuration
var brokerUrl = "broker.hivemq.com";
var brokerPort = 8000;
var clientId = "microej";
var appName = "js-demo-mqtt";
var weatherTopic = "dt/" + appName + "/weather/" + clientId;
var positionTopic = "cmd/" + appName + "/position/" + clientId;

// The time interval in miliseconds between each weather update.
var updateInterval = 10000;

var client = new Paho.Client(brokerUrl, brokerPort, "/mqtt", clientId);
client.onConnectionLost = onConnectionLost;
client.onMessageArrived = onMessageArrived;
client.connect({onSuccess:onConnect});

/**
 * Once a connection has been made, subscribe to the configured topic.
 */
function onConnect() {
	logger.info("Subscribe to channel " + positionTopic);
	client.subscribe(positionTopic);
	updatePosition();
};

/**
 * Print error message, if any, when connection is lost.
 */
function onConnectionLost(responseObject) {
	logger.info("Connection lost");
	if (responseObject.errorCode !== 0) {
		logger.info("Error: " + responseObject.errorMessage);
	}
};

/**
 * Print the message payload to the console when published
 * on the topic.
 */
function onMessageArrived(message) {
	blink();
	var newPositionString = message.payloadString;
	logger.info("Message received: " + newPositionString);
	var newPosition = JSON.parse(newPositionString);
	updatePosition(newPosition);
};


var availableLeds = Leds.getNumberOfLeds();

/**
 * First LED blinks during 7 seconds.
 */
function blink() {
	if (availableLeds) {
		var on = false;
		var stop = false;

		var interval = setInterval(function() {
			if (on || stop) {
				Leds.setLedOff(0);
			} else {
				Leds.setLedOn(0);
			}

			on = !on;
		}, 1000);
		
		setTimeout(function() {
			stop = true;
			clearInterval(interval);
		}, 7000);
	}
}


/**
 * The position object stores the latitude and longitude.
 * It contains an update function which calls a callback
 * when it changes.
 */
var position = {
	latitude: 0,
	longitude: 0,
	
	update: function (latitude, longitude, onPositionUpdate) {
		this.latitude = latitude;
		this.longitude = longitude;
		onPositionUpdate(this);
	}
};

// Get the LocationProvider Java object
var locationProvider = ServiceLoaderFactory.getServiceLoader().getService(LocationProvider.class, IPAPILocationProvider.class);

/**
 * Update the position with the one given in argument if not null,
 * request the location provider of the firmware otherwise.
 */
function updatePosition(newPosition) {
	if (newPosition) {
		position.update(newPosition.latitude, newPosition.longitude, updateWeather);
	} else {
		// No position given, get the current position of the device from Location API.
		var location = locationProvider.requestCurrentLocation();
		if (location != null) {
			position.update(location.getLatitude(), location.getLongitude(),updateWeather);
		}
	}
}

// Send pariodically weather for the last position
setInterval(function() {updatePosition(position)}, updateInterval);

// API key for the OpenWeather service
// https://openweathermap.org/
var openWeatherApiKey = "41a84bb5c1440b07994ac29a1f49886a";

// The weather service URL.
var weatherUrl = "http://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&units=metric&appid=" + openWeatherApiKey;


var restClient = new Resty();

/**
 * Update the weather at the given postion by requesting a REST weather service.
 */
function updateWeather(newPosition) {
	var weatherFullUrl = weatherUrl.replace('{lat}', newPosition.latitude).replace('{lon}', newPosition.longitude);
	
	sendWeather(restClient.text(weatherFullUrl));
}

function sendWeather(responseString) {
	var weatherData = JSON.parse(responseString);
	var timestamp = Util.currentTimeMillis();

	var weatherObject = {
		timestamp: timestamp,
		location: {
			city: weatherData.name,
			country: weatherData.sys.country
		},
		temperature: weatherData.main.temp,
		descritpion: weatherData.weather[0].description
	};

	var weatherString = JSON.stringify(weatherObject);
	logger.info("Sending weather data: " + weatherString);

	try {
		var message = new Paho.Message(weatherString);
		message.destinationName = weatherTopic;
		client.send(message);
	} catch (e) {
		logger.severe("Failed to send weather information");
	}
}