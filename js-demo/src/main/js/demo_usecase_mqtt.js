/*
 * JavaScript
 *
 * Copyright 2020-2021 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */

var INVALID = 0
var ADD_DEVICE = 1
var REMOVE_DEVICE = 2
var UPDATE_DEVICE = 3
var UPDATE_DEVICE_FW = 4
var UPDATE_DEVICE_BL = 5

var raw_data = '[ \
	{ "id": 6423,	"type": 3,	"payload": "device xyz firmware 1.2" }, \
	{ "id": 8149,	"type": 1,	"payload": "new device ABC" }, \
	{ "id": 6713, "type": 0,		"payload": "....." }, \
	{ "id": 5555,	"type": 1212,	"payload": "?" }, \
]'

var messages = JSON.parse(raw_data)

print("Messages IDs:", messages.map(function(m) { return m.id }))
print("Messages types:", messages.map(function(m) { return m.type }))
print("Valid messages:", messages.filter(function(m) { return m.type !== INVALID }).length)
print("Has remove:", messages.some(function(m) { return m.type === REMOVE_DEVICE }))
print("Has update:", messages.some(function(m) { return m.type === UPDATE_DEVICE }))

messages
	.filter(function(m) { return m.type !== INVALID })
	.forEach(function(m) {
		switch (m.type) {
			case ADD_DEVICE:
				print("-> add device", m.payload)
				break
			case REMOVE_DEVICE:
				print("-> remove device", m.payload)
				break
			default:
				print("-> unknown message!")
				break
			case UPDATE_DEVICE:
				print("update device message...")
			case UPDATE_DEVICE_FW:
			case UPDATE_DEVICE_BL:
				print("-> update", m.payload)
		}
	})

main:
for (var message in messages) {
	var i = Math.sqrt(10);
	while (i-- > 0) {
		print("i = " + i)
		break main;
	}
}
print("finished")

