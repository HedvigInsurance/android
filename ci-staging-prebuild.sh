#!/usr/bin/env sh
set +uex

cat <<EOT > lokalise.properties
id=${LOKALISE_ID}
token=${LOKALISE_TOKEN}
EOT

cat <<EOT > app/src/staging/res/values/adyen.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="ADYEN_CLIENT_KEY" translatable="false">${ADYEN_CLIENT_KEY}</string>
</resources>
EOT


cat <<EOT > app/src/staging/res/values/shake.xml
<?xml version="1.0" encoding="utf-8" ?>
<resources>
    <string name="SHAKE_CLIENT_ID" translatable="false">${SHAKE_CLIENT_ID}</string>
    <string name="SHAKE_CLIENT_SECRET" translatable="false">${SHAKE_CLIENT_SECRET}</string>
</resources>
EOT

./gradlew :apollo:downloadGiraffeApolloSchemaFromIntrospection
./gradlew :core-resources:downloadStrings
./gradlew licenseReleaseReport

cp app/src/staging/res/values/adyen.xml app/src/pullrequest/res/values/adyen.xml
cp app/src/staging/res/values/shake.xml app/src/pullrequest/res/values/shake.xml

