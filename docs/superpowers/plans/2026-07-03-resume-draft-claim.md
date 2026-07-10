# Resume Draft Claim Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bring the Android `feat-resume-claim` branch to parity with iOS PR HedvigInsurance/ugglan#2434: draft card on home, draft/delete/expired/leave dialogs, `enable_claim_intent_resume` flag gating, displayName chat title.

**Architecture:** A new KMP `data-claim-intent` module owns the shared resumable-draft queries (get + delete) behind project-owned types. Home carries the draft in its existing Home query (flag-gated via `@include`), the claim-cards pager gains a sealed card type with a new `DraftClaimCard`, and both start-claim entry points (home + inbox) show a `DraftClaimDialog` before the pledge sheet when a draft exists. The claim chat tracks `displayName`/`resumable` through its single `handleNext` seam.

**Tech Stack:** Kotlin/KMP, Jetpack Compose, Apollo GraphQL (octopus), Metro DI, Molecule, Navigation 3, Unleash feature flags, Lokalise compose resources.

**Spec:** `docs/superpowers/specs/2026-07-03-resume-draft-claim-design.md` (approved). Read it for the behavioral rationale; this plan is the how.

## Global Constraints

- Working branch: `feat-resume-claim`. Repo root: `/Users/stylianosgakis/hedvig/apps/android_copy_2`. Run all gradle commands from the repo root.
- Unleash flag key is exactly `enable_claim_intent_resume`; enum entry `ENABLE_CLAIM_INTENT_RESUME`. No bootstrap entry.
- All user-facing copy uses the `RESUME_CLAIM_*` Lokalise keys (verified in Task 1). Exception: the "Started" progress-segment label, which has no Lokalise key; hardcode it with a `// TODO: Add "Started" / "Påbörjad" to Lokalise` comment (iOS hardcodes it too).
- Never call Timber/Log/println; use `logcat` from `:logging-public`.
- GraphQL `octopus.*` types must not leak into public signatures; map to project-owned types inside `internal` impls.
- Kotlin style: 2-space indent, trailing commas, no wildcard imports, max line 120. Run `./gradlew ktlintFormat` before every commit.
- Never use " — " (spaced em-dash) in any prose you write (commit messages, comments): use commas, colons, or parentheses.
- `ClaimIntent.createdAt` is the `DateTime` scalar, mapped to `kotlin.time.Instant` (see `app/apollo/apollo-octopus-public/build.gradle.kts:29`). All `startedAt` fields in this plan are `kotlin.time.Instant`.
- Feature modules must not depend on other feature modules. The new shared code goes in `app/data/data-claim-intent` and `app/ui/claim-status` and `app/design-system/design-system-hedvig`.
- Commit after every task. Commit messages are short imperative sentences without a `feat:` prefix (match `git log` style, e.g. "Add resume-draft-claim design spec").

---

### Task 1: Groundwork: commit schema, download and verify strings

**Files:**
- Modify (commit only): `app/apollo/apollo-octopus-public/src/commonMain/graphql/com/hedvig/android/apollo/octopus/schema.graphqls` (already changed on disk, unstaged)
- Modify (generated): `app/core/core-resources/src/commonMain/composeResources/values*/strings.xml` (via `downloadStrings`)

**Interfaces:**
- Produces: schema fields `ClaimIntent.resumable: Boolean!`, `Mutation.claimIntentDeleteDraft(id: ID!): Boolean!`, and string resources `Res.string.RESUME_CLAIM_*` used by every later task.

- [ ] **Step 1: Verify and commit the schema diff**

Run: `git -C /Users/stylianosgakis/hedvig/apps/android_copy_2 diff --stat -- app/apollo/apollo-octopus-public/src/commonMain/graphql/com/hedvig/android/apollo/octopus/schema.graphqls`
Expected: 1 file changed (the diff adds `resumable: Boolean!` on `ClaimIntent` and the `claimIntentDeleteDraft` mutation).

```bash
cd /Users/stylianosgakis/hedvig/apps/android_copy_2
git add app/apollo/apollo-octopus-public/src/commonMain/graphql/com/hedvig/android/apollo/octopus/schema.graphqls
git commit -m "Download schema"
```

- [ ] **Step 2: Download strings**

Run: `./gradlew downloadStrings`
Expected: BUILD SUCCESSFUL; `strings.xml` files regenerate. (Requires `lokalise.properties`; if the task fails on credentials, stop and ask the user.)

- [ ] **Step 3: Verify the RESUME_CLAIM keys arrived**

Run: `rg -c "RESUME_CLAIM" app/core/core-resources/src/commonMain/composeResources/values/strings.xml`
Expected: a count >= 17 (keys: CONTINUE_BUTTON, DEFAULT_TITLE, DELETE_BODY, DELETE_BUTTON, DELETE_TITLE, DRAFT, DRAFT_ALERT_BODY, DRAFT_ALERT_CONTINUE, DRAFT_ALERT_START_NEW, DRAFT_ALERT_TITLE, EXPIRED_BODY, EXPIRED_TITLE, FALLBACK_TITLE, LEAVE_BODY, LEAVE_CONFIRM, LEAVE_TITLE, STATED).
If any key is missing, STOP and report to the user; do not hardcode substitutes.

Note: `RESUME_CLAIM_STATED` is a format string ("Started %1$s" in the Android export); it takes one argument.

- [ ] **Step 4: Commit the strings**

```bash
git add -A app/core/core-resources
git commit -m "Download strings with RESUME_CLAIM keys"
```

---

### Task 2: Feature flag ENABLE_CLAIM_INTENT_RESUME

**Files:**
- Modify: `app/featureflags/feature-flags/src/commonMain/kotlin/com/hedvig/android/featureflags/flags/Feature.kt`
- Modify: `app/featureflags/feature-flags/src/androidMain/kotlin/com/hedvig/android/featureflags/flags/FeatureUnleashKey.kt`

**Interfaces:**
- Produces: `Feature.ENABLE_CLAIM_INTENT_RESUME` enum entry, read everywhere later via `featureManager.isFeatureEnabled(Feature.ENABLE_CLAIM_INTENT_RESUME): Flow<Boolean>`.

- [ ] **Step 1: Add the enum entry**

In `Feature.kt`, add to the `Feature` enum (alphabetical placement next to the existing entries is fine; match the existing style):

```kotlin
ENABLE_CLAIM_INTENT_RESUME(
  "Enables resuming a draft claim: the draft card on the home screen, the draft-claim dialogs, " +
    "and the resumable-aware leave dialog in the claim chat.",
),
```

- [ ] **Step 2: Map the Unleash key**

In `FeatureUnleashKey.kt`, add to the `when`:

```kotlin
Feature.ENABLE_CLAIM_INTENT_RESUME -> "enable_claim_intent_resume"
```

- [ ] **Step 3: Compile (an exhaustive `when` elsewhere may need the new entry)**

Run: `./gradlew :feature-flags:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL. If any other module fails later on an exhaustive `when` over `Feature`, add the entry there following the file's existing pattern.

No bootstrap change: the flag must resolve to off when Unleash was never fetched, which is the desired default (see `app/featureflags/feature-flags/FEATURE_FLAG_DEFAULTS.md`).

- [ ] **Step 4: Commit**

```bash
./gradlew ktlintFormat
git add -A app/featureflags
git commit -m "Add enable_claim_intent_resume feature flag"
```

---

### Task 3: New data module data-claim-intent

**Files:**
- Create: `app/data/data-claim-intent/build.gradle.kts`
- Create: `app/data/data-claim-intent/src/commonMain/graphql/QueryResumableClaimIntent.graphql`
- Create: `app/data/data-claim-intent/src/commonMain/graphql/MutationClaimIntentDeleteDraft.graphql`
- Create: `app/data/data-claim-intent/src/commonMain/kotlin/com/hedvig/android/data/claimintent/ResumableClaimIntent.kt`
- Create: `app/data/data-claim-intent/src/commonMain/kotlin/com/hedvig/android/data/claimintent/GetResumableClaimIntentUseCase.kt`
- Create: `app/data/data-claim-intent/src/commonMain/kotlin/com/hedvig/android/data/claimintent/DeleteClaimIntentDraftUseCase.kt`

**Interfaces:**
- Produces (public API consumed by Tasks 8, 9, 11):

```kotlin
data class ResumableClaimIntent(val id: String, val displayName: String?, val startedAt: Instant)
interface GetResumableClaimIntentUseCase { suspend fun invoke(): Either<ErrorMessage, ResumableClaimIntent?> }
interface DeleteClaimIntentDraftUseCase { suspend fun invoke(id: String): Either<ErrorMessage, Unit> }
```

Both bound in Metro `AppScope` via `@ContributesBinding`. Type-safe accessor: `projects.dataClaimIntent`.

The module is modeled on `app/data/data-conversations` (KMP, apollo, no test source set; the mapping is trivial and consumer behavior is tested in HomePresenterTest in Task 8).

- [ ] **Step 1: Create the build file**

`app/data/data-claim-intent/build.gradle.kts`:

```kotlin
plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.arrow.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreCommonPublic)
      implementation(projects.loggingPublic)
    }
  }
}
```

The module is auto-discovered by `settings.gradle.kts` (any directory under `app/` with a `build.gradle.kts`).

- [ ] **Step 2: Create the GraphQL operations**

`QueryResumableClaimIntent.graphql`:

```graphql
query ResumableClaimIntent {
  currentMember {
    resumableClaimIntent {
      id
      displayName
      createdAt
    }
  }
}
```

`MutationClaimIntentDeleteDraft.graphql`:

```graphql
mutation ClaimIntentDeleteDraft($id: ID!) {
  claimIntentDeleteDraft(id: $id)
}
```

- [ ] **Step 3: Create the model**

`ResumableClaimIntent.kt`:

```kotlin
package com.hedvig.android.data.claimintent

import kotlin.time.Instant

data class ResumableClaimIntent(
  val id: String,
  val displayName: String?,
  val startedAt: Instant,
)
```

- [ ] **Step 4: Create the get use case**

`GetResumableClaimIntentUseCase.kt`:

```kotlin
package com.hedvig.android.data.claimintent

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import octopus.ResumableClaimIntentQuery

interface GetResumableClaimIntentUseCase {
  /**
   * A null right side means the member has no resumable draft claim.
   */
  suspend fun invoke(): Either<ErrorMessage, ResumableClaimIntent?>
}

@Inject
@ContributesBinding(AppScope::class)
internal class GetResumableClaimIntentUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetResumableClaimIntentUseCase {
  override suspend fun invoke(): Either<ErrorMessage, ResumableClaimIntent?> {
    return either {
      apolloClient
        .query(ResumableClaimIntentQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute()
        .mapLeft { error ->
          logcat(operationError = error) { "GetResumableClaimIntentUseCase failed with $error" }
          ErrorMessage()
        }
        .bind()
        .currentMember
        .resumableClaimIntent
        ?.let { resumableClaimIntent ->
          ResumableClaimIntent(
            id = resumableClaimIntent.id,
            displayName = resumableClaimIntent.displayName,
            startedAt = resumableClaimIntent.createdAt,
          )
        }
    }
  }
}
```

Note: the `logcat(operationError = ...)` overload lives next to `safeExecute` in `:apollo-core` (`com.hedvig.android.apollo`); copy the import that `GetHomeDataUseCase.kt` uses for it. If the memory-cache dependency is missing for `fetchPolicy`, add `implementation(libs.apollo.normalizedCache)` to `commonMain.dependencies`.

- [ ] **Step 5: Create the delete use case**

`DeleteClaimIntentDraftUseCase.kt`:

```kotlin
package com.hedvig.android.data.claimintent

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import octopus.ClaimIntentDeleteDraftMutation

interface DeleteClaimIntentDraftUseCase {
  suspend fun invoke(id: String): Either<ErrorMessage, Unit>
}

@Inject
@ContributesBinding(AppScope::class)
internal class DeleteClaimIntentDraftUseCaseImpl(
  private val apolloClient: ApolloClient,
) : DeleteClaimIntentDraftUseCase {
  override suspend fun invoke(id: String): Either<ErrorMessage, Unit> {
    return either {
      val deleted = apolloClient
        .mutation(ClaimIntentDeleteDraftMutation(id))
        .safeExecute()
        .mapLeft { error ->
          logcat(operationError = error) { "DeleteClaimIntentDraftUseCase failed with $error" }
          ErrorMessage()
        }
        .bind()
        .claimIntentDeleteDraft
      ensure(deleted) { ErrorMessage() }
    }
  }
}
```

- [ ] **Step 6: Compile**

Run: `./gradlew :data-claim-intent:compileKotlinJvm`
Expected: BUILD SUCCESSFUL (Apollo codegen runs first). If the KMP target set differs from data-conversations and `compileKotlinJvm` doesn't exist, run `./gradlew :data-claim-intent:tasks --all | rg compile` and use the main compile task.

- [ ] **Step 7: Commit**

```bash
./gradlew ktlintFormat
git add -A app/data/data-claim-intent
git commit -m "Add data-claim-intent module with resumable draft get and delete use cases"
```

---

### Task 4: Revert StartClaimBottomSheet, add DraftClaimDialog

**Files:**
- Modify (revert to develop): `app/design-system/design-system-hedvig/src/commonMain/kotlin/com/hedvig/android/design/system/hedvig/StartClaimBottomSheet.kt`
- Create: `app/design-system/design-system-hedvig/src/commonMain/kotlin/com/hedvig/android/design/system/hedvig/DraftClaimDialog.kt`
- Modify: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/ui/HomeDestination.kt` (sheet call site, ~lines 230-300)
- Modify: `app/feature/feature-chat/src/main/kotlin/com/hedvig/android/feature/chat/inbox/InboxDestination.kt` (sheet call site, ~lines 130-160)

**Interfaces:**
- Consumes: `Res.string.RESUME_CLAIM_DRAFT_ALERT_*` (Task 1).
- Produces: `StartClaimBottomSheet(state: HedvigBottomSheetState<Unit>, navigateToClaimChat: () -> Unit)` (back to the develop signature; `StartClaimSheetData` is deleted) and:

```kotlin
@Composable
fun DraftClaimDialog(
  onDismissRequest: () -> Unit,
  onContinueDraft: () -> Unit,
  onStartNewClaim: () -> Unit,
  modifier: Modifier = Modifier,
)
```

- [ ] **Step 1: Revert the sheet file to the develop version**

The develop version is exactly the pre-branch state we want (sheet keyed on `Unit`, no draft button, no `StartClaimSheetData`, no stray formatting):

```bash
git checkout develop -- app/design-system/design-system-hedvig/src/commonMain/kotlin/com/hedvig/android/design/system/hedvig/StartClaimBottomSheet.kt
```

- [ ] **Step 2: Create DraftClaimDialog**

`DraftClaimDialog.kt`:

```kotlin
package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import hedvig.resources.RESUME_CLAIM_DRAFT_ALERT_BODY
import hedvig.resources.RESUME_CLAIM_DRAFT_ALERT_CONTINUE
import hedvig.resources.RESUME_CLAIM_DRAFT_ALERT_START_NEW
import hedvig.resources.RESUME_CLAIM_DRAFT_ALERT_TITLE
import hedvig.resources.Res
import hedvig.resources.general_cancel_button
import org.jetbrains.compose.resources.stringResource

@Composable
fun DraftClaimDialog(
  onDismissRequest: () -> Unit,
  onContinueDraft: () -> Unit,
  onStartNewClaim: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigDialog(
    onDismissRequest = onDismissRequest,
    modifier = modifier,
  ) {
    Column {
      HedvigText(
        text = stringResource(Res.string.RESUME_CLAIM_DRAFT_ALERT_TITLE),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(8.dp))
      HedvigText(
        text = stringResource(Res.string.RESUME_CLAIM_DRAFT_ALERT_BODY),
        textAlign = TextAlign.Center,
        color = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(24.dp))
      HedvigButton(
        text = stringResource(Res.string.RESUME_CLAIM_DRAFT_ALERT_CONTINUE),
        onClick = onContinueDraft,
        enabled = true,
        buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(8.dp))
      HedvigButton(
        text = stringResource(Res.string.RESUME_CLAIM_DRAFT_ALERT_START_NEW),
        onClick = onStartNewClaim,
        enabled = true,
        buttonStyle = ButtonDefaults.ButtonStyle.Red,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(8.dp))
      HedvigButton(
        text = stringResource(Res.string.general_cancel_button),
        onClick = onDismissRequest,
        enabled = true,
        buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewDraftClaimDialog() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      DraftClaimDialog({}, {}, {})
    }
  }
}
```

If `HedvigDialog`'s default `style` (`DialogDefaults.defaultDialogStyle`) is not `NoButtons`, pass `style = DialogDefaults.DialogStyle.NoButtons` explicitly (check `Dialog.kt:287` and the `defaultDialogStyle` value).

- [ ] **Step 3: Fix the home call site**

In `HomeDestination.kt`, the current block (~line 230):

```kotlin
val resumableClaimId = (uiState as? Success)?.resumableClaimId
val startClaimBottomSheetState = rememberHedvigBottomSheetState<StartClaimSheetData>()
StartClaimBottomSheet(
  state = startClaimBottomSheetState,
  navigateToOldClaim = {
    navigateToClaimChat(resumableClaimId)
  },
  navigateToClaimChat = {
    navigateToClaimChat(null)
  },
)
```

becomes:

```kotlin
val startClaimBottomSheetState = rememberHedvigBottomSheetState<Unit>()
StartClaimBottomSheet(
  state = startClaimBottomSheetState,
  navigateToClaimChat = {
    navigateToClaimChat(null)
  },
)
```

and further down, `openClaimFlowSheet = { startClaimBottomSheetState.show(StartClaimSheetData(resumableClaimId)) }` becomes `openClaimFlowSheet = { startClaimBottomSheetState.show(Unit) }`. Remove the now-unused `StartClaimSheetData` import. (`navigateToClaimChat` is still `(String?) -> Unit` at this point; Task 6 changes it to `(Boolean) -> Unit`. The draft dialog gets wired in Task 9.)

- [ ] **Step 4: Fix the inbox call site**

In `InboxDestination.kt` (~line 132): change `rememberHedvigBottomSheetState<StartClaimSheetData>()` back to `rememberHedvigBottomSheetState<Unit>()`, `startClaimBottomSheetState.show(StartClaimSheetData(null))` back to `.show(Unit)`, and delete the `navigateToOldClaim = {}` argument and the `StartClaimSheetData` import.

- [ ] **Step 5: Compile all three modules**

Run: `./gradlew :design-system-hedvig:compileDebugKotlinAndroid :feature-home:compileDebugKotlin :feature-chat:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
./gradlew ktlintFormat
git add -A app/design-system app/feature/feature-home app/feature/feature-chat
git commit -m "Revert draft entry from pledge sheet and add DraftClaimDialog"
```

---

### Task 5: Draft card in ui/claim-status

**Files:**
- Create: `app/ui/claim-status/src/main/kotlin/com/hedvig/android/ui/claimstatus/model/ClaimCardUiState.kt`
- Create: `app/ui/claim-status/src/main/kotlin/com/hedvig/android/ui/claimstatus/DraftClaimCard.kt`
- Modify: `app/ui/claim-status/src/main/kotlin/com/hedvig/android/ui/claimstatus/ClaimStatusCards.kt`
- Modify: `app/ui/claim-status/src/main/kotlin/com/hedvig/android/ui/claimstatus/model/ClaimProgressSegment.kt` (add `Started` segment text)
- Modify: `app/ui/claim-status/src/main/kotlin/com/hedvig/android/ui/claimstatus/internal/ClaimProgressRow.kt` (map `Started`)
- Modify: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/ui/HomeDestination.kt` (the `claimStatusCards = { ... }` block, ~line 485)

**Interfaces:**
- Consumes: `Res.string.RESUME_CLAIM_DRAFT`, `RESUME_CLAIM_FALLBACK_TITLE`, `RESUME_CLAIM_STATED`, `RESUME_CLAIM_DELETE_BUTTON`, `RESUME_CLAIM_CONTINUE_BUTTON` (Task 1).
- Produces (consumed by Task 9):

```kotlin
sealed interface ClaimCardUiState {
  data class Claim(val uiState: ClaimStatusCardUiState) : ClaimCardUiState
  data class Draft(val id: String, val title: String?, val startedAt: Instant) : ClaimCardUiState
}

@Composable
fun ClaimStatusCards(
  onClick: (claimId: String) -> Unit,
  onContinueDraftClaim: () -> Unit,
  onDeleteDraftClaim: (draftId: String) -> Unit,
  claimCardsUiState: NonEmptyList<ClaimCardUiState>,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
)
```

- [ ] **Step 1: Add the sealed card type**

`model/ClaimCardUiState.kt`:

```kotlin
package com.hedvig.android.ui.claimstatus.model

import kotlin.time.Instant

sealed interface ClaimCardUiState {
  data class Claim(val uiState: ClaimStatusCardUiState) : ClaimCardUiState

  data class Draft(
    val id: String,
    val title: String?,
    val startedAt: Instant,
  ) : ClaimCardUiState
}
```

- [ ] **Step 2: Add the Started segment label**

In `model/ClaimProgressSegment.kt` change the enum:

```kotlin
enum class SegmentText {
  Submitted,
  BeingHandled,
  Closed,
  Started,
}
```

In `internal/ClaimProgressRow.kt`, the `when (segmentText)` at line ~80 gains:

```kotlin
// TODO: Add "Started" / "Påbörjad" to Lokalise
Started -> "Started"
```

(add the `Started` import next to the existing `Submitted`/`BeingHandled`/`Closed` imports).

- [ ] **Step 3: Create DraftClaimCard**

`DraftClaimCard.kt`:

```kotlin
package com.hedvig.android.ui.claimstatus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Medium
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Primary
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.MEDIUM
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.ui.claimstatus.internal.ClaimProgressRow
import com.hedvig.android.ui.claimstatus.model.ClaimCardUiState
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentText
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentType.INACTIVE
import hedvig.resources.RESUME_CLAIM_CONTINUE_BUTTON
import hedvig.resources.RESUME_CLAIM_DELETE_BUTTON
import hedvig.resources.RESUME_CLAIM_DRAFT
import hedvig.resources.RESUME_CLAIM_FALLBACK_TITLE
import hedvig.resources.RESUME_CLAIM_STATED
import hedvig.resources.Res
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun DraftClaimCard(
  uiState: ClaimCardUiState.Draft,
  onContinueClick: () -> Unit,
  onDeleteClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(modifier = modifier) {
    Column(Modifier.padding(16.dp)) {
      HighlightLabel(
        labelText = stringResource(Res.string.RESUME_CLAIM_DRAFT),
        size = HighLightSize.Small,
        color = HighlightColor.Amber(MEDIUM),
      )
      Spacer(Modifier.height(16.dp))
      HedvigText(
        text = uiState.title ?: stringResource(Res.string.RESUME_CLAIM_FALLBACK_TITLE),
        style = HedvigTheme.typography.bodySmall,
        modifier = Modifier.padding(horizontal = 2.dp),
      )
      val formattedDate = HedvigDateTimeFormatterDefaults
        .dateMonthAndYear(getLocale())
        .format(uiState.startedAt.toLocalDateTime(TimeZone.currentSystemDefault()))
      HedvigText(
        text = stringResource(Res.string.RESUME_CLAIM_STATED, formattedDate),
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier.padding(horizontal = 2.dp),
      )
      Spacer(Modifier.height(18.dp))
      ClaimProgressRow(
        claimProgressItemsUiState = listOf(
          ClaimProgressSegment(SegmentText.Started, INACTIVE),
          ClaimProgressSegment(SegmentText.BeingHandled, INACTIVE),
          ClaimProgressSegment(SegmentText.Closed, INACTIVE),
        ),
      )
      Spacer(Modifier.height(16.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HedvigButton(
          text = stringResource(Res.string.RESUME_CLAIM_DELETE_BUTTON),
          onClick = onDeleteClick,
          enabled = true,
          buttonStyle = Secondary,
          buttonSize = Medium,
          modifier = Modifier.weight(1f),
        )
        HedvigButton(
          text = stringResource(Res.string.RESUME_CLAIM_CONTINUE_BUTTON),
          onClick = onContinueClick,
          enabled = true,
          buttonStyle = Primary,
          buttonSize = Medium,
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewDraftClaimCard() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      DraftClaimCard(
        uiState = ClaimCardUiState.Draft(
          id = "id",
          title = "My things",
          startedAt = Instant.parse("2026-07-02T00:00:00Z"),
        ),
        onContinueClick = {},
        onDeleteClick = {},
      )
    }
  }
}
```

Verify the exact nesting of `HighLightSize`/`HighlightColor`/`HighlightShade` in `HighlightLabel.kt` (they may be top-level in the file rather than inside `HighlightLabelDefaults`); adjust imports to match, keeping `Amber` + medium shade + small size.

- [ ] **Step 4: Rework ClaimStatusCards over the sealed type**

Replace the two branches in `ClaimStatusCards.kt` so both the single-card and pager paths render through one private helper:

```kotlin
@Composable
fun ClaimStatusCards(
  onClick: (claimId: String) -> Unit,
  onContinueDraftClaim: () -> Unit,
  onDeleteDraftClaim: (draftId: String) -> Unit,
  claimCardsUiState: NonEmptyList<ClaimCardUiState>,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  if (claimCardsUiState.size == 1) {
    ClaimCard(
      uiState = claimCardsUiState.first(),
      onClick = onClick,
      onContinueDraftClaim = onContinueDraftClaim,
      onDeleteDraftClaim = onDeleteDraftClaim,
      modifier = modifier.padding(contentPadding),
    )
  } else {
    val pagerState = rememberPagerState(pageCount = { claimCardsUiState.size })
    Column(modifier) {
      HorizontalPager(
        state = pagerState,
        contentPadding = contentPadding,
        beyondViewportPageCount = 1,
        pageSpacing = 8.dp,
        modifier = Modifier.fillMaxWidth().systemGestureExclusion(),
      ) { page: Int ->
        ClaimCard(
          uiState = claimCardsUiState[page],
          onClick = onClick,
          onContinueDraftClaim = onContinueDraftClaim,
          onDeleteDraftClaim = onDeleteDraftClaim,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      Spacer(Modifier.height(16.dp))
      HorizontalPagerIndicator(
        pagerState = pagerState,
        pageCount = claimCardsUiState.size,
        activeColor = LocalContentColor.current,
        modifier = Modifier.padding(contentPadding).align(Alignment.CenterHorizontally),
      )
    }
  }
}

@Composable
private fun ClaimCard(
  uiState: ClaimCardUiState,
  onClick: (claimId: String) -> Unit,
  onContinueDraftClaim: () -> Unit,
  onDeleteDraftClaim: (draftId: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  when (uiState) {
    is ClaimCardUiState.Claim -> ClaimStatusCard(
      uiState = uiState.uiState,
      onClick = onClick,
      modifier = modifier,
    )

    is ClaimCardUiState.Draft -> DraftClaimCard(
      uiState = uiState,
      onContinueClick = onContinueDraftClaim,
      onDeleteClick = { onDeleteDraftClaim(uiState.id) },
      modifier = modifier,
    )
  }
}
```

Update the file's preview to wrap its fake states in `ClaimCardUiState.Claim(...)` and add one `ClaimCardUiState.Draft("id", "My things", Instant.parse("2026-07-02T00:00:00Z"))` first in the list.

- [ ] **Step 5: Update the only external caller (feature-home)**

Run: `rg -n "ClaimStatusCards\(" app/ --type kotlin -g '!*claim-status*'`
Expected: only `HomeDestination.kt`. In its `claimStatusCards = { ... }` block (~line 485), adapt (draft callbacks stay no-ops until Task 9):

```kotlin
claimStatusCards = {
  val claimCards = uiState.claimStatusCardsData?.claimStatusCardsUiState
    ?.map<ClaimCardUiState> { ClaimCardUiState.Claim(it) }
    ?.toNonEmptyListOrNull()
  if (claimCards != null) {
    ClaimStatusCards(
      onClick = onClaimDetailCardClicked,
      onContinueDraftClaim = {},
      onDeleteDraftClaim = {},
      claimCardsUiState = claimCards,
      contentPadding = PaddingValues(horizontal = 16.dp) + horizontalInsets,
    )
  }
},
```

with imports `com.hedvig.android.ui.claimstatus.model.ClaimCardUiState` and `arrow.core.toNonEmptyListOrNull`.

- [ ] **Step 6: Compile**

Run: `./gradlew :claim-status:compileDebugKotlin :feature-home:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit**

```bash
./gradlew ktlintFormat
git add -A app/ui/claim-status app/feature/feature-home
git commit -m "Add draft claim card to the claim status cards pager"
```

---

### Task 6: ClaimChatKey.resumeClaim and navigation plumbing

**Files:**
- Modify: `app/feature/feature-claim-chat/src/androidMain/kotlin/com/hedvig/feature/claim/chat/navigation/ClaimChatEntries.kt`
- Modify: `app/feature/feature-claim-chat/src/commonMain/kotlin/com/hedvig/feature/claim/chat/ClaimChatViewModel.kt`
- Modify: `app/feature/feature-claim-chat/src/commonMain/kotlin/com/hedvig/feature/claim/chat/ui/ClaimChatDestination.kt`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigEntryProvider.kt`
- Modify: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/navigation/HomeEntries.kt`
- Modify: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/ui/HomeDestination.kt`
- Modify: `app/feature/feature-chat/src/main/kotlin/com/hedvig/android/feature/chat/navigation/CbmChatEntries.kt`
- Modify: `app/feature/feature-chat/src/main/kotlin/com/hedvig/android/feature/chat/inbox/InboxDestination.kt`

**Interfaces:**
- Produces: `ClaimChatKey(isDevelopmentFlow: Boolean = false, messageId: String? = null, resumeClaim: Boolean = false)`; all `navigateToClaimChat` lambdas become `(resumeClaim: Boolean) -> Unit`; `ClaimChatViewModelFactory.create(developmentFlow: Boolean, resumeClaim: Boolean)`.
- Rationale: the resume query takes no id (one draft per member; iOS passes no id), and a stale id after process-death restore would be misleading.

- [ ] **Step 1: Change the key**

In `ClaimChatEntries.kt`:

```kotlin
@Serializable
data class ClaimChatKey(
  val isDevelopmentFlow: Boolean = false,
  val messageId: String? = null,
  val resumeClaim: Boolean = false,
) : HedvigNavKey
```

and in the `entry<ClaimChatKey>` block: `resumableClaimId = key.resumableClaimId,` becomes `resumeClaim = key.resumeClaim,`.

- [ ] **Step 2: Change the ViewModel's assisted params**

Two same-typed `@Assisted` Booleans need identifiers (the `:viewmodel-processor` propagates them; see `HedvigViewModelProcessorTest`, the `@Assisted("topicId")` test):

```kotlin
@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class ClaimChatViewModel(
  @Assisted("developmentFlow") developmentFlow: Boolean,
  @Assisted("resumeClaim") resumeClaim: Boolean,
  startClaimIntentUseCase: StartClaimIntentUseCase,
  // ... rest unchanged ...
```

Thread it to the presenter: `ClaimChatPresenter(..., resumeClaim, resumeClaimUseCase)`; in `ClaimChatPresenter` replace `private val resumableClaimId: String?` with `private val resumeClaim: Boolean`, and in the initializing block replace `val isResumingClaim = resumableClaimId != null` with `val isResumingClaim = resumeClaim`.

- [ ] **Step 3: Change the destination**

In `ClaimChatDestination.kt`: parameter `resumableClaimId: String?` becomes `resumeClaim: Boolean`, and the resolution becomes `create(isDevelopmentFlow, resumeClaim)`.

- [ ] **Step 4: Update all navigate lambdas**

- `HedvigEntryProvider.kt` `addHomeEntries` wiring:

```kotlin
navigateToClaimChat = { resumeClaim ->
  backstack.add(
    ClaimChatKey(
      messageId = null,
      isDevelopmentFlow = false,
      resumeClaim = resumeClaim,
    ),
  )
},
```

- `HedvigEntryProvider.kt` `addChatEntries` wiring:

```kotlin
navigateToClaimChat = { resumeClaim ->
  backstack.add(ClaimChatKey(messageId = null, isDevelopmentFlow = false, resumeClaim = resumeClaim))
},
```

- `HomeEntries.kt`: `navigateToClaimChat: (String?) -> Unit` becomes `navigateToClaimChat: (resumeClaim: Boolean) -> Unit`.
- `HomeDestination.kt`: both `navigateToClaimChat: (String?) -> Unit` params become `(resumeClaim: Boolean) -> Unit`; the sheet callback becomes `navigateToClaimChat = { navigateToClaimChat(false) }`.
- `CbmChatEntries.kt`: `navigateToClaimChat: () -> Unit` becomes `navigateToClaimChat: (resumeClaim: Boolean) -> Unit` (passed through to `InboxDestination` unchanged otherwise).
- `InboxDestination.kt`: `navigateToClaimChat: () -> Unit` params become `(resumeClaim: Boolean) -> Unit`; the sheet callback body becomes `navigateToClaimChat(false)`.

Run `rg -n "resumableClaimId" app/` afterwards; the only remaining hits must be inside `feature-claim-chat`'s presenter history (none expected) or nothing at all except `feature-home`'s data layer (`GetHomeDataUseCase.kt`, `HomePresenter.kt`, `HomePresenterTest.kt`), which Task 7 removes.

- [ ] **Step 5: Compile and run the serialization guard**

Run: `./gradlew :feature-claim-chat:compileDebugKotlinAndroid :feature-home:compileDebugKotlin :feature-chat:compileDebugKotlin :app:compileDevelopKotlin`
Expected: BUILD SUCCESSFUL. (If `:app:compileDevelopKotlin` is not a task, use `./gradlew :app:assembleDevelop -x lint`.)

Run: `./gradlew :app:test --tests "*ExhaustiveBackStackSerializationTest*"`
Expected: PASS (the key is still `@Serializable` with defaults; `navKeys()` is already applied in feature-claim-chat).

- [ ] **Step 6: Commit**

```bash
./gradlew ktlintFormat
git add -A app/
git commit -m "Replace ClaimChatKey resumableClaimId with resumeClaim flag"
```

---

### Task 7: Home data layer: DraftClaim in the Home query, flag-gated

**Files:**
- Modify: `app/feature/feature-home/src/main/graphql/QueryHome.graphql`
- Modify: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/data/GetHomeDataUseCase.kt`
- Modify: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/data/GetHomeDataUseCaseDemo.kt`
- Modify: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/ui/HomePresenter.kt`
- Test: `app/feature/feature-home/src/test/kotlin/com/hedvig/android/feature/home/home/ui/HomePresenterTest.kt`

**Interfaces:**
- Consumes: `Feature.ENABLE_CLAIM_INTENT_RESUME` (Task 2).
- Produces (consumed by Tasks 8, 9):

```kotlin
// in HomeData:
val draftClaim: DraftClaim?  // replaces resumableClaimId: String?
data class DraftClaim(val id: String, val displayName: String?, val startedAt: Instant) {
  fun isExpired(now: Instant): Boolean
}
// in HomeUiState.Success:
val draftClaim: HomeData.DraftClaim?  // replaces resumableClaimId: String?
```

- [ ] **Step 1: Extend the query, flag-gated**

`QueryHome.graphql` header and the resumable selection become:

```graphql
query Home($claimsHistoryFlag: Boolean!, $resumeClaimEnabled: Boolean!) {
  currentMember {
    resumableClaimIntent @include(if: $resumeClaimEnabled) {
      id
      displayName
      createdAt
    }
```

(rest of the query unchanged).

- [ ] **Step 2: Read the flag before building the query**

In `GetHomeDataUseCase.kt`, wrap the existing `combine(...)` in a `flatMapLatest` on the flag so the query variable is available (the existing `featureManager` property is already injected):

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
override fun invoke(forceNetworkFetch: Boolean): Flow<Either<ApolloOperationError, HomeData>> {
  return featureManager.isFeatureEnabled(Feature.ENABLE_CLAIM_INTENT_RESUME)
    .flatMapLatest { resumeClaimEnabled ->
      combine(
        apolloClient.query(HomeQuery(true, resumeClaimEnabled))
          .fetchPolicy(if (forceNetworkFetch) FetchPolicy.NetworkOnly else FetchPolicy.CacheAndNetwork)
          .safeFlow(),
        // ... the five other flows, unchanged ...
      ) {
        // ... existing lambda, unchanged except the HomeData construction below ...
      }
    }
}
```

Add imports `kotlinx.coroutines.ExperimentalCoroutinesApi` and `kotlinx.coroutines.flow.flatMapLatest`. In the `HomeData(...)` construction, replace `resumableClaimId = homeQueryData.currentMember.resumableClaimIntent?.id` with:

```kotlin
draftClaim = homeQueryData.currentMember.resumableClaimIntent?.let { resumableClaimIntent ->
  HomeData.DraftClaim(
    id = resumableClaimIntent.id,
    displayName = resumableClaimIntent.displayName,
    startedAt = resumableClaimIntent.createdAt,
  )
},
```

- [ ] **Step 3: Replace the HomeData field**

In the `HomeData` data class, replace `val resumableClaimId: String?` with `val draftClaim: DraftClaim?` and add inside `HomeData`:

```kotlin
data class DraftClaim(
  val id: String,
  val displayName: String?,
  val startedAt: Instant,
) {
  /**
   * Drafts are kept for 7 days on the backend ("Your claim is automatically saved for 7 days").
   * Client-side heuristic, same as iOS.
   */
  fun isExpired(now: Instant): Boolean = now > startedAt + 7.days
}
```

Imports: `kotlin.time.Instant`, `kotlin.time.Duration.Companion.days`.

In `GetHomeDataUseCaseDemo.kt`: `resumableClaimId = null` becomes `draftClaim = null`.

- [ ] **Step 4: Thread through the presenter**

In `HomePresenter.kt`, in `HomeUiState.Success`, `SuccessData`, and both mapping spots (`fromLastState`, the `fromHomeData`-style builder), replace `resumableClaimId: String?` / `resumableClaimId = ...` with `draftClaim: HomeData.DraftClaim?` / `draftClaim = ...` (the value comes from `homeData.draftClaim` and `lastState.draftClaim`).

- [ ] **Step 5: Fix the tests mechanically**

In `HomePresenterTest.kt`, replace every `resumableClaimId = null` with `draftClaim = null` (17 occurrences per the branch diff; use find-replace).

- [ ] **Step 6: Run the tests**

Run: `./gradlew :feature-home:test`
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
./gradlew ktlintFormat
git add -A app/feature/feature-home
git commit -m "Carry flag-gated DraftClaim through the home data layer"
```

---

### Task 8: HomePresenter delete-draft event (TDD)

**Files:**
- Modify: `app/feature/feature-home/build.gradle.kts` (add `implementation(projects.dataClaimIntent)`)
- Modify: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/ui/HomePresenter.kt`
- Modify: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/ui/HomeViewModel.kt`
- Test: `app/feature/feature-home/src/test/kotlin/com/hedvig/android/feature/home/home/ui/HomePresenterTest.kt`

**Interfaces:**
- Consumes: `DeleteClaimIntentDraftUseCase` (Task 3), `HomeData.DraftClaim` (Task 7).
- Produces: `HomeEvent.DeleteDraftClaim(val draftId: String)`; `HomePresenter` constructor gains a trailing `deleteClaimIntentDraftUseCase: DeleteClaimIntentDraftUseCase` param (consumed by Task 9's UI wiring).

- [ ] **Step 1: Add the module dependency**

In `app/feature/feature-home/build.gradle.kts` dependencies block (alphabetical): `implementation(projects.dataClaimIntent)`.

- [ ] **Step 2: Write the failing tests**

Add to `HomePresenterTest.kt` (reuse the existing `TestGetHomeDataUseCase`, `someIrrelevantHomeDataInstance`, `homePresenter.test` pattern; every existing `HomePresenter(...)` construction in the file also gains the new last argument `TestDeleteClaimIntentDraftUseCase()`):

```kotlin
@Test
fun `deleting the draft claim calls the use case and reloads home on success`() = runTest {
  val getHomeDataUseCase = TestGetHomeDataUseCase()
  val deleteClaimIntentDraftUseCase = TestDeleteClaimIntentDraftUseCase()
  val homePresenter = HomePresenter(
    getHomeDataUseCase,
    SeenImportantMessagesStorageImpl(),
    FakeCrossSellHomeNotificationService(),
    ApplicationScope(backgroundScope),
    false,
    deleteClaimIntentDraftUseCase,
  )
  homePresenter.test(HomeUiState.Loading) {
    assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)
    assertThat(getHomeDataUseCase.forceNetworkFetchTurbine.awaitItem()).isFalse()
    getHomeDataUseCase.responseTurbine.add(
      someIrrelevantHomeDataInstance.copy(
        draftClaim = HomeData.DraftClaim("draft-id", "My things", Instant.parse("2026-07-01T00:00:00Z")),
      ).right(),
    )
    assertThat(awaitItem()).isInstanceOf<HomeUiState.Success>()

    sendEvent(HomeEvent.DeleteDraftClaim("draft-id"))
    assertThat(deleteClaimIntentDraftUseCase.deletedIdsTurbine.awaitItem()).isEqualTo("draft-id")
    assertThat(getHomeDataUseCase.forceNetworkFetchTurbine.awaitItem()).isTrue()
  }
}

@Test
fun `a failed draft deletion does not reload home`() = runTest {
  val getHomeDataUseCase = TestGetHomeDataUseCase()
  val deleteClaimIntentDraftUseCase = TestDeleteClaimIntentDraftUseCase().apply {
    result = ErrorMessage().left()
  }
  val homePresenter = HomePresenter(
    getHomeDataUseCase,
    SeenImportantMessagesStorageImpl(),
    FakeCrossSellHomeNotificationService(),
    ApplicationScope(backgroundScope),
    false,
    deleteClaimIntentDraftUseCase,
  )
  homePresenter.test(HomeUiState.Loading) {
    assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)
    assertThat(getHomeDataUseCase.forceNetworkFetchTurbine.awaitItem()).isFalse()
    getHomeDataUseCase.responseTurbine.add(someIrrelevantHomeDataInstance.right())
    assertThat(awaitItem()).isInstanceOf<HomeUiState.Success>()

    sendEvent(HomeEvent.DeleteDraftClaim("draft-id"))
    assertThat(deleteClaimIntentDraftUseCase.deletedIdsTurbine.awaitItem()).isEqualTo("draft-id")
    getHomeDataUseCase.forceNetworkFetchTurbine.expectNoEvents()
  }
}
```

and the fake at the bottom of the file:

```kotlin
private class TestDeleteClaimIntentDraftUseCase : DeleteClaimIntentDraftUseCase {
  val deletedIdsTurbine = Turbine<String>()
  var result: Either<ErrorMessage, Unit> = Unit.right()

  override suspend fun invoke(id: String): Either<ErrorMessage, Unit> {
    deletedIdsTurbine.add(id)
    return result
  }
}
```

Imports needed: `com.hedvig.android.data.claimintent.DeleteClaimIntentDraftUseCase`, `com.hedvig.android.core.common.ErrorMessage`, `kotlin.time.Instant`, `arrow.core.left`.

- [ ] **Step 3: Run the tests to verify they fail**

Run: `./gradlew :feature-home:test --tests "*HomePresenterTest*"`
Expected: FAIL to compile ("no value passed for parameter" / unresolved `DeleteDraftClaim`).

- [ ] **Step 4: Implement**

`HomePresenter.kt`:

```kotlin
internal class HomePresenter(
  private val getHomeDataUseCase: GetHomeDataUseCase,
  private val seenImportantMessagesStorage: SeenImportantMessagesStorage,
  private val crossSellHomeNotificationService: CrossSellHomeNotificationService,
  private val applicationScope: ApplicationScope,
  private val isProduction: Boolean,
  private val deleteClaimIntentDraftUseCase: DeleteClaimIntentDraftUseCase,
) : MoleculePresenter<HomeEvent, HomeUiState> {
```

New event in `HomeEvent`:

```kotlin
data class DeleteDraftClaim(val draftId: String) : HomeEvent
```

In the `CollectEvents` block:

```kotlin
is HomeEvent.DeleteDraftClaim -> {
  launch {
    deleteClaimIntentDraftUseCase.invoke(homeEvent.draftId).fold(
      ifLeft = { logcat(LogPriority.ERROR) { "Failed to delete draft claim: $it" } },
      ifRight = { loadIteration++ },
    )
  }
}
```

`HomeViewModel.kt`: add `deleteClaimIntentDraftUseCase: DeleteClaimIntentDraftUseCase` to the constructor and pass it as the presenter's last argument (Metro resolves it; no other DI wiring needed).

- [ ] **Step 5: Run the tests to verify they pass**

Run: `./gradlew :feature-home:test`
Expected: PASS (all of HomePresenterTest, including the two new tests).

- [ ] **Step 6: Commit**

```bash
./gradlew ktlintFormat
git add -A app/feature/feature-home
git commit -m "Add delete-draft-claim event to the home presenter"
```

---

### Task 9: Home UI wiring: draft card, dialogs, draft-aware start flow

**Files:**
- Modify: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/ui/HomeDestination.kt`

**Interfaces:**
- Consumes: `ClaimCardUiState`/`ClaimStatusCards`/`DraftClaimCard` (Task 5), `DraftClaimDialog` (Task 4), `HomeUiState.Success.draftClaim` + `DraftClaim.isExpired` (Task 7), `HomeEvent.DeleteDraftClaim` (Task 8), `navigateToClaimChat: (Boolean) -> Unit` (Task 6), `Res.string.RESUME_CLAIM_DELETE_*` and `RESUME_CLAIM_EXPIRED_*` (Task 1).
- Produces: complete home-side behavior; nothing new for later tasks.

- [ ] **Step 1: Add dialog state and the draft branch in HomeScreen**

In the `HomeScreen` composable (where `startClaimBottomSheetState` lives), add:

```kotlin
val draftClaim = (uiState as? Success)?.draftClaim
var showDraftClaimDialog by remember { mutableStateOf(false) }
var showDraftExpiredDialog by remember { mutableStateOf(false) }
var draftIdPendingDeleteConfirmation by remember { mutableStateOf<String?>(null) }
```

Below the `StartClaimBottomSheet(...)` call, add the three dialogs:

```kotlin
if (showDraftClaimDialog) {
  DraftClaimDialog(
    onDismissRequest = { showDraftClaimDialog = false },
    onContinueDraft = {
      showDraftClaimDialog = false
      navigateToClaimChat(true)
    },
    onStartNewClaim = {
      showDraftClaimDialog = false
      startClaimBottomSheetState.show(Unit)
    },
  )
}
if (showDraftExpiredDialog) {
  ErrorDialog(
    title = stringResource(Res.string.RESUME_CLAIM_EXPIRED_TITLE),
    message = stringResource(Res.string.RESUME_CLAIM_EXPIRED_BODY),
    onDismiss = { showDraftExpiredDialog = false },
  )
}
val draftIdToDelete = draftIdPendingDeleteConfirmation
if (draftIdToDelete != null) {
  HedvigAlertDialog(
    title = stringResource(Res.string.RESUME_CLAIM_DELETE_TITLE),
    text = stringResource(Res.string.RESUME_CLAIM_DELETE_BODY),
    confirmButtonLabel = stringResource(Res.string.RESUME_CLAIM_DELETE_BUTTON),
    dismissButtonLabel = stringResource(Res.string.general_cancel_button),
    onDismissRequest = { draftIdPendingDeleteConfirmation = null },
    onConfirmClick = {
      draftIdPendingDeleteConfirmation = null
      deleteDraftClaim(draftIdToDelete)
    },
  )
}
```

`ErrorDialog` and `HedvigAlertDialog` come from `com.hedvig.android.design.system.hedvig` (same components the claim chat uses).

- [ ] **Step 2: Thread the callbacks**

- `HomeDestination` (top-level composable): create `deleteDraftClaim = { draftId: String -> viewModel.emit(HomeEvent.DeleteDraftClaim(draftId)) }` and pass it into `HomeScreen` as a new `deleteDraftClaim: (String) -> Unit` parameter (follow the pattern of the existing `reload`/`markMessageAsSeen` lambdas; if events are sent with a different method name than `emit`, match whatever `reload` uses).
- `openClaimFlowSheet` becomes draft-aware:

```kotlin
openClaimFlowSheet = {
  if (draftClaim != null) {
    showDraftClaimDialog = true
  } else {
    startClaimBottomSheetState.show(Unit)
  }
},
```

- Add two new params to `HomeScreenSuccess` and thread them into the `claimStatusCards = { ... }` slot: `onContinueDraftClaim: () -> Unit` and `onDeleteDraftClaim: (String) -> Unit`, built in `HomeScreen` as:

```kotlin
onContinueDraftClaim = {
  if (draftClaim != null) {
    if (draftClaim.isExpired(Clock.System.now())) {
      showDraftExpiredDialog = true
    } else {
      navigateToClaimChat(true)
    }
  }
},
onDeleteDraftClaim = { draftId -> draftIdPendingDeleteConfirmation = draftId },
```

(`Clock` is `kotlin.time.Clock`, matching the `kotlin.time.Instant` in `DraftClaim`.)

- [ ] **Step 3: Put the draft card first in the cards list**

In the `claimStatusCards = { ... }` block (Task 5 left the callbacks as no-ops), build the combined list:

```kotlin
claimStatusCards = {
  val claimCards: NonEmptyList<ClaimCardUiState>? = buildList {
    uiState.draftClaim?.let { draftClaim ->
      add(ClaimCardUiState.Draft(draftClaim.id, draftClaim.displayName, draftClaim.startedAt))
    }
    uiState.claimStatusCardsData?.claimStatusCardsUiState?.forEach { add(ClaimCardUiState.Claim(it)) }
  }.toNonEmptyListOrNull()
  if (claimCards != null) {
    ClaimStatusCards(
      onClick = onClaimDetailCardClicked,
      onContinueDraftClaim = onContinueDraftClaim,
      onDeleteDraftClaim = onDeleteDraftClaim,
      claimCardsUiState = claimCards,
      contentPadding = PaddingValues(horizontal = 16.dp) + horizontalInsets,
    )
  }
},
```

Update the two `PreviewHomeScreen*` fakes for any new required params (previews construct `HomeUiState.Success`, which already has `draftClaim` from Task 7).

- [ ] **Step 4: Compile and eyeball previews**

Run: `./gradlew :feature-home:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
./gradlew ktlintFormat
git add -A app/feature/feature-home
git commit -m "Wire draft claim card, delete and expired dialogs on home"
```

---

### Task 10: Claim chat: displayName title, resumable-gated leave dialog

**Files:**
- Modify: `app/feature/feature-claim-chat/build.gradle.kts` (add `implementation(projects.featureFlags)` to `commonMain.dependencies`)
- Modify: `app/feature/feature-claim-chat/src/commonMain/graphql/FragmentClaimIntent.graphql`
- Modify: `app/feature/feature-claim-chat/src/commonMain/kotlin/com/hedvig/feature/claim/chat/data/ClaimIntent.kt`
- Modify: `app/feature/feature-claim-chat/src/commonMain/kotlin/com/hedvig/feature/claim/chat/data/ClaimIntentExt.kt`
- Modify: `app/feature/feature-claim-chat/src/commonMain/kotlin/com/hedvig/feature/claim/chat/ClaimChatViewModel.kt`
- Modify: `app/feature/feature-claim-chat/src/commonMain/kotlin/com/hedvig/feature/claim/chat/ui/ClaimChatDestination.kt`

**Interfaces:**
- Consumes: `Feature.ENABLE_CLAIM_INTENT_RESUME` (Task 2), schema `resumable` field (Task 1).
- Produces: `ClaimChatUiState.ClaimChat` gains `title: String?`, `isResumable: Boolean`, `resumeClaimEnabled: Boolean`. Internal `ClaimIntent` gains `displayName: String?`, `resumable: Boolean`.

- [ ] **Step 1: Fragment fields**

In `FragmentClaimIntent.graphql`, inside `fragment ClaimIntentFragment on ClaimIntent`, add two fields next to `progress`:

```graphql
  displayName
  resumable
```

- [ ] **Step 2: Model + mapping**

`ClaimIntent.kt`:

```kotlin
internal data class ClaimIntent(
  val id: ClaimIntentId,
  val next: Next,
  val progress: Float?,
  val displayName: String?,
  val resumable: Boolean,
  val previousSteps: List<ClaimIntentStep>,
) {
```

`ClaimIntentExt.kt` `toClaimIntent`: add `displayName = displayName,` and `resumable = resumable,` to the `ClaimIntent(...)` construction.

- [ ] **Step 3: Feature flag into the presenter**

`build.gradle.kts` commonMain deps: `implementation(projects.featureFlags)` (alphabetical).

`ClaimChatViewModel`: add `featureManager: FeatureManager` to the constructor (a normal Metro dependency, placed with the other non-assisted params) and pass it to `ClaimChatPresenter`, which stores `private val featureManager: FeatureManager`.

- [ ] **Step 4: Track title/isResumable through handleNext**

In `ClaimChatPresenter.present`, next to the existing `progress` state (~line 250):

```kotlin
var title by remember { mutableStateOf((lastState as? ClaimChatUiState.ClaimChat)?.title) }
var isResumable by remember {
  mutableStateOf((lastState as? ClaimChatUiState.ClaimChat)?.isResumable ?: false)
}
val updateIntentMetadata: (ClaimIntent) -> Unit = { intent ->
  progress = intent.progress
  // Keep the previous title when a step comes back without one, matching iOS.
  title = intent.displayName ?: title
  isResumable = intent.resumable
}
val resumeClaimEnabled by remember {
  featureManager.isFeatureEnabled(Feature.ENABLE_CLAIM_INTENT_RESUME)
}.collectAsState(initial = false)
```

(import `androidx.compose.runtime.collectAsState`, `com.hedvig.android.featureflags.FeatureManager`, `com.hedvig.android.featureflags.flags.Feature`.)

Change the top-level `handleNext` signature (line ~1074) from `setProgress: (Float?) -> Unit` to `updateIntentMetadata: (ClaimIntent) -> Unit`, and its body's `setProgress(intent.progress)` to `updateIntentMetadata(intent)`. Update every call site from `) { progress = it }` to `, updateIntentMetadata)` (9 hits; verify with `rg -n "handleNext\(" app/feature/feature-claim-chat/src/commonMain/kotlin/com/hedvig/feature/claim/chat/ClaimChatViewModel.kt`; they are `handleNext(steps, setOutcome, claimIntent) { progress = it }` today).

In the initializing block, in BOTH the resume and start branches, replace `progress = claimIntent.progress` with `updateIntentMetadata(claimIntent)`.

Extend the returned state:

```kotlin
claimIntentId != null -> ClaimChatUiState.ClaimChat(
  // ... existing args ...
  title = title,
  isResumable = isResumable,
  resumeClaimEnabled = resumeClaimEnabled,
)
```

and the `ClaimChatUiState.ClaimChat` data class gains:

```kotlin
val title: String?,
val isResumable: Boolean,
val resumeClaimEnabled: Boolean,
```

- [ ] **Step 5: Title and leave dialog in the destination**

In `ClaimChatDestination.kt`:

Title (line ~359):

```kotlin
val legacyTitle = stringResource(Res.string.CHAT_CONVERSATION_CLAIM_TITLE)
val title = if (uiState.resumeClaimEnabled) uiState.title ?: legacyTitle else legacyTitle
```

Leave-confirmation gating: define next to `showCloseFlowDialog` (line ~279):

```kotlin
// Flag on: only a resumable draft warrants a leave confirmation (it will be saved).
// Flag off: legacy behavior, always confirm.
val showLeaveConfirmation = if (uiState.resumeClaimEnabled) uiState.isResumable else true
```

- `NavigationEventHandler`'s `isBackEnabled = uiState.steps.size > 1` becomes `isBackEnabled = uiState.steps.size > 1 && showLeaveConfirmation`.
- The `TopAppBar` `onActionClick` condition `if (uiState.steps.size > 1)` becomes `if (uiState.steps.size > 1 && showLeaveConfirmation)`.
- The dialog itself (line ~323) becomes:

```kotlin
if (showCloseFlowDialog) {
  if (uiState.resumeClaimEnabled) {
    HedvigAlertDialog(
      title = stringResource(Res.string.RESUME_CLAIM_LEAVE_TITLE),
      text = stringResource(Res.string.RESUME_CLAIM_LEAVE_BODY),
      confirmButtonLabel = stringResource(Res.string.RESUME_CLAIM_LEAVE_CONFIRM),
      onDismissRequest = { showCloseFlowDialog = false },
      onConfirmClick = navigateUp,
    )
  } else {
    HedvigAlertDialog(
      title = stringResource(Res.string.GENERAL_ARE_YOU_SURE),
      text = stringResource(Res.string.claims_alert_body),
      onDismissRequest = { showCloseFlowDialog = false },
      onConfirmClick = navigateUp,
    )
  }
}
```

(The dismiss label defaults to `GENERAL_NO`, which matches "No". This removes the hardcoded "Your answers will be saved in a draft claim" TODO line and its commented-out predecessor.)

- [ ] **Step 6: Compile and test**

Run: `./gradlew :feature-claim-chat:compileDebugKotlinAndroid :feature-claim-chat:test`
Expected: BUILD SUCCESSFUL, tests (if the module has any) PASS.

- [ ] **Step 7: Commit**

```bash
./gradlew ktlintFormat
git add -A app/feature/feature-claim-chat
git commit -m "Use displayName title and resumable-gated leave dialog in claim chat"
```

---

### Task 11: Inbox draft handling

**Files:**
- Modify: `app/feature/feature-chat/build.gradle.kts` (add `implementation(projects.dataClaimIntent)`)
- Modify: `app/feature/feature-chat/src/main/kotlin/com/hedvig/android/feature/chat/inbox/InboxViewModel.kt`
- Modify: `app/feature/feature-chat/src/main/kotlin/com/hedvig/android/feature/chat/inbox/InboxDestination.kt`

**Interfaces:**
- Consumes: `GetResumableClaimIntentUseCase` (Task 3), `Feature.ENABLE_CLAIM_INTENT_RESUME` (Task 2), `DraftClaimDialog` (Task 4), `navigateToClaimChat: (Boolean) -> Unit` (Task 6).
- Produces: `InboxUiState.Success` gains `hasDraftClaim: Boolean`.

- [ ] **Step 1: Dependency**

`app/feature/feature-chat/build.gradle.kts`: add `implementation(projects.dataClaimIntent)` (alphabetical).

- [ ] **Step 2: Fetch the draft in InboxPresenter**

`InboxViewModel.kt`:

```kotlin
@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class InboxViewModel(
  getAllConversationsUseCase: GetAllConversationsUseCase,
  featureManager: FeatureManager,
  getResumableClaimIntentUseCase: GetResumableClaimIntentUseCase,
) : MoleculeViewModel<InboxEvent, InboxUiState>(
    initialState = InboxUiState.Loading,
    presenter = InboxPresenter(getAllConversationsUseCase, featureManager, getResumableClaimIntentUseCase),
  )

internal class InboxPresenter(
  private val getAllConversationsUseCase: GetAllConversationsUseCase,
  private val featureManager: FeatureManager,
  private val getResumableClaimIntentUseCase: GetResumableClaimIntentUseCase,
) : MoleculePresenter<InboxEvent, InboxUiState> {
```

Inside `LaunchedEffect(loadIteration)`, before the existing `combine`:

```kotlin
val hasDraftClaim = if (featureManager.isFeatureEnabled(Feature.ENABLE_CLAIM_INTENT_RESUME).first()) {
  getResumableClaimIntentUseCase.invoke().fold(
    // A failed draft lookup must not block starting a claim; treat it as no draft.
    ifLeft = { false },
    ifRight = { it != null },
  )
} else {
  false
}
```

and extend the success state construction: `currentState = InboxUiState.Success(conversations, newChatButtonAvailable, hasDraftClaim)`. `InboxUiState.Success` gains `val hasDraftClaim: Boolean`. Imports: `com.hedvig.android.data.claimintent.GetResumableClaimIntentUseCase`, `kotlinx.coroutines.flow.first`.

- [ ] **Step 3: Show the dialog before the pledge sheet**

In `InboxDestination.kt`'s `InboxScreen`, add next to the sheet states:

```kotlin
var showDraftClaimDialog by remember { mutableStateOf(false) }
if (showDraftClaimDialog) {
  DraftClaimDialog(
    onDismissRequest = { showDraftClaimDialog = false },
    onContinueDraft = {
      showDraftClaimDialog = false
      navigateToClaimChat(true)
    },
    onStartNewClaim = {
      showDraftClaimDialog = false
      startClaimBottomSheetState.show(Unit)
    },
  )
}
```

and change the `onStartNewClaim` callback inside the new-chat-select sheet:

```kotlin
onStartNewClaim = {
  newChatSelectBottomSheetState.dismiss()
  if ((uiState as? InboxUiState.Success)?.hasDraftClaim == true) {
    showDraftClaimDialog = true
  } else {
    startClaimBottomSheetState.show(Unit)
  }
},
```

Import `com.hedvig.android.design.system.hedvig.DraftClaimDialog` plus `androidx.compose.runtime.getValue/mutableStateOf/remember/setValue` as needed.

- [ ] **Step 4: Compile and test**

Run: `./gradlew :feature-chat:compileDebugKotlin :feature-chat:test`
Expected: BUILD SUCCESSFUL; existing tests PASS. If an `InboxPresenterTest` exists, add the fake use case there the same way Task 8 did for home; if none exists, do not create new test infrastructure (the branching logic mirrors home's, which is tested).

- [ ] **Step 5: Commit**

```bash
./gradlew ktlintFormat
git add -A app/feature/feature-chat
git commit -m "Show draft claim dialog from the inbox start-claim entry"
```

---

### Task 12: Cleanups and full verification

**Files:**
- Modify: `app/feature/feature-claim-chat/src/commonMain/kotlin/com/hedvig/feature/claim/chat/data/ResumeClaimUseCase.kt`
- Modify: `app/feature/feature-claim-chat/src/commonMain/kotlin/com/hedvig/feature/claim/chat/data/ClaimIntentExt.kt`

**Interfaces:** none new; this task closes out branch debt and verifies the whole feature.

- [ ] **Step 1: Fix the copy-pasted log tag**

In `ResumeClaimUseCase.kt`, `logcat { "StartClaimIntentUseCase error: $it" }` becomes `logcat { "ResumeClaimUseCase error: $it" }`. Also switch it to the Apollo overload for consistency: `logcat(operationError = it) { "ResumeClaimUseCase failed with $it" }` (same import as in Task 3's use cases).

- [ ] **Step 2: Resolve the isPrepared TODO**

In `ClaimIntentExt.kt`, the resumed-audio mapping has `isPrepared = true, // TODO: check`. Replace the comment:

```kotlin
AudioRecordingStepState.AudioRecording.Playback(
  audioPath = AudioPath.RemoteUrl(audioUrl),
  isPlaying = false,
  // A resumed remote recording has no local MediaPlayer to prepare; the remote audio player
  // handles its own buffering, so the playback UI can show immediately.
  isPrepared = true,
  hasError = false,
)
```

Verify during manual QA (Step 5) that a resumed audio step actually plays via `PlayableAudioSource.RemoteUrl`. The `AudioRecordingBottomSheet` returning a `null` player for remote URLs is fine: that sheet is only reachable while recording, before a step was ever submitted.

- [ ] **Step 3: Format and full test run**

```bash
./gradlew ktlintFormat
./gradlew ktlintCheck
./gradlew test
```

Expected: all PASS (includes `ExhaustiveBackStackSerializationTest` and `BackstackTest` in `:app`, `HomePresenterTest`, and the feature-claim-chat tests).

- [ ] **Step 4: Full app assembly**

Run: `./gradlew :app:assembleDevelop`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Manual QA checklist (staging build, flag on in Unleash)**

Home refresh mechanics note: `MoleculeViewModel` uses `SharingStarted.WhileSubscribed(5.seconds)`, so leaving home for the claim chat for more than 5 seconds stops the home presenter; returning restarts it and re-runs the Home query with `CacheAndNetwork`. This is the expected refresh-on-return mechanism; verify it in item 2 and only add an explicit reload trigger if it does not hold.

1. Start a claim, answer a step, leave via the top-bar X: "Leave claim?" dialog with "No" / "Yes, leave" appears only once a resumable step was reached.
2. Back on home: the Draft card appears first in the cards carousel (amber Draft pill, title, "Started {date}", inactive segments). If it does not appear without a manual refresh, add `viewModel.emit(HomeEvent.RefreshData)` in a `LifecycleResumeEffect` and re-verify.
3. Card "Continue": resumes the chat with previous steps replayed, no honesty pledge, title from displayName.
4. Card "Delete": "Delete draft?" confirm, then the card disappears.
5. Home "Make a claim" with a draft: "You have a draft claim" dialog; "Continue draft" resumes; "Start new claim" opens the pledge sheet; "Cancel" does nothing.
6. Inbox → new conversation → "Start claim" with a draft: same dialog behavior.
7. A draft whose current step is a deflect outcome resumes into the deflect screen.
8. Resumed audio step: remote recording plays; resumed free-text step shows the saved text.
9. Flag off in Unleash: no draft card, no draft dialogs, legacy chat title, legacy always-on leave dialog.
10. Demo mode: home loads with no draft card.

- [ ] **Step 6: Commit**

```bash
git add -A app/
git commit -m "Clean up resume-claim leftovers"
```
