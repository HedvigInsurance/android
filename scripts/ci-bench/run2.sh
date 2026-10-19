#!/usr/bin/env bash
# One build per job, on top of a Gradle User Home restored from the GitHub Actions cache that the
# seed job wrote. This is the shape of a PR job once develop keeps the cache warm.
#   none        nothing changed
#   leaf-body   body of a private function in a leaf module (feature-help-center)
#   root-body   body of a public function in a root module (core-common-public, 113 dependents)
#   root-abi    new public declaration in the same root module
set -u
SCENARIO="$1"
TASKS="$2"
OUT="${BENCH_OUT:?}"
mkdir -p "$OUT"
LEAF_FILE=app/feature/feature-help-center/src/commonMain/kotlin/com/hedvig/android/feature/help/center/HelpCenterEntries.kt
ROOT_FILE=app/core/core-common-public/src/commonMain/kotlin/com/hedvig/android/core/common/daysUntil.kt
ROOT_DIR=app/core/core-common-public/src/commonMain/kotlin/com/hedvig/android/core/common

case "$SCENARIO" in
  none) ;;
  leaf-body) sed -i.bak 's/backstack.add(HelpCenterTopicKey(topicId = topicId))/backstack.add(HelpCenterTopicKey(topicId = topicId.trim()))/' "$LEAF_FILE" ;;
  root-body) sed -i.bak 's/  return startOfToday.daysUntil(startOfDayOfRenewal, timeZone)/  return startOfToday.daysUntil(startOfDayOfRenewal, timeZone) + 0 * 7/' "$ROOT_FILE" ;;
  root-abi) printf 'package com.hedvig.android.core.common\n\nval benchAbiMarker: Int = 1\n' > "$ROOT_DIR/BenchAbiMarker.kt" ;;
  *) echo "unknown scenario $SCENARIO"; exit 1 ;;
esac
rm -f "$LEAF_FILE.bak" "$ROOT_FILE.bak"
CHANGED=$(git status --porcelain | wc -l | tr -d ' ')
if [ "$SCENARIO" != none ] && [ "$CHANGED" -eq 0 ]; then
  echo "::error::scenario $SCENARIO did not change any file"
  exit 1
fi

if [ "$(uname)" = "Darwin" ]; then
  NPROC=$(sysctl -n hw.ncpu)
  sample() {
    echo "$(vm_stat | awk -v ps="$(pagesize)" '/Pages active/ {a=$3} /Pages wired down/ {w=$4} /occupied by compressor/ {c=$5} END {gsub("\\.","",a); gsub("\\.","",w); gsub("\\.","",c); print int((a+w+c)*ps/1048576)}') $(sysctl -n vm.swapusage | sed -E 's/.*used = ([0-9]+).*/\1/')"
  }
else
  NPROC=$(nproc)
  sample() {
    awk '/MemTotal/ {t=$2} /MemAvailable/ {a=$2} /SwapTotal/ {st=$2} /SwapFree/ {sf=$2} END {print int((t-a)/1024), int((st-sf)/1024)}' /proc/meminfo
  }
fi
( while true; do sample; sleep 2; done ) > "$OUT/mem" &
SAMPLER=$!
START=$(date +%s)
# shellcheck disable=SC2086
./gradlew $TASKS --continue > "$OUT/build.log" 2>&1
STATUS=$?
END=$(date +%s)
kill "$SAMPLER"
PEAK=$(awk 'BEGIN {m=0} $1>m {m=$1} END {print m}' "$OUT/mem")
PEAK_SWAP=$(awk 'BEGIN {m=0} $2>m {m=$2} END {print m}' "$OUT/mem")
STATS=$(grep -E 'actionable tasks' "$OUT/build.log" | tail -1)
printf '{"config":"%s","task":"%s","scenario":"%s","rep":"%s","seconds":%d,"exit":%d,"stats":"%s","nproc":%s,"peak_mem_mb":%s,"peak_swap_mb":%s}\n' \
  "$BENCH_CONFIG" "$BENCH_TASK" "$SCENARIO" "$BENCH_REP" $((END - START)) "$STATUS" "$STATS" "$NPROC" "$PEAK" "$PEAK_SWAP" | tee "$OUT/result.json"
grep -E 'BUILD (SUCCESSFUL|FAILED)|What went wrong' -A2 "$OUT/build.log" | head -6
exit "$STATUS"
