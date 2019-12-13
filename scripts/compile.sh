#!/usr/bin/env bash

javac -Xlint:unchecked -cp ../jade/lib/jade.jar:../src ../src/DataProducer.java -d ../out
javac -Xlint:unchecked -cp ../jade/lib/jade.jar:../src ../src/Factory.java -d ../out