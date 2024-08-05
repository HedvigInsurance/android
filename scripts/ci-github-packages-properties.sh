#!/usr/bin/env sh
set +uex

echo "" >> ~/.gradle/gradle.properties
echo "authlibUsername=${HEDVIG_GITHUB_PACKAGES_USER}" >> ~/.gradle/gradle.properties
echo "authlibPassword=${HEDVIG_GITHUB_PACKAGES_TOKEN}" >> ~/.gradle/gradle.properties