# Onboarding progress bar: continuous progression during the shared transition

## Problem

Each onboarding step is a separate `NavEntry`, i.e. a separate composition
(`OnboardingEntries.kt`). The header `Row` in `OnboardingStepScaffold` is a single
shared element (`sharedBounds`, key `"onboarding-top-bar"`) pinned across steps. During a
step transition both the outgoing and incoming headers are composed at the same bounds and
`sharedBounds` crossfades their content. Because the two progress bars differ only in how
many segments are filled, the user sees the old fill dissolve into the new one instead of a
fill moving across.

## Goal

The bar should look like one continuous fill that moves from the previous step to the current
step as the transition runs, with the dividers between segments still visible. It must track a
predictive-back gesture frame for frame and return smoothly if the gesture is cancelled, rather
than jumping or getting stranded.

## Key insight

The fill must be driven by a value that is **the same in both compositions and moves with the
transition**. If the outgoing and incoming bars render the identical value every frame, the
`sharedBounds` crossfade becomes invisible (both sides look the same) while the value moves.

Compose already exposes a value that moves with the transition, including predictive-back seek:
`animatedVisibilityScope.transition.animateFloat { Visible -> 1f else 0f }`. This is each step's
"how visible am I" (1 on screen, 0 gone), and Nav3 seeks it with the gesture. Driving the fill
from this, rather than a self-timed animation, is what makes it track and cancel correctly.

## Design

### 1. Shared, flow-scoped holder (UI layer)

A new `internal` class in the onboarding `ui` package, scoped to the onboarding flow's lifetime
(matching `OnboardingSessionStore`, `@SingleIn(ActivityRetainedScope::class)`):

```kotlin
@Inject
@SingleIn(ActivityRetainedScope::class)
internal class OnboardingProgressBarAnimation {
  private val visibleSteps = mutableStateMapOf<Any, VisibleStep>()

  val filledStepCount: Float
    get() {
      var stepNumberSum = 0f
      var visibleSum = 0f
      for (step in visibleSteps.values) {
        stepNumberSum += step.stepNumber * step.visibleAmount
        visibleSum += step.visibleAmount
      }
      if (visibleSum > MinimumVisible) return stepNumberSum / visibleSum
      if (visibleSteps.isEmpty()) return 0f
      return visibleSteps.values.sumOf { it.stepNumber.toDouble() }.toFloat() / visibleSteps.size
    }

  fun setVisibleStep(key: Any, stepNumber: Int, visibleAmount: Float) { visibleSteps[key] = VisibleStep(stepNumber, visibleAmount) }
  fun removeStep(key: Any) { visibleSteps.remove(key) }

  private data class VisibleStep(val stepNumber: Int, val visibleAmount: Float)
  private companion object { const val MinimumVisible = 0.0001f }
}
```

Every step currently on screen registers itself with its **step number** (`currentIndex + 1`,
1-based) and its **visible amount** (0–1). `filledStepCount` is the step numbers averaged by
visibility, a possibly-fractional step count. Because it is a weighted average it always lands
between the smallest and largest on-screen step number, so it never runs away as step numbers
grow. The map only ever holds the on-screen steps (one at rest, two during a transition; each
step removes itself on dispose).

Why this shape rather than a single `Animatable.animateTo(target)`: a self-timed animation runs
on its own clock, so it jumps instead of tracking the gesture, and a cancelled predictive-back
gesture leaves it stranded with nothing to drive it back. A visibility-weighted average is a pure
function of the seekable transition state, so it tracks and cancels for free. A single on-screen
step averages to its own number, so fresh launches and process-death restores need no seeding.

### 2. Delivery

`OnboardingStepScaffold` takes a nullable `progressAnimation: OnboardingProgressBarAnimation?`
(null in isolated previews, mirroring how it already tolerates `LocalSharedTransitionScope == null`).
Each step's `ViewModel` gains the holder as a constructor dependency and exposes it as a plain
property; each destination passes `viewModel.progressBarAnimation` into the scaffold.

Delivery has to happen inside the onboarding module because the holder is `internal` and each step
is a separate `NavEntry` with no shared parent composition, so the established `metroViewModel()`
injection path is the only seam. A `CompositionLocal` would not avoid this (it would still need a
per-entry instance to provide), so an explicit param is the simpler, more honest choice. This
touches the ~9 step ViewModels and their destinations, a couple of lines each.

### 3. Scaffold change

Inside `OnboardingStepScaffold`, `animatedVisibilityScope` is read once
(`LocalNavAnimatedContentScope.current` when a shared-transition scope is present, else null) and
passed to `OnboardingProgressBar` alongside the holder. The bar:

```kotlin
val stepNumber = progress.currentIndex + 1
val filledStepCount = if (animation != null && animatedVisibilityScope != null) {
  val visibleAmount = animatedVisibilityScope.transition.animateFloat(
    transitionSpec = { tween(durationMillis = 300, easing = FastOutSlowInEasing) },
    label = "onboardingStepVisibleAmount",
  ) { state -> if (state == EnterExitState.Visible) 1f else 0f }
  val key = remember { Any() }
  DisposableEffect(animation, key) { onDispose { animation.removeStep(key) } }
  LaunchedEffect(animation, key, stepNumber) {
    snapshotFlow { visibleAmount.value }.collect { animation.setVisibleStep(key, stepNumber, it) }
  }
  animation.filledStepCount
} else {
  stepNumber.toFloat()
}
```

The bar is a `Row` of `totalSteps` slices (`Arrangement.spacedBy(4.dp)` keeps the dividers). Slice
`index` fills by `(filledStepCount - index).coerceIn(0f, 1f)`, so slices fill in turn and the fill
flows continuously across the dividers. At rest `filledStepCount == stepNumber`, reproducing the
original discrete behaviour (welcome shows one filled slice).

The presence tween is `300ms` to match the nav `sharedXAxis` transition (`DurationMedium2`); during
a predictive-back seek it is driven by the gesture regardless of the tween.

## Files touched

- New: `OnboardingProgressBarAnimation` holder (onboarding `ui` package).
- `OnboardingStepScaffold.kt`: read the animated-visibility scope, drive/read the holder, render
  the segmented continuous fill.
- Each step `ViewModel` that renders the scaffold: add the holder dependency + expose it.
- Each corresponding destination: pass `viewModel.progressBarAnimation` into the scaffold.

## Non-goals

- No change to how `OnboardingProgress` is computed or where progress comes from.
- No change to the shared-element header key or the `sharedBounds` wiring itself.
- No change to navigation, DI scopes, or the onboarding data layer.

## Edge cases

- Fresh launch / process-death restore onto a mid-path step: one on-screen step, so the average is
  just its own number, no sweep from 0.
- Back navigation and predictive-back seek: the average moves between the two on-screen steps as
  their visibilities trade off; a cancelled gesture returns smoothly as the visibilities return.
- Isolated previews (no NavEntry, null holder/scope): the bar fills statically up to `stepNumber`.
