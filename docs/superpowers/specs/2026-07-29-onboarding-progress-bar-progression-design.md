# Onboarding progress bar: continuous progression during the shared transition

## Problem

Each onboarding step is a separate `NavEntry`, i.e. a separate composition
(`OnboardingEntries.kt`). The header `Row` in `OnboardingStepScaffold` is a single
shared element (`sharedBounds`, key `"onboarding-top-bar"`) pinned across steps. During a
step transition both the outgoing and incoming headers are composed at the same bounds and
`sharedBounds` crossfades their content. Because the two `OnboardingProgressBar`s differ
only in how many segments are filled, the user sees the old fill dissolve into the new one
instead of a fill growing left-to-right.

## Goal

Make the progress bar look like it is genuinely progressing left-to-right as the shared
transition runs: a single continuous fill that grows (or shrinks, on back navigation) from
the previous step's fraction to the current step's fraction, seamlessly, with no visible
crossfade between two different states.

## Key insight

The fill must be driven by a value that is **the same instance in both compositions and
animates across the transition**. If the outgoing and incoming bars both read the identical
value every frame, the `sharedBounds` crossfade becomes invisible (both sides look the same)
while the value animates, producing a clean growing fill.

## Design

### 1. Shared, flow-scoped animation holder (UI layer)

A new `internal` class in the onboarding `ui` package, scoped to the onboarding flow's
lifetime (matching `OnboardingSessionStore`, which is `@SingleIn(ActivityRetainedScope::class)`
and survives across every step within one Activity):

```kotlin
@Inject
@SingleIn(ActivityRetainedScope::class)
internal class OnboardingProgressBarAnimation {
  private val animatable = Animatable(0f)
  private var seeded = false

  val fraction: Float get() = animatable.value

  suspend fun moveTo(target: Float) {
    if (!seeded) {
      seeded = true
      animatable.snapTo(target)
    } else {
      animatable.animateTo(target)
    }
  }
}
```

One `Animatable`, one instance for the whole flow. `fraction` is snapshot-backed, so both
the outgoing and incoming step recompose in lockstep as it animates.

The `seeded` flag handles timing edge cases: the first value the holder ever sees snaps
(fresh flow launch, or process-death restore landing directly on a mid-path step), while
every later step-to-step transition animates from the current fill. Back navigation animates
the fill shrinking left, the natural inverse.

### 2. Delivery via CompositionLocal

A `CompositionLocal` defaulting to `null` (mirroring how the scaffold already reads
`LocalSharedTransitionScope` and tolerates `null` in isolated previews):

```kotlin
internal val LocalOnboardingProgressBarAnimation =
  staticCompositionLocalOf<OnboardingProgressBarAnimation?> { null }
```

Each step's `ViewModel` gains the holder as a constructor dependency and exposes it as a
plain property. Each destination's top-level composable wraps its screen content with
`CompositionLocalProvider(LocalOnboardingProgressBarAnimation provides viewModel.progressBarAnimation)`.
This keeps the inner screen composable signatures (and their previews) unchanged; only the
top-level destination composable and the `ViewModel` constructor change.

Delivery has to happen inside the onboarding module because the holder is `internal`; `:app`
cannot reference it, so the established `metroViewModel()` injection path is the seam. This
touches the ~9 step ViewModels and their destinations, each a couple of lines. This is the
cost of the seamless result; it is the reason a self-contained per-step approach would be
cheaper but less crisp.

### 3. Scaffold change

`OnboardingStepScaffold` computes the target fraction and drives the shared holder:

```kotlin
val target = progress?.let { (it.currentIndex + 1f) / it.totalSteps }
val animation = LocalOnboardingProgressBarAnimation.current
LaunchedEffect(target, animation) {
  if (target != null) animation?.moveTo(target)
}
```

`(currentIndex + 1) / totalSteps` reproduces today's behaviour where welcome
(`currentIndex == 0`) already shows one filled segment out of `totalSteps`.

`OnboardingProgressBar` renders a single rounded track instead of N discrete segments:
a `surfaceSecondary` background with a `fillPrimary` foreground bar whose width is the
current fraction of the track, clipped to `CircleShape`. The displayed fraction is
`animation?.fraction` when the holder is present, otherwise `target` directly (static, for
previews). No segment notches/dividers, for the cleanest progressing look.

## Files touched

- New: `OnboardingProgressBarAnimation` holder + `LocalOnboardingProgressBarAnimation`
  (onboarding `ui` package).
- `OnboardingStepScaffold.kt`: read the local, drive the animation, render a continuous fill.
- Each step `ViewModel` that renders the scaffold: add the holder dependency + expose it.
- Each corresponding destination top-level composable: provide the local.

## Non-goals

- No change to how `OnboardingProgress` is computed or where progress comes from.
- No change to the shared-element header key or the `sharedBounds` wiring itself.
- No change to navigation, DI scopes, or the onboarding data layer.

## Edge cases

- Fresh launch / process-death restore onto a mid-path step: `seeded` snaps, no sweep from 0.
- Back navigation: animates the fill shrinking to the previous fraction.
- Isolated previews (no NavEntry, `null` local): static fill at `target`, no animation.
