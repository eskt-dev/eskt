#!/usr/bin/env bash

./gradlew -Pversion=main-SNAPSHOT --parallel --max-workers 8 publish
