#!/usr/bin/env bash
# Reads the network calls Chucker recorded in a debug build, over adb.
#
# The database is private to the app, so it is copied off the device with `run-as` (debug builds
# only) and queried with the host's sqlite3. Room keeps it in WAL mode, so the -wal and -shm files
# come along too: recent calls live in the -wal file until SQLite checkpoints it.
#
# Usage:
#   chucker.sh mark             Print the device clock in epoch ms, to pass to `since` later
#   chucker.sh recent [N]       The last N calls (default 20)
#   chucker.sh since <ms>       Calls made after a `mark`
#   chucker.sh errors [N]       Calls with an HTTP error, a transport error or GraphQL errors.
#                               Cancelled calls are left out, they are routine (see recent/since)
#   chucker.sh show <id>        Every column of one call, as JSON
#   chucker.sh sql "<query>"    Any query against the `transactions` table
#
# Env: ANDROID_SERIAL picks the device when several are attached, PACKAGE overrides the app id.
set -euo pipefail

PACKAGE="${PACKAGE:-com.hedvig.dev.app}"

die() {
  echo "chucker.sh: $*" >&2
  exit 1
}

command -v adb >/dev/null || die "adb is not on PATH"
command -v sqlite3 >/dev/null || die "sqlite3 is not on PATH"

pull_database() {
  local files
  files="$(adb exec-out run-as "$PACKAGE" ls databases 2>&1)" ||
    die "cannot read $PACKAGE's databases: $files (is a debug build installed?)"
  case "$files" in
    *"not debuggable"* | *"unknown package"*) die "$files. Install a debug build: ./gradlew :app:installDebug" ;;
  esac
  grep -qx "chucker.db" <<<"$files" ||
    die "no chucker.db yet. Chucker creates it on the first call the app makes"

  DB_DIR="$(mktemp -d)"
  trap 'rm -rf "$DB_DIR"' EXIT
  local file
  for file in chucker.db chucker.db-wal chucker.db-shm; do
    if grep -qx "$file" <<<"$files"; then
      adb exec-out run-as "$PACKAGE" cat "databases/$file" >"$DB_DIR/$file"
    fi
  done
}

query() {
  local output
  output="$(sqlite3 -header -separator ' | ' "$DB_DIR/chucker.db" "$1")"
  if [[ -n "$output" ]]; then echo "$output"; else echo "(no matching calls)"; fi
}

# Every GraphQL call is a POST to the same router URL, so the operation name comes from the body.
# Chucker's own graphQlOperationName column stays empty, Apollo sends no operation-name header.
SUMMARY_COLUMNS="id,
  strftime('%H:%M:%f', requestDate / 1000.0, 'unixepoch', 'localtime') AS time,
  coalesce(json_extract(requestBody, '\$.operationName'), method || ' ' || path) AS call,
  responseCode AS code,
  tookMs AS ms,
  responsePayloadSize AS bytes,
  CASE WHEN json_valid(responseBody) AND json_extract(responseBody, '\$.errors') IS NOT NULL
    THEN 'graphql errors' ELSE coalesce(error, '') END AS problem"

require_number() {
  [[ "$1" =~ ^[0-9]+$ ]] || die "expected a number, got '$1'"
}

command="${1:-recent}"
case "$command" in
  mark)
    adb shell date +%s%3N
    ;;
  recent)
    limit="${2:-20}"
    require_number "$limit"
    pull_database
    query "SELECT $SUMMARY_COLUMNS FROM transactions ORDER BY requestDate DESC LIMIT $limit"
    ;;
  since)
    [[ $# -ge 2 ]] || die "usage: chucker.sh since <epoch ms from 'chucker.sh mark'>"
    require_number "$2"
    pull_database
    query "SELECT $SUMMARY_COLUMNS FROM transactions WHERE requestDate >= $2 ORDER BY requestDate"
    ;;
  errors)
    limit="${2:-20}"
    require_number "$limit"
    pull_database
    query "SELECT $SUMMARY_COLUMNS FROM transactions
      WHERE responseCode >= 400
        OR (error IS NOT NULL AND error NOT LIKE '%Canceled%')
        OR (json_valid(responseBody) AND json_extract(responseBody, '\$.errors') IS NOT NULL)
      ORDER BY requestDate DESC LIMIT $limit"
    ;;
  show)
    [[ $# -ge 2 ]] || die "usage: chucker.sh show <id>"
    require_number "$2"
    pull_database
    # Headers and JSON bodies are nested as JSON rather than escaped strings. Downloaded images
    # are a blob, which is no use as text, so they are left out.
    fields="$(sqlite3 "$DB_DIR/chucker.db" "SELECT group_concat(
        quote(name) || ', ' || CASE WHEN name IN ('requestHeaders', 'responseHeaders', 'requestBody', 'responseBody')
          THEN 'CASE WHEN json_valid(' || name || ') THEN json(' || name || ') ELSE ' || name || ' END'
          ELSE name END, ', ')
      FROM pragma_table_info('transactions') WHERE name != 'responseImageData'")"
    result="$(sqlite3 "$DB_DIR/chucker.db" "SELECT json_object($fields) FROM transactions WHERE id = $2")"
    [[ -n "$result" ]] || die "no call with id $2"
    if command -v jq >/dev/null; then jq . <<<"$result"; else echo "$result"; fi
    ;;
  sql)
    [[ $# -ge 2 ]] || die "usage: chucker.sh sql \"<query>\""
    pull_database
    query "$2"
    ;;
  *)
    sed -n '2,16p' "$0" | sed 's/^# \{0,1\}//'
    exit 1
    ;;
esac
