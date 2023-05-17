#!/usr/bin/env sh
set +uex

cat <<EOT > lokalise.properties
id=${LOKALISE_ID}
token=${LOKALISE_TOKEN}
EOT

cat <<EOT > app/app/src/debug/res/values/adyen.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="ADYEN_CLIENT_KEY" translatable="false">${ADYEN_CLIENT_KEY}</string>
</resources>
EOT

./gradlew downloadApolloSchemasFromIntrospection
./gradlew downloadStrings
