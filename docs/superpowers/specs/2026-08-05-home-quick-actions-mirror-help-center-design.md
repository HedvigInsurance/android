# Home quick-action tiles mirror the Help Center

## Goal

The three quick-action tiles on the Home screen should stop being a hardcoded set
(Edit insurance / Change address / Travel certificate) and instead show the **first
three** of the member's actual Help Center quick-action list, in the same order, with
the same eligibility and the same behavior, each rendered with an appropriate icon.

Specifically:

- The "Edit insurance" tile must not navigate directly to the change-tier flow. It must
  mirror the Help Center's "Edit insurance" option, which opens a bottom sheet of
  sub-options (edit co-insured, edit co-owners, change coverage, terminate) filtered by
  eligibility.
- Every tile taps through to exactly the destination the Help Center would use for that
  action.

## Background / current state

The Help Center quick-action machinery lives entirely in `feature-help-center`:

- `GetMemberActionsUseCase` runs `MemberActionsQuery` and returns a project-owned
  `MemberAction` (the `is…Enabled` eligibility flags plus FirstVet/SickAbroad details).
- `GetQuickLinksUseCase` builds an ordered `List<QuickAction>` from `MemberAction` (plus a
  contracts query for co-insured/co-owner). Order: **Edit insurance → Change address →
  Payments → Travel certificate → FirstVet → Sick abroad**, each present only if eligible.
- `QuickAction` is `StandaloneQuickLink(titleRes, hintTextRes, quickLinkDestination)` or
  `MultiSelectExpandedLink(titleRes, hintTextRes, links)`. "Edit insurance" is the
  multi-select; tapping it opens a bottom sheet listing its sub-links.
- `QuickLinkDestination` has `OuterDestination` (navigable flows: change tier, change
  address, connect payment, travel certificate, termination, co-insured/co-owner) and
  `InnerHelpCenterDestination` (FirstVet → the member's FirstVet sections; SickAbroad →
  `EmergencyKey(deflectData)`; deflect UI already lives in the shared
  `app/shared/partners-deflect` module).

`feature-home` cannot depend on `feature-help-center` (feature-to-feature dependencies are
banned), so none of this is reachable from Home today.

Home currently gates three hardcoded tiles on three booleans
(`isEditInsuranceEnabled` / `isMovingEnabled` / `isTravelCertificateEnabled`) sourced from
`memberActions` fields added to `QueryHome`. This design supersedes that (see
"Relationship to the recent gating work").

## Non-goals (YAGNI)

- No icons added to the Help Center list; Help Center's appearance is unchanged.
- No generic cross-feature quick-actions **UI** component; Home renders its own tiles and
  its own Edit-insurance sheet.
- No change to Help Center behavior, ordering, or destinations.

## Architecture

### 1. New shared module: `app/shared/member-quick-actions`

A KMP module (commonMain, no UI, no design-system dependency) that becomes the single
source of truth for member actions and quick links. Moved into it from
`feature-help-center`:

- `GetMemberActionsUseCase` + `MemberAction` (and `MemberActionWithDetails`).
- `GetQuickLinksUseCase`, renamed `GetMemberQuickActionsUseCase`, + `QuickLinkDestination`.
- `QuickAction` model.
- The GraphQL operations they own: `MemberActionsQuery`, `AvailableSelfServiceOnContractsQuery`.

`QuickLinkDestination` (including the previously-`internal` `InnerHelpCenterDestination`
cases for FirstVet and SickAbroad) becomes public in the shared module, since both Help
Center and Home map destinations to navigation.

Dependencies: apollo octopus, featureflags, `shared/partners-deflect` (for `DeflectData`),
`ui-emergency` (for `FirstVetSection`), core-common, logging, metro. Bindings remain
`@ContributesBinding(AppScope::class)`, so DI wiring is unchanged.

### 2. Help Center refactor (pure move, no behavior change)

`feature-help-center` depends on the new module and imports the moved types/use cases
instead of its local copies. List, ordering, and the Edit-insurance multi-select render
exactly as today. `GetQuickLinksUseCaseTest` moves to the shared module.

### 3. Home consumption

`HomePresenter` injects `GetMemberQuickActionsUseCase`, obtains `List<QuickAction>`, takes
the **first three** (no exclusions — FirstVet and SickAbroad are both navigable from Home),
and exposes them on `HomeUiState.Success`. This replaces the three `is…Enabled` booleans
and the hardcoded tiles, and lets Home drop the `memberActions` fields from `QueryHome`
(the shared use case runs its own query). The quick-actions section is hidden when the list
is empty (same empty-guard behavior, now list-driven).

### 4. Home UI

Render up to three tiles, each an icon plus the action's `titleRes`. The icon is mapped in
Home's UI layer from the action type / destination (e.g. `Settings` → Edit insurance,
`Reload` → Change address, a card icon → Payments, `Travel` → Travel certificate, a FirstVet
glyph → FirstVet, a fitting glyph → Sick abroad). Tap behavior:

- `StandaloneQuickLink` → navigate via a Home `(QuickLinkDestination) -> Unit` handler.
- `MultiSelectExpandedLink` (Edit insurance) → open a Home-owned `HedvigBottomSheet` listing
  its sub-links; each row navigates through the same handler. (The Help Center sheet
  composable is not reusable cross-feature, so Home builds its own simple sheet.)

### 5. Navigation wiring

`:app` owns a single `navigateToQuickLink(destination: QuickLinkDestination)` mapping
(destination → `backstack.add(key)` / tab switch) and uses it to wire **both** Help Center
and Home, so the destination→nav-key mapping is not duplicated. SickAbroad → `EmergencyKey`,
FirstVet → Home's existing FirstVet destination, the rest to their existing keys (all in
`-navigation` modules `:app` already depends on).

## Relationship to the recent gating work

Two commits on this branch (`gate Edit insurance tile on change-tier eligibility` and
`gate all quick-action tiles on member eligibility`) implemented the interim hardcoded-tile
gating via boolean flags on `QueryHome`. This design replaces that implementation: the
booleans and the `QueryHome.memberActions` additions are removed in favor of the shared
`GetMemberQuickActionsUseCase`. The interface-refactor commit
(`drop the vestigial isHelpCenterEnabled interface property`) stands on its own and is kept.

## Testing

- Move `GetQuickLinksUseCaseTest` to the shared module.
- Add a `HomePresenter` test for the list mapping: first-three selection, fewer-than-three,
  and the empty case (section hidden).
- Existing Help Center tests continue to pass unchanged after the move.

## Risks / notes

- The move touches `feature-help-center` broadly (imports); the plan should do the move as
  a mechanical first step and verify Help Center compiles + tests pass before Home consumes
  the module.
- `QuickLinkDestination` gains public visibility for its inner cases; confirm nothing relied
  on their `internal` scoping for correctness (they are data holders).
