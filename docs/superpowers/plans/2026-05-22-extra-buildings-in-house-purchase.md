# Extra Buildings in House Purchase Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let the vacation-home (fritidshus) and villa (SE_HOUSE) in-app purchase forms collect a list of extra buildings (garage, shed, etc.) and submit them as part of the existing `priceIntentDataUpdate` mutation, so returned offers reflect the declared buildings.

**Architecture:** Copy the section + dialog UI from the move flow's `AddHouseInformationDestination.kt` into a new file in `feature-purchase-house/.../ui/extrabuildings/`. Adapt to plain `List<ExtraBuildingInfo>` + callbacks (no `ListInput<T>` wrapper). Wire two new events (`AddExtraBuilding`, `RemoveExtraBuilding`) into both `VacationHomeFormViewModel` and `HouseFormViewModel`, render the section in both destinations, and serialize the list into the existing `PricingFormData` map inside both submit use cases. No backend changes, no incremental mutations, no edit affordance.

**Tech Stack:** Kotlin, Jetpack Compose, Apollo GraphQL, Molecule (MVI), Koin DI, Arrow (Either), Hedvig design system.

**Base branch:** This work must touch both the vacation-home form and the villa form. The villa files (`HouseFormViewModel`, `HouseFormDestination`, `SubmitHouseFormAndGetOffersUseCase`, branched `HousePurchaseNavGraph`) live only on `feat/in-app-house-purchase`. The implementation branch **must** be cut from `feat/in-app-house-purchase` (not `develop`, not `feat/in-app-vacation-home-purchase`).

**Spec:** [docs/superpowers/specs/2026-05-22-extra-buildings-in-house-purchase-design.md](../specs/2026-05-22-extra-buildings-in-house-purchase-design.md)

---

### Task 0: Verify base branch

**Files:** none

- [ ] **Step 1: Confirm branch is based off `feat/in-app-house-purchase`**

Run: `git merge-base --is-ancestor feat/in-app-house-purchase HEAD && echo "OK: stacked on house" || echo "FAIL: not stacked on house"`
Expected: `OK: stacked on house`.

If this fails, stop and rebranch from `feat/in-app-house-purchase` before continuing — the villa form files won't exist otherwise and Tasks 3 / 4 will be impossible.

- [ ] **Step 2: Confirm both target form files exist**

Run:
```bash
ls app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/vacationhome/VacationHomeFormViewModel.kt \
   app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/vacationhome/VacationHomeFormDestination.kt \
   app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/data/SubmitVacationHomeFormAndGetOffersUseCase.kt \
   app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/house/HouseFormViewModel.kt \
   app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/house/HouseFormDestination.kt \
   app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/data/SubmitHouseFormAndGetOffersUseCase.kt
```
Expected: all six paths exist with no `No such file or directory`.

- [ ] **Step 3: Confirm the move flow's reference UI is present (we'll be copying from it)**

Run: `ls app/feature/feature-movingflow/src/main/kotlin/com/hedvig/android/feature/movingflow/ui/addhouseinformation/AddHouseInformationDestination.kt`
Expected: file exists.

---

### Task 1: Create the shared extra-buildings file

**Files:**
- Create: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/extrabuildings/ExtraBuildingsSection.kt`

- [ ] **Step 1: Create the new file with data types, hardcoded type list, and copied composables**

File: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/extrabuildings/ExtraBuildingsSection.kt`

```kotlin
package com.hedvig.android.feature.purchase.house.ui.extrabuildings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.PrimaryAlt
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle.NoButtons
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigDialog
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.ErrorState.Error.WithoutMessage
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.ErrorState.NoError
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigToggle
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleDefaultStyleSize
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleStyle
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons

internal data class ExtraBuildingInfo(
  val area: Int,
  val type: String,
  val displayName: String,
  val hasWaterConnected: Boolean,
)

internal data class MoveExtraBuildingType(
  val type: String,
  val displayName: String,
)

// TODO: Replace these English strings with stringResource(...) lookups when Lokalise keys
//  exist for each building type's display name. Today the move flow gets these from the
//  backend (extraBuildingTypesV2.displayName), but PriceIntent does not expose that field.
internal val allExtraBuildingTypes: List<MoveExtraBuildingType> = listOf(
  MoveExtraBuildingType("GARAGE", "Garage"),
  MoveExtraBuildingType("CARPORT", "Carport"),
  MoveExtraBuildingType("SHED", "Shed"),
  MoveExtraBuildingType("STOREHOUSE", "Storehouse"),
  MoveExtraBuildingType("FRIGGEBOD", "Friggebod"),
  MoveExtraBuildingType("ATTEFALL", "Attefallshus"),
  MoveExtraBuildingType("OUTHOUSE", "Outhouse"),
  MoveExtraBuildingType("GUESTHOUSE", "Guesthouse"),
  MoveExtraBuildingType("GAZEBO", "Gazebo"),
  MoveExtraBuildingType("GREENHOUSE", "Greenhouse"),
  MoveExtraBuildingType("SAUNA", "Sauna"),
  MoveExtraBuildingType("BARN", "Barn"),
  MoveExtraBuildingType("BOATHOUSE", "Boathouse"),
  MoveExtraBuildingType("OTHER", "Other"),
)

@Composable
internal fun ExtraBuildingsSection(
  extraBuildings: List<ExtraBuildingInfo>,
  allowedExtraBuildings: List<MoveExtraBuildingType>,
  onAddBuilding: (ExtraBuildingInfo) -> Unit,
  onRemoveBuilding: (ExtraBuildingInfo) -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier,
) {
  var dialogOpen by rememberSaveable { mutableStateOf(false) }
  if (dialogOpen) {
    HedvigDialog(
      contentPadding = PaddingValues(0.dp),
      dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
      onDismissRequest = { dialogOpen = false },
      style = NoButtons,
    ) {
      AddExtraBuildingDialogContent(
        allowedExtraBuildings = allowedExtraBuildings,
        onSaveBuilding = { building ->
          onAddBuilding(building)
          dialogOpen = false
        },
        dismissDialog = { dialogOpen = false },
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
  }
  HedvigCard(modifier.fillMaxWidth()) {
    Column(
      Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp),
    ) {
      HedvigText(
        // TODO: Add "Extra buildings" / "Extra byggnader" to Lokalise (or reuse CHANGE_ADDRESS_EXTRA_BUILDINGS_LABEL).
        text = "Extra buildings",
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
      )
      if (extraBuildings.isNotEmpty()) {
        Column(
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.padding(vertical = 12.dp),
        ) {
          for ((index, extraBuilding) in extraBuildings.withIndex()) {
            if (index != 0) {
              HorizontalDivider()
            }
            key(extraBuilding) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                  HedvigText(extraBuilding.displayName)
                  HedvigText(
                    text = buildString {
                      append(extraBuilding.area)
                      // TODO: Add area suffix " m²" to Lokalise (or reuse CHANGE_ADDRESS_SIZE_SUFFIX).
                      append(" m²")
                      if (extraBuilding.hasWaterConnected) {
                        append(" ∙ ")
                        // TODO: Add "Water connected" / "Vattenanslutet" to Lokalise (or reuse CHANGE_ADDRESS_EXTRA_BUILDINGS_WATER_LABEL).
                        append("Water connected")
                      }
                    },
                    color = HedvigTheme.colorScheme.textSecondary,
                    style = HedvigTheme.typography.label,
                  )
                }
                IconButton(
                  onClick = { onRemoveBuilding(extraBuilding) },
                  enabled = enabled,
                ) {
                  // TODO: Add "Remove" / "Ta bort" content description to Lokalise (or reuse GENERAL_REMOVE).
                  Icon(HedvigIcons.Close, "Remove", Modifier.size(16.dp))
                }
              }
            }
          }
        }
      } else {
        Spacer(Modifier.height(8.dp))
      }
      HedvigButton(
        // TODO: Add "Add extra building" / "Lägg till extra byggnad" to Lokalise (or reuse CHANGE_ADDRESS_EXTRA_BUILDINGS_BOTTOM_SHEET_TITLE).
        text = "Add extra building",
        onClick = { dialogOpen = true },
        enabled = enabled,
        buttonStyle = PrimaryAlt,
        buttonSize = ButtonSize.Medium,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun AddExtraBuildingDialogContent(
  allowedExtraBuildings: List<MoveExtraBuildingType>,
  onSaveBuilding: (ExtraBuildingInfo) -> Unit,
  dismissDialog: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var chosenBuilding: MoveExtraBuildingType? by remember { mutableStateOf(null) }
  var size: Int? by remember { mutableStateOf(null) }
  var isConnectedToWater: Boolean by remember { mutableStateOf(false) }
  var isSizeMissing by remember { mutableStateOf(false) }
  Column(modifier) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      // TODO: Add "Add extra building" / "Lägg till extra byggnad" to Lokalise.
      text = "Add extra building",
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .wrapContentWidth(Alignment.CenterHorizontally)
        .semantics { heading() },
    )
    Spacer(Modifier.height(8.dp))
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
      Spacer(Modifier.height(8.dp))
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HedvigCard {
          RadioGroup(
            options = allowedExtraBuildings.map { buildingType ->
              RadioOption(
                RadioOptionId(buildingType.type),
                buildingType.displayName,
              )
            },
            selectedOption = chosenBuilding?.type?.let { RadioOptionId(it) },
            onRadioOptionSelected = { selected ->
              chosenBuilding = allowedExtraBuildings.firstOrNull { it.type == selected.id }
            },
            // TODO: Add "Type of building" / "Typ av byggnad" to Lokalise (or reuse CHANGE_ADDRESS_EXTRA_BUILDING_CONTAINER_TITLE).
            style = RadioGroupStyle.Labeled.VerticalWithDivider("Type of building"),
          )
        }
        HedvigTextField(
          text = size?.toString() ?: "",
          onValueChange = {
            isSizeMissing = false
            size = it.toIntOrNull()
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          // TODO: Add "Size (m²)" / "Yta (m²)" to Lokalise (or reuse CHANGE_ADDRESS_EXTRA_BUILDING_SIZE_LABEL).
          labelText = "Size (m²)",
          textFieldSize = TextFieldSize.Medium,
          errorState = if (isSizeMissing) WithoutMessage else NoError,
        )
        HedvigToggle(
          // TODO: Add "Water connected" / "Vattenanslutet" to Lokalise (or reuse CHANGE_ADDRESS_EXTRA_BUILDINGS_WATER_INPUT_LABEL).
          labelText = "Water connected",
          turnedOn = isConnectedToWater,
          onClick = { isConnectedToWater = it },
          enabled = true,
          toggleStyle = ToggleStyle.Default(ToggleDefaultStyleSize.Medium),
        )
      }
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        // TODO: Add "Save" / "Spara" to Lokalise (or reuse general_save_button).
        text = "Save",
        onClick = {
          if (size == null) {
            isSizeMissing = true
          }
          val area = size ?: return@HedvigButton
          val type = chosenBuilding?.type ?: return@HedvigButton
          val displayName = chosenBuilding?.displayName ?: return@HedvigButton
          onSaveBuilding(ExtraBuildingInfo(area, type, displayName, isConnectedToWater))
        },
        enabled = chosenBuilding != null,
        buttonSize = ButtonSize.Large,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        // TODO: Add "Cancel" / "Avbryt" to Lokalise (or reuse general_cancel_button).
        text = "Cancel",
        onClick = dismissDialog,
        enabled = true,
        buttonSize = ButtonSize.Large,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))
    }
  }
}
```

- [ ] **Step 2: Format + compile-check the module**

Run: `./gradlew :feature-purchase-house:ktlintFormat :feature-purchase-house:compileDebugKotlin --quiet`
Expected: completes without errors. Compilation should succeed because no other file references the new symbols yet.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/extrabuildings/ExtraBuildingsSection.kt
git commit -m "feat: add ExtraBuildingsSection for house purchase forms"
```

---

### Task 2: Wire extra buildings into the vacation-home flow

**Files:**
- Modify: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/vacationhome/VacationHomeFormViewModel.kt`
- Modify: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/vacationhome/VacationHomeFormDestination.kt`
- Modify: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/data/SubmitVacationHomeFormAndGetOffersUseCase.kt`

- [ ] **Step 1: Add `extraBuildings` field to `VacationHomeFormState` and two new events**

In `VacationHomeFormViewModel.kt`:

1. At the top of the file, add the import:

```kotlin
import com.hedvig.android.feature.purchase.house.ui.extrabuildings.ExtraBuildingInfo
```

2. In the `VacationHomeFormEvent` sealed interface, add two new event types alongside the existing ones:

```kotlin
data class AddExtraBuilding(val building: ExtraBuildingInfo) : VacationHomeFormEvent

data class RemoveExtraBuilding(val building: ExtraBuildingInfo) : VacationHomeFormEvent
```

3. In `VacationHomeFormState`, add this field (default empty list):

```kotlin
val extraBuildings: List<ExtraBuildingInfo> = emptyList(),
```

- [ ] **Step 2: Handle the two new events in the presenter**

In the same file, inside `VacationHomeFormPresenter.present`'s `CollectEvents { event -> when (event) { ... } }` block, add two new branches:

```kotlin
is VacationHomeFormEvent.AddExtraBuilding -> {
  currentState = currentState.copy(extraBuildings = currentState.extraBuildings + event.building)
}

is VacationHomeFormEvent.RemoveExtraBuilding -> {
  currentState = currentState.copy(
    extraBuildings = currentState.extraBuildings.filterNot { it == event.building },
  )
}
```

- [ ] **Step 3: Pass extra buildings into the submit use case**

In the same file's `LaunchedEffect(submitIteration) { ... }` block, find the call to `submitVacationHomeFormAndGetOffersUseCase.invoke(...)`. Add one final argument before the closing parenthesis:

```kotlin
extraBuildings = currentState.extraBuildings,
```

- [ ] **Step 4: Render `ExtraBuildingsSection` in the destination**

In `VacationHomeFormDestination.kt`:

1. Add the imports:

```kotlin
import com.hedvig.android.feature.purchase.house.ui.extrabuildings.ExtraBuildingsSection
import com.hedvig.android.feature.purchase.house.ui.extrabuildings.allExtraBuildingTypes
```

2. In the destination's content composable (the place where the existing form fields are laid out), after the existing `isSubleted` field and before the submit `HedvigButton`, insert:

```kotlin
Spacer(Modifier.height(16.dp))
ExtraBuildingsSection(
  extraBuildings = uiState.extraBuildings,
  allowedExtraBuildings = allExtraBuildingTypes,
  onAddBuilding = { onEvent(VacationHomeFormEvent.AddExtraBuilding(it)) },
  onRemoveBuilding = { onEvent(VacationHomeFormEvent.RemoveExtraBuilding(it)) },
  enabled = !uiState.isSubmitting,
  modifier = Modifier.fillMaxWidth(),
)
```

If the surrounding scope already imports `Spacer`, `Modifier`, `fillMaxWidth`, and `height`, do not duplicate those imports. Otherwise add them.

- [ ] **Step 5: Add the new parameter to `SubmitVacationHomeFormAndGetOffersUseCase`**

In `SubmitVacationHomeFormAndGetOffersUseCase.kt`:

1. Add the import:

```kotlin
import com.hedvig.android.feature.purchase.house.ui.extrabuildings.ExtraBuildingInfo
```

2. Add a parameter to `invoke(...)`. The current signature lists each form field; add `extraBuildings: List<ExtraBuildingInfo>` at the end of the parameter list.

3. Inside the function body, replace the placeholder line at [SubmitVacationHomeFormAndGetOffersUseCase.kt:59](app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/data/SubmitVacationHomeFormAndGetOffersUseCase.kt:59):

```kotlin
put("extraBuildings", emptyList<Map<String, Any>>())
```

with:

```kotlin
put(
  "extraBuildings",
  extraBuildings.map { building ->
    mapOf(
      "type" to building.type,
      "area" to building.area,
      "hasWaterConnected" to building.hasWaterConnected,
    )
  },
)
```

- [ ] **Step 6: Format + compile-check**

Run: `./gradlew :feature-purchase-house:ktlintFormat :feature-purchase-house:compileDebugKotlin --quiet`
Expected: completes without errors.

- [ ] **Step 7: Commit**

```bash
git add app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/vacationhome/VacationHomeFormViewModel.kt \
        app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/vacationhome/VacationHomeFormDestination.kt \
        app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/data/SubmitVacationHomeFormAndGetOffersUseCase.kt
git commit -m "feat: wire extra buildings into vacation home purchase flow"
```

---

### Task 3: Wire extra buildings into the villa (SE_HOUSE) flow

**Files:**
- Modify: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/house/HouseFormViewModel.kt`
- Modify: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/house/HouseFormDestination.kt`
- Modify: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/data/SubmitHouseFormAndGetOffersUseCase.kt`

The villa form mirrors the vacation home form structure (the commit `de584b9146 refactor: align vacation home form with pet form pattern` was applied across both). Apply identical changes; do **not** assume any helper from Task 2 is reusable across the two flows beyond the file from Task 1.

- [ ] **Step 1: Add `extraBuildings` field to `HouseFormState` and two new events**

In `HouseFormViewModel.kt`:

1. Add the import:

```kotlin
import com.hedvig.android.feature.purchase.house.ui.extrabuildings.ExtraBuildingInfo
```

2. In the `HouseFormEvent` sealed interface, add:

```kotlin
data class AddExtraBuilding(val building: ExtraBuildingInfo) : HouseFormEvent

data class RemoveExtraBuilding(val building: ExtraBuildingInfo) : HouseFormEvent
```

3. In `HouseFormState`, add:

```kotlin
val extraBuildings: List<ExtraBuildingInfo> = emptyList(),
```

- [ ] **Step 2: Handle the two new events in the villa presenter**

Inside the villa presenter's `CollectEvents { event -> when (event) { ... } }` block, add:

```kotlin
is HouseFormEvent.AddExtraBuilding -> {
  currentState = currentState.copy(extraBuildings = currentState.extraBuildings + event.building)
}

is HouseFormEvent.RemoveExtraBuilding -> {
  currentState = currentState.copy(
    extraBuildings = currentState.extraBuildings.filterNot { it == event.building },
  )
}
```

- [ ] **Step 3: Pass extra buildings into the submit use case**

In the villa presenter's `LaunchedEffect(submitIteration) { ... }` block, find the call to `submitHouseFormAndGetOffersUseCase.invoke(...)` and add as the final argument:

```kotlin
extraBuildings = currentState.extraBuildings,
```

- [ ] **Step 4: Render `ExtraBuildingsSection` in the villa destination**

In `HouseFormDestination.kt`:

1. Add the imports:

```kotlin
import com.hedvig.android.feature.purchase.house.ui.extrabuildings.ExtraBuildingsSection
import com.hedvig.android.feature.purchase.house.ui.extrabuildings.allExtraBuildingTypes
```

2. After the existing `isSubleted` field (the last collected form field before the submit button) and before the submit `HedvigButton`, insert:

```kotlin
Spacer(Modifier.height(16.dp))
ExtraBuildingsSection(
  extraBuildings = uiState.extraBuildings,
  allowedExtraBuildings = allExtraBuildingTypes,
  onAddBuilding = { onEvent(HouseFormEvent.AddExtraBuilding(it)) },
  onRemoveBuilding = { onEvent(HouseFormEvent.RemoveExtraBuilding(it)) },
  enabled = !uiState.isSubmitting,
  modifier = Modifier.fillMaxWidth(),
)
```

Add `Spacer`, `Modifier`, `fillMaxWidth`, `height` imports only if not already present.

- [ ] **Step 5: Add the new parameter to `SubmitHouseFormAndGetOffersUseCase`**

In `SubmitHouseFormAndGetOffersUseCase.kt`:

1. Add the import:

```kotlin
import com.hedvig.android.feature.purchase.house.ui.extrabuildings.ExtraBuildingInfo
```

2. Add a parameter `extraBuildings: List<ExtraBuildingInfo>` at the end of `invoke(...)`'s parameter list.

3. Find the placeholder `put("extraBuildings", emptyList<Map<String, Any>>())` line (analogous to the one in `SubmitVacationHomeFormAndGetOffersUseCase`) and replace it with:

```kotlin
put(
  "extraBuildings",
  extraBuildings.map { building ->
    mapOf(
      "type" to building.type,
      "area" to building.area,
      "hasWaterConnected" to building.hasWaterConnected,
    )
  },
)
```

If the file does **not** contain the `put("extraBuildings", emptyList(...))` placeholder (the villa use case may not have been written symmetrically), add a new `put("extraBuildings", ...)` entry to the same map-building block where other fields like `numberOfBathrooms` and `isSubleted` are added.

- [ ] **Step 6: Format + compile-check**

Run: `./gradlew :feature-purchase-house:ktlintFormat :feature-purchase-house:compileDebugKotlin --quiet`
Expected: completes without errors.

- [ ] **Step 7: Commit**

```bash
git add app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/house/HouseFormViewModel.kt \
        app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/house/HouseFormDestination.kt \
        app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/data/SubmitHouseFormAndGetOffersUseCase.kt
git commit -m "feat: wire extra buildings into villa purchase flow"
```

---

### Task 4: Verify in emulator

**Files:** none

- [ ] **Step 1: Invoke the emulator verification skill**

Use the `verifying-android-changes-in-emulator` skill. This is **required** per repo conventions — Android changes must be verified end-to-end, not only by build/compile.

- [ ] **Step 2: Manual scenarios to run**

For each of vacation-home and villa purchase flows:

1. **Empty-list submission:** Enter all required fields, do **not** add any extra buildings, submit. Verify offers screen appears (no regression).
2. **Add and submit:** Enter all required fields, tap "Add extra building", pick `Garage`, enter `25` for size, toggle water on, tap Save. Verify the row appears under "Extra buildings" showing `Garage` / `25 m² ∙ Water connected`. Submit. Verify offers screen appears.
3. **Add multiple, remove one, submit:** Add `Garage` (25 m², water on), add `Shed` (10 m², water off). Tap the close button on the `Garage` row — verify only `Shed` remains. Submit. Verify offers screen appears.
4. **Dialog cancel:** Tap "Add extra building", do not pick anything, tap Cancel — verify dialog closes and no row is added.
5. **Dialog dismiss without size:** Pick `Garage`, leave size empty, tap Save — verify the size field shows the error state and the row is not added.
6. **Repeat all five** for the villa form.

- [ ] **Step 3: Confirm the offers reflect the buildings**

After a submission with one or more extra buildings, compare the displayed offer price against a submission of the same form data **without** extra buildings. The price should differ (the backend uses extra buildings in pricing). If the prices are identical, that is a signal the serialization didn't reach the backend correctly — re-check the `put("extraBuildings", ...)` blocks in both use cases.

- [ ] **Step 4: Run ktlint + module build once more before opening a PR**

Run: `./gradlew :feature-purchase-house:ktlintCheck :feature-purchase-house:assembleDebug --quiet`
Expected: completes without errors.

---

## Out of scope (do not implement)

- Sharing the section with `feature-movingflow` (would require a new `ui-extra-buildings` module). Defer until either UI needs to change in lockstep or a third consumer appears.
- Adding `extraBuildingTypesV2` to `PriceIntent` in the backend.
- Edit-in-place affordance on rows.
- New Lokalise keys (TODO comments only — translation strings ship in a follow-up PR once Lokalise is updated).
