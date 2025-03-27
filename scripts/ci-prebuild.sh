#!/usr/bin/env sh
set +uex

echo "" >> ~/.gradle/gradle.properties
echo "authlibUsername=${HEDVIG_GITHUB_PACKAGES_USER}" >> ~/.gradle/gradle.properties
echo "authlibPassword=${HEDVIG_GITHUB_PACKAGES_TOKEN}" >> ~/.gradle/gradle.properties
echo "DATADOG_API_KEY=${DATADOG_API_KEY}" >> ~/.gradle/gradle.properties

cat <<EOT > lokalise.properties
id=${LOKALISE_ID}
token=${LOKALISE_TOKEN}
EOT

./gradlew downloadOctopusApolloSchemaFromIntrospection
./gradlew downloadStrings