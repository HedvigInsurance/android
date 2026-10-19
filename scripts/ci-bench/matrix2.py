"""Matrix for the warm-cache bench: every PR-shaped job restores the cache the seed job wrote."""
import json
import sys

CONFIGS = {
    "8c-w2": ("ubuntu-latest-8-vcpu", "current"),
    "8c-w8": ("ubuntu-latest-8-vcpu", "uncapped"),
    "4c-w2": ("ubuntu-latest", "current"),
    "4c-w4": ("ubuntu-latest", "uncapped"),
}
TASKS = {
    "unit_test": "test jvmTest",
    "lint": "lint",
    "build": ":app:bundleDebug",
    "ktlint": "ktlintCheck",
    "all": "test jvmTest lint ktlintCheck :app:bundleDebug",
}
SCENARIOS = ["none", "leaf-body", "root-body", "root-abi"]

smoke = sys.argv[1] == "smoke"
cells = [
    {"config": c, "runner": r, "props": p, "task": t, "gradle": g, "scenario": s, "rep": n}
    for n in ([1] if smoke else [1, 2, 3])
    for s in SCENARIOS
    for c, (r, p) in CONFIGS.items()
    for t, g in TASKS.items()
    if not smoke or (c, t, s) in {("4c-w4", "ktlint", "leaf-body")}
]
print(f"cells={json.dumps({'include': cells})}")
