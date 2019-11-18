#!/usr/bin/env bash

cp -r ../examples ../out/examples
javac -Xlint:unchecked -cp ../jade/lib/jade.jar:../src ../src/Factory.java -d ../out