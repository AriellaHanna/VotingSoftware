#!/bin/bash
set -v off
title SIS Server

cd ..
cd NewSISServer
javac *.java
java SISServer

sleep 100