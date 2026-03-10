# Termination Offer Step Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a backend-driven interstitial offer screen to the termination flow that promotes retention by showing discounts/alternatives before the user proceeds with cancellation.

**Architecture:** The backend sends a new `FlowTerminationOfferStep` step type containing offer content and an action type (UPDATE_ADDRESS or CHANGE_TIER). The Android app parses this new step, navigates to a new offer screen, and handles CTA (deflect to moving flow or change tier flow) and skip (call a mutation to advance to the next step).

**Tech Stack:** Jetpack Compose, Apollo GraphQL, Molecule MVI, Koin DI, Navigation Compose

---

## File Structure

| File | Action | Responsibility |
|------|--------|----------------|
| `feature-terminate-insurance/src/main/graphql/FragmentTerminationFlowStepFragment.graphql` | Modify | Add FlowTerminationOfferStep fragment |
| `feature-terminate-insurance/.../data/TerminateInsuranceStep.kt` | Modify | Add OfferStep data class + parsing + destination mapping |
| `feature-terminate-insurance/.../data/TerminateInsuranceRepository.kt` | Modify | Add SUPPORTED_STEPS entry + skip mutation method |
| `feature-terminate-insurance/.../navigation/TerminateInsuranceDestination.kt` | Modify | Add OfferScreen destination |
| `feature-terminate-insurance/.../navigation/TerminateInsuranceGraph.kt` | Modify | Wire OfferScreen destination |
| `feature-terminate-insurance/.../step/offer/TerminationOfferViewModel.kt` | Create | ViewModel + Presenter for skip action |
| `feature-terminate-insurance/.../step/offer/TerminationOfferDestination.kt` | Create | Offer screen composable |
| `feature-terminate-insurance/.../di/TerminateInsuranceModule.kt` | Modify | Register OfferViewModel in Koin |

All paths below are relative to: `app/feature/feature-terminate-insurance/src/main/kotlin/com/hedvig/android/feature/terminateinsurance/`

---

## Chunk 1: GraphQL + Data Layer

### Task 1: Add GraphQL fragment for FlowTerminationOfferStep

**Files:**
- Modify: `app/feature/feature-terminate-insurance/src/main/graphql/FragmentTerminationFlowStepFragment.graphql:1-12`

- [ ] **Step 1: Add the new fragment spread to TerminationFlowStepFragment**

In `FragmentTerminationFlowStepFragment.graphql`, add `...FlowTerminationOfferStepFragment` to the `currentStep` block (after line 10):

```graphql
fragment TerminationFlowStepFragment on Flow {
  currentStep {
    id
    ...FlowTerminationDateStepFragment
    ...FlowTerminationDeletionStepFragment
    ...FlowTerminationFailedStepFragment
    ...FlowTerminationSuccessStepFragment
    ...FlowTerminationSurveyStepFragment
    ...FlowTerminationAutoDecomStepFragment
    ...FlowTerminationCarDeflectAutoCancelStepFragment
    ...FlowTerminationOfferStepFragment
  }
}
```

- [ ] **Step 2: Add the FlowTerminationOfferStepFragment definition**

Append after the `ExtraCoverageItemFragment` (after line 117):

```graphql
fragment FlowTerminationOfferStepFragment on FlowTerminationOfferStep {
  id
  title
  description
  buttonTitle
  skipButtonTitle
  action
}
```

Note: The exact GraphQL type name (`FlowTerminationOfferStep`) and field names must match whatever the backend team defines. Coordinate with them. The `action` field returns a `FlowTerminationOfferAction` enum with values `UPDATE_ADDRESS` and `CHANGE_TIER`.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-terminate-insurance/src/main/graphql/FragmentTerminationFlowStepFragment.graphql
git commit -m "feat: add GraphQL fragment for FlowTerminationOfferStep (RND-1453)"
```

---

### Task 2: Add OfferStep to the data layer

**Files:**
- Modify: `data/TerminateInsuranceStep.kt`
- Modify: `navigation/TerminateInsuranceDestination.kt`

- [ ] **Step 1: Add OfferAction enum and OfferStep data class**

In `TerminateInsuranceStep.kt`, add after `DeflectAutoDecommissionStep` (after line 70):

```kotlin
data class OfferStep(
  val title: String,
  val description: String,
  val buttonTitle: String,
  val skipButtonTitle: String,
  val action: OfferAction,
) : TerminateInsuranceStep

enum class OfferAction {
  UPDATE_ADDRESS,
  CHANGE_TIER,
}
```

- [ ] **Step 2: Add parsing in toTerminateInsuranceStep()**

In the `when (this)` block in `toTerminateInsuranceStep()` (before the `else` at line 120), add:

```kotlin
is TerminationFlowStepFragment.FlowTerminationOfferStepCurrentStep -> {
  TerminateInsuranceStep.OfferStep(
    title = title,
    description = description,
    buttonTitle = buttonTitle,
    skipButtonTitle = skipButtonTitle,
    action = when (action) {
      octopus.type.FlowTerminationOfferAction.UPDATE_ADDRESS -> OfferAction.UPDATE_ADDRESS
      octopus.type.FlowTerminationOfferAction.CHANGE_TIER -> OfferAction.CHANGE_TIER
      else -> {
        logcat(LogPriority.WARN) { "Unknown FlowTerminationOfferAction: ${action.rawValue}" }
        return TerminateInsuranceStep.UnknownStep()
      }
    },
  )
}
```

Note: The exact generated class name (`FlowTerminationOfferStepCurrentStep`) depends on Apollo codegen. After building, check the generated types and adjust if needed.

- [ ] **Step 3: Add OfferScreen destination**

In `TerminateInsuranceDestination.kt`, add after `DeflectAutoDecommission` (before the closing `}`):

```kotlin
@Serializable
data class OfferScreen(
  val title: String,
  val description: String,
  val buttonTitle: String,
  val skipButtonTitle: String,
  val action: OfferAction,
  val commonParams: TerminationGraphParameters,
) : TerminateInsuranceDestination, Destination {
  companion object : DestinationNavTypeAware {
    override val typeList: List<KType> = listOf(
      typeOf<OfferAction>(),
      typeOf<TerminationGraphParameters>(),
    )
  }
}
```

You'll need to add an import for `OfferAction`:

```kotlin
import com.hedvig.android.feature.terminateinsurance.data.OfferAction
```

- [ ] **Step 4: Add destination mapping in toTerminateInsuranceDestination()**

In `TerminateInsuranceStep.kt`, add a new branch in `toTerminateInsuranceDestination()` (before the closing `}`):

```kotlin
is TerminateInsuranceStep.OfferStep -> {
  TerminateInsuranceDestination.OfferScreen(
    title = title,
    description = description,
    buttonTitle = buttonTitle,
    skipButtonTitle = skipButtonTitle,
    action = action,
    commonParams = commonParams,
  )
}
```

You'll need to add the import:

```kotlin
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination.OfferScreen
```

- [ ] **Step 5: Add "FlowTerminationOfferStep" to SUPPORTED_STEPS**

In `TerminateInsuranceRepository.kt`, add to the `SUPPORTED_STEPS` list (line 154-163):

```kotlin
private val SUPPORTED_STEPS = Optional.present(
  listOf(
    "FlowTerminationSurveyStep",
    "FlowTerminationDateStep",
    "FlowTerminationDeletionStep",
    "FlowTerminationSuccessStep",
    "FlowTerminationFailedStep",
    "FlowTerminationCarDeflectAutoCancelStep",
    "FlowTerminationCarAutoDecomStep",
    "FlowTerminationOfferStep",
  ),
)
```

- [ ] **Step 6: Add skip mutation method to repository**

In `TerminateInsuranceRepository.kt`, add to the interface (after line 41):

```kotlin
suspend fun skipOfferStep(): Either<ErrorMessage, TerminateInsuranceStep>
```

Add implementation in `TerminateInsuranceRepositoryImpl`. The skip action needs a dedicated mutation or reuses an existing one. Coordinate with backend team on which mutation to use. A likely approach is a new `FlowTerminationOfferNextMutation`:

```kotlin
override suspend fun skipOfferStep(): Either<ErrorMessage, TerminateInsuranceStep> {
  return either {
    val isAddonsEnabled = featureManager.isFeatureEnabled(TRAVEL_ADDON).first()
    val result = apolloClient
      .mutation(
        FlowTerminationOfferNextMutation(
          context = terminationFlowContextStorage.getContext(),
          addonsEnabled = isAddonsEnabled,
        ),
      )
      .safeExecute(::ErrorMessage)
      .bind()
      .flowTerminationOfferNext
    terminationFlowContextStorage.saveContext(result.context)
    result.currentStep.toTerminateInsuranceStep()
  }
}
```

Note: The exact mutation name and structure depends on what the backend provides. You will also need to create the corresponding `.graphql` mutation file. Create `app/feature/feature-terminate-insurance/src/main/graphql/FlowTerminationOfferNextMutation.graphql`:

```graphql
mutation FlowTerminationOfferNext($context: String!, $addonsEnabled: Boolean!) {
  flowTerminationOfferNext(context: $context) {
    context
    ...TerminationFlowStepFragment
  }
}
```

- [ ] **Step 7: Commit**

```bash
git add app/feature/feature-terminate-insurance/src/main/graphql/ app/feature/feature-terminate-insurance/src/main/kotlin/
git commit -m "feat: add OfferStep data model, parsing, destination, and repository (RND-1453)"
```

---

## Chunk 2: ViewModel + UI + Navigation Wiring

### Task 3: Create TerminationOfferViewModel

**Files:**
- Create: `step/offer/TerminationOfferViewModel.kt`

- [ ] **Step 1: Create the ViewModel file**

Create `app/feature/feature-terminate-insurance/src/main/kotlin/com/hedvig/android/feature/terminateinsurance/step/offer/TerminationOfferViewModel.kt`:

```kotlin
package com.hedvig.android.feature.terminateinsurance.step.offer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.terminateinsurance.data.OfferAction
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class TerminationOfferViewModel(
  title: String,
  description: String,
  buttonTitle: String,
  skipButtonTitle: String,
  action: OfferAction,
  terminateInsuranceRepository: TerminateInsuranceRepository,
) : MoleculeViewModel<TerminationOfferEvent, TerminationOfferUiState>(
    initialState = TerminationOfferUiState.Content(
      title = title,
      description = description,
      buttonTitle = buttonTitle,
      skipButtonTitle = skipButtonTitle,
      action = action,
    ),
    presenter = TerminationOfferPresenter(terminateInsuranceRepository),
  )

private class TerminationOfferPresenter(
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
) : MoleculePresenter<TerminationOfferEvent, TerminationOfferUiState> {
  @Composable
  override fun MoleculePresenterScope<TerminationOfferEvent>.present(
    lastState: TerminationOfferUiState,
  ): TerminationOfferUiState {
    var skipIteration by remember { mutableIntStateOf(0) }
    var currentState by remember { mutableStateOf(lastState) }

    CollectEvents { event ->
      when (event) {
        TerminationOfferEvent.Skip -> skipIteration++
        TerminationOfferEvent.ClearNextStep -> {
          val state = currentState as? TerminationOfferUiState.Content ?: return@CollectEvents
          currentState = state.copy(nextStep = null, skipLoading = false)
        }
      }
    }

    androidx.compose.runtime.LaunchedEffect(skipIteration) {
      if (skipIteration > 0) {
        val state = currentState as? TerminationOfferUiState.Content ?: return@LaunchedEffect
        currentState = state.copy(skipLoading = true)
        currentState = terminateInsuranceRepository.skipOfferStep().fold(
          ifLeft = { TerminationOfferUiState.Error },
          ifRight = { step -> state.copy(skipLoading = false, nextStep = step) },
        )
      }
    }

    return currentState
  }
}

internal sealed interface TerminationOfferUiState {
  data class Content(
    val title: String,
    val description: String,
    val buttonTitle: String,
    val skipButtonTitle: String,
    val action: OfferAction,
    val skipLoading: Boolean = false,
    val nextStep: TerminateInsuranceStep? = null,
  ) : TerminationOfferUiState

  data object Error : TerminationOfferUiState
}

internal sealed interface TerminationOfferEvent {
  data object Skip : TerminationOfferEvent
  data object ClearNextStep : TerminationOfferEvent
}
```

- [ ] **Step 2: Commit**

```bash
git add app/feature/feature-terminate-insurance/src/main/kotlin/com/hedvig/android/feature/terminateinsurance/step/offer/TerminationOfferViewModel.kt
git commit -m "feat: add TerminationOfferViewModel with skip mutation support (RND-1453)"
```

---

### Task 4: Create TerminationOfferDestination composable

**Files:**
- Create: `step/offer/TerminationOfferDestination.kt`

- [ ] **Step 1: Create the offer screen composable**

Create `app/feature/feature-terminate-insurance/src/main/kotlin/com/hedvig/android/feature/terminateinsurance/step/offer/TerminationOfferDestination.kt`:

```kotlin
package com.hedvig.android.feature.terminateinsurance.step.offer

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.feature.terminateinsurance.data.OfferAction
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.ui.TerminationScaffold

@Composable
internal fun TerminationOfferDestination(
  viewModel: TerminationOfferViewModel,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
  onCtaClick: (OfferAction) -> Unit,
  onNavigateToNextStep: (TerminateInsuranceStep) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(uiState) {
    val state = uiState as? TerminationOfferUiState.Content ?: return@LaunchedEffect
    if (state.nextStep != null) {
      viewModel.emit(TerminationOfferEvent.ClearNextStep)
      onNavigateToNextStep(state.nextStep)
    }
  }
  TerminationOfferScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
    onCtaClick = onCtaClick,
    onSkipClick = { viewModel.emit(TerminationOfferEvent.Skip) },
    onRetry = { viewModel.emit(TerminationOfferEvent.Skip) },
  )
}

@Composable
private fun TerminationOfferScreen(
  uiState: TerminationOfferUiState,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
  onCtaClick: (OfferAction) -> Unit,
  onSkipClick: () -> Unit,
  onRetry: () -> Unit,
) {
  when (uiState) {
    TerminationOfferUiState.Error -> {
      HedvigScaffold(navigateUp = navigateUp) {
        HedvigErrorSection(
          onButtonClick = onRetry,
          modifier = Modifier.weight(1f),
        )
      }
    }

    is TerminationOfferUiState.Content -> {
      TerminationOfferContentScreen(
        uiState = uiState,
        navigateUp = navigateUp,
        closeTerminationFlow = closeTerminationFlow,
        onCtaClick = { onCtaClick(uiState.action) },
        onSkipClick = onSkipClick,
      )
    }
  }
}

@Composable
private fun TerminationOfferContentScreen(
  uiState: TerminationOfferUiState.Content,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
  onCtaClick: () -> Unit,
  onSkipClick: () -> Unit,
) {
  TerminationScaffold(
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
  ) { _ ->
    FlowHeading(
      title = uiState.title,
      description = null,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    HedvigNotificationCard(
      message = uiState.description,
      priority = NotificationDefaults.NotificationPriority.Campaign,
      withIcon = true,
      modifier = Modifier.padding(horizontal = 16.dp),
    ) {
      HedvigButton(
        text = uiState.buttonTitle,
        enabled = true,
        onClick = onCtaClick,
        modifier = Modifier.fillMaxWidth(),
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigTextButton(
      text = uiState.skipButtonTitle,
      isLoading = uiState.skipLoading,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      onClick = onSkipClick,
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigShortMultiScreenPreview
@Composable
private fun PreviewTerminationOfferScreen(
  @PreviewParameter(OfferUiStateProvider::class) uiState: TerminationOfferUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TerminationOfferScreen(
        uiState = uiState,
        navigateUp = {},
        closeTerminationFlow = {},
        onCtaClick = {},
        onSkipClick = {},
        onRetry = {},
      )
    }
  }
}

private class OfferUiStateProvider :
  CollectionPreviewParameterProvider<TerminationOfferUiState>(
    listOf(
      TerminationOfferUiState.Content(
        title = "Erbjudande för dig",
        description = "Vill du se vilket pris du får hos Hedvig? Väljer du att försäkra ditt nya hem får du 20% rabatt de första 6 månaderna",
        buttonTitle = "Få ett prisförslag",
        skipButtonTitle = "Hoppa över",
        action = OfferAction.UPDATE_ADDRESS,
      ),
      TerminationOfferUiState.Content(
        title = "Erbjudande för dig",
        description = "Vill du se vilket pris du får hos Hedvig? Väljer du att försäkra ditt nya hem får du 20% rabatt de första 6 månaderna",
        buttonTitle = "Få ett prisförslag",
        skipButtonTitle = "Hoppa över",
        action = OfferAction.UPDATE_ADDRESS,
        skipLoading = true,
      ),
      TerminationOfferUiState.Error,
    ),
  )
```

Note: The `HedvigNotificationCard` with `Campaign` priority gives the green styling seen in the design. If this priority doesn't exist or the green styling requires a different approach, check the design system components. The card in the design has a green background with white text, a checkmark icon, and contains a white CTA button inside it. Adjust the composable to match the exact design system API.

- [ ] **Step 2: Commit**

```bash
git add app/feature/feature-terminate-insurance/src/main/kotlin/com/hedvig/android/feature/terminateinsurance/step/offer/TerminationOfferDestination.kt
git commit -m "feat: add TerminationOfferDestination composable (RND-1453)"
```

---

### Task 5: Wire navigation and DI

**Files:**
- Modify: `navigation/TerminateInsuranceGraph.kt`
- Modify: `di/TerminateInsuranceModule.kt`

- [ ] **Step 1: Register ViewModel in Koin module**

In `TerminateInsuranceModule.kt`, add the import and ViewModel registration after the `DeflectAutoDecommissionStepViewModel` registration (after line 70):

```kotlin
import com.hedvig.android.feature.terminateinsurance.step.offer.TerminationOfferViewModel
import com.hedvig.android.feature.terminateinsurance.data.OfferAction
```

```kotlin
viewModel<TerminationOfferViewModel> { params ->
  TerminationOfferViewModel(
    title = params.get<String>(),
    description = params.get<String>(),
    buttonTitle = params.get<String>(),
    skipButtonTitle = params.get<String>(),
    action = params.get<OfferAction>(),
    terminateInsuranceRepository = get<TerminateInsuranceRepository>(),
  )
}
```

Note: Koin's `params.get<String>()` returns parameters positionally. This may need adjustment — if multiple String params cause ambiguity, use indexed access: `params[0] as String`, `params[1] as String`, etc. Alternatively, wrap the params in a data class similar to how `TerminationDateParameters` is used.

A cleaner approach would be a parameters data class:

```kotlin
viewModel<TerminationOfferViewModel> { params ->
  val destination = params.get<TerminateInsuranceDestination.OfferScreen>()
  TerminationOfferViewModel(
    title = destination.title,
    description = destination.description,
    buttonTitle = destination.buttonTitle,
    skipButtonTitle = destination.skipButtonTitle,
    action = destination.action,
    terminateInsuranceRepository = get<TerminateInsuranceRepository>(),
  )
}
```

- [ ] **Step 2: Add navdestination for OfferScreen in the graph**

In `TerminateInsuranceGraph.kt`, add the import:

```kotlin
import com.hedvig.android.feature.terminateinsurance.step.offer.TerminationOfferDestination
import com.hedvig.android.feature.terminateinsurance.step.offer.TerminationOfferViewModel
import com.hedvig.android.feature.terminateinsurance.data.OfferAction
```

Add the new navdestination inside the `navgraph` block (after the `DeflectAutoDecommission` destination, before line 275):

```kotlin
navdestination<TerminateInsuranceDestination.OfferScreen>(
  TerminateInsuranceDestination.OfferScreen,
) {
  val viewModel: TerminationOfferViewModel = koinViewModel {
    parametersOf(
      this@navdestination,  // pass the destination itself
    )
  }
  TerminationOfferDestination(
    viewModel = viewModel,
    navigateUp = navController::navigateUp,
    closeTerminationFlow = closeTerminationFlow,
    onCtaClick = dropUnlessResumed { action ->
      when (action) {
        OfferAction.UPDATE_ADDRESS -> navigateToMovingFlow()
        OfferAction.CHANGE_TIER -> {
          // For CHANGE_TIER, we need to fetch the intent first
          // This may need the same logic as the current TryToDowngradePrice flow
          // For now, redirect with a placeholder - coordinate with existing tier change logic
          redirectToChangeTierFlow(commonParams.contractId to IntentOutput(/* ... */))
        }
      }
    },
    onNavigateToNextStep = { step ->
      navController.navigateToTerminateFlowDestination(
        destination = step.toTerminateInsuranceDestination(commonParams),
      )
    },
  )
}
```

Important notes for the `CHANGE_TIER` action:
- The current `redirectToChangeTierFlow` requires a `Pair<String, IntentOutput>`.
- The existing flow in `TerminationSurveyViewModel` calls `changeTierRepository.startChangeTierIntentAndGetQuotesId()` to get the `IntentOutput`.
- For the offer screen CTA, you may need to either:
  1. Have the ViewModel fetch the intent when CTA is clicked (add a `CtaClick` event), or
  2. Navigate to a loading state that fetches the intent then redirects
- Coordinate with the existing `TryToDowngradePrice` logic in `TerminationSurveyViewModel` (lines 117-183) for the exact implementation.

For UPDATE_ADDRESS, it's simpler — just call `navigateToMovingFlow()`.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-terminate-insurance/src/main/kotlin/
git commit -m "feat: wire OfferScreen destination, navigation, and DI (RND-1453)"
```

---

## Chunk 3: Testing & Verification

### Task 6: Build verification and manual testing

- [ ] **Step 1: Build the project to verify Apollo codegen**

Run:
```bash
./gradlew :app:feature:feature-terminate-insurance:generateApolloSources
```

Expected: Successful code generation with new types for `FlowTerminationOfferStep`.

If this fails, the backend schema hasn't been updated yet. You can temporarily comment out the GraphQL fragment additions and work with mock data until the schema is available. Run:
```bash
./gradlew downloadOctopusApolloSchemaFromIntrospection
```
to download the latest schema.

- [ ] **Step 2: Verify the module compiles**

Run:
```bash
./gradlew :app:feature:feature-terminate-insurance:compileDebugKotlin
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Run ktlint**

Run:
```bash
./gradlew :app:feature:feature-terminate-insurance:ktlintFormat
```

Fix any formatting issues.

- [ ] **Step 4: Verify Compose previews render**

Open `TerminationOfferDestination.kt` in Android Studio and check that the `@HedvigShortMultiScreenPreview` renders correctly. Verify:
- Title appears at top
- Green notification card with description and CTA button
- Skip button at the bottom
- Error state shows error section

- [ ] **Step 5: Run the full module test suite**

Run:
```bash
./gradlew :app:feature:feature-terminate-insurance:test
```

Expected: All existing tests pass (no regressions).

- [ ] **Step 6: Final commit with any fixes**

```bash
git add -A
git commit -m "chore: fix formatting and build issues for offer step (RND-1453)"
```

---

## Implementation Notes

### Backend coordination required
- The backend team needs to create the `FlowTerminationOfferStep` type and `FlowTerminationOfferAction` enum in the GraphQL schema
- They need to provide a mutation for skipping the offer step (e.g., `flowTerminationOfferNext`)
- They need to insert this step in the flow at the right points (after "Flyttar till en ny adress" selection and after "Ändra skyddsnivå" sub-option selection)
- Download the updated schema: `./gradlew downloadOctopusApolloSchemaFromIntrospection`

### Design system verification
- The green card in the design uses a specific style. Check if `NotificationDefaults.NotificationPriority.Campaign` or `.Offer` gives the green background with white text. If not, consult the design system documentation or check `HedvigNotificationCard` source for available priorities.
- The CTA button inside the green card appears white — verify this is how the design system renders buttons inside notification cards, or if custom styling is needed.

### CHANGE_TIER CTA complexity
- The `UPDATE_ADDRESS` CTA is straightforward (navigate to moving flow)
- The `CHANGE_TIER` CTA is more complex — it likely needs to fetch a `ChangeTierIntent` first, similar to the existing `TryToDowngradePrice` flow in `TerminationSurveyViewModel`. Consider whether this fetching should happen in `TerminationOfferViewModel` or be handled by the navigation layer.
