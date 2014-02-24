# Firmata Web Sample

This is a Compojure-over-HttpKit example of providing arduino sensor readings over web sockets, using [clj-firmata](https://github.com/peterschwarz/clj-firmata).

## Configuring your Arduino

Currently, the analog port is hard-coded to be A0, and a notification light when a user connects is on pin 10

Run a repl to figure out what serial port name to use:

	=> (use 'serial.core)
	nil
	=> (list-ports)
	0 : cu.Bluetooth-Incoming-Port
	1 : tty.Bluetooth-Incoming-Port
	2 : cu.Bluetooth-Modem
	3 : tty.Bluetooth-Modem
	4 : cu.usbmodemfd131
	5 : tty.usbmodemfd131


## Usage

	lein trampoline run <port-name>

## License

Copyright © 2014 Peter Schwarz

Distributed under the Eclipse Public License either version 1.0, same as Clojure