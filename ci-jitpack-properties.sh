#!/usr/bin/env sh
set +uex

cat <<EOT > jitpack.properties
odysseyRepositoryUsername=${JITPACK_AUTH_TOKEN}
odysseyRepositoryPassword=${JITPACK_AUTH_TOKEN}
EOT
