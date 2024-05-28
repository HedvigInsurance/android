#!/usr/bin/env sh
set +uex

cat <<EOT > lokalise.properties
id=${LOKALISE_ID}
token=${LOKALISE_TOKEN}
EOT

./gradlew downloadOctopusApolloSchemaFromIntrospection
./gradlew downloadStrings
