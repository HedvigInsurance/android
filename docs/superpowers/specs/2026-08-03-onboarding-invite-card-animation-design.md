# Staggered friend-row entrance on the onboarding invite step

## Goal

Give the invite-a-friend onboarding step a playful feel: the example referrals card starts
with one friend row visible, then the 2nd and 3rd rows animate in one after the other, the
card visibly growing with each reveal. Timing values will be tuned together with the
designer afterwards, so they must be easy to find and tweak.

## Behavior

- On first arrival at the invite step (once `Content` is showing), the card shows only the
  first row ("Hampus"). After a short pause the 2nd row expands and fades in, then after
  another pause the 3rd row does the same.
- The animation plays only the very first time. A completion flag lives on
  `OnboardingInviteViewModel`, which is retained while the invite entry is on the back
  stack, so returning to the step (from the next step, or after backgrounding) shows all
  three rows immediately with no replay.
- Leaving onboarding entirely and starting again creates a new ViewModel, so the animation
  replays. Process death also loses the flag and replays. Both are acceptable.
- If the user leaves mid-sequence, the flag is not set (it is set only on completion), so
  the next fresh visit replays the full sequence.
- Loading and Error states are untouched; the sequence starts when `Content` first
  composes.

## Implementation shape

All changes are in `OnboardingInviteDestination.kt` in `:feature-onboarding`; no
presenter/UiState changes.

- `OnboardingInviteViewModel` gains a plain `var inviteCardAnimationPlayed = false`.
- `ExampleReferralsCard` drives a `visibleRows` state (1..3) from a `LaunchedEffect`: if
  the flag is already set, start at 3; otherwise start at 1, delay, reveal row 2, delay,
  reveal row 3, then set the flag.
- Rows 2 and 3, each together with its preceding `HorizontalDivider`, are wrapped in
  `AnimatedVisibility(enter = expandVertically(...) + fadeIn(...))`, so the card grows
  vertically as each row appears. The surrounding `weight(1f)` spacers keep the card
  centered while it grows.
- Row 1 is always visible.

## Timing defaults (designer-tweakable, named constants grouped in one place)

- 450 ms pause after the card first appears before row 2 starts.
- 600 ms between row 2 and row 3.
- Each reveal animates over 400 ms (`MotionTokens.DurationMedium4`) using
  `MotionTokens.EasingEmphasizedDecelerateCubicBezier` for the expand, combined with a
  fade-in.

## Testing

No unit-testable logic is added; timing lives in the composable. Verification is visual,
matching the plan to iterate on the values with the designer.
