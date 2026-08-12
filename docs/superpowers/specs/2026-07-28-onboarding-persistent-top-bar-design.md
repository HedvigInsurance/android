# Onboarding top bar as a persistent shared element

## Problem

In the onboarding flow every step is its own Nav3 entry (`OnboardingKey` for
welcome, `OnboardingStepKey` for each subsequent step). Each entry independently
renders its own top header through `OnboardingStepScaffold`: a 56dp `Row` holding
the back button, the progress-steps bar, and the close button.

Because the header is part of the entry, it animates with the rest of the screen
during navigation. The default onboarding transition is `sharedXAxis` (slide +
fade), so the header slides and fades along with the page content. We want the
header, and specifically the progress-steps component, to stay anchored at the
top so the member perceives one continuous progressive flow rather than a fresh
screen each step.

## Goal

During transitions between onboarding entries that share the scaffold, the entire
56dp header (back button + progress steps + close) stays visually pinned at the
top while only the page content below it slides/fades. Content within the header
that legitimately differs per step (the progress fill advancing, the back button
appearing after welcome) cross-fades in place.

## Mechanism

Nav3 already runs the whole app inside a single `SharedTransitionLayout` in
`HedvigApp`, exposed app-wide via `LocalSharedTransitionScope` (from
`:compose-ui`). It also provides `LocalNavAnimatedContentScope` (from
`navigation3-ui`) inside every `NavEntry`.

A shared element declared with a stable key in **both** the outgoing and incoming
entry is lifted into the transition overlay, and its bounds are animated from the
source position to the target position. Because the header occupies the identical
position and size in every onboarding entry, that bounds animation is a visual
no-op: the bar looks pinned while everything below runs the normal `sharedXAxis`
transition.

We use `sharedBounds` (not `sharedElement`) because the header's content is
legitimately different between steps; `sharedBounds` cross-fades the differing
content while animating the shared bounds.

## Scope of the change

One production file: `OnboardingStepScaffold.kt`.

- Wrap the header `Row` in
  `Modifier.sharedBounds(rememberSharedContentState(key), animatedVisibilityScope = LocalNavAnimatedContentScope.current)`
  using the app-wide `LocalSharedTransitionScope`.
- The key is a single module-level constant so every onboarding entry matches the
  same element.

Every onboarding destination already routes its header through this one scaffold,
so no per-destination edits are needed.

### Preview safety

The scaffold's `@HedvigPreview`s call it outside any `SharedTransitionLayout` or
`NavEntry`. `LocalSharedTransitionScope.current` is nullable and safe to read
(null in previews), but `LocalNavAnimatedContentScope.current` throws when read
outside a NavEntry.

Guard: read the nullable shared-transition scope first; only when it is non-null
(i.e. we are in the live app) do we touch `LocalNavAnimatedContentScope` and apply
`sharedBounds`. Otherwise the header renders plainly. Previews keep working.

### Module dependencies

`feature-onboarding` reaches neither local transitively today
(`navigation-compose` pulls `navigation3-ui` as `implementation`; `:compose-ui`
is not exposed). Add two:

- `implementation(projects.composeUi)` for `LocalSharedTransitionScope`
- `implementation(libs.androidx.navigation3.ui)` for `LocalNavAnimatedContentScope`

## Explicitly out of scope

- `OnboardingForeverKey` renders `ForeverDestination`, not this scaffold, so it has
  no header to match. Navigating into/out of it (and into the external Trustly
  connect-payment flow) has no shared element and transitions normally. This is
  the intended behavior: only in-flow steps sharing the scaffold carry the pinned
  bar.
- No progress-fill animation beyond the default `sharedBounds` cross-fade.
- No new tests. This is a visual transition with no logic branch; the existing
  (now guarded) scaffold previews plus a manual run-through of the flow are the
  verification surface.
