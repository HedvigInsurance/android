#!/usr/bin/env sh
set +uex

echo "" >> ~/.gradle/gradle.properties
echo "DATADOG_API_KEY=${DATADOG_API_KEY}" >> ~/.gradle/gradle.properties

cat <<EOT > lokalise.properties
id=${LOKALISE_ID}
token=${LOKALISE_TOKEN}
EOT