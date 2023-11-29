#!/usr/bin/env bash

./gradlew -Pversion=local-SNAPSHOT \
   publishJvmPublicationToMavenLocal \
   :hex-arch-ports:publishToMavenLocal \
   :hex-arch-adapters-spring6:publishToMavenLocal \

