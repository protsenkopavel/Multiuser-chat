#!/bin/sh

./gradlew :client:clean :client:build

rm -rf ./client/build/runnable
mkdir ./client/build/runnable

bsdtar --strip-components=1 -C ./client/build/runnable -xf ./client/build/distributions/*.zip

./client/build/runnable/bin/client