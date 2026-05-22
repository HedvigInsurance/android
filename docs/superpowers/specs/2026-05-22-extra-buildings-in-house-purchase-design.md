# Extra buildings in house purchase flows

## Context

The web checkout (racoon) collects a list of "extra buildings" (garage, shed, etc.) as part of the price intent for house and vacation-home insurance. The Android in-app purchase flows for the same products (`feature-purchase-house`, screens `VacationHomeFormDestination` and `HouseFormDestination`) currently submit `extraBuildings = emptyList()` as a placeholder, so users on Android cannot influence the price by declaring outbuildings.

The Android move flow (`feature-movingflow`) already has a working extra-buildings UI inside `AddHouseInformationDestination.kt`. We will copy that UI into the purchase module rather than extract a shared module — extraction is deferred until there is a clear third caller.

This spec covers the in-app **purchase** flows only (the buy flow). The move flow is untouched.

## Goal

Allow the user to add and remove extra buildings during the vacation-home and villa (SE_HOUSE) purchase forms, and submit them as part of the existing `priceIntentDataUpdate` mutation so that returned offers reflect the declared buildings.

## Non-goals

- No backend changes. We do **not** add `extraBuildingTypesV2` to `PriceIntent`. The 14 building types are hardcoded client-side, matching racoon's per-template approach.
- No incremental/live mutation per add/remove. The list is collected locally and submitted in the existing batched `SubmitForm` mutation.
- No edit affordance on existing items. Users add and remove only — matching racoon and the move flow.
- No shared `ui-extra-buildings` module. UI is copied into `feature-purchase-house`.
- No moving-flow refactor.

## Locked design decisions

| # | Decision | Choice |
|---|----------|--------|
| 1 | Where shared extra-buildings code lives | Inside `feature-purchase-house`, in a new `ui/extrabuildings/` package, consumed by both purchase forms |
| 2 | Submission timing | Batch — included in the existing form-submit `priceIntentDataUpdate` mutation |
| 3 | UI surface for adding | `HedvigDialog` (copied from move flow, which uses dialog not bottom sheet) |
| 4 | Type list source | Hardcoded client-side list of 14 `MoveExtraBuildingType` entries with Lokalise display names |
| 5 | Edit behavior | Add and remove only; no edit |
| 6 | Code source | Copy from `feature-movingflow` (Path 2), not extract |

## Files changed

### New

`app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/extrabuildings/ExtraBuildingsSection.kt`

Contains, all `internal`:

- `data class ExtraBuildingInfo(val area: Int, val type: String, val displayName: String, val hasWaterConnected: Boolean)`
- `data class MoveExtraBuildingType(val type: String, val displayName: String)`
- `@Composable fun rememberAllExtraBuildingTypes(): List<MoveExtraBuildingType>` — returns the 14 hardcoded entries, with `displayName` resolved via `stringResource(...)`. Wrapped in `remember` keyed on configuration so translations refresh on locale change.
- `@Composable fun ExtraBuildingsSection(extraBuildings: List<ExtraBuildingInfo>, allowedExtraBuildings: List<MoveExtraBuildingType>, onAddBuilding: (ExtraBuildingInfo) -> Unit, onRemoveBuilding: (ExtraBuildingInfo) -> Unit, enabled: Boolean, modifier: Modifier = Modifier)` — copied from `ExtraBuildingsCard`, with the `ListInput<T>` parameter replaced by plain list + callbacks. `enabled` mirrors the move-flow's `shouldDisableInput` semantics but inverted (true = interactive).
- `@Composable private fun AddExtraBuildingDialogContent(...)` — copied from `ExtraBuildingsDialogContent`, with the same parameter substitution.

### Modified

**`app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/vacationhome/VacationHomeFormViewModel.kt`**

- `VacationHomeFormState` gains `val extraBuildings: List<ExtraBuildingInfo> = emptyList()`.
- `VacationHomeFormEvent` gains:
  - `data class AddExtraBuilding(val building: ExtraBuildingInfo) : VacationHomeFormEvent`
  - `data class RemoveExtraBuilding(val building: ExtraBuildingInfo) : VacationHomeFormEvent`
- `CollectEvents { ... }` handles the two new events with `currentState.copy(extraBuildings = currentState.extraBuildings + event.building)` and `currentState.copy(extraBuildings = currentState.extraBuildings.filterNot { it == event.building })`.
- The `SubmitForm` branch reads `currentState.extraBuildings` and passes it to the use-case call.

**`app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/vacationhome/VacationHomeFormDestination.kt`**

- Render `ExtraBuildingsSection(...)` after the `isSubleted` field, before the submit button. Wire `onAddBuilding`/`onRemoveBuilding` to emit the new events. Build `allowedExtraBuildings` via `rememberAllExtraBuildingTypes()`.

**`app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/house/HouseFormViewModel.kt`** (lives on `feat/in-app-house-purchase`)

Same shape of changes as `VacationHomeFormViewModel.kt`.

**`app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/house/HouseFormDestination.kt`** (lives on `feat/in-app-house-purchase`)

Same shape of changes as `VacationHomeFormDestination.kt`.

**`app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/data/SubmitVacationHomeFormAndGetOffersUseCase.kt`**

- Add parameter: `extraBuildings: List<ExtraBuildingInfo>`.
- Replace the placeholder `put("extraBuildings", emptyList<Map<String, Any>>())` (currently around line 59) with:

  ```kotlin
  put("extraBuildings", extraBuildings.map {
    mapOf(
      "type" to it.type,
      "area" to it.area,
      "hasWaterConnected" to it.hasWaterConnected,
    )
  })
  ```

**`app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/data/SubmitHouseFormAndGetOffersUseCase.kt`** (lives on `feat/in-app-house-purchase`)

Same shape of changes as `SubmitVacationHomeFormAndGetOffersUseCase.kt`.

## Data model

`ExtraBuildingInfo` is the in-app value object. `displayName` is client-side only and is never serialised to the backend — only `type`, `area`, and `hasWaterConnected` are sent.

`MoveExtraBuildingType` is a UI helper pairing the canonical type string with a translated display name. The canonical strings mirror the `MoveExtraBuildingType` GraphQL enum: `GARAGE`, `CARPORT`, `SHED`, `STOREHOUSE`, `FRIGGEBOD`, `ATTEFALL`, `OUTHOUSE`, `GUESTHOUSE`, `GAZEBO`, `GREENHOUSE`, `SAUNA`, `BARN`, `BOATHOUSE`, `OTHER`.

## Lokalise

Reuse the existing `CHANGE_ADDRESS_EXTRA_BUILDINGS_*` keys for the section title, "Add" button, water label, size label, and bottom-sheet title — the UI text is identical between the move and purchase contexts, and renaming would require a Lokalise round-trip with no UX benefit.

For the 14 building-type display names: if existing Lokalise keys cover them (e.g. via the move flow), reuse those keys. If any are missing, stub with `// TODO: Add "<English>" / "<Swedish>" to Lokalise` comments mirroring the existing TODO pattern in `VacationHomeFormViewModel.kt`.

## Validation

- The extra-buildings list is optional. Empty list is valid; no submission-time validation is added.
- Inside the add-dialog (copied as-is from the move flow): the user must pick a type before "Save" enables. Missing area shows the existing `WithoutMessage` error state.

## Branching

This worktree (`claude/amazing-murdock-160a34`) is based on `feat/in-app-vacation-home-purchase`, which contains only the vacation-home form. The villa (`HouseForm*`) files live on `feat/in-app-house-purchase`, stacked on top.

The implementation branch should stack on `feat/in-app-house-purchase` so both forms can be modified in one PR. If kept on the vacation-home branch, the villa changes would have to land in a follow-up PR after rebase.

## Test plan

- Manual: open the vacation-home purchase flow, add two buildings of different types with water connected/not, remove one, submit. Verify offers reflect the data. Repeat for villa.
- Manual: submit with no extra buildings — verify the existing batched flow behaves unchanged.
- Existing unit tests (if any) for the form view models continue to pass; if they assert on state shape, extend them to cover the new `extraBuildings` field.

## Out of scope / follow-ups

- Sharing the UI with the move flow (extract to a `ui-extra-buildings` module). Defer until either UI needs to change in lockstep, or a third consumer appears.
- Server-driven type list on `PriceIntent` (would require backend work to add `extraBuildingTypesV2` there).
- Edit-in-place affordance on existing rows.
