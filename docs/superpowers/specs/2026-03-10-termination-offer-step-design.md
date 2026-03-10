# Moving Flow Discount - Termination Flow (RND-1453)

## Overview

Add a new interstitial offer screen to the termination flow that promotes retention by offering discounts or alternative options before the user completes cancellation. The screen appears at two points in the flow, driven entirely by the backend.

## Flows

### Flow 1: "Flyttar till en ny adress" (Moving)

1. Survey screen → user selects "Flyttar till en ny adress" → Continue
2. Backend returns new `FlowTerminationOfferStep` (action: UPDATE_ADDRESS)
3. **New offer screen** — "Erbjudande för dig"
   - Green card with offer text (e.g. "20% rabatt de första 6 månaderna")
   - CTA: "Få ett prisförslag" → navigates to moving flow
   - Skip: "Hoppa över" → calls next mutation
4. Backend returns sub-options survey step (Jag har flyttat ihop med någon, etc.)
5. Sub-options → continue termination

### Flow 2: "Hittat ett bättre pris" → "Ändra skyddsnivå" (Better price)

1. Survey screen → user selects "Hittat ett bättre pris" → Continue
2. Backend returns sub-options survey step (includes "Ändra skyddsnivå")
3. User selects "Ändra skyddsnivå" → Continue
4. Backend returns new `FlowTerminationOfferStep` (action: CHANGE_TIER)
5. **New offer screen** — pushing tier change
   - CTA → navigates to change tier flow
   - Skip → calls next mutation, continues termination

## Backend Contract

### New step type: `FlowTerminationOfferStep`

```graphql
type FlowTerminationOfferStep {
  id: ID!
  title: String!           # Screen title, e.g. "Erbjudande för dig"
  description: String!     # Offer body text
  buttonTitle: String!     # CTA button text, e.g. "Få ett prisförslag"
  skipButtonTitle: String! # Skip button text, e.g. "Hoppa över"
  action: FlowTerminationOfferAction!
}

enum FlowTerminationOfferAction {
  UPDATE_ADDRESS
  CHANGE_TIER
}
```

The backend inserts this step in the flow response:
- After "Flyttar till en ny adress" is selected (before sub-options)
- After "Ändra skyddsnivå" sub-option is selected (before continuing termination)

### Skip mutation

When the user taps "Hoppa över", the app calls the existing `FlowTerminationSurveyNextMutation` (or a new dedicated mutation if the backend prefers) to advance to the next step.

## Android Changes

### 1. GraphQL Fragment

Add `FlowTerminationOfferStep` to `FragmentTerminationFlowStepFragment.graphql`:

```graphql
... on FlowTerminationOfferStep {
  id
  title
  description
  buttonTitle
  skipButtonTitle
  action
}
```

### 2. Data Layer — `TerminateInsuranceStep`

Add a new sealed class variant:

```kotlin
data class TerminationOfferStep(
  val id: String,
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

Add parsing in the step mapping function to handle the new GraphQL type.

### 3. Navigation — `TerminateInsuranceDestination`

Add new destination:

```kotlin
@Serializable
data class OfferScreen(
  val title: String,
  val description: String,
  val buttonTitle: String,
  val skipButtonTitle: String,
  val action: OfferAction,
) : TerminateInsuranceDestination
```

### 4. UI — `TerminationOfferDestination.kt`

New composable matching the design:
- Title at top ("Erbjudande för dig")
- Centered green `HedvigNotificationCard` with checkmark icon, description text, and CTA button
- "Hoppa över" text button anchored at the bottom

### 5. Navigation Graph — `TerminateInsuranceGraph.kt`

Wire the new destination:
- CTA button:
  - `UPDATE_ADDRESS` → `navigateToMovingFlow()`
  - `CHANGE_TIER` → `redirectToChangeTierFlow()`
- Skip button → call next mutation to get next step, navigate accordingly

### 6. Step-to-Destination Mapping

Update `TerminateInsuranceStep.toTerminateInsuranceDestination()` to map `TerminationOfferStep` → `TerminateInsuranceDestination.OfferScreen`.

## Files to Modify

| File | Change |
|------|--------|
| `FragmentTerminationFlowStepFragment.graphql` | Add FlowTerminationOfferStep fragment |
| `TerminateInsuranceStep.kt` | Add OfferStep data class + parsing |
| `TerminateInsuranceDestination.kt` | Add OfferScreen destination |
| `TerminateInsuranceGraph.kt` | Wire new destination with navigation |
| `TerminationOfferDestination.kt` (new) | Offer screen composable |
| `TerminationOfferViewModel.kt` (new) | Handle skip action (call next mutation) |
| `TerminateInsuranceModule.kt` | Register new ViewModel in Koin |

## UI Design Reference

Screen layout (from designs):
- Dark background
- Back arrow (left) + close X (right) in toolbar
- Title: bold, top-left aligned
- Green card (centered vertically or bottom-anchored):
  - Green checkmark icon
  - Description text (white on green)
  - White CTA button inside the card
- "Hoppa över" text button at the very bottom

## Out of Scope (V2)

- Compricer quote for "other" exposure
- Push notification with prefilled discount code
