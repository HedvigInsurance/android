#!/usr/bin/env bash
#
# Cuts a production release with git flow, the same way it is done by hand:
#
#   git flow release start X.Y.Z
#   bump versionName, commit "Bump version"
#   git push origin release/X.Y.Z           # triggers upload-to-play-store.yml
#   git flow release finish X.Y.Z           # default merge messages, tag message "X.Y.Z"
#   git push origin develop master X.Y.Z
#
# Usage: scripts/release.sh <major|minor|patch|X.Y.Z> [--dry-run] [--yes]
#
#   --dry-run  Print the version bump and what would ship, then stop.
#   --yes      Skip the confirmation prompt (for non-interactive use).
#
# If the Play Store build fails, fix it on develop and run this again with `patch`.

set -euo pipefail

GRADLE_FILE="app/app/build.gradle.kts"
REMOTE="origin"

die() { echo "error: $*" >&2; exit 1; }
step() { echo; echo "==> $*"; }

bump=""
dry_run=false
assume_yes=false
for arg in "$@"; do
  case "$arg" in
    --dry-run) dry_run=true ;;
    --yes) assume_yes=true ;;
    major | minor | patch) bump="$arg" ;;
    [0-9]*.[0-9]*.[0-9]*) bump="$arg" ;;
    *) die "unknown argument '$arg'. Usage: scripts/release.sh <major|minor|patch|X.Y.Z> [--dry-run] [--yes]" ;;
  esac
done
[ -n "$bump" ] || die "missing version. Usage: scripts/release.sh <major|minor|patch|X.Y.Z> [--dry-run] [--yes]"

cd "$(git rev-parse --show-toplevel)"
command -v git-flow > /dev/null || die "git-flow is not installed (brew install git-flow)"
[ "$(git config --get gitflow.branch.master)" = "master" ] || die "git flow is not initialised in this clone, run 'git flow init -d'"
[ -z "$(git config --get gitflow.prefix.versiontag)" ] || die "git flow version tag prefix must be empty, tags are bare X.Y.Z"

# Preflight: git flow finish refuses to run unless develop and master match origin,
# so check it all before anything is created or pushed.
step "Updating develop and master from $REMOTE"
[ "$(git branch --show-current)" = "develop" ] || die "not on develop"
[ -z "$(git status --porcelain)" ] || die "working tree is not clean"
git fetch --quiet "$REMOTE" develop master --tags
git merge --quiet --ff-only "$REMOTE/develop" || die "develop has diverged from $REMOTE/develop"
if [ "$(git rev-parse master)" != "$(git rev-parse "$REMOTE/master")" ]; then
  git fetch --quiet "$REMOTE" master:master || die "master has diverged from $REMOTE/master"
fi

current="$(sed -n 's/^ *versionName = "\([0-9]*\.[0-9]*\.[0-9]*\)"$/\1/p' "$GRADLE_FILE")"
[ -n "$current" ] || die "could not read versionName from $GRADLE_FILE"
IFS=. read -r major minor patch <<< "$current"
case "$bump" in
  major) next="$((major + 1)).0.0" ;;
  minor) next="$major.$((minor + 1)).0" ;;
  patch) next="$major.$minor.$((patch + 1))" ;;
  *) next="$bump" ;;
esac
[[ "$next" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || die "'$next' is not a version like 14.5.0"
[ "$next" != "$current" ] || die "$next is already the current version"
if git rev-parse --quiet --verify "refs/tags/$next" > /dev/null || [ -n "$(git ls-remote --tags "$REMOTE" "refs/tags/$next")" ]; then
  die "tag $next already exists"
fi
[ -z "$(git ls-remote --heads "$REMOTE" "release/$next")" ] || die "release/$next already exists on $REMOTE"

step "Releasing $current -> $next. Merged into develop since $current:"
if git rev-parse --quiet --verify "refs/tags/$current" > /dev/null; then
  git --no-pager log --first-parent --format='  %h %s' "$current..develop"
else
  echo "  (no tag $current to compare against)"
fi

if $dry_run; then
  echo
  echo "Dry run, nothing was changed."
  exit 0
fi
if ! $assume_yes; then
  echo
  read -r -p "Start release $next? [y/N] " answer
  [ "$answer" = "y" ] || [ "$answer" = "Y" ] || die "aborted"
fi

step "git flow release start $next"
git flow release start "$next"

step "Bumping versionName to $next"
sed -i '' "s/^\( *versionName = \)\"$current\"$/\1\"$next\"/" "$GRADLE_FILE"
[ "$(git diff --numstat -- "$GRADLE_FILE")" = "$(printf '1\t1\t%s' "$GRADLE_FILE")" ] || die "unexpected diff in $GRADLE_FILE, check it by hand"
git commit --quiet -m "Bump version" -- "$GRADLE_FILE"

step "Pushing release/$next (starts the Play Store upload)"
git push "$REMOTE" "release/$next"

step "git flow release finish $next"
# GIT_MERGE_AUTOEDIT=no keeps the default merge messages without opening an editor.
# -n skips git flow's own tagging: in git flow 0.4.1, `-m 14.5.0` produces the tag
# message '14.5.0' with literal quotes, so the tag is created below instead.
GIT_MERGE_AUTOEDIT=no git flow release finish -n "$next"
git tag -a -m "$next" "$next" master

step "Pushing develop, master and tag $next"
git push --atomic "$REMOTE" develop master "refs/tags/$next"

echo
echo "Released $next. Follow the upload with:"
echo "  gh run list --workflow=upload-to-play-store.yml --branch release/$next --limit 1"
