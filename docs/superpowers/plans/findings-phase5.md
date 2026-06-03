# Phase 5 findings — deep-link audit after Nav2→Nav3 migration

Branch: `eng/metro-nav3-pr2-nav2-to-nav3`. No production behavior changed during this audit.

## 1. Inventory
- **38 logical URI patterns** mapped to a `HedvigNavKey`, contributed by **19** feature
  `DeepLinkMatcherProvider`s. Each logical pattern expands to 3 concrete URIs (one per deep-link
  host: NEW `link.*`, HOST `*/deeplink`, OLD `*.page.link`).
- Full table in `docs/superpowers/plans/deeplink-inventory.md`.
- Every `HedvigDeepLinkContainer` field is consumed by exactly one provider — **no orphan
  container patterns** (including `moveContract` → `SelectContractForMovingKey` in MovingFlowDeepLinks.kt).

## 2. Manifest cross-check — NO MISMATCHES
`AndroidManifest.xml` advertises 3 autoVerify VIEW intent-filters, all `scheme="https"`:
- `DEEP_LINK_DOMAIN_HOST_NEW` (no path) = link.dev.hedvigit.com / link.hedvig.com
- `DEEP_LINK_DOMAIN_HOST` (pathPrefix=/deeplink) = dev.hedvigit.com / www.hedvig.com
- `DEEP_LINK_DOMAIN_HOST_OLD` (no path) = hedvig{develop,test}.page.link / hedvig.page.link

These exactly match `CommonHedvigBuildConstants.deepLinkHosts(flavor)`, which feeds
`HedvigDeepLinkContainerImpl.baseDeepLinkDomains`. The `/deeplink` prefix is baked into the HOST
entry. No host advertised that the matcher lacks; no matcher host the manifest lacks. No mismatch.

Matcher → key existence: every target key in the inventory confirmed to exist as a declared
`: HedvigNavKey` type via grep. **No matcher produces a non-existent key.**

## 3. Matcher implementation + test coverage
`HedvigDeepLinkMatcher.match()` (commonMain) — logic intact:
- Parses URI via `DeepLinkRequest.fromUriString` (null/non-match on parse failure).
- Iterates all matchers; keeps highest-priority `UriMatchResult` via `compareTo(...) > 0` (strict),
  so ties keep the first-registered matcher.
- Nav3 precedence (verified against upstream `UriDeepLinkMatcher` source): exact-path > more
  path-args > has-args > more total args. Path/query arg extraction confirmed. A required-arg
  decode failure is correctly treated as a non-match (e.g. `ClaimDetailsKey.claimId` no-default →
  matcher returns null → falls back). `AddonDeepLinkMatcher` overrides `matchArguments` to inject
  the fixed `AddonBannerSource` (not URI-encoded) — verified correct.

**Existing matcher tests: NONE.** Only `HedvigNavBackstackTest` existed (back-stack helpers).
Coverage gap = 100% of inventory.

**Tests added (not run):**
`app/navigation/navigation-compose/src/commonTest/kotlin/com/hedvig/android/navigation/compose/HedvigDeepLinkMatcherTest.kt`.
Feature keys/patterns are unreachable from this module, so the test defines local `HedvigNavKey`
types mirroring real key shapes (same `@SerialName` arg mapping + nullability/defaults) and wires a
`HedvigDeepLinkMatcher` with the real pattern shapes. Cases: home (bare host + /submit-claim),
payments, chat/inbox, conversation (arg), help-center home + topic (id arg), forever, claim-details
(claimId arg + missing-arg non-match), terminate-contract (contractId), edit-coinsured
(with/without id), contract (with-id prefers detail vs without-id falls back to insurances),
insurances, plus unknown-path and non-deep-link-host null cases.
Caveat: tests assert the matcher engine + precedence + arg mapping; they do not import the
production container, so a future edit to a container pattern *string* is not caught here.

## 4. Logged-out buffering — VERDICT: CORRECT
`HedvigApp.kt` (~L127-139): the `deepLinkChannel` collector runs
`snapshotFlow { backstackController.isLoggedIn }.first { it }` (suspends until logged in) BEFORE
`deepLinkFirstUriHandler.openUri(uri)`, which does `backstack.add(destination)`.
`isLoggedIn = backstack.firstOrNull()?.topLevelGraphOrNull() != null` — true only after
`setLoggedIn()` does `clear(); add(HomeKey)`. Logged out the list is `[LoginKey]` (topLevelGraph
null → isLoggedIn false). Because the flag can only flip *because* `setLoggedIn()` reset the list to
`[HomeKey]`, the buffered `add(...)` necessarily runs AFTER the reset — the key lands on top of
`[HomeKey]`, never inside the `[LoginKey]` stack that gets cleared. Links arriving while already
logged in push immediately. Buffering and ordering are correct.
