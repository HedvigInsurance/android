"""Prints the bench matrices as GitHub Actions outputs. `smoke` runs one cheap cell per OS."""
import json
import sys

ANDROID_CONFIGS = {
    "8c-current": ("ubuntu-latest-8-vcpu", "current"),
    "8c-uncapped": ("ubuntu-latest-8-vcpu", "uncapped"),
    "4c-current": ("ubuntu-latest", "current"),
    "4c-uncapped": ("ubuntu-latest", "uncapped"),
    "4c-uncapped-inc": ("ubuntu-latest", "uncapped-inc"),
}
TASKS = {
    "unit_test": "test jvmTest",
    "lint": "lint",
    "build": ":app:bundleDebug",
    "ktlint": "ktlintCheck",
    "all": "test jvmTest lint ktlintCheck :app:bundleDebug",
}
UMBRELLA_CONFIGS = {
    "xl-current": ("macos-26-xlarge", "current"),
    "xl-uncapped": ("macos-26-xlarge", "uncapped"),
    "intel-current": ("macos-26-intel", "current"),
    "intel-uncapped": ("macos-26-intel", "uncapped"),
    "arm3-lowheap": ("macos-26", "uncapped-lowheap"),
}

smoke = sys.argv[1] == "smoke"
android_reps = [1] if smoke else [1, 2, 3]
umbrella_reps = [1] if smoke else [1, 2]
android = [
    {"config": c, "runner": r, "props": p, "task": t, "gradle": g, "rep": n}
    for n in android_reps
    for c, (r, p) in ANDROID_CONFIGS.items()
    for t, g in TASKS.items()
    if not smoke or (c, t) == ("4c-current", "ktlint")
]
umbrella = [
    {"config": c, "runner": r, "props": p, "rep": n}
    for n in umbrella_reps
    for c, (r, p) in UMBRELLA_CONFIGS.items()
    if not smoke or c == "arm3-lowheap"
]
print(f"android={json.dumps({'include': android})}")
print(f"umbrella={json.dumps({'include': umbrella})}")
