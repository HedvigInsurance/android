#!/usr/bin/env bash
# OSS license gate for the Android app.
#
# Reads the jaredsburrows `licenseReleaseReport` JSON — which is build-resolved, so it
# reflects the app's real, complete dependency closure (unlike a manifest scanner).
# Android ships to end-user devices (a distribution context), so weak copyleft
# (LGPL/MPL/EPL) is blocked alongside strong/network copyleft and source-available.
#
# Usage: license-gate.sh <report.json> [enforce]
#   enforce=true  -> exit 1 on violations (blocking)
#   enforce=false -> warn only, always exit 0 (pilot; default)
set -euo pipefail

REPORT="${1:?usage: license-gate.sh <licenseReleaseReport.json> [enforce]}"
ENFORCE="${2:-false}"

if [[ ! -f "$REPORT" ]]; then
  echo "::warning::license report not found at $REPORT (did licenseReleaseReport run?) — skipping gate"
  exit 0
fi

# jaredsburrows reports free-text license NAMES, not SPDX ids, so match by pattern.
# Blocked for a distributed app = any copyleft + source-available license.
BLOCK_RE='Affero|AGPL|Lesser General Public|LGPL|GNU General Public|GPLv|GPL-2|GPL-3|Mozilla Public|MPL-|Eclipse Public|EPL-|Common Development and Distribution|CDDL|Server Side Public|SSPL|Business Source|BUSL|Commons Clause'

total=$(jq 'length' "$REPORT")
blocked=$(jq -r --arg re "$BLOCK_RE" '
  .[] | .dependency as $d | (.licenses // [])
  | .[]? | select(.license | test($re; "i")) | "\($d) :: \(.license)"' "$REPORT" | sort -u)
unknown=$(jq -r '.[] | select((.licenses // []) | length == 0) | .dependency' "$REPORT" | sort -u)

n_blocked=$([[ -n "$blocked" ]] && grep -c . <<<"$blocked" || echo 0)
n_unknown=$([[ -n "$unknown" ]] && grep -c . <<<"$unknown" || echo 0)

echo "### License gate"
echo "Dependencies scanned: $total"
echo "Blocked (copyleft / source-available): $n_blocked"
[[ -n "$blocked" ]] && sed 's/^/  - /' <<<"$blocked"
echo "No detected license: $n_unknown"
[[ -n "$unknown" ]] && sed 's/^/  - /' <<<"$unknown" | head -20

if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
  {
    echo "### License gate"
    echo "- Dependencies: $total"
    echo "- Blocked (copyleft / source-available): $n_blocked"
    echo "- No detected license: $n_unknown"
    [[ -n "$blocked" ]] && { echo; echo '```'; echo "$blocked"; echo '```'; }
  } >> "$GITHUB_STEP_SUMMARY"
fi

if [[ -n "$blocked" && "$ENFORCE" == "true" ]]; then
  echo "::error::Disallowed licenses present. Remove/replace, or record an Engineering-Lead-approved exception."
  exit 1
fi
echo "OK ($([[ "$ENFORCE" == "true" ]] && echo enforcing || echo 'warn-only'))"
