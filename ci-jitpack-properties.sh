#!/usr/bin/env sh
set +uex

echo "odysseyRepositoryUsername=${JITPACK_AUTH_TOKEN}" >> ~/.gradle/gradle.properties
echo "odysseyRepositoryPassword=${JITPACK_AUTH_TOKEN}" >> ~/.gradle/gradle.properties