# Task 20 whole-branch review fixes

Branch: `feat/onboarding`. All work under `app/feature/feature-onboarding` unless noted.

## Commits

| SHA | Subject |
| --- | --- |
| 594f931a5b | Scope the onboarding session cache to the current member |
| 958d96fad0 | Reset phone step submitting state after save |
| 5ca6d033ae | Anchor onboarding step content above the bottom buttons |
| 27e3a45408 | Polish onboarding UI details and comments |

## A. Member-scoped OnboardingSessionStore cache (CRITICAL)

`data/OnboardingSessionStore.kt`:
- Added internal `fun interface OnboardingMemberIdProvider { fun memberId(): Flow<String?> }` plus
  `OnboardingMemberIdProviderImpl` (`@ContributesBinding(AppScope::class)`, `@Inject`) delegating to
  `MemberIdService.getMemberId()`.
- `OnboardingSession` gained `val memberId: String` as its first property.
- `OnboardingSessionStore` constructor now takes `memberIdProvider`.
  - `getOrFetchSession()` (inside the mutex): resolves `currentMemberId` (returns
    `ErrorMessage("No member id for onboarding session").left()` if null); returns the cached
    session only when `cachedSession?.memberId == currentMemberId`, otherwise clears the cache and
    fetches fresh, storing the session stamped with the current member id.
  - `refreshData()`: resolves the member id, treats a cached session with a different memberId as
    "no session to refresh" (clears cache, returns the existing error path).
- `OnboardingGateImpl` (`gate/OnboardingGate.kt`) unchanged and still compiles: it resolves memberId
  itself then calls `getOrFetchSession()`; the double resolution is harmless.
- Tests: added `FakeOnboardingMemberIdProvider(var memberId)` to `FakeOnboardingRepository.kt`;
  updated all 30 `OnboardingSessionStore(...)` construction sites across 10 test files (phone, theme,
  consent, welcome, coinsured, petid, invite, payment, bundle presenter tests + navigator test) to
  pass the fake, with the import added (alphabetically before `FakeOnboardingRepository`).
- New `data/OnboardingSessionStoreTest.kt`:
  - `same member reuses the cached session without refetching` — one repository response fed, two
    `getOrFetchSession()` calls return equal sessions, `expectNoEvents()` on the Turbine proves no
    refetch.
  - `a member change discards the cache and fetches fresh` — member A response (phone 111) cached,
    fake switched to member B, second response (phone 222) fed, second call returns the member-B
    session.

## B. Phone step submit-state bugs

`ui/phone/OnboardingPhoneDestination.kt` submit LaunchedEffect:
- On success: `currentState = content.copy(isSubmitting = false)` BEFORE `navigator.continueFrom(...)`.
- On failure: `currentState = (currentState as? OnboardingPhoneUiState.Content ?: content).copy(isSubmitting = false, showSubmissionError = true)` so digits typed during submission survive.
- Test `save success advances to the next step` updated for the extra emission and asserts the final
  emitted Content has `isSubmitting == false`.

## C. Single ownership of vertical free space

`ui/OnboardingStepScaffold.kt`: removed the leading `Spacer(Modifier.weight(1f))` from
`OnboardingStepButtons` (kept its 16dp spacing); added a `Spacer(weight(1f))` to both previews.
Per-destination arrangement now has exactly one `weight(1f)` split each:
- welcome: added the second `Spacer(weight(1f))` after the centered content block.
- phone: added `Spacer(weight(1f))` between the text field and the buttons.
- invite: added `Spacer(weight(1f))` before and after the centered code text.
- bundle: added `Spacer(weight(1f))` after the rows loop.
- consent, theme, coinsured, petid, payment (both connected/not-connected): already had the single
  weight in the required place, left unchanged.

## D. Small quality fixes

1. `OnboardingStepHeader`: `Spacer(Modifier.height(4.dp))` between title and description.
2. `ui/theme/OnboardingThemeDestination.kt`: (a) load-effect initial Content now uses
   `selectedTheme = Theme.SYSTEM_DEFAULT` with comment `// Placeholder; the return site merges the
   live stored theme.` (b) `ThemeOptionRow` internal padding changed `padding(16.dp)` ->
   `padding(vertical = 16.dp)`; call sites keep horizontal 16dp so the total horizontal inset is 16dp
   and the ripple (clip+clickable before the padding) still covers the row.
3. `navigation/OnboardingNavigator.kt`: removed the dead `navigateBack()` (no source references).
4. `tracking-firebase/.../ConsentAwareEventTrackingClient.kt`: added the public-hints comment on the
   providers interface (ktlint moved it above the annotations, which is the correct spot).
5. `build.gradle.kts`: comment above `isReturnDefaultValues = true`.
6. Removed redundant `val content = state` aliases in invite and bundle destinations (use the
   smart-cast `state` directly).

## Verification

Commands run (grepped for BUILD SUCCESSFUL, piped exit codes not trusted):

- `./gradlew :feature-onboarding:testDebugUnitTest :tracking-firebase:testDebugUnitTest` -> BUILD SUCCESSFUL
- `./gradlew :app:compileDebugKotlin` -> BUILD SUCCESSFUL
- `./gradlew ktlintFormat` -> BUILD SUCCESSFUL (only reflowed the new tracking-firebase comment,
  committed in D)

Working tree clean after the four commits.

## Deviations

None. The theme ThemeOptionRow modifier order (clip -> clickable -> padding) already produced a
ripple covering the row, so no reorder beyond the padding change was needed.

## Fix: DENIED disables SDK collection

### What changed

`ConsentAwareEventTrackingClient.kt`:
- Added `private var collectionRequestedEnabled: Boolean = true` (guarded by `lock`).
- `setCollectionEnabled(enabled)` now stores the value under the lock, then calls
  `applyCollectionEnabled()` instead of delegating directly.
- New `private fun applyCollectionEnabled()`: computes `effective = collectionRequestedEnabled &&
  consent != DENIED` under the lock, then calls `delegate.setCollectionEnabled(effective)` outside
  it. NOT_DECIDED and GRANTED leave collection enabled (subject to the demo gate); only DENIED forces
  it off; flipping back to GRANTED re-enables if the demo gate allows.
- The consent collector's `init` block now calls `applyCollectionEnabled()` after every consent
  update.
- KDoc updated to document the DENIED-disables-SDK-collection behavior.

`ConsentAwareEventTrackingClientTest.kt`:
- `RecordingClient.setCollectionEnabled` now records each call into `collectionEnabledCalls`.
- Three new tests added:
  - `denying consent disables SDK collection and granting re-enables it`
  - `demo mode wins over granted consent`
  - `enabling collection while denied stays disabled`

`docs/superpowers/specs/2026-07-21-onboarding-design.md`:
- Appended one sentence to the "Analytics consent mechanism" section documenting the post-review
  product decision.

### Commands run

```
./gradlew :tracking-firebase:testDebugUnitTest 2>&1 | tee /tmp/test_output.txt
grep "BUILD SUCCESSFUL" /tmp/test_output.txt  -> BUILD SUCCESSFUL in 1m 3s

./gradlew ktlintFormat 2>&1 | tee /tmp/ktlint_output.txt
grep "BUILD SUCCESSFUL" /tmp/ktlint_output.txt  -> BUILD SUCCESSFUL in 41s
```

Both BUILD SUCCESSFUL outputs confirmed. ktlintFormat made no further changes to the modified files.
