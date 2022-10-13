#!/usr/bin/env sh
set +uex

echo "" >> ~/.gradle/gradle.properties
echo "odysseyRepositoryUsername=${JITPACK_AUTH_TOKEN}" >> ~/.gradle/gradle.properties
echo "odysseyRepositoryPassword=" >> ~/.gradle/gradle.properties