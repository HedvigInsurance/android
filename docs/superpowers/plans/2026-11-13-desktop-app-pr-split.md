# Task 38: how to split the desktop integration branch into PRs

Read-only analysis. Fixed points: base `229bd79df0` (merge-base of `feat/desktop-app-integration` with
`origin/develop`), tip `bb772ba399`. Nothing was pushed, committed, rebased or built. Every number below comes from
git on those two commits; the scripts are in the session scratchpad (`t38/diffstat.py`, `groups.py`, `plan.py`,
`planR.py`).

**Staleness of `origin/develop`.** `git log --oneline 229bd79df0..origin/develop | wc -l` is 0: the local
`origin/develop` ref still points at the merge-base itself, `229bd79df0` (commit date Wed 11 Nov 2026 12:00 +0100,
"Merge pull request #3357"). The last fetch (`.git/FETCH_HEAD`) was Thu 12 Nov 17:51. So, as far as this clone knows,
develop has not moved, but the ref is about two days old and the real develop is unknown. I did not fetch. Everything
in section 3.8 about conflicts is therefore based on develop's history *before* the base (churn) plus the known open
PR 3305, not on what develop did in the last two days.

**How lines are classified.** "Changed lines" is added plus removed lines of `git diff -M -l0` (rename detection on,
no limit). Then:

- *Pure moves*: files git reports as R100. They contribute 0 lines.
- *Build files*: `*.gradle.kts` and `libs.versions.toml`. These follow one template per module type and are treated
  as mechanical.
- *Imports*: lines that are only `import ...` or `package ...`.
- *Relocated blocks*: lines `git diff --color-moved=plain --color-moved-ws=allow-indentation-change` marks as moved
  (a block of at least 20 alphanumeric characters that appears removed in one place and added in another). This is a
  heuristic: it also marks some lines of new test files that happen to match removed lines, so it overstates
  relocation a little and "fresh" below is a lower bound.
- *Real*: changed lines minus build-file lines minus import lines. This is what a reviewer reads line by line or at
  least skims.
- *Fresh*: real minus relocated blocks. This is new or rewritten code.

---

## 1. The diff, as a reviewer would experience it

### 1.1 Totals (`git diff -M 229bd79df0 bb772ba399`)

| | Files | Lines |
|---|---|---|
| Moved unchanged (R100) | 585 | 0 |
| Moved with edits (R50 to R99) | 69 | in the totals below |
| Modified in place | 98 | |
| Added | 158 | |
| Deleted | 6 (five empty `AndroidManifest.xml`, `NoopFeatureFlagProvider.kt`) | |
| **All** | **916** | **+10,535 / -2,261 = 12,796** |
| Raw unified diff with renames | | 20,517 diff lines, 1.06 MB |
| Raw unified diff without rename detection | | 182,407 diff lines, 7.0 MB |

Where the 12,796 changed lines go:

| Category | Lines | Share |
|---|---|---|
| Build files (67 `build.gradle.kts`, 11 of them new, plus `libs.versions.toml`) | 2,753 | 21.5% |
| Import and package lines | 1,933 | 15.1% |
| Relocated blocks (heuristic) | 3,626 | 28.3% |
| Fresh, tests (136 test files touched) | 2,252 | 17.6% |
| Fresh, production | 2,232 | 17.4% |
| **Real** (everything except build files and imports) | **8,110** | 63.4% (3,405 of it tests) |

Plus 585 files that are pure renames, which GitHub collapses to one line each.

So, measured in what a person has to read:

- **Mechanical, can be trusted after a spot check**: 585 pure moves, 2,753 build-file lines, 1,933 import lines.
  About 37% of changed lines and 64% of files.
- **Skim**: about 3,600 relocated lines (code moved between files, typically from `src/main` into an `expect` plus
  `androidMain`/`jvmMain` actuals, or out of `HedvigEntryProvider.kt` into host blocks). 28%.
- **Read**: about 4,500 fresh lines, half of them tests. 35%. Production code a reviewer must actually reason about
  is roughly 2,200 lines, of which about 1,000 are desktop-only (`:app-desktop`).

### 1.2 By area

| Area | Files | R100 | R<100 | M | A | D | +/- | Build | Imports | Relocated | Fresh (of which tests) |
|---|---|---|---|---|---|---|---|---|---|---|---|
| Libraries (`app/core`, `design-system`, `featureflags`, `language`, `compose`, `network`, `auth`, `data`, ...) | 326 | 206 | 8 | 63 | 45 | 4 | +3,434 / -519 | 742 | 593 | 1,086 | 1,532 (901) |
| Feature modules (18 touched) | 518 | 362 | 52 | 21 | 81 | 2 | +3,859 / -1,357 | 1,680 | 833 | 1,409 | 1,294 (634) |
| Desktop host `:app-desktop` (new) | 25 | 0 | 0 | 0 | 25 | 0 | +2,118 / -0 | 190 | 364 | 540 | 1,024 (546) |
| build-logic | 6 | 0 | 0 | 4 | 2 | 0 | +415 / -0 | 3 | 60 | 90 | 262 (90) |
| App shell `:app-shell` (new, mostly moved from `:app`) | 30 | 17 | 9 | 0 | 4 | 0 | +560 / -299 | 118 | 63 | 436 | 242 (81) |
| Android app `:app` (what stays) | 4 | 0 | 0 | 4 | 0 | 0 | +69 / -43 | 9 | 20 | 58 | 25 (0) |
| Docs in the branch (`CLAUDE.md`, `navigation-and-di.md`) | 3 | 0 | 0 | 3 | 0 | 0 | +49 / -38 | 0 | 0 | 6 | 81 (0) |
| CI (`pr.yml`) | 1 | 0 | 0 | 1 | 0 | 0 | +8 / -4 | 0 | 0 | 0 | 12 (0) |
| Root build files (`libs.versions.toml`, `settings`, allow-list) | 3 | 0 | 0 | 2 | 1 | 0 | +23 / -1 | 11 | 0 | 1 | 12 (0) |

Tests across all areas: 136 files, 3,405 real lines, 2,252 fresh.

Per feature module (rows with any change):

| Feature | Files | R100 | R<100 | M | A | D | +/- | Build | Imports | Relocated | Fresh (tests) |
|---|---|---|---|---|---|---|---|---|---|---|---|
| feature-chat | 70 | 29 | 11 | 1 | 29 | 0 | +1,194 / -307 | 163 | 318 | 492 | 528 (233) |
| feature-login | 35 | 14 | 8 | 1 | 12 | 0 | +394 / -63 | 141 | 85 | 100 | 131 (95) |
| feature-claim-details | 17 | 6 | 5 | 1 | 5 | 0 | +298 / -112 | 118 | 79 | 84 | 129 (64) |
| feature-home | 27 | 21 | 2 | 1 | 3 | 0 | +291 / -80 | 171 | 35 | 65 | 100 (69) |
| feature-connect-payment-trustly | 21 | 16 | 1 | 1 | 3 | 0 | +273 / -91 | 82 | 68 | 126 | 88 (59) |
| feature-onboarding | 61 | 46 | 4 | 1 | 10 | 0 | +294 / -98 | 146 | 49 | 116 | 81 (70) |
| feature-profile | 54 | 40 | 5 | 1 | 8 | 0 | +406 / -193 | 152 | 109 | 267 | 71 (0) |
| feature-claim-chat | 10 | 3 | 0 | 5 | 2 | 0 | +110 / -22 | 6 | 18 | 56 | 52 (23) |
| feature-travel-certificate | 30 | 23 | 5 | 1 | 0 | 1 | +72 / -74 | 87 | 15 | 5 | 39 (0) |
| feature-image-viewer | 8 | 2 | 1 | 1 | 4 | 0 | +127 / -30 | 67 | 22 | 31 | 37 (17) |
| feature-payin-account | 42 | 37 | 1 | 1 | 3 | 0 | +126 / -56 | 97 | 13 | 54 | 18 (4) |
| feature-insurance-certificate | 17 | 13 | 3 | 1 | 0 | 0 | +50 / -45 | 72 | 7 | 2 | 14 (0) |
| feature-terminate-insurance | 34 | 28 | 4 | 1 | 0 | 1 | +51 / -52 | 90 | 7 | 0 | 6 (0) |
| feature-insurances | 37 | 33 | 1 | 1 | 2 | 0 | +89 / -57 | 128 | 7 | 11 | 0 (0) |
| feature-payments | 48 | 46 | 1 | 1 | 0 | 0 | +64 / -62 | 125 | 1 | 0 | 0 (0) |
| feature-forever | 4 | 3 | 0 | 1 | 0 | 0 | +20 / -13 | 33 | 0 | 0 | 0 (0) |
| feature-claim-history | 2 | 1 | 0 | 1 | 0 | 0 | +0 / -2 | 2 | 0 | 0 | 0 (0) |
| feature-remove-addons | 1 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 (0) |

The `-navigation` modules count under their feature here. Seven feature conversions (home, insurances, payments,
forever, payin-account, terminate-insurance, insurance-certificate) carry almost no code change at all: 0 to 18 fresh
lines each. The four that carry real seams are chat, login, claim details and Trustly.

### 1.3 The ten files with the most real change

By fresh lines (new or rewritten code, imports and relocated blocks removed):

| File | Status | +/- | Fresh | Kind |
|---|---|---|---|---|
| `build-logic/convention/src/main/kotlin/com/hedvig/android/CommonMainImportGuard.kt` | A | +231 / -0 | 155 | prod |
| `app/featureflags/feature-flags/src/jvmTest/.../JvmUnleashClientTest.kt` | A | +207 / -0 | 121 | test |
| `app/app-desktop/src/jvmTest/.../DesktopBackTest.kt` | A | +217 / -0 | 120 | test |
| `app/app-desktop/src/jvmTest/.../DesktopEntryProviderTest.kt` | A | +193 / -0 | 108 | test |
| `build-logic/convention/src/test/kotlin/com/hedvig/android/CommonMainImportGuardTest.kt` | A | +155 / -0 | 90 | test |
| `app/core/core-file-upload/src/jvmTest/.../OpenPdfTest.kt` | A | +153 / -0 | 88 | test |
| `app/featureflags/feature-flags/src/jvmMain/.../JvmUnleashClient.kt` | A | +168 / -0 | 88 | prod |
| `app/app-desktop/src/jvmMain/.../Main.kt` | A | +143 / -0 | 85 | prod |
| `app/app-desktop/src/jvmTest/.../DesktopUnmatchedLinkRouterTest.kt` | A | +159 / -0 | 84 | test |
| `app/app-desktop/src/jvmTest/.../DesktopGraphTest.kt` | A | +129 / -0 | 80 | test |

By real lines (relocation included), the two files that matter most for Android review are not in that list because
much of their change is relocation:

| File | Status | +/- | Real | Fresh |
|---|---|---|---|---|
| `app/{app/src/main => app-shell/src/commonMain}/.../navigation/HedvigEntryProvider.kt` | R081 | +175 / -221 | 394 | 74 |
| `app/feature/feature-chat/src/{main => commonMain}/.../ChatLoadedScreen.kt` | R088 | +79 / -146 | 188 | 77 |
| `app/feature/feature-chat/src/androidMain/.../ui/ChatVideoMessage.android.kt` | A | +170 / -0 | 130 | mostly relocated from `ChatLoadedScreen.kt`; GitHub shows it as new |

`HedvigEntryProvider.kt` is also the second-hottest file on develop (section 3.8). `BackstackController.kt` (R097),
`HedvigApp.kt` and `HomeDestination.kt` are moved with small edits.

### 1.4 How GitHub will display it

Limits as I know them from GitHub's documentation ("About comparing branches in pull requests", diff limits). I could
not re-check them in this read-only session, so treat the exact values as from memory:

- A whole PR diff stops loading past **20,000 diff lines or 1 MB of raw diff**; the remaining files show
  "Load diff" or are not rendered.
- One file's diff: 20,000 lines or 500 KB maximum; only the first **400 lines / 20 KB** of a file load
  automatically.
- At most **3,000 files** per diff; the commits tab lists at most 250 commits.
- Renames: GitHub uses git's rename detection. Exact renames (R100) are found by content hash, so they are cheap and
  always shown as "File renamed without changes". Inexact renames depend on git's similarity threshold (50% by
  default) and rename limit; here the inexact candidate set is small (69 sources and 6 deletions against about 230
  destinations), far below git's default limit of 1,000. I know of no separate, lower GitHub threshold for this, but I
  could not verify that GitHub never falls back to add plus delete on very large PRs.

What that means:

- **The whole branch as one PR**: 916 files and 20,517 diff lines / 1.06 MB with renames. That is at the
  documented 20,000-line / 1 MB total, so GitHub would render part of the diff and leave the rest behind "Load diff"
  or unrendered. 152 commits plus 48 merge commits = 200 entries, under the 250-commit list limit. If renames were
  not detected it would be 182,000 lines, hopeless.
- **Every proposed PR below is well inside the limits.** The largest in the recommended split by diff lines is R6
  (1,582) and by files R8 (279) and R5 (249, of which 205 pure renames). In the thematic plan the largest is B7
  (3,324 diff lines) and B2 (302 files). No proposed PR comes near 20,000 lines, 1 MB or 3,000 files.
- Single files over the 400-line auto-load: none in the net diff (the largest, `HedvigEntryProvider.kt`, is 396 lines
  of diff), so every file renders without a click.
- GitHub does not highlight moved blocks. Relocated code such as `ChatVideoMessage.android.kt` reads as brand-new.
  Reviewers who want the relocation view should run `git diff --color-moved=zebra` locally; say so in the PR text.

---

## 2. Commit hygiene

### 2.1 Shape of the history

- 48 `--no-ff` merges on the first-parent chain; 3 branches were merged twice (`fix/molecule-jvm-frame-clock`,
  `build/commonmain-import-guard`, `feat/share-pdf-seam`), so 45 distinct branches.
- 152 non-merge commits: 150 on branches, 2 made directly on the integration branch (`eec1697dcf`, `ecaf450c25`,
  test fixes needed only because of merge order).
- One merge inside a branch: `refactor/kmp-features-entries-common` merged `feat/not-available-on-desktop` into
  itself (`9bf4510031`).
- One conflict resolution inside an integration merge: `150f117a53` (`refactor/kmp-libs-data`), in
  `app/auth/auth-core-public/build.gradle.kts`: `test-clock` was kept only in `jvmTest.dependencies`
  (`git show --remerge-diff 150f117a53`). It exists only in that merge commit, so any cherry-pick-based split must redo
  it.
- The first 28 merges say "Merge branch 'x' into feat/desktop-app-integration", the rest "Merge x into the desktop
  integration branch". Cosmetic, but none of these 48 merges should reach develop.
- develop merges PRs with merge commits that keep every PR commit (the last 15 PR merges carried 1 to 8 commits
  each), so **every commit of a PR becomes develop history**. Commit hygiene matters here, unlike in a squash-merge
  repo.

### 2.2 Commits that do not build on their own

- **29 pure-move commits** (all files R100). 28 of them cannot compile by construction: they move a module's sources
  from `src/main` to `src/commonMain` while the build file still applies `hedvig.android.library` or
  `hedvig.jvm.library`, which do not compile `commonMain`, or they move Android-API code from `androidMain` into
  `commonMain` before the next commit replaces the Android calls. The exception is plausibly `771026bcf3` (remove
  addons entries), which no later commit edits. I did not compile any of them; the reasoning is from the build files at
  each commit's parent (checked for core-icons, home, chat, onboarding, the data modules and navigation-compose).
- **`1ed6ee5ce5`** (shared Apollo interceptors) says in its own message that `:app-desktop` does not compile at that
  commit.
- The ledger records the share-pdf pure moves as non-compiling (Task 23a minors) and `1ed6ee5ce5` (Task 27).

So about 30 of 152 commits (20%) break `git bisect` in the naive form. Because develop uses merge commits,
`git bisect --first-parent` steps over them, which makes this tolerable if the team knows to use it. Where the convert
commit edits only build files, squashing the move into it loses nothing: git still reports the moved sources as R100
in the squashed commit, and GitHub's PR view shows the same net diff either way. The pairs worth keeping separate are
the ones whose convert commit edits the moved files heavily (chat, login, profile, claim details, Trustly,
onboarding): there the pure-move commit is a real review aid in GitHub's per-commit view.

### 2.3 Per branch

"Move then convert" = a pure R100 commit followed by the conversion commit. "Fix rounds" = later commits in the same
branch that correct or reword an earlier one and would be better as `fixup!` squashes.

| # | Branch | Commits | Pure-move commits | Move then convert | Fix-round commits to squash | Message or structure issues |
|---|---|---|---|---|---|---|
| 1, 17 | `build/commonmain-import-guard` | 1 + 1 | 0 | n/a | none (`50356dd83d` extends the guard; fine as its own commit) | merged twice |
| 2 | `refactor/kmp-core-icons` | 2 | 1 | yes | | |
| 3 | `refactor/kmp-notification-permission` | 2 | 1 | yes | | |
| 4 | `feat/not-available-on-desktop` | 1 | 0 | n/a | | |
| 5 | `refactor/kmp-forever-ui` | 2 | 1 | yes | | |
| 6 | `refactor/common-email-validation` | 1 | 0 | n/a | | |
| 7 | `refactor/kmp-feature-insurance-certificate` | 2 | 1 | yes | | |
| 8 | `refactor/kmp-feature-terminate-insurance` | 2 | 1 | yes | | |
| 9 | `refactor/kmp-feature-forever` | 2 | 1 | yes | | |
| 10 | `refactor/kmp-feature-travel-certificate` | 2 | 1 | yes | | |
| 11, 16 | `fix/molecule-jvm-frame-clock` | 1 + 1 | 0 | n/a | `a67aea1628` into `9f99e51483` | merged twice |
| 12 | `refactor/navigation-compose-common` | 2 | 1 | yes | | |
| 13 | `feat/jvm-platform-bindings` | 8 | 0 | n/a | `d9161e85a6` into `6c85d038a0` | |
| 14 | `refactor/shell-keys-navigation-modules` | 5 | 0 | n/a | | one commit per key, good |
| 15 | `refactor/kmp-features-entries-common` | 5 | 3 | yes | | contains a merge of branch 4 |
| 18 | `refactor/kmp-member-reminders-ui` | 2 | 1 | yes | | |
| 19 | `refactor/kmp-feature-payin-account` | 2 | 1 | yes | | |
| 20 | `refactor/kmp-feature-payments` | 2 | 1 | yes | | |
| 21 | `refactor/kmp-feature-profile` | 6 | 1 | yes | `5d33cdc2aa`, `524b17429b` | carries two library commits (`10330a3da7` Language.label, `84d1b3432b` notification permission JVM) |
| 22 | `refactor/app-shell` | 12 | 0 | partly (`01bc1ba670` is 26 R100 plus the module) | `c365342f79`, `d30ca119eb`, `72330198d4`, `7eafd34417` (two review rounds, three of them doc/rule rewording) | carries three key-module commits that belong with branch 14; `14ae1facd0` is one +545/-405 commit on the hottest file |
| 23 | `refactor/shell-shared-entries` | 6 | 0 | n/a | `58865dcffe` (cleanup of fields added in 22) | |
| 24, 25 | `feat/share-pdf-seam` | 6 + 1 | 2 | yes | `6977434c8b` into `816b4236a8` | merged twice; moves do not compile (ledger) |
| 26 | `refactor/kmp-feature-home` | 2 | 1 | yes | | |
| 27 | `refactor/kmp-feature-login` | 3 | 1 | yes | | carries `60166f4af9` (network-clients TlsDiagnostics), a library change |
| 28 | `refactor/kmp-feature-insurances` | 2 | 1 | yes | | |
| 29 | `feat/app-desktop` | 7 | 0 | n/a | `59fdb90629` into `f237d78e38`; `6d9c8d85b5` | `6d9c8d85b5` "Harden the desktop host after review" is a six-bullet grab-bag with process wording; shared changes `f237d78e38` (core-build-constants) and `931a2c86cd` (HedvigApp fallback) hide in a desktop branch |
| 30 | `refactor/kmp-feature-onboarding` | 2 | 1 | yes | | |
| 31 | `refactor/shell-converted-entries` | 2 | 0 | n/a | | mixes desktop graph (`c959695369`) with the shared provider (`3cd18b2834`) |
| 32 | `fix/jvm-launcher-actuals` | 1 | 0 | n/a | | |
| 33 | `refactor/kmp-feature-image-viewer` | 2 | 1 | yes | | |
| 34 | `refactor/kmp-feature-claim-details` | 2 | 1 | yes | | |
| 35 | `refactor/kmp-feature-connect-payment-trustly` | 2 | 1 | yes | | needs direct fix `eec1697dcf` after 37 |
| 36 | `refactor/database-jvm-builder` | 1 | 0 | n/a | | |
| 37 | `refactor/shared-apollo-interceptors` | 4 | 0 | n/a | | `1ed6ee5ce5` does not compile `:app-desktop` (stated); broke two other branches' JVM graph tests, fixed by direct commits 38 and 41; mixes shell (`1ed6ee5ce5`, `419ec32f84`) and desktop (`11a829ca18`) |
| 38 | direct `eec1697dcf` | 1 | | | belongs in the PR of 37 | made on the integration branch |
| 39 | `refactor/shell-converted-entries-2` | 2 | 0 | n/a | | desktop graph plus shared provider again |
| 40 | `refactor/kmp-feature-chat` | 5 | 1 | yes | `22d80b7f7a` into `04ad4700e3` | `aedae07f0f` "Make the molecule ViewModel dispatcher public" also deletes `feature-chat/src/main/AndroidManifest.xml`, which the message does not mention; `04ad4700e3` is one +829/-251 commit |
| 41 | direct `ecaf450c25` | 1 | | | belongs in the PR of 37 | made on the integration branch |
| 42 | `fix/desktop-audit-fixes` | 4 | 0 | n/a | | grab-bag: desktop language store, take-photo availability (shared UI in claim chat, claim details, file-upload-ui), desktop update action, stray test deps |
| 43 | `refactor/shell-converted-entries-3` | 3 | 0 | n/a | `5fdfbf9d18` | desktop graph plus shared provider again |
| 44 | `refactor/kmp-libs-data` (stack of `-auth`, `-core`, `-leaves`, `-data`) | 11 | 4 | yes | | four stacked branches merged as one; conflict resolved only in merge `150f117a53` |
| 45 | `fix/chat-camera-availability` | 1 | 0 | n/a | | |
| 46 | `build/kmp-jvm-default` | 1 | 0 | n/a | | |
| 47 | `fix/desktop-ux-fixes` | 7 | 0 | n/a | `005a0b6d3b` into `956b5c9b9e` (must: the first ships an inverted PanMove branch); `97787c7a11` into `c46fef32df`; `c7b89d88d3` into `5bb5de7bab` | `5bb5de7bab` says a text field "keeps Cmd+Left and Alt+Left for moving the caret": wrong for Alt+Left on Windows and Linux (corrected only in the KDoc by `c7b89d88d3`); `956b5c9b9e`'s message describes the PanMove handling that `005a0b6d3b` removes; two shared Android-reaching fixes (Home, navigation bar) inside a desktop branch; `1d5eebc835` mixes a desktop router with a shared core-build-constants refactor |
| 48 | `fix/desktop-followups` | 6 | 0 | n/a | `cc36d03e29` into `bee2a343b5` (must: the first over-clears a supported language); `a0a4d00b5d` into `23050db0e3` | grab-bag: PermissionManager binding (reaches Android) next to desktop-only logging and file paths |
| 49 | `build/dx-test-task-and-guard` | 2 | 0 | n/a | | two unrelated build changes, each with value on develop today |
| 50 | `fix/chat-thumbnail-fallback` | 1 | 0 | n/a | | good |

Totals: 29 pure-move commits, 25 branches follow move then convert, about 21 fix-round commits (the 20 listed plus
`58865dcffe`), 2 direct commits, 2 commits that must be squashed because the earlier one is wrong on its own
(`956b5c9b9e`, `bee2a343b5`), 1 wrong commit message (`5bb5de7bab`), 1 message that omits a change (`aedae07f0f`),
1 grab-bag commit with process wording (`6d9c8d85b5`).

Messages otherwise are good: imperative subjects, bodies that say why, no em-dashes, honest about the one
intermediate commit that does not compile.

### 2.4 Verdict

**Not presentable as it is**, and **squashing per branch is the wrong fix**: it would erase the move-then-convert
pairs that make the feature conversions reviewable, and it would not fix the real problem, which is that branch
boundaries do not match PR boundaries. Desktop-only pieces sit inside shell branches (`shell-converted-entries`
1/2/3, `shared-apollo-interceptors`), shared Android-reaching fixes sit inside desktop fix branches
(`desktop-ux-fixes`, `desktop-audit-fixes`, `desktop-followups`), library changes ride in feature branches (profile,
login, chat), and three key-module commits sit in `refactor/app-shell`.

It needs **restructuring by cherry-pick, not a rewrite**: build each PR branch from develop by cherry-picking the
listed commits in order (the commit lists are in `plan.py` and in section 3.5), autosquash the ~21 fix-round commits
into their targets, split about six commits across PRs (`1d5eebc835`, `aedae07f0f`, and the three shell-converted
entries branches are already split by commit; `0b567e17c2` and `808a6ee335` need a hunk dropped on develop, see
3.5), and redo the one recorded conflict resolution. Roughly 152 commits become about 125, almost all byte-for-byte
the originals, so the verification already done (Pixel smoke on 884ee23e96, R8 staging build, 1,151 tests) still
describes the end state, though not each intermediate PR.

**The worst five:**

1. `fix/desktop-ux-fixes`: a shared commit that is wrong on its own (inverted PanMove sign, `956b5c9b9e`), a commit
   message that misstates Alt+Left (`5bb5de7bab`), three fix rounds, and two Android-reaching shared fixes buried in a
   desktop branch.
2. `refactor/app-shell`: 12 commits of which 4 are review-round rewordings, 3 belong to another branch, and one
   (`14ae1facd0`, +545/-405) rewrites the hottest navigation file in a single step.
3. `refactor/kmp-libs-data`: four stacked branches merged as one, four non-compiling move commits, and the only
   conflict resolution of the whole effort living in an integration merge commit that will not travel.
4. `feat/app-desktop`: shared core-build-constants and HedvigApp changes hidden in a desktop branch, plus a
   six-topic "after review" grab-bag commit.
5. `fix/desktop-audit-fixes` with `fix/desktop-followups`: grab-bags mixing Android-reaching changes (stray test
   dependencies, take-photo availability, PermissionManager binding) with desktop-only ones, and a commit
   (`bee2a343b5`) that over-clears a supported language until the next one fixes it.

Runner-up: `refactor/shared-apollo-interceptors`, whose first commit does not compile and whose merge broke two other
branches' tests (fixed by direct commits on integration).

---

## 3. PR split plan

### 3.1 Constraints

- **CI passes per PR when merged in order.** `pr.yml` runs four parallel jobs on `ubuntu-latest-8-vcpu`:
  `unit_test` (`./gradlew test jvmTest`), `lint` (`./gradlew lint --continue`), `ktlint` (`ktlintCheck`, then
  `checkCommonMainImports`, the latter added by this work), and `build` (`:app:bundleDebug`, then
  `:app-desktop:compileKotlinJvm`, also added by this work). The guard step must arrive with or before the first PR
  that relies on it; the desktop compile step must arrive with `:app-desktop` (R16), not before.
- **Android behaviour unchanged at every step**, except the PRs that exist to change it on purpose (R3 navigation bar
  hover/focus, R12 Home wheel and chat thumbnail fallback, R2 release classpath). Those are behaviour changes and must
  say so.
- **One sitting.** I assume a reviewer can read about 600 real production lines plus their tests (about 1,000 real
  lines in total) in one sitting, with pure renames, build-file template rewrites and import lines spot-checked
  rather than read. A PR may be larger in files if the extra files are pure renames.
- **Pure moves separable.** Inside each PR, keep move-then-convert pairs as separate commits where the convert commit
  edits the moved files; GitHub's per-commit view then shows the move commit as renames only.
- **Independent value lands first and alone.** Guard plus `test` task, `jvmDefault`, stray test dependencies,
  navigation bar highlight fix, Home wheel fix, chat thumbnail fallback.
- **Stacked PRs cannot auto-merge.** A PR whose base is another PR's branch must be retargeted to develop after its
  parent lands before merge-when-ready can be armed.
- PR descriptions stay short (1000 characters for the generated part), so each PR below states its one "why".

### 3.2 The real dependency graph between the merged branches

Derived from three sources: which modules each branch converts or creates (build file plugin before and after), which
of those modules each later branch's build files depend on, and file overlap between branch diffs, checked against
the ledger's BASE for each task. File overlap alone overstates it (every branch that adds a module touches
`app/app/build.gradle.kts`), so edges that are only "both list a module in `:app`'s dependencies" were dropped.

Independent of everything (can land on develop today, any order):

- guard (1, 17, and `90db99d446` of 49), `test` task (`808a6ee335` of 49), `jvmDefault` (46)
- molecule JVM clock (11, 16) and `aedae07f0f` (dispatcher public)
- `isValidEmail` in commonMain (6), PermissionManager per platform (`fe4c92452d` of 48)
- navigation bar hover/focus (`c46fef32df`, `97787c7a11` of 47): `MappedInteractionSource.kt` is already in
  `commonMain` on develop and design-system already has a JVM target
- stray test dependencies (`0b567e17c2` of 42): develop's `feature-login` already has
  `implementation(projects.authCoreTest)` in main and `auth-core-public` has `test-clock` in main
- key modules (14 plus `4032460991`, `29c8a41e21`, `3a54d2d174` of 22): packages unchanged, so serialized names and
  Datadog view names do not change
- library conversions (2, 3, 5, 12, 18, 44, plus `10330a3da7`, `84d1b3432b`)
- JVM bindings and actuals (4, 13, 32, 36, plus the JVM-only commits of 27, 29, 42, 48)

Chains:

- certificates (7, 10) need `isValidEmail` (6); forever (9) needs forever-ui (5)
- claim chat / history / remove addons entries (15) need navigation-compose (12), notification-permission (3) and
  `NotAvailableOnDesktop` (4)
- payin-account and payments (19, 20) need notification-permission (3) and member-reminders-ui (18)
- profile (21) needs key modules (14), core-icons (2), notification-permission (3), member-reminders-ui (18),
  `Language.label`
- home (26) needs key modules (14), notification-permission (3), member-reminders-ui (18)
- login (27) needs key modules (14), `TlsDiagnostics` in commonMain (`60166f4af9`) and the JVM bindings (13) for
  `JvmLoginGraphTest`
- insurances (28) needs key modules (14); onboarding (30) needs key modules (14), forever-ui (5), JVM bindings (13)
- image viewer and claim details (33, 34) need the chat/image-viewer/claim-details key modules (from 22); claim
  details and Trustly (34, 35) need JVM bindings for their graph tests
- chat (40) needs the chat key module (from 22), molecule (11, `aedae07f0f`), database-jvm (36), JVM bindings (13)
- app shell (22) needs every feature its shared provider registers at that point: certificates, terminate, forever,
  payin, payments, profile (7, 8, 9, 10, 19, 20, 21) plus key modules
- shared entries (23, 24, 25, shared halves of 31, 39, 43) need the shell and each feature they register
- Apollo interceptors (37) need the shell and the shared provider; the direct fixes 38 and 41 need 35 and 40
- desktop host (29 and desktop halves of 31, 39, 43, 37) needs everything above
- desktop UX (47 desktop half, 48 desktop half, `b8e8ac2b6d`) needs the desktop host
- fixes on converted code: Home wheel needs 26; chat thumbnail (50) and chat camera (45) need 40; take-photo
  (`91603ade7b`) needs 34 and the JVM actuals (32)
- `libs` (44) is independent of the feature work (the features consumed those modules as JVM libraries before), but
  conflicts with the stray-test-deps change on `auth-core-public/build.gradle.kts` (the recorded resolution)

Longest chain: libraries, then key modules, then profile, then app shell, then shared entries, then Apollo
interceptors, then desktop host, then desktop UX, then docs. Nine serial landings; everything else runs beside it.

### 3.3 Option (a): many small PRs in dependency order (31 code PRs plus docs)

Each row is computed from the listed branches: whole branches use their merge's net diff (`M^1..M`), split branches
use the listed commits' diffs, and rows that combine several sources add them up. "Real" is changed lines minus
build-file and import lines; "Fresh" also removes relocated blocks. Sums across rows exceed the net totals (10,054
real versus 8,110) because the same files were edited by several branches and by fix rounds; squashing the fix rounds
recovers part of that.

| PR | Title | Contents | Base after | Commits | Files (R100) | +/- | Real (tests) | Fresh | Android risk | Auto-merge |
|---|---|---|---|---|---|---|---|---|---|---|
| A1 | Guard: commonMain imports, androidx rules, CI step | 1, 17, `90db99d446` | develop | 3 | 6 (0) | +431 / -22 | 388 (113) | 388 | none (build check) | yes |
| A2 | `test` lifecycle task on KMP modules | `808a6ee335` | develop | 1 | 6 (0) | +20 / -24 | 29 (0) | 24 | none | yes |
| A3 | `jvmDefault` NO_COMPATIBILITY | 46 | develop | 1 | 2 (0) | +4 / -0 | 2 (0) | 2 | low: bytecode, no `$DefaultImpls`; staging APK size identical | yes |
| A4 | Test modules out of production deps | `0b567e17c2` | develop | 1 | 2 (0) | +1 / -2 | 0 | 0 | low: release classpath loses 4 test libs | yes |
| A5 | Navigation bar hover/focus reset | `c46fef32df`, `97787c7a11` | develop | 2 | 4 (0) | +262 / -31 | 245 (148) | 133 | low, intended: mouse/keyboard only | yes |
| A6 | Molecule JVM frame clock, dispatcher public | 11, 16, `aedae07f0f` | develop | 3 | 7 (0) | +119 / -21 | 114 (66) | 106 | none (JVM; visibility) | yes |
| A7 | `isValidEmail` in commonMain | 6 | develop | 1 | 5 (0) | +68 / -1 | 62 (33) | 62 | none | yes |
| A8 | Cross-feature keys into `-navigation` modules | 14, `4032460991`, `29c8a41e21`, `3a54d2d174` | develop | 8 | 38 (2) | +294 / -79 | 147 (0) | 7 | low: same packages | yes |
| A9 | Libraries to KMP: icons, notification-permission, forever-ui, reminders-ui, navigation-compose, `Language.label` | 2, 3, 5, 18, 12, `10330a3da7`, `84d1b3432b` | develop | 12 | 131 (110) | +332 / -123 | 258 (38) | 129 | low: icons switch to the JetBrains material-icons-core wrapper | yes |
| A10 | Libraries to KMP with iOS targets (auth, core, leaves, data) | 44 | A4 | 11 | 118 (95) | +299 / -240 | 22 (0) | 22 | low: `uuid4` for two id generators | yes |
| A11 | JVM bindings for shared libraries | 13, 36, `60166f4af9`, `f237d78e38`, `59fdb90629`, `993c20f8fb`, `bee2a343b5`+`cc36d03e29`, `23050db0e3`+`a0a4d00b5d` | develop | 17 | 42 (1) | +1,479 / -103 | 1,204 (667) | 927 | low: Unleash keys move to commonMain, platform name, `isSupportedLocaleTag` extracted | yes |
| A12 | JVM actuals: pickers, camera, audio, NotAvailableOnDesktop | 4, 32 | develop | 2 | 12 (0) | +408 / -10 | 339 (191) | 304 | none (jvmMain) | yes |
| A13 | PermissionManager bound per platform | `fe4c92452d` | develop | 1 | 5 (0) | +46 / -2 | 32 (18) | 32 | low to medium: a missed binding silently disables notifications (has happened); graph reported identical | after a notification check |
| A14 | Convert insurance-certificate, terminate, forever, travel-certificate | 7, 8, 9, 10 | A7, A9 | 8 | 83 (67) | +192 / -169 | 52 (0) | 52 | low | yes |
| A15 | Claim chat, claim history, remove addons entries to commonMain | 15 | A9, A12 | 5 | 11 (5) | +87 / -7 | 72 (39) | 65 | low | yes |
| A16 | Convert payin-account and payments | 19, 20 | A9 | 4 | 90 (83) | +193 / -116 | 72 (8) | 21 | low | yes |
| A17 | Convert profile | `99461d2497`, `834941ab7c`, `5d33cdc2aa`, `524b17429b` | A8, A9 | 4 | 53 (42) | +384 / -196 | 355 (0) | 119 | low: bug-report email becomes expect/actual | yes |
| A18 | Convert home | 26 | A8, A9 | 2 | 24 (23) | +76 / -74 | 0 | 0 | low: build file and moves only | yes |
| A19 | Convert login | `096ef6e59a`, `d0a0a9a523` | A8, A11 | 2 | 33 (15) | +371 / -59 | 224 (140) | 151 | medium: BankIdState becomes expect/actual (collides with PR 3305) | no, manual after login check |
| A20 | Convert insurances | 28 | A8 | 2 | 35 (34) | +57 / -52 | 0 | 0 | low | yes |
| A21 | Convert onboarding | 30 | A8, A9, A11 | 2 | 54 (49) | +205 / -58 | 101 (91) | 76 | low | yes |
| A22 | Convert image viewer, claim details, Trustly | 33, 34, 35 | A8, A11 | 6 | 45 (24) | +611 / -224 | 445 (188) | 274 | low to medium: file pickers, Trustly web view | yes |
| A23 | Convert chat | `2fa9338960`, `2d3ff69881`, `04ad4700e3`, `22d80b7f7a` | A6, A8, A11 | 4 | 76 (33) | +980 / -301 | 849 (239) | 470 | medium: Uri, video player, pickers, Room transactions | no, manual after a chat smoke |
| A24 | Take-photo hidden where there is no camera | `91603ade7b`, 45 | A12, A22, A23 | 2 | 11 (0) | +125 / -42 | 152 (32) | 95 | none: Android `isAvailable` is constant true | yes |
| A25 | Home: reopen hero on unused upward wheel | `956b5c9b9e`+`005a0b6d3b` | A18 | 2 | 3 (0) | +204 / -20 | 192 (127) | 182 | low, intended: mouse wheel only | yes |
| A26 | Chat: file card when a thumbnail fails | 50 | A23 | 1 | 2 (0) | +153 / -22 | 153 (75) | 114 | low, intended: failure path only | yes |
| A27 | App shell module, AppHost, split entry provider | 22 without the three key commits, `931a2c86cd` | A8, A14, A16, A17 | 10 | 44 (24) | +877 / -586 | 1,232 (14) | 431 | medium: BackstackController, HedvigApp, entry provider move; view naming must stay | no, manual after a staging smoke |
| A28 | Shared provider registers converted features; PDF seam | 23, 24, 25, `3cd18b2834`, `e608eafcbf`, `e208a36cbc`, `5fdfbf9d18` | A27, A18 to A23 | 17 | 25 (1) | +916 / -636 | 1,299 (379) | 712 | medium: every registration moves; key set identical | no, manual after a staging smoke |
| A29 | Shared Apollo interceptors | `1ed6ee5ce5`, `419ec32f84`, `35f88b7a6e`, 38, 41 | A28 | 5 | 14 (1) | +275 / -34 | 234 (194) | 203 | low to medium: logout-on-unauthenticated rebinding; bytecode reported identical | yes |
| A30 | Desktop host and CI compile step | `13553b9539`, `20a9de07c8`, `131711d7b8`, `6d9c8d85b5`, `c959695369`, `5dfa55b576`, `343b026928`, `11a829ca18` | A29, A11, A12 | 8 | 22 (0) | +1,514 / -90 | 1,083 (439) | 1,003 | none at runtime; CI gains a compile step | yes |
| A31 | Desktop UX: links, back keys, logs, update action | `1d5eebc835`, `5bb5de7bab`+`c7b89d88d3`, `b5758ba53f`, `b8e8ac2b6d` | A30 | 5 | 13 (0) | +720 / -85 | 697 (370) | 465 | low: `1d5eebc835` refactors `CommonHedvigBuildConstants` deep-link hosts, which Android reads | yes |
| A32 | Docs: CLAUDE.md desktop section, design, plan, as-built | `docs/desktop-app-claude-md` | A30 | 3 | 4 | +1,303 / -27 | docs | | none | yes |

Every one of the 152 commits is placed exactly once (checked by script). Notes on adapting to develop:

- A2 on develop: drop the hunks for `feature-login` and `feature-payin-account` (their hand-written `test` tasks are
  added by the conversions, which then should simply not add them); keep the `feature-flags` hunk, its hand-written
  task exists on develop.
- A4 on develop: `feature-login/build.gradle.kts` is still the Android-library shape there, so the one-line removal
  needs re-applying by hand. Then A10 must carry the recorded resolution (`test-clock` only in `jvmTest`).
- A1's allow-list names files in modules that convert later (`data-addons`, `feature-onboarding`); harmless before
  they exist, but run `checkCommonMainImports` on develop before opening, since the androidx rules have only ever run
  against the converted tree.
- Over one sitting: A11 (1,204 real, 537 production), A27 (1,232, mostly the entry provider split: 801 relocated
  lines), A28 (1,299, three registration batches on the same file) and A30 (1,083). Splits if wanted: A11 into the
  Unleash JVM client (`e8153b220e`, `6c85d038a0`, `d9161e85a6`: 631 diff lines, 343 fresh) and the rest (981 diff
  lines, 693 fresh); A30 into the graph (674 diff lines) and the window (930 diff lines).

### 3.4 Option (b): a few large thematic PRs (8 code PRs plus docs)

| PR | Theme | Built from | Commits | Files (R100) | +/- | Real (tests) | Fresh | Android risk |
|---|---|---|---|---|---|---|---|---|
| B1 | Foundation and guard: guard, `test` task, `jvmDefault`, stray test deps, nav highlight, molecule clock, email, PermissionManager | A1 to A7, A13 | 13 | 34 (0) | +951 / -103 | 872 (378) | 747 | low |
| B2 | Libraries: KMP conversions, iOS targets, JVM bindings and actuals | A9 to A12 | 42 | 302 (206) | +2,518 / -476 | 1,823 (896) | 1,382 | low |
| B3 | Key modules and mechanical feature conversions | A8, A14, A15, A16, A18, A20 | 29 | 279 (214) | +899 / -497 | 343 (47) | 145 | low |
| B4 | Feature conversions with platform seams: profile, login, onboarding, image viewer, claim details, Trustly | A17, A19, A21, A22 | 14 | 185 (130) | +1,571 / -537 | 1,125 (419) | 620 | medium |
| B5 | Chat conversion | A23 | 4 | 76 (33) | +980 / -301 | 849 (239) | 470 | medium |
| B6 | Fixes on converted code: take-photo, Home wheel, chat thumbnail | A24 to A26 | 5 | 16 (0) | +482 / -84 | 497 (234) | 391 | low, intended |
| B7 | App shell: module, shared provider, Apollo interceptors | A27 to A29 | 32 | 74 (26) | +2,068 / -1,256 | 2,765 (587) | 1,346 | medium |
| B8 | Desktop host and desktop UX | A30, A31 | 13 | 29 (0) | +2,234 / -175 | 1,780 (809) | 1,468 | none to low |
| B9 | Docs | A32 | 3 | 4 | +1,303 / -27 | docs | | none |

Order: B1 and B2 in parallel, then B3, then B4 and B5 in parallel, then B6 and B7, then B8, then B9.

### 3.5 Comparison and recommendation

| | (a) 31 small + docs | (b) 8 thematic + docs |
|---|---|---|
| Reviews | 32 short reviews, most under 400 real lines; 9 PRs are under 60 real lines | 9 reviews, 3 of them (B2, B7, B8) are two to three sittings each |
| Serial landings on the longest chain | 9 | 6 |
| CI runs (minimum, one per PR) | 32, plus one per rebase | 9, plus one per rebase |
| Independent value lands early | yes, each alone | only as part of B1 (fine) or B6 (needs B3 to B5 first) |
| Revert granularity | one feature or fix | a whole theme |
| Rebase pain on hot files | many small rebases, each trivial | few, but each PR stays open longer, so more develop changes pile up against it |
| Reviewer fatigue | high PR count for a small team; mechanical PRs are cheap though | B7 puts the riskiest Android change (navigation shell) in the largest PR |

**Recommendation for a small team: a middle split of 17 code PRs plus docs**, (b)'s themes cut where a theme would
need more than one sitting, and (a)'s independent-value PRs kept alone. Computed the same way:

| PR | Title | Contents | Base after | Commits | Files (R100) | +/- | Real | of which tests | Real prod | Fresh | Android risk | Auto-merge |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| R1 | Guard platform imports in commonMain; `test` task on KMP modules | A1, A2 | develop | 4 | 11 (0) | +451 / -46 | 417 | 113 | 304 | 412 | none | yes |
| R2 | `jvmDefault` on KMP targets; test modules out of production deps | A3, A4 | develop | 2 | 4 (0) | +5 / -2 | 2 | 0 | 2 | 2 | low | yes |
| R3 | End stale hover and focus on navigation bar items | A5 | develop | 2 | 4 (0) | +262 / -31 | 245 | 148 | 97 | 133 | low, intended | yes |
| R4 | Small shared seams: molecule JVM clock, `isValidEmail`, per-platform PermissionManager | A6, A7, A13 | develop | 5 | 17 (0) | +233 / -24 | 208 | 117 | 91 | 200 | low to medium (PermissionManager) | after a notification check |
| R5 | Libraries to KMP (the libs batch adds iOS targets to 13 modules) | A9, A10 | develop (R2 for the resolution) | 23 | 249 (205) | +631 / -363 | 280 | 38 | 242 | 151 | low | yes |
| R6 | JVM bindings for shared libraries | A11 | develop | 17 | 42 (1) | +1,479 / -103 | 1,204 | 667 | 537 | 927 | low | yes |
| R7 | JVM actuals for pickers, camera, audio; NotAvailableOnDesktop | A12 | develop | 2 | 12 (0) | +408 / -10 | 339 | 191 | 148 | 304 | none | yes |
| R8 | Key modules and mechanical feature conversions | A8, A14, A15, A16, A18, A20 | R4, R5, R7 | 29 | 279 (214) | +899 / -497 | 343 | 47 | 296 | 145 | low | yes |
| R9 | Convert profile and onboarding | A17, A21 | R6, R8 | 6 | 107 (91) | +589 / -254 | 456 | 91 | 365 | 195 | low | yes |
| R10 | Convert login, image viewer, claim details, Trustly | A19, A22 | R6, R8 | 8 | 78 (39) | +982 / -283 | 669 | 328 | 341 | 425 | medium (login) | manual |
| R11 | Convert chat | A23 | R4, R6, R8 | 4 | 76 (33) | +980 / -301 | 849 | 239 | 610 | 470 | medium | manual |
| R12 | Fixes on converted code: Home wheel, chat thumbnail, take-photo | A24, A25, A26 | R10, R11 | 5 | 16 (0) | +482 / -84 | 497 | 234 | 263 | 391 | low, intended | yes |
| R13 | App shell module | A27 | R9 | 10 | 44 (24) | +877 / -586 | 1,232 | 14 | 1,218 | 431 | medium | manual |
| R14 | Shared provider registers converted features; PDF seam | A28 | R10, R11, R13 | 17 | 25 (1) | +916 / -636 | 1,299 | 379 | 920 | 712 | medium | manual |
| R15 | Shared Apollo interceptors | A29 | R14 | 5 | 14 (1) | +275 / -34 | 234 | 194 | 40 | 203 | low to medium | yes |
| R16 | Desktop host and CI compile step | A30 | R15 | 8 | 22 (0) | +1,514 / -90 | 1,083 | 439 | 644 | 1,003 | none | yes |
| R17 | Desktop UX: links, back keys, logs, update action | A31 | R16 | 5 | 13 (0) | +720 / -85 | 697 | 370 | 327 | 465 | low | yes |
| R18 | Docs | A32 | R16 | 3 | 4 | +1,303 / -27 | docs | | | | none | yes |

R13 and R14 are over the one-sitting budget on paper, but most of it is relocation inside `HedvigEntryProvider.kt`
(R13: 801 relocated lines; the net diff of `refactor/app-shell` alone is 936 real lines, 332 fresh) and the
per-commit sum double-counts the three registration batches on the same file (R14). After autosquashing their fix
rounds, both should read as one sitting each with `--color-moved`. R6 is large but 667 of its 1,204 real lines are
tests and nearly all production lines are `jvmMain`, invisible to Android.

What a reviewer must look at, per recommended PR:

- R1: the guard's rule list and allow-list format; that `test` now depends on `jvmTest` and `testAndroidHostTest`
  (at the integration tip `./gradlew test` grew from 42 to 168 test tasks). Likely useful on develop today too, where
  12 features are already KMP and only CI's explicit `jvmTest` runs their tests (not measured on develop).
- R2: two build-logic lines; the release classpath diff (turbine, coroutines-test, auth-core-test, test-clock gone).
- R3: `MappedInteractionSource` replaying missed hover-exit/unfocus; touch untouched.
- R4: the per-platform PermissionManager binding (the CLAUDE.md incident pattern) and the molecule dispatcher.
- R5: the plugin swaps and the JetBrains icons wrapper; `uuid4` call sites.
- R6: the JVM bindings are `jvmMain` only; look at the three commonMain touches (Unleash keys, `isSupportedLocaleTag`,
  `TlsDiagnostics`).
- R8: spot-check the build-file template once; the key modules' packages.
- R9 to R11: each `expect`/`actual` seam; the Android actual must be the old code moved, nothing else.
- R12: the three behaviour changes and their tests.
- R13: `AppHost`, the entry provider split, `BackstackController`/`HedvigApp` moves, Datadog view naming
  (`ScreenName`, `Navigation3TrackingEffect`).
- R14: registration moves, `DesktopEntryProviderTest` as the guard against a missing entry.
- R15: interceptor order and the per-platform binding.
- R16 and R17: desktop code; for Android only `1d5eebc835`'s `CommonHedvigBuildConstants` refactor.

**The first three PRs to open:** R1 (guard and `test` task), R2 (`jvmDefault` and stray test dependencies), R3
(navigation bar highlight fix). All three are based on develop, independent of each other and of everything else,
small (417, 2 and 245 real lines), carry value for Android even if nothing else lands, and can be armed for
auto-merge as soon as they are reviewed. R4 to R7 can open the same day.

### 3.6 Sequencing without stacked auto-merge

1. Open every PR whose dependencies are already in develop against develop directly. At the start that is R1 to R7:
   seven parallel PRs, each auto-merge-able on its own.
2. For a PR whose dependencies are not yet in develop, open it as a draft stacked on its parent branch so review can
   start early and the diff shows only its own changes. When the parent lands: rebase it onto develop, retarget its
   base to develop, wait for CI, then arm merge-when-ready (verify through the API that auto-merge is actually on).
3. PRs that share a lane can be reviewed in parallel, but each landing forces a rebase of its siblings that touch the
   same files (`app/app/build.gradle.kts`, `app-shell` build file, `HedvigEntryProvider.kt`, `libs.versions.toml`).
4. Before the manual merges (R10, R11, R13, R14): a staging build plus the Pixel smoke from the recipe, on the PR
   branch rebased on current develop.

**How long.** CI duration is unknown: `pr.yml` contains no timing, I did not query GitHub, and the ledger's DX
assessment explicitly did not measure it. The only local numbers are a clean `:app:assembleDebug` of 6m44s at the
tip and a 7m46s minified staging build; CI also runs lint and the full unit tests. The train has 9 serial landings on
its longest chain (R5, R8, R9, R13, R14, R15, R16, R17, R18), and 18 PRs need at least 18 green CI runs plus one per
forced rebase. If branch protection requires branches to be up to date with develop (unknown), every landing forces a
rebase and a fresh CI run of every open PR, which makes the train effectively serial: 18 times (CI time plus rebase
plus review latency). With one review round per PR and a reviewer available daily, that is plausibly two to four
weeks; this estimate rests on review latency, not on CI, and is a guess about the team, not a measurement.

### 3.7 Merging the integration branch as one PR

It is technically possible: the branch is based on develop, and at the tip it passed `test jvmTest`,
`checkCommonMainImports` and `:app:assembleDebug` locally plus a Pixel smoke and an R8 staging build. On GitHub it
would be 916 files and about 20,500 diff lines / 1.06 MB, at the documented total-diff limit, so parts of the diff
would not render without "Load diff"; the 585 pure renames would show as renames. Its commits tab would list 200
entries, 48 of them integration merges.

What is lost:

- Reviewability: about 4,500 fresh lines and the riskiest Android change (navigation shell) in one review, next to
  2,000 lines of desktop code most reviewers do not care about.
- History: 48 integration merges, two merged twice, 29 non-compiling move commits, about 21 fix-round commits, the
  one wrong message, all on develop forever (develop keeps PR commits).
- Early value: the guard, `test` task, `jvmDefault`, the three Android fixes and the test-dependency cleanup wait for
  the whole thing.
- Revert granularity: one revert of 916 files.
- The option to stop before the desktop host (section 4).

It is the right shape only if the team explicitly decides to treat this as a single atomic migration and review it
from the overview artifact rather than the diff.

### 3.8 Conflicts with ongoing work on develop

Churn on develop in the 90 days before the base (`git log --since=2026-08-13 --first-parent --name-only 229bd79df0`,
250 first-parent commits), for files this work changes:

| File | develop commits in 90 days | What this work does to it |
|---|---|---|
| `gradle/libs.versions.toml` | 27 | adds 5 lines (JetBrains nav3, material-icons wrappers, coroutines-swing) |
| `feature-home/.../HomeDestination.kt` | 25 (140 in six months per the DX report) | moved with edits (R5 path change plus the wheel handler in R12) |
| `app/app/build.gradle.kts` | 21 | every new module is added |
| `CLAUDE.md` | 16 | test commands, shell section, desktop section |
| `feature-home/.../GetHomeDataUseCase.kt`, `HomePresenter.kt`, `HomePresenterTest.kt` | 10, 9, 9 | pure moves |
| `app/.../HedvigEntryProvider.kt` | 9 (45 in six months) | R081, entries block moved and split |
| `app/.../HedvigApp.kt` | 8 | moved to `app-shell` with edits |
| feature-onboarding sources and build file | 5 to 7 each | moves plus build rewrite |
| `.github/workflows/pr.yml` | 6 | two steps added |

303 of the 654 moved files were touched on develop in those 90 days.

How to handle it:

- Pure moves follow on rebase: git applies a develop edit to a moved-unchanged file at the new path (verified by the
  DX assessment's rebase simulation). Edits to changed regions of moved-with-edits files, to `HedvigEntryProvider.kt`
  and to any converted `build.gradle.kts` conflict.
- **New files are the trap.** A develop PR that adds a file under a converted module's `src/main` gets a
  "CONFLICT (file location)" with git proposing `src/commonMain`; after the module is converted, a file left in
  `src/main` is no longer a source set. Announce each module conversion in the team channel before it lands.
- Land the hot-module conversions early and quickly rather than late: home (R8), the shell (R13, R14). Each day they
  stay open, more develop changes pile up against `HomeDestination.kt` and `HedvigEntryProvider.kt`.
- Rebase the PR, not develop's open branches: this work owns the conflicts. When a conversion PR conflicts, redo the
  move from develop's current state (the move commits are mechanical and can be regenerated) instead of resolving
  file by file.
- **PR 3305** edits `feature-login/build.gradle.kts`, `SwedishLoginDestination.kt` and `SwedishLoginPresenter.kt`, and
  moves `BankIdState.kt` into a new Android-only module `app/ui/ui-bankid` with a `String` parameter. This work makes
  `BankIdState` an `expect`/`actual` seam in `feature-login` with a `uri-kmp` parameter (R091 to
  `BankIdState.android.kt`, plus new `commonMain` and `jvmMain` files). Recommendation: let 3305 land first, then
  rebuild R10's login part on top of it, putting the seam where 3305 put the class (or making `ui-bankid` a KMP module
  with the seam). Tell 3305's developer before R10 opens; this is still listed as open in the ledger. I could not see
  3305's branch locally, so its current state is unknown.

---

## 4. What should not go to develop, or not yet

- **The walkthrough harness (`tool/desktop-walkthrough`).** Local branch at `45f72306ca`: 5 commits ahead of the tip
  (one of them a merge), 10 files, +1,923 lines; `origin/tool/desktop-walkthrough` is behind that. It drives the
  desktop app headlessly, can run against a real staging session in place, and is tooling for this effort. Keep it as
  a branch. If the team wants it later, it is one PR after R17, squashed, with the session-in-place mode reviewed for
  token safety.
- **The docs.** The `CLAUDE.md` and `docs/architecture/navigation-and-di.md` edits already in the integration branch
  belong in the PR of the code they describe (R1 for test commands, R13 and R14 for the shell, R16 for the desktop
  host's fallback entry). The `docs/desktop-app-claude-md` branch (rewritten `CLAUDE.md` desktop section, the design
  spec, the plan, the as-built document, 1,303 added lines) should ride as R18 after R16, or its `CLAUDE.md` part with
  R16 and the three documents as a docs-only PR. `docs/desktop-app-design` is a subset (the three documents) and does
  not need its own PR. develop already has `docs/superpowers/specs` and `plans`, so the documents have a home. The
  as-built document describes the integration tip and would need updating to the as-landed state.
- **Desktop-only work that can be held back** if the team wants the shared-code part first: R16 and R17 entirely
  (`:app-desktop`, its CI step, desktop UX), and much of R6 (JVM bindings: Unleash JVM client, desktop data
  directory, JVM language persistence, `JvmFileService`) and R7 (JVM actuals). What cannot be held back without
  dropping the JVM target altogether: the `jvmMain` actuals inside each converted feature (for example
  `BankIdState.jvm.kt`, `BugReportEmail.jvm.kt`, the chat file-card video fallback), since the JVM target must compile
  for `jvmTest` and the graph tests.

**Is there an Android-only value subset?** Yes, but it is small:

| Piece | Android value | Lands on develop as is? |
|---|---|---|
| `test` lifecycle task (R1) | `./gradlew test` and `:module:test` also run the JVM tests of modules that are already KMP on develop (12 of 28 features at the base). On develop only CI's explicit `jvmTest` covers them, and `feature-flags` carries a hand-written `test` task for its own case; I did not run Gradle on develop to count what `test` skips there | yes, minus two hunks |
| commonMain import guard (R1) | catches `java.*`, `android.*` and Android-only androidx in the existing KMP modules | yes |
| `jvmDefault` (R2) | removes `$DefaultImpls` from the existing KMP modules (22 to 0 at the tip) | yes |
| Stray test dependencies (R2) | turbine, coroutines-test, auth-core-test, test-clock leave the release runtime classpath | yes, re-applied by hand |
| Navigation bar hover/focus (R3) | correct with a mouse, trackpad or keyboard focus (ChromeOS, tablets) | yes |
| PermissionManager per platform (R4) | removes the footgun behind the notifications incident | yes |
| Home wheel (R12) and chat thumbnail fallback (R12) | mouse-wheel Home hero; a file card instead of a blank red tile when a thumbnail fails | not as is: both live in converted files with `jvmTest` tests; portable to `src/main` with the tests rewritten as Android host tests |
| iOS targets on 13 libraries (R5) | none for Android; groundwork for sharing non-UI code with iOS | yes |

Roughly 1,000 real lines. The feature conversions themselves have little Android-only value: their payoff is the
desktop host (or a future non-UI share with iOS, which converts libraries, not feature UI). They cost something on
Android, measured by the DX assessment: clean `assembleDebug` 6m05s to 6m44s (+11%, one run each), tasks per edit 108
to 165, three files for each Android-only need, and rebase conflicts on the hot files. If the desktop host is not
wanted, land R1 to R5, port the two UI fixes, and stop; converting features without the host is cost without payoff.

---

## What I could not determine

- The current state of develop (the local ref is two days old; no fetch).
- CI duration of `pr.yml` and whether branch protection requires up-to-date branches.
- That each proposed PR compiles and passes CI on its own: no Gradle runs. The dependency graph is derived from build
  files and file overlap, not from compiling each cut.
- Whether GitHub falls back from rename detection on very large PRs; the diff limits are quoted from memory of
  GitHub's documentation.
- PR 3305's current branch.
- Exact net line counts for multi-branch PRs: rows that combine branches add per-branch diffs, so they overstate where
  branches edit the same lines.
