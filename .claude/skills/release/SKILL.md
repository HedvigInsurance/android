---
name: release
description: Cut a production release of the Android app (git flow release, version bump, push to trigger the Play Store upload). Use when asked to cut, start or ship a release, bump the app version for production, or retry a release after a failed Play Store build.
---

# Cutting a release

`scripts/release.sh` does the whole release. This skill is about running it with the
user in the loop, because it pushes to `master`, `develop` and a `release/` branch.

1. **Preview.** From the repo root, on a clean `develop`, run
   `scripts/release.sh <bump> --dry-run`, where `<bump>` is what the user asked for.
   If they did not say, preview with `minor` and let them choose. The output lists
   every merge since the last release.
2. **Agree on the version.** Show the user the version change and the list of merges.
   Patch, minor or major is their call. Suggest one if asked, but a wrong pick is
   harmless, so do not dwell on it. Also point out anything in the list that looks
   unfinished or unexpected.
3. **Get a clear yes in chat** for that exact version. Then run
   `scripts/release.sh <X.Y.Z> --yes`, passing the exact version agreed on, not the
   bump keyword.
4. **Follow CI.** The push to `release/X.Y.Z` starts `upload-to-play-store.yml`. Run
   `gh run watch "$(gh run list --workflow=upload-to-play-store.yml --branch release/X.Y.Z --limit 1 --json databaseId -q '.[0].databaseId')" --exit-status`
   in the background and report the result. After a green run, the Play Console part
   is the user's.

## If something fails

- **The script refuses before step 3 changes anything** (tree not clean, not on
  `develop`, diverged branch, tag already exists): fix the cause and rerun.
- **The Play Store build fails:** the release is still finished and tagged, as it is
  when done by hand. Fix it on `develop` through a normal PR, then cut the next
  version with `patch`.
- **The script stops partway through:** do not rerun it. Check which of
  `release/X.Y.Z`, the tag and the `develop`/`master` merges exist locally and on
  `origin`, then show the user and finish the remaining steps by hand. The header of
  `scripts/release.sh` lists them.
