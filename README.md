# Firmata Web Sample

This is a Compojure-over-HttpKit example of providing arduino sensor readings over web sockets, using [clj-firmata](https://github.com/peterschwarz/clj-firmata).

## Configuring your Arduino

Currently, the analog port is hard-coded to be A0, and a notification light when a user connects is on pin 10

## Usage

	lein trampoline run

## License

Copyright © 2014 Peter Schwarz

Distributed under the Eclipse Public License either version 1.0, same as Clojure