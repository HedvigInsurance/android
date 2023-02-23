#!/usr/bin/env sh
set +uex

cat <<EOT > lokalise.properties
id=${LOKALISE_ID}
token=${LOKALISE_TOKEN}
EOT

cat <<EOT > app/src/debug/res/values/adyen.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="ADYEN_CLIENT_KEY" translatable="false">${ADYEN_CLIENT_KEY}</string>
</resources>
EOT


cat <<EOT > app/src/debug/res/values/shake.xml
<?xml version="1.0" encoding="utf-8" ?>
<resources>
    <string name="SHAKE_CLIENT_ID" translatable="false">${SHAKE_CLIENT_ID}</string>
    <string name="SHAKE_CLIENT_SECRET" translatable="false">${SHAKE_CLIENT_SECRET}</string>
</resources>
EOT

cat <<EOT > app/src/debug/res/values/customerio.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="CUSTOMERIO_SE_SITE_ID" translatable="false">${CUSTOMERIO_SE_SITE_ID_TEST}</string>
    <string name="CUSTOMERIO_SE_API_KEY" translatable="false">${CUSTOMERIO_SE_API_KEY_TEST}</string>
    <string name="CUSTOMERIO_DE_SITE_ID" translatable="false">${CUSTOMERIO_DE_SITE_ID_TEST}</string>
    <string name="CUSTOMERIO_DE_API_KEY" translatable="false">${CUSTOMERIO_DE_API_KEY_TEST}</string>
    <string name="CUSTOMERIO_NO_SITE_ID" translatable="false">${CUSTOMERIO_NO_SITE_ID_TEST}</string>
    <string name="CUSTOMERIO_NO_API_KEY" translatable="false">${CUSTOMERIO_NO_API_KEY_TEST}</string>
</resources>
EOT

./gradlew :apollo:downloadGiraffeApolloSchemaFromIntrospection
./gradlew :core-resources:downloadStrings
