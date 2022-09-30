#!/usr/bin/env sh
set +uex

cat <<EOT > jitpack.properties
odysseyRepositoryUsername=${{ secrets.JITPACK_AUTH_TOKEN }}
odysseyRepositoryPassword=${{ secrets.JITPACK_AUTH_TOKEN }}
EOT
