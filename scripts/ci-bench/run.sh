#!/usr/bin/env bash
# Throwaway CI benchmark. Runs one Gradle invocation several times on the same runner, each time
# from a fresh checkout state, to compare runner sizes and Gradle properties under different
# cache states and change shapes. Results go to $BENCH_OUT/results.jsonl.
#
# Phases, in order:
#   cold        nothing in ~/.gradle at all (dependencies, transforms, build cache all empty)
#   deps-warm   dependencies and transforms present, build cache empty. Close to what PR jobs
#               get today, since their restored cache rarely holds matching task outputs.
#   none        build cache holds the outputs of the base commit, nothing changed
#   leaf-nonabi / leaf-abi / root-nonabi / root-abi
#               build cache holds the base outputs, one module changed
#   downloaded  nothing changed by hand, but the schema and strings are downloaded first, as the
#               PR workflow's setup does. Shows what drift from the committed files costs.
# Every phase starts from `git clean`, so build dirs and the project's configuration cache are gone,
# which is what a fresh CI checkout looks like.
set -u

TASKS="$1"
OUT="${BENCH_OUT:?}"
mkdir -p "$OUT"
BASE=$(git rev-parse HEAD)
ROOT_FILE=app/core/core-common-public/src/commonMain/kotlin/com/hedvig/android/core/common/daysUntil.kt
ROOT_DIR=app/core/core-common-public/src/commonMain/kotlin/com/hedvig/android/core/common
LEAF_FILE=app/feature/feature-help-center/src/commonMain/kotlin/com/hedvig/android/feature/help/center/HelpCenterEntries.kt
LEAF_DIR=app/feature/feature-help-center/src/commonMain/kotlin/com/hedvig/android/feature/help/center

if [ "$(uname)" = "Darwin" ]; then
  NPROC=$(sysctl -n hw.ncpu)
  MEM_TOTAL_MB=$(( $(sysctl -n hw.memsize) / 1048576 ))
else
  NPROC=$(nproc)
  MEM_TOTAL_MB=$(( $(awk '/MemTotal/ {print $2}' /proc/meminfo) / 1024 ))
fi

mem_used_mb() {
  if [ "$(uname)" = "Darwin" ]; then
    vm_stat | awk -v ps="$(pagesize)" '
      /Pages active/ {a=$3} /Pages wired down/ {w=$4} /occupied by compressor/ {c=$5}
      END {gsub("\\.","",a); gsub("\\.","",w); gsub("\\.","",c); print int((a+w+c)*ps/1048576)}'
  else
    awk '/MemTotal/ {t=$2} /MemAvailable/ {a=$2} END {print int((t-a)/1024)}' /proc/meminfo
  fi
}

swap_used_mb() {
  if [ "$(uname)" = "Darwin" ]; then
    sysctl -n vm.swapusage | sed -E 's/.*used = ([0-9.]+)M.*/\1/' | cut -d. -f1
  else
    awk '/SwapTotal/ {t=$2} /SwapFree/ {f=$2} END {print int((t-f)/1024)}' /proc/meminfo
  fi
}

clean_tree() {
  git reset -q --hard "$BASE"
  git clean -q -ffdx -e lokalise.properties
}

apply_change() {
  case "$1" in
    leaf-nonabi) printf '\nprivate fun benchNonAbi(): Int = 1\n' >> "$LEAF_FILE" ;;
    leaf-abi) printf 'package com.hedvig.android.feature.help.center\n\nval benchAbiMarker: Int = 1\n' > "$LEAF_DIR/BenchAbiMarker.kt" ;;
    root-nonabi) printf '\nprivate fun benchNonAbi(): Int = 1\n' >> "$ROOT_FILE" ;;
    root-abi) printf 'package com.hedvig.android.core.common\n\nval benchAbiMarker: Int = 1\n' > "$ROOT_DIR/BenchAbiMarker.kt" ;;
    downloaded)
      # What the PR workflow's setup does on top of the committed schema and strings.
      ./gradlew downloadOctopusApolloSchemaFromIntrospection downloadStrings > "$OUT/downloaded-setup.log" 2>&1
      git status --porcelain > "$OUT/downloaded-changes.txt"
      echo "downloads changed $(wc -l < "$OUT/downloaded-changes.txt") files"
      ;;
  esac
}

run_phase() {
  local phase="$1"
  local log="$OUT/$phase.log"
  local peak=0 peak_swap=0
  ( while true; do echo "$(mem_used_mb) $(swap_used_mb)"; sleep 2; done ) > "$OUT/$phase.mem" &
  local sampler=$!
  local start end status
  start=$(date +%s)
  # shellcheck disable=SC2086
  ./gradlew $TASKS --continue > "$log" 2>&1
  status=$?
  end=$(date +%s)
  kill "$sampler" 2>/dev/null
  wait "$sampler" 2>/dev/null
  peak=$(awk 'BEGIN {m=0} $1>m {m=$1} END {print m}' "$OUT/$phase.mem")
  peak_swap=$(awk 'BEGIN {m=0} $2>m {m=$2} END {print m}' "$OUT/$phase.mem")
  local stats
  stats=$(grep -E 'actionable tasks' "$log" | tail -1 | tr -d '"')
  printf '{"config":"%s","runner":"%s","task":"%s","rep":"%s","phase":"%s","seconds":%d,"exit":%d,"stats":"%s","nproc":%s,"mem_total_mb":%s,"peak_mem_mb":%s,"peak_swap_mb":%s}\n' \
    "$BENCH_CONFIG" "$BENCH_RUNNER" "$BENCH_TASK" "$BENCH_REP" "$phase" $((end - start)) "$status" "$stats" \
    "$NPROC" "$MEM_TOTAL_MB" "$peak" "$peak_swap" | tee -a "$OUT/results.jsonl"
  grep -E 'BUILD (SUCCESSFUL|FAILED)|OutOfMemory|daemon disappeared|Killed' "$log" | head -5
  [ "$status" -eq 0 ] || FAILED_PHASES="$FAILED_PHASES $phase"
}

FAILED_PHASES=""

# Download the wrapper distribution outside the timed phases.
./gradlew --version > /dev/null

run_phase cold

clean_tree
rm -rf ~/.gradle/caches/build-cache-1
run_phase deps-warm

for scenario in none downloaded leaf-nonabi leaf-abi root-nonabi root-abi; do
  clean_tree
  apply_change "$scenario"
  run_phase "$scenario"
done
clean_tree

if [ -n "$FAILED_PHASES" ]; then
  echo "::error::Gradle failed in:$FAILED_PHASES"
  exit 1
fi
