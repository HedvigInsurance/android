# Onboarding Invite Card Staggered Animation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Animate the example referrals card on the onboarding invite step so it starts with one friend row and staggers in the 2nd and 3rd rows, playing only the very first time the step is seen.

**Architecture:** Pure UI-driven staggering inside `ExampleReferralsCard` in `OnboardingInviteDestination.kt`; a `LaunchedEffect` advances a `visibleRows` counter with delays, rows 2 and 3 enter via `AnimatedVisibility(expandVertically + fadeIn)`, and a plain "played" flag on `OnboardingInviteViewModel` (retained while the invite entry is on the back stack) suppresses replays. No presenter or UiState changes.

**Tech Stack:** Jetpack Compose animation (`AnimatedVisibility`, `expandVertically`, `fadeIn`, `tween`), design-system `MotionTokens`, `kotlinx.coroutines.delay`.

## Global Constraints

- Spec: `docs/superpowers/specs/2026-08-03-onboarding-invite-card-animation-design.md`.
- All changes in one file: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/ui/invite/OnboardingInviteDestination.kt`.
- Timing values must be named constants grouped in one place, easy to tweak with the designer: 450 ms initial pause, 600 ms stagger, 400 ms reveal (`MotionTokens.DurationMedium4`) with `MotionTokens.EasingEmphasizedDecelerateCubicBezier`.
- The "played" flag is set only when the sequence completes.
- No unit tests: the change is pure presentation timing with no extractable logic (per spec); verification is compile + ktlint + visual pass with the designer.
- ktlint: 2-space indent, 120-char lines, trailing commas. Run `./gradlew ktlintFormat` before committing.
- When checking Gradle output, grep for `BUILD SUCCESSFUL` explicitly; piping to `tail`/`grep` masks the exit code.

---

### Task 1: Staggered row entrance in ExampleReferralsCard

**Files:**
- Modify: `app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/ui/invite/OnboardingInviteDestination.kt`

**Interfaces:**
- Consumes: existing `OnboardingInviteViewModel`, `ExampleReferralsCard`, design-system `MotionTokens` (already used elsewhere in `:feature-onboarding`).
- Produces: `OnboardingInviteViewModel.inviteCardAnimationPlayed: Boolean` (plain `var`, default `false`); `ExampleReferralsCard(incentiveDisplay: String, animationAlreadyPlayed: Boolean, onAnimationCompleted: () -> Unit, modifier: Modifier)`. Nothing outside this file consumes either.

- [ ] **Step 1: Add the played flag to the ViewModel**

In `OnboardingInviteViewModel`, add a body with the flag (the class currently has no body):

```kotlin
@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class OnboardingInviteViewModel(
  sessionStore: OnboardingSessionStore,
  navigator: OnboardingNavigator,
  val progressBarAnimation: OnboardingProgressBarAnimation,
) : MoleculeViewModel<OnboardingInviteEvent, OnboardingInviteUiState>(
    initialState = OnboardingInviteUiState.Loading,
    presenter = OnboardingInvitePresenter(sessionStore, navigator),
  ) {
  /**
   * The example referrals card staggers its rows in the first time it is seen. Lives here so the
   * one-shot survives leaving and returning to this step within one onboarding session.
   */
  var inviteCardAnimationPlayed: Boolean = false
}
```

- [ ] **Step 2: Rewrite ExampleReferralsCard with the staggered reveal**

Replace the current `ExampleReferralsCard` with the version below, and extract the row content into a private `ExampleReferralRow` so the static first row and the animated rows share it:

```kotlin
@Composable
private fun ExampleReferralsCard(
  incentiveDisplay: String,
  animationAlreadyPlayed: Boolean,
  onAnimationCompleted: () -> Unit,
  modifier: Modifier = Modifier,
) {
  // Illustrative, hardcoded example names showing what the referral list looks like once populated.
  val exampleNames = listOf("Hampus", "Li", "Elin")
  var visibleRows by remember { mutableIntStateOf(if (animationAlreadyPlayed) exampleNames.size else 1) }
  LaunchedEffect(Unit) {
    if (animationAlreadyPlayed) return@LaunchedEffect
    delay(RowRevealInitialDelayMillis)
    visibleRows = 2
    delay(RowRevealStaggerDelayMillis)
    visibleRows = 3
    onAnimationCompleted()
  }
  Surface(
    modifier = modifier,
    shape = HedvigTheme.shapes.cornerLarge,
    color = HedvigTheme.colorScheme.surfacePrimary,
  ) {
    Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
      exampleNames.forEachIndexed { index, name ->
        if (index == 0) {
          ExampleReferralRow(name = name, incentiveDisplay = incentiveDisplay)
        } else {
          AnimatedVisibility(
            visible = visibleRows > index,
            enter = expandVertically(rowRevealAnimationSpec()) + fadeIn(rowRevealAnimationSpec()),
          ) {
            Column {
              HorizontalDivider()
              ExampleReferralRow(name = name, incentiveDisplay = incentiveDisplay)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ExampleReferralRow(name: String, incentiveDisplay: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(vertical = 12.dp),
  ) {
    Box(
      Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(HedvigTheme.colorScheme.signalGreenElement),
    )
    Spacer(Modifier.width(8.dp))
    HedvigText(text = name, style = HedvigTheme.typography.bodySmall)
    Spacer(Modifier.weight(1f))
    HedvigText(text = "-$incentiveDisplay", style = HedvigTheme.typography.bodySmall)
  }
}

// Reveal timing for the example referral rows, grouped for easy tuning with design.
private const val RowRevealInitialDelayMillis = 450L
private const val RowRevealStaggerDelayMillis = 600L

private fun <T> rowRevealAnimationSpec(): TweenSpec<T> = tween(
  durationMillis = MotionTokens.DurationMedium4.toInt(),
  easing = MotionTokens.EasingEmphasizedDecelerateCubicBezier,
)
```

Note: `expandVertically` takes an `FiniteAnimationSpec<IntSize>` and `fadeIn` a `FiniteAnimationSpec<Float>`, hence the generic helper. If ktlint or the compiler complains about the generic function, inline the two `tween(...)` calls into `expandVertically`/`fadeIn` instead and keep the duration/easing referenced from `MotionTokens` directly.

- [ ] **Step 3: Wire the card call site to the ViewModel flag**

In `OnboardingInviteDestination`, update the `ExampleReferralsCard` call inside the `Content` branch:

```kotlin
ExampleReferralsCard(
  incentiveDisplay = state.incentiveDisplay,
  animationAlreadyPlayed = viewModel.inviteCardAnimationPlayed,
  onAnimationCompleted = { viewModel.inviteCardAnimationPlayed = true },
  modifier = Modifier
    .align(Alignment.CenterHorizontally)
    .fillMaxWidth(0.72f),
)
```

- [ ] **Step 4: Add the new imports**

Add to the existing import list (keep alphabetical order, no wildcards):

```kotlin
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import com.hedvig.android.design.system.hedvig.tokens.MotionTokens
import kotlinx.coroutines.delay
```

`mutableIntStateOf`, `getValue`/`setValue`, and `remember` are already imported.

- [ ] **Step 5: Format and compile**

Run:

```bash
./gradlew :feature-onboarding:ktlintFormat :feature-onboarding:compileDebugKotlin
```

Expected: output contains `BUILD SUCCESSFUL` (check the full output, not a piped tail).

- [ ] **Step 6: Commit**

```bash
git add app/feature/feature-onboarding/src/main/kotlin/com/hedvig/android/feature/onboarding/ui/invite/OnboardingInviteDestination.kt
git commit -m "Stagger in the example referral rows on the onboarding invite step"
```

- [ ] **Step 7: Visual verification (manual)**

Install the develop build on a device/emulator and walk the onboarding flow to the invite step. Verify:
- Card appears with only "Hampus", then "Li" expands+fades in after ~450 ms, then "Elin" ~600 ms later.
- Card stays horizontally/vertically centered while growing.
- Continue to the next step, navigate back: all three rows show immediately, no replay.

This step is the designer-tuning entry point; timing constants live at the bottom of `OnboardingInviteDestination.kt`.
