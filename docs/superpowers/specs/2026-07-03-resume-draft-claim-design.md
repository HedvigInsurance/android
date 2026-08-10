# Resume draft claim: Android parity with iOS PR #2434

Date: 2026-07-03
Branch: `feat-resume-claim`
References: iOS PR HedvigInsurance/ugglan#2434 (final state as of 2026-07-03, including the
"Added flag" and "Added expired alert" commits), backend schema fields
`ClaimIntent.resumable: Boolean!` and `claimIntentDeleteDraft(id: ID!): Boolean!`, and the
`RESUME_CLAIM_*` Lokalise string batch.

## Goal

Bring the Android `feat-resume-claim` branch to behavioral parity with the final state of the
iOS resume-claim PR and the current backend schema. The branch already implements the resume
mechanics inside the claim chat (previous-step replay, seeding of `current*` values, the
free-text-per-step fix). This spec covers the remaining gaps:

1. A Draft card in the home claim-cards carousel, always first, with Delete + Continue.
2. A "You have a draft claim" dialog when starting a new claim while a draft exists,
   replacing the current in-sheet "Continue with the draft claim" button.
3. The claim chat leave dialog gated on the new `ClaimIntent.resumable` field, with new copy.
4. The claim chat title taken from `ClaimIntent.displayName`.
5. The inbox start-claim entry point gets the same draft handling as home.

Decisions already made with the user:

- Full parity with iOS, including the draft card.
- Everything is gated behind the Unleash flag `enable_claim_intent_resume`, matching iOS.
- Shared draft-claim data goes in a new small KMP data module (Approach A), not per-feature
  duplication and not an observable store singleton.
- All new user-facing copy uses the `RESUME_CLAIM_*` Lokalise string resources (already
  uploaded to Lokalise; run `./gradlew downloadStrings` and verify the keys are present
  before implementation). No hardcoded strings.

## Section 0: Feature flag

Add `ENABLE_CLAIM_INTENT_RESUME` to the `Feature` enum (commonMain), mapped to the raw
Unleash key `enable_claim_intent_resume` in `Feature.unleashKey` (androidMain), per
`FEATURE_FLAG_DEFAULTS.md`. Positive polarity; no bootstrap entry needed: when Unleash has
never been fetched the flag resolves to off, which safely hides the feature (it does not
gate anything the app needs to function).

Gating points (all matching iOS):

- **Home:** the Home query's `resumableClaimIntent` selection gets
  `@include(if: $resumeClaimEnabledFlag)`, following the existing `$claimsHistoryFlag`
  variable pattern in `GetHomeDataUseCaseImpl`. Flag off means `draftClaim == null`:
  no card, no draft dialog.
- **Inbox:** `InboxPresenter` skips the `GetResumableClaimIntentUseCase` fetch when the
  flag is off (treats it as no draft).
- **Claim chat title:** `displayName` is used only when the flag is on; otherwise the
  legacy `CHAT_CONVERSATION_CLAIM_TITLE` string.
- **Claim chat leave dialog:** flag on gives the new resumable-gated behavior (Section 4);
  flag off keeps the legacy behavior of always confirming dismissal with the generic
  claims alert copy (`claims_alert_body`).
- The claim chat presenter/destination reads the flag via an injected `FeatureManager`
  (same pattern as other features).

## Section 1: Schema and shared data module

**Schema:** commit the already-downloaded `schema.graphqls` diff (adds `ClaimIntent.resumable`
and the `claimIntentDeleteDraft` mutation) as its own "Download schema" commit, matching the
existing commit convention.

**New module `app/data/data-claim-intent`** (KMP, `hedvig.multiplatform.library` +
`hedvig.gradle.plugin`, `hedvig { apollo("octopus") }`). Auto-discovered by settings. Note:
`app/data/data-claim-flow` and `app/data/data-claim-triaging` are empty leftover directories;
this module is new, not a revival of those.

Public API (no `octopus.*` types leak):

```kotlin
data class ResumableClaimIntent(
  val id: String,
  val displayName: String?,
  val startedAt: LocalDate, // from ClaimIntent.createdAt
)

interface GetResumableClaimIntentUseCase {
  suspend fun invoke(): Either<ErrorMessage, ResumableClaimIntent?> // null = no draft
}

interface DeleteClaimIntentDraftUseCase {
  suspend fun invoke(id: String): Either<ErrorMessage, Unit>
}
```

- `GetResumableClaimIntentUseCaseImpl` runs
  `query ResumableClaimIntent { currentMember { resumableClaimIntent { id displayName createdAt } } }`
  with `FetchPolicy.NetworkOnly`.
- `DeleteClaimIntentDraftUseCaseImpl` runs `mutation { claimIntentDeleteDraft(id: $id) }` and
  `ensure`s the returned Boolean is true.
- Both impls are `internal`, bound with `@ContributesBinding(AppScope::class)`.
- `feature-home` and `feature-chat` add `implementation(projects.dataClaimIntent)`.
- `feature-claim-chat` keeps its own internal full `ResumeClaimQuery` (it needs the whole step
  fragment, which is feature-internal) and does not depend on the new module.

## Section 2: Home draft card

**Home query** (`app/feature/feature-home/src/main/graphql/QueryHome.graphql`): extend the
existing embedded selection to `resumableClaimIntent { id displayName createdAt }`. The card
data rides along with the rest of home: one network call, refreshes with home reload.

**`HomeData`** (`GetHomeDataUseCase.kt`): replace `resumableClaimId: String?` with
`draftClaim: DraftClaim?` where `DraftClaim(id: String, displayName: String?, startedAt: LocalDate)`
(project-owned, mapped from the query). Demo use case returns `null`.

**Card UI in `app/ui/claim-status`** (`ClaimStatusCards.kt`): introduce a sealed list item so
the existing pager hosts both card kinds:

```kotlin
sealed interface ClaimCardUiState {
  data class Claim(val uiState: ClaimStatusCardUiState) : ClaimCardUiState
  data class Draft(val id: String, val title: String?, val startedAt: LocalDate) : ClaimCardUiState
}
```

- `ClaimStatusCards` takes `NonEmptyList<ClaimCardUiState>` plus `onContinueDraftClaim` and
  `onDeleteDraftClaim` lambdas. Existing single-card call sites that render a claim directly
  (for example claim details) keep using the inner `ClaimStatusCard` composable and are untouched.
- New `DraftClaimCard` composable, per the iOS screenshots: "Draft" highlight pill in the
  amber style (`RESUME_CLAIM_DRAFT`), title = `title ?: RESUME_CLAIM_FALLBACK_TITLE`
  ("Continue where you stopped", the effective iOS fallback), subtitle
  `RESUME_CLAIM_STATED` formatted with the started date ("Started %1$@"), the three progress
  segments ("Started", "Being handled", "Closed") rendered in the disabled/inactive style,
  and a button row of Delete (secondary, `RESUME_CLAIM_DELETE_BUTTON`) + Continue (primary,
  `RESUME_CLAIM_CONTINUE_BUTTON`).

**Ordering:** home builds the pager list with the draft first, then active claims (per the
Slack thread: draft is always first, even when a claim updates).

**Delete flow:** Delete opens a `HedvigAlertDialog` (`RESUME_CLAIM_DELETE_TITLE` /
`RESUME_CLAIM_DELETE_BODY` / cancel = `general_cancel_button`, confirm =
`RESUME_CLAIM_DELETE_BUTTON`). Confirm sends a new `HomeEvent.DeleteDraftClaim` to
`HomePresenter`, which calls `DeleteClaimIntentDraftUseCase(id)` and, on success, triggers
the existing reload path so the card disappears. On failure: log only (matches iOS's silent
catch); no new error UI.

**Expired drafts (matches iOS "Added expired alert"):** `DraftClaim` exposes
`fun isExpired(clock: Clock): Boolean`, true when today is past `startedAt + 7 days`
(client-side, same heuristic as iOS; the backend copy says drafts live 7 days). When the
card's Continue button is tapped on an expired draft, show an alert
(`RESUME_CLAIM_EXPIRED_TITLE` / `RESUME_CLAIM_EXPIRED_BODY`, single dismiss button) instead
of navigating. Only the card's Continue checks expiry, matching iOS.

**Continue flow (non-expired):** navigates straight into the resumed claim chat, skipping the
honesty pledge (matches iOS).

**Refresh on return:** verify that home refetches when the user comes back from the claim chat
(iOS refetches explicitly on flow dismissal). If home does not already reload on re-entry,
trigger the reload when returning from the claim flow so a freshly abandoned claim shows its
draft card immediately.

## Section 3: Start-claim entry points (home + inbox)

**Design system `StartClaimBottomSheet.kt`:**

- Revert the sheet to `HedvigBottomSheetState<Unit>`; delete `StartClaimSheetData` and the
  in-sheet "Continue with the draft claim" button (iOS ended up not putting resume inside the
  pledge sheet). This also reverts the leftover unused parameters on `StartClaimPledgeScreen`.
- Add `DraftClaimDialog` composable next to it: a `HedvigDialog` with three stacked big buttons.
  Copy: title `RESUME_CLAIM_DRAFT_ALERT_TITLE`, body `RESUME_CLAIM_DRAFT_ALERT_BODY`, buttons
  `RESUME_CLAIM_DRAFT_ALERT_CONTINUE`, `RESUME_CLAIM_DRAFT_ALERT_START_NEW` (red/attention
  text style, per screenshot), `general_cancel_button`.

**Flow on both screens (matches iOS):** on tapping "Make a claim" (home) or "Start claim"
(inbox's `NewChatSelectBottomSheetContent`):

- No draft: open the pledge sheet directly (unchanged).
- Draft exists: show `DraftClaimDialog` first.
  - "Continue draft": navigate straight into the resumed claim chat (no pledge).
  - "Start new claim": open the pledge sheet (plain new-claim flow).
  - "Cancel": dismiss, nothing else.

**Home wiring** (`HomeDestination.kt`): branch on `draftClaim != null` before
`startClaimBottomSheetState.show(Unit)`.

**Inbox wiring** (`InboxViewModel.kt` / `InboxDestination.kt`): `InboxPresenter` injects
`GetResumableClaimIntentUseCase` and fetches it alongside conversations on load/reload,
exposing the draft (or just its presence) in `InboxUiState`. `InboxDestination` branches the
same way home does. `inboxEntries` and `HedvigEntryProvider` thread a resume-capable
navigate lambda.

**Navigation key** (`ClaimChatEntries.kt`): `ClaimChatKey.resumableClaimId: String?` becomes
`resumeClaim: Boolean = false`. The resume query takes no id (a member has at most one draft;
iOS passes no id either), and a stale id after process-death restore would be misleading.
`navigateToClaimChat` lambdas change from `(String?) -> Unit` to `(resumeClaim: Boolean) -> Unit`.
`ClaimChatViewModel`/`ClaimChatPresenter` assisted param changes accordingly.

## Section 4: Claim chat screen

**Fragment** (`FragmentClaimIntent.graphql`): add `displayName` and `resumable` to
`ClaimIntentFragment`. Map both into the internal `ClaimIntent` model and into
`ClaimChatUiState.InProgress` as `title: String?` and `isResumable: Boolean`.

**Title:** with the flag on, the `TopAppBar` uses `displayName` when present, falling back to
the existing `CHAT_CONVERSATION_CLAIM_TITLE` string ("My things", "My trip • Sickness" on
iOS). Flag off keeps the legacy title.

**Leave dialog** (replaces the current always-shown dialog with hardcoded TODO text in
`ClaimChatDestination.kt`):

- Flag on, `isResumable == true`: dialog with title `RESUME_CLAIM_LEAVE_TITLE`, body
  `RESUME_CLAIM_LEAVE_BODY`, dismiss `GENERAL_NO`, confirm `RESUME_CLAIM_LEAVE_CONFIRM`.
- Flag on, `isResumable == false`: no dialog; close immediately (matches iOS's conditional
  dismiss).
- Flag off: legacy behavior, always confirm with the generic claims alert copy
  (`claims_alert_body`), restoring the string the branch commented out.

**Deflect resume:** the presenter's resume path already routes `ClaimIntent.Next.Outcome` to
the outcome/deflect screen; verify against the deflect screenshots rather than adding code.

**Cleanups on the branch:**

- Resolve `isPrepared = true, // TODO: check` in `ClaimIntentExt.kt` for resumed remote audio:
  verify remote playback via `PlayableAudioSource.RemoteUrl` and either keep the value with a
  real rationale or fix it. Note `AudioRecordingBottomSheet` returns a `null` player for remote
  URLs; that path is only reachable pre-submit, verify and leave as is if confirmed.
- The commented-out `// text = stringResource(Res.string.claims_alert_body)` line and its
  TODO replacement disappear into the flag-dependent leave-dialog logic above.
- Run `./gradlew ktlintFormat` over the branch (style drift exists: `resumableClaimId!=null`,
  missing trailing commas, odd line breaks in `StartClaimBottomSheet.kt`).

## Testing

- `HomePresenterTest`: update for `draftClaim` (replacing `resumableClaimId`), add coverage for
  the delete-draft event (success reloads, failure keeps state) and for the flag-off case
  (no draft in ui state even when the backend would return one).
- Inbox presenter test (if the existing test pattern covers `InboxPresenter`): draft presence
  reaches the ui state.
- `ExhaustiveBackStackSerializationTest` picks up the `ClaimChatKey` shape change automatically.
- `feature-claim-chat` presenter test for the resume-seeding path if presenter test infra
  exists there: previous steps are replayed with Task steps filtered out, and an
  outcome-terminal draft resumes into the outcome.

## Error handling summary

- Get resumable intent fails on home: home already surfaces query errors through its existing
  error state; the draft card is simply absent when the field is null.
- Get resumable intent fails on inbox: treat as "no draft" (do not block starting a claim).
- Delete draft fails: log, keep the card (silent, matches iOS).
- Draft expired at the card: expired alert on Continue (Section 2); delete still works.
- Resume query returns null (draft expired/deleted between card render and entering the
  chat): the existing `failedToStart` error state in the claim chat shows; acceptable for
  now (iOS has the same hole past the card check).

## Out of scope

- The experimental AI development flow's step-loop issue (backend-side, tracked in
  #rnd-claims-automation).
- Deep links directly into a resumed claim.
- Bootstrap entry for the new flag (off-when-unfetched is the desired default).
