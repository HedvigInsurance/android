# Termination Survey Redirection Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Adopt the backend `terminationSurvey` "redirection" capability so that, when a survey option offers an alternative to cancelling (today only "moving to a new home" → the moving flow), we show a dedicated full-screen interstitial ("Before we continue") offering that alternative before the member proceeds with cancellation.

**Architecture:** The change is client-opt-in. We start sending `redirectionEnabled: true` on the `TerminationSurvey` query. When enabled, the backend removes the address sub-option from a parent option (e.g. `MOVING`) and returns a `redirection` object on that parent. We map it into a project-owned `SurveyOptionRedirection` model. When the member selects an option that carries a redirection and taps Continue, we navigate to a new full-screen destination (`TerminationRedirectionKey`) that shows the offer image, title, description, a primary CTA into the moving flow (`actionText` from the backend), and a secondary "Continue cancelling" that resumes the normal survey flow.

**Tech Stack:** Kotlin, Apollo GraphQL (octopus schema), Molecule presenter, Jetpack Compose, Coil (image), Metro DI, Navigation 3 (`HedvigNavKey`), kotlinx.serialization.

**Design:** Figma "App P2 2026", node `645-1942` (three frames: survey list → "Before we continue" interstitial → moving sub-options). https://www.figma.com/design/SogcacjzOxkCC46XcZP8lQ/App-P2-2026?node-id=645-1942&m=dev

## Global Constraints

- DI is **Metro** (`dev.zacsweers.metro`). No Koin, no `get()`. `TerminateInsuranceRepositoryImpl` is already `@ContributesBinding(AppScope::class) @SingleIn(AppScope::class) @Inject`; do not change its wiring.
- Navigation is **Navigation 3**: destinations are `@Serializable HedvigNavKey`s, registered via `entry<Key> { }` in `TerminateInsuranceEntries`. No `NavController`/routes. The module already has `navKeys()`, so a new `@Serializable` key is auto-registered for process-death survival.
- `backstack.navigateUp()` is **only** for a screen's top-app-bar back arrow. The interstitial's in-content buttons ("See price for new home", "Continue cancelling") use `backstack.add(...)` / `popBackstack()` semantics, never `navigateUp()`.
- **Never expose Apollo/`octopus.*` types in public API.** The `redirection` shape is mapped into `SurveyOptionRedirection` inside the `internal` repository impl before leaving the data layer.
- All logging goes through `logcat` from `:logging-public`. Never Timber/`Log`/`println`.
- **Strings are Lokalise-managed.** Do not add keys to `strings.xml`. The primary button label is backend-provided (`actionText`). Any client-owned copy we still need ("Continue cancelling", the "Before we continue" heading if no existing key fits) is hardcoded in English with a `// TODO: Add "<EN>" / "<SV>" to Lokalise` comment until synced.
- Run `./gradlew ktlintFormat` before every commit. Indent 2 spaces, max line 120, trailing commas on.

---

## Prerequisites & Decisions

Depends on the **unmerged backend PR** [underwriter#1741](https://github.com/HedvigInsurance/underwriter/pull/1741). The new `redirectionEnabled` argument, the `redirection` field, and the redirection types do **not** exist in our octopus schema yet (`schema.graphqls` still has `terminationSurvey(contractId: UUID!)` and no redirection types). **Task 2 onward cannot compile until the backend PR merges and reaches the octopus gateway.**

Decisions locked with Sladan (Slack, 2026-07-29):

1. **Dedicated enum.** `redirection.type` is a new backend enum `TerminationFlowSurveyOptionRedirectionType`, currently only `UPDATE_ADDRESS` (not the broad `TerminationFlowSurveyOptionSuggestionType`). We mirror it as a small project-owned `RedirectionType { UPDATE_ADDRESS, UNKNOWN }`. Apollo still generates an `UNKNOWN__` sentinel, so the mapper needs an `else -> RedirectionType.UNKNOWN`; the UI wires only `UPDATE_ADDRESS` and treats `UNKNOWN` as "no CTA / do not offer".
2. **Image is nullable; render nothing when null.** Backend currently sends `image = null` (Storyblok image pending access). The design shows the image prominently but it is not structurally required; when `image == null` we omit the image block and show title + description + buttons.

**Rollout decision (recommended, confirm before Task 2):** send `redirectionEnabled: true` **unconditionally** rather than behind an Unleash flag. The argument is itself the switch, the change is reversible in one line, and it ships atomically with the UI that renders the field. If product wants a staged rollout, gate the *argument value* behind an Unleash flag instead (out of scope unless asked).

**Task ordering:** Task 1 (model + presenter test) is first and **backend-independent** (fake repository, project-owned models), so the model/navigation shape can land and be reviewed before the backend is live. Only Task 2 (query + mapping) needs the merged schema.

---

## The flow being built

Today, `TerminationSurveyViewModel`'s `Continue` handler does: if the selected option has sub-options → push `TerminationSurveySecondStepKey`; else → `navigateAfterSurvey(...)` (deflect / date / delete). The address option is currently a deep sub-option shown as an inline `HedvigNotificationCard` CTA.

After this change, when `redirectionEnabled: true`, the backend promotes the address sub-option into a `redirection` on the parent `MOVING` option. The new flow:

- Member selects "Moving to a new home" → taps **Continue**.
- Because the selected option has a `redirection`, we push **`TerminationRedirectionKey`** (the "Before we continue" screen) instead of going straight to sub-options.
- On that screen: **"See price for new home"** (`redirection.actionText`) → `navigateToMovingFlow()`; **"Continue cancelling"** → resumes exactly what Continue would have done without the redirection (sub-options → second step, else deflect/date/delete).

To keep "Continue cancelling" behaviourally identical to a plain Continue, the sub-options-vs-`navigateAfterSurvey` branch is extracted into one shared function reused by both the ViewModel and the interstitial entry.

---

## File Structure

- `.../data/TerminationSurveyOption.kt` — add `SurveyOptionRedirection`, `RedirectionImage`, `RedirectionType`, and a `redirection` field on `TerminationSurveyOption`. (Task 1)
- `.../step/survey/TerminationSurveyViewModel.kt` — intercept `Continue` when a redirection is present; extract the shared post-selection navigation function. (Task 1)
- `.../navigation/TerminateInsuranceDestination.kt` — add `TerminationRedirectionKey`. (Task 1)
- `.../step/survey/TerminationSurveyPresenterTest.kt` — test that Continue on a redirection option pushes `TerminationRedirectionKey`. (Task 1)
- `.../src/main/graphql/QueryTerminationSurvey.graphql` — send `redirectionEnabled: true`; select `redirection { ... }` on top-level options. (Task 2)
- `.../data/TerminateInsuranceRepository.kt` — map `redirection` (top-level options only). (Task 2)
- `.../step/redirection/TerminationRedirectionDestination.kt` — new full-screen interstitial composable. (Task 3)
- `.../navigation/TerminateInsuranceEntries.kt` — register the `entry<TerminationRedirectionKey>`. (Task 3)

(Base package: `app/feature/feature-terminate-insurance/src/main/kotlin/com/hedvig/android/feature/terminateinsurance/`)

---

## Task 1: Model + navigation key + Continue interception (backend-independent)

**Files:**
- Modify: `.../data/TerminationSurveyOption.kt`
- Modify: `.../step/survey/TerminationSurveyViewModel.kt`
- Modify: `.../navigation/TerminateInsuranceDestination.kt`
- Test: `.../step/survey/TerminationSurveyPresenterTest.kt`

**Interfaces:**
- Produces:
  - `TerminationSurveyOption` gains `val redirection: SurveyOptionRedirection? = null` (defaulted so existing constructors compile unchanged).
  - `data class SurveyOptionRedirection(val title: String, val description: String, val type: RedirectionType, val actionText: String, val image: RedirectionImage?)`
  - `data class RedirectionImage(val url: String, val overlayText: String?)`
  - `enum class RedirectionType { UPDATE_ADDRESS, UNKNOWN }`
  - `TerminationRedirectionKey(redirection, selectedOption, action, commonParams, feedbackComment)` nav key.
  - `internal fun Backstack.continueAfterSurveySelection(selectedOption, feedbackText, action, commonParams)` — the shared post-selection navigation (sub-options → second step, else `navigateAfterSurvey`).
- Consumes: existing `TerminationAction`, `TerminationGraphParameters`, `TerminationSurveySecondStepKey`, and the private `navigateAfterSurvey` (folded into the shared function).

- [ ] **Step 1: Write the failing test**

Add to `TerminationSurveyPresenterTest`, mirroring the existing `presenter.test { }` setup and the `TestBackstack` used by the neighbouring tests. Select an option that has a redirection and a sub-option, emit `Continue`, and assert the interstitial key was pushed.

```kotlin
@Test
fun `continuing on an option with a redirection navigates to the redirection interstitial`() = runTest {
  val redirection = SurveyOptionRedirection(
    title = "Bring Hedvig to your new home",
    description = "Move your insurance to your new home and get 15% off the first year",
    type = RedirectionType.UPDATE_ADDRESS,
    actionText = "See price for new home",
    image = null,
  )
  val movingOption = TerminationSurveyOption(
    id = "MOVING",
    listIndex = 0,
    title = "Moving to a new home",
    feedbackRequired = false,
    suggestion = null,
    subOptions = listOf(
      TerminationSurveyOption(
        id = "MOVED_IN_WITH_SOMEONE",
        listIndex = 0,
        title = "I have moved in with someone else",
        feedbackRequired = false,
        suggestion = null,
        subOptions = emptyList(),
      ),
    ),
    redirection = redirection,
  )
  val backstack = TestBackstack()
  val presenter = TerminationSurveyPresenter(
    listOf(movingOption), testAction, testCommonParams, backstack, FakeChangeTierRepository(),
  )

  presenter.test(TerminationSurveyState(listOf(movingOption))) {
    awaitItem()
    sendEvent(TerminationSurveyEvent.SelectOption(movingOption))
    awaitItem()
    sendEvent(TerminationSurveyEvent.Continue)
    val pushed = backstack.lastPushedKey() // adapt to TestBackstack's actual accessor
    assertThat(pushed).isInstanceOf(TerminationRedirectionKey::class)
  }
}
```

> Adapt `backstack.lastPushedKey()` to whatever `TestBackstack` already exposes in this test file (the existing tests already assert on pushed keys such as `TerminationDateKey` / `TerminationSurveySecondStepKey`; reuse that mechanism verbatim).

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :feature-terminate-insurance:testDebugUnitTest --tests "*TerminationSurveyPresenterTest*"`
Expected: compile failure — `SurveyOptionRedirection`, `RedirectionType`, `TerminationRedirectionKey`, and the `redirection` parameter are unresolved.

- [ ] **Step 3: Add the data models**

In `TerminationSurveyOption.kt`:

```kotlin
@Serializable
internal data class TerminationSurveyOption(
  val id: String,
  val listIndex: Int,
  val title: String,
  val feedbackRequired: Boolean,
  val suggestion: SurveyOptionSuggestion?,
  val subOptions: List<TerminationSurveyOption>,
  val redirection: SurveyOptionRedirection? = null,
  val isDisabled: Boolean = false,
)

@Serializable
internal data class SurveyOptionRedirection(
  val title: String,
  val description: String,
  val type: RedirectionType,
  val actionText: String,
  val image: RedirectionImage?,
)

@Serializable
internal data class RedirectionImage(
  val url: String,
  val overlayText: String?,
)

@Serializable
internal enum class RedirectionType {
  UPDATE_ADDRESS,
  UNKNOWN,
}
```

- [ ] **Step 4: Add the navigation key**

In `TerminateInsuranceDestination.kt`, alongside the other keys:

```kotlin
@Serializable
internal data class TerminationRedirectionKey(
  val redirection: SurveyOptionRedirection,
  val selectedOption: TerminationSurveyOption,
  val action: TerminationAction,
  val commonParams: TerminationGraphParameters,
  val feedbackComment: String?,
) : HedvigNavKey
```

- [ ] **Step 5: Extract the shared post-selection navigation and intercept Continue**

In `TerminationSurveyViewModel.kt`, fold the current sub-options branch into `navigateAfterSurvey` by adding a shared `internal` function, and make `Continue` route to the interstitial when a redirection is present:

```kotlin
// Replace the body of the `is Continue ->` branch with:
is Continue -> {
  val selectedOption = currentState.selectedOption ?: return@CollectEvents
  currentState = currentState.copy(errorWhileLoadingNextStep = false)
  val redirection = selectedOption.redirection
  if (redirection != null) {
    backstack.add(
      TerminationRedirectionKey(
        redirection = redirection,
        selectedOption = selectedOption,
        action = action,
        commonParams = commonParams,
        feedbackComment = feedbackText,
      ),
    )
  } else {
    backstack.continueAfterSurveySelection(selectedOption, feedbackText, action, commonParams)
  }
}
```

```kotlin
// New shared function; the existing subOptions branch + navigateAfterSurvey merged into one.
internal fun Backstack.continueAfterSurveySelection(
  selectedOption: TerminationSurveyOption,
  feedbackText: String?,
  action: TerminationAction,
  commonParams: TerminationGraphParameters,
) {
  if (selectedOption.subOptions.isNotEmpty()) {
    add(TerminationSurveySecondStepKey(selectedOption.subOptions, action, commonParams))
  } else {
    navigateAfterSurvey(selectedOption, feedbackText, action, commonParams)
  }
}
```

Leave the existing private `navigateAfterSurvey` as-is (now called only from `continueAfterSurveySelection`).

- [ ] **Step 6: Run the test to verify it passes**

Run: `./gradlew :feature-terminate-insurance:testDebugUnitTest --tests "*TerminationSurveyPresenterTest*"`
Expected: PASS (all existing tests still green — `redirection` is defaulted; the extraction preserves the old subOptions-vs-navigateAfterSurvey behaviour for the `redirection == null` path).

- [ ] **Step 7: Format and commit**

```bash
./gradlew :feature-terminate-insurance:ktlintFormat
git add app/feature/feature-terminate-insurance/src/main/kotlin/com/hedvig/android/feature/terminateinsurance/data/TerminationSurveyOption.kt \
        app/feature/feature-terminate-insurance/src/main/kotlin/com/hedvig/android/feature/terminateinsurance/navigation/TerminateInsuranceDestination.kt \
        app/feature/feature-terminate-insurance/src/main/kotlin/com/hedvig/android/feature/terminateinsurance/step/survey/TerminationSurveyViewModel.kt \
        app/feature/feature-terminate-insurance/src/test/kotlin/com/hedvig/android/feature/terminateinsurance/step/survey/TerminationSurveyPresenterTest.kt
git commit -m "feat(terminate): model survey redirection and route Continue to an interstitial"
```

---

## Task 2: Send `redirectionEnabled` and map the `redirection` field

> **GATE:** Do not start until underwriter#1741 is merged and the new schema is available via introspection. Step 1 downloads it.

**Files:**
- Modify: `.../src/main/graphql/QueryTerminationSurvey.graphql`
- Modify: `.../data/TerminateInsuranceRepository.kt`

**Interfaces:**
- Consumes: `SurveyOptionRedirection`, `RedirectionImage`, `RedirectionType` from Task 1.
- Produces: `getTerminationSurvey(contractId)` returns top-level options with `redirection` populated when the backend promotes one.

- [ ] **Step 1: Download the updated schema and confirm it landed**

Run: `./gradlew downloadOctopusApolloSchemaFromIntrospection`
Run: `grep -n "redirectionEnabled\|TerminationFlowSurveyOptionRedirectionType\|TerminationFlowSurveyOptionRedirection\b" app/apollo/apollo-octopus-public/src/commonMain/graphql/com/hedvig/android/apollo/octopus/schema.graphqls`
Expected: matches for the argument, the redirection type, and the dedicated enum. **If empty, STOP — the backend change has not propagated; do not hand-edit the schema.**

- [ ] **Step 2: Update the query**

Send the argument and select `redirection` on the **top-level** options only (the backend only promotes onto top-level options):

```graphql
query TerminationSurvey($contractId: UUID!) {
  terminationSurvey(contractId: $contractId, redirectionEnabled: true) {
    options {
      id
      title
      feedbackRequired
      redirection {
        title
        description
        type
        actionText
        image {
          url
          overlayText
        }
      }
      subOptions {
        # ... unchanged existing selection ...
      }
      suggestion {
        ...TerminationSurveyOptionSuggestionFragment
      }
    }
    action {
      # ... unchanged ...
    }
  }
}
```

- [ ] **Step 3: Generate Apollo sources**

Run: `./gradlew :feature-terminate-insurance:generateApolloSources`
Expected: `TerminationSurveyQuery.Data.TerminationSurvey.Option` gains a nullable `redirection` with `title`, `description`, `type` (the generated `TerminationFlowSurveyOptionRedirectionType`), `actionText`, and nullable `image { url, overlayText }`. **Confirm the exact generated nested type names from the output before writing the mapper.**

- [ ] **Step 4: Map the redirection in the repository**

In `TerminateInsuranceRepository.kt`, in the **top-level** `toTerminationSurveyOption(index)` construction add `redirection = redirection?.toRedirection()` (leave the nested sub-option constructions untouched). Add:

```kotlin
private fun TerminationSurveyQuery.Data.TerminationSurvey.Option.Redirection.toRedirection(): SurveyOptionRedirection {
  return SurveyOptionRedirection(
    title = title,
    description = description,
    type = when (type) {
      TerminationFlowSurveyOptionRedirectionType.UPDATE_ADDRESS -> RedirectionType.UPDATE_ADDRESS
      else -> RedirectionType.UNKNOWN
    },
    actionText = actionText,
    image = image?.let { RedirectionImage(url = it.url, overlayText = it.overlayText) },
  )
}
```

Add the `import octopus.type.TerminationFlowSurveyOptionRedirectionType`. Adjust the receiver type (`...Option.Redirection`, `...Option.Redirection.Image`) to match Step 3's generated names.

- [ ] **Step 5: Compile the module**

Run: `./gradlew :feature-terminate-insurance:compileDebugKotlin`
Expected: success.

- [ ] **Step 6: Run the module's unit tests (no regressions)**

Run: `./gradlew :feature-terminate-insurance:testDebugUnitTest`
Expected: PASS.

- [ ] **Step 7: Format and commit**

```bash
./gradlew :feature-terminate-insurance:ktlintFormat
git add app/feature/feature-terminate-insurance/src/main/graphql/QueryTerminationSurvey.graphql \
        app/feature/feature-terminate-insurance/src/main/kotlin/com/hedvig/android/feature/terminateinsurance/data/TerminateInsuranceRepository.kt
git commit -m "feat(terminate): request and map survey redirection from octopus"
```

---

## Task 3: The "Before we continue" interstitial screen

**Files:**
- Create: `.../step/redirection/TerminationRedirectionDestination.kt`
- Modify: `.../navigation/TerminateInsuranceEntries.kt`

**Interfaces:**
- Consumes: `SurveyOptionRedirection`, `RedirectionType`, `RedirectionImage` (Task 1); `TerminationRedirectionKey` (Task 1); `Backstack.continueAfterSurveySelection` (Task 1); the already-threaded `navigateToMovingFlow: () -> Unit`, `closeTerminationFlow`, and `backstack` in `TerminateInsuranceEntries`.
- Produces: a full-screen composable `TerminationRedirectionDestination(redirection, navigateUp, closeTerminationFlow, onSeePrice, onContinueCancelling)` and its `entry<TerminationRedirectionKey>`.

- [ ] **Step 1: Create the interstitial composable**

Mirror `DeflectSuggestionDestination` (plain composable, no ViewModel, inside `TerminationScaffold`). Render, top to bottom: the "Before we continue" heading; the image block **only when `redirection.image != null`** (Coil `AsyncImage` of `image.url`, with `image.overlayText` shown as a small pill badge over the top-left when non-null); `redirection.title`; `redirection.description` via `HedvigMarkdownText`; then a primary `HedvigButton` labelled `redirection.actionText` calling `onSeePrice`, and a secondary/ghost button "Continue cancelling" calling `onContinueCancelling`.

```kotlin
@Composable
internal fun TerminationRedirectionDestination(
  redirection: SurveyOptionRedirection,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
  onSeePrice: () -> Unit,
  onContinueCancelling: () -> Unit,
) {
  TerminationScaffold(navigateUp = navigateUp, closeTerminationFlow = closeTerminationFlow) { title ->
    // TODO: Add "Before we continue" / "Innan vi fortsätter" to Lokalise
    FlowHeading(title, "Before we continue", modifier = Modifier.padding(horizontal = 16.dp))
    Spacer(Modifier.height(16.dp))
    redirection.image?.let { image ->
      RedirectionImageBlock(image, modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth())
      Spacer(Modifier.height(16.dp))
    }
    Text(redirection.title, style = HedvigTheme.typography.headlineSmall, modifier = Modifier.padding(horizontal = 16.dp))
    Spacer(Modifier.height(8.dp))
    HedvigMarkdownText(redirection.description, modifier = Modifier.padding(horizontal = 16.dp))
    Spacer(Modifier.weight(1f))
    when (redirection.type) {
      RedirectionType.UPDATE_ADDRESS -> HedvigButton(
        text = redirection.actionText,
        onClick = dropUnlessResumed { onSeePrice() },
        enabled = true,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      )
      RedirectionType.UNKNOWN -> Unit // unknown target: no primary CTA, member can still continue cancelling
    }
    Spacer(Modifier.height(8.dp))
    // TODO: Add "Continue cancelling" / "Fortsätt avsluta" to Lokalise
    HedvigTextButton(
      text = "Continue cancelling",
      onClick = dropUnlessResumed { onContinueCancelling() },
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}
```

> Confirm the exact design-system component names/APIs (`HedvigButton`, `HedvigTextButton`, `FlowHeading`, `TerminationScaffold`, typography tokens) against the neighbouring destinations before finalizing. `RedirectionImageBlock` is a small private composable in this file: an `AsyncImage` with rounded corners and, when `overlayText != null`, a pill badge overlaid top-left (match the "15% off" badge in the design).

- [ ] **Step 2: Register the entry**

In `TerminateInsuranceEntries.kt`, next to `entry<DeflectSuggestionKey>`:

```kotlin
entry<TerminationRedirectionKey> { key ->
  TerminationRedirectionDestination(
    redirection = key.redirection,
    navigateUp = backstack::navigateUp,
    closeTerminationFlow = closeTerminationFlow,
    onSeePrice = dropUnlessResumed { navigateToMovingFlow() },
    onContinueCancelling = {
      backstack.continueAfterSurveySelection(
        selectedOption = key.selectedOption,
        feedbackText = key.feedbackComment,
        action = key.action,
        commonParams = key.commonParams,
      )
    },
  )
}
```

- [ ] **Step 3: Add a preview**

Add a `@HedvigPreview` composable rendering `TerminationRedirectionDestination` with a `SurveyOptionRedirection` of type `UPDATE_ADDRESS` and `image = null`, so the null-image fallback is visually reviewable, plus a second preview with a non-null image to check the badge.

- [ ] **Step 4: Compile the module**

Run: `./gradlew :feature-terminate-insurance:compileDebugKotlin`
Expected: success.

- [ ] **Step 5: Verify previews render**

Open `TerminationRedirectionDestination.kt` in Android Studio; confirm both previews render (with and without image), the primary button shows the backend `actionText`, and the null-image preview simply omits the image block.

- [ ] **Step 6: Format and commit**

```bash
./gradlew :feature-terminate-insurance:ktlintFormat
git add app/feature/feature-terminate-insurance/src/main/kotlin/com/hedvig/android/feature/terminateinsurance/step/redirection/TerminationRedirectionDestination.kt \
        app/feature/feature-terminate-insurance/src/main/kotlin/com/hedvig/android/feature/terminateinsurance/navigation/TerminateInsuranceEntries.kt
git commit -m "feat(terminate): add Before we continue redirection interstitial"
```

---

## Task 4: End-to-end verification

**Files:** none (verification only).

- [ ] **Step 1: Build the app**

Run: `./gradlew :app:assembleDebug`
Expected: success.

- [ ] **Step 2: Manual smoke test on staging/develop**

Trigger termination for a Swedish home contract that has a "moving to a new home" reason. On the survey screen select it and tap Continue, then confirm:
  - the "Before we continue" interstitial appears with the backend title/description and (once the backend serves one) the image with its overlay badge; with a null image, no image block shows;
  - "See price for new home" opens the moving flow (the same destination `navigateToMovingFlow` reaches today);
  - "Continue cancelling" lands on the remaining moving sub-options (the address sub-option is no longer listed, since it was promoted);
  - options **without** a redirection behave exactly as before (Continue → sub-options or date/delete).

- [ ] **Step 3: Process-death check**

Background the app on the "Before we continue" screen, let the process be killed, and restore. `TerminationRedirectionKey` (and its nested `SurveyOptionRedirection` / `TerminationSurveyOption`, all `@Serializable`) must round-trip with no crash. This is also guarded by `ExhaustiveBackStackSerializationTest`; run it:

Run: `./gradlew :app:testDebugUnitTest --tests "*ExhaustiveBackStackSerializationTest*"`
Expected: PASS (the new key is auto-registered via the module's `navKeys()`).

---

## Self-Review Notes

- **Spec coverage:** query arg + field mapping (Task 2) · project-owned model, no octopus leakage (Task 1 + Task 2) · dedicated enum mirrored with UNKNOWN fallback (Task 1 model + Task 2 mapper) · full-screen interstitial per Figma with image/title/description/primary+secondary CTA (Task 3) · nullable image → omitted (Task 3) · Continue interception + behaviour-preserving "Continue cancelling" (Task 1 shared function, Task 3 entry) · process-death (Task 4). All covered.
- **Type consistency:** `SurveyOptionRedirection(title, description, type: RedirectionType, actionText, image: RedirectionImage?)` and `RedirectionImage(url, overlayText)` identical across Tasks 1–3. `TerminationRedirectionKey(redirection, selectedOption, action, commonParams, feedbackComment)` consistent between the key definition (Task 1), the push site (Task 1 ViewModel), and the entry (Task 3). `continueAfterSurveySelection(selectedOption, feedbackText, action, commonParams)` signature identical at both call sites.
- **Assumptions to confirm before merge (not blockers to building):** exact generated Apollo nested type names (Task 2 Step 3); design-system component/API names for the interstitial (Task 3 Step 1); final Lokalise keys for "Before we continue" and "Continue cancelling" (hardcoded with TODOs until synced); the rollout decision (unconditional `true` vs Unleash-gated).
```
