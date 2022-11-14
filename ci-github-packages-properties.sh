#!/usr/bin/env sh
set +uex

echo "" >> ~/.gradle/gradle.properties
echo "odysseyUsername=${HEDVIG_GITHUB_PACKAGES_TOKEN}" >> ~/.gradle/gradle.properties
echo "odysseyPassword=${HEDVIG_GITHUB_PACKAGES_USER}" >> ~/.gradle/gradle.properties