# Claim chat P2-2026 design changes

Design source: [App P2-2026, "Updated Claims Submission Critique"](https://www.figma.com/design/SogcacjzOxkCC46XcZP8lQ/App-P2-2026?node-id=2274-11650&p=f&m=dev)

All work lands in `app/feature/feature-claim-chat`, with one change in
`app/design-system/design-system-hedvig`. The module is KMP (`commonMain`, with
`androidMain`/`nativeMain`/`jvmMain` actuals) but Android is its only consumer today. That
structure is preserved: new code goes in `commonMain` and stays platform-agnostic, using
expect/actual only where a platform API is genuinely needed, exactly as the module already does.
No iOS coordination is required.

## Decisions

| # | Item | Decision |
|---|------|----------|
| 1 | Animated loading text | In scope. The trailing "Done" beat is **out** of v1. |
| 2 | Summary sheet hierarchy | In scope. |
| 3 | Remove Confirm + Pill | Dropped in the meeting. |
| 4 | Error escape hatch | Dropped in the meeting. |
| 5 | Select insurance | In scope. Backend confirmed to deliver it as a `Form` step with `SINGLE_SELECT`. |
| 6a | Input mode row and overlays | In scope. Draft preservation is **deferred**, see below. |
| 6b | Sticky input | In scope, by hoisting the input out of the transcript list. |
| 7 | Skip buttons to Ghost | In scope, as its own pass sequenced last. |

Each item gets its own branch off `develop` and its own draft PR. 6b is deliberately isolated
from the other four so it can be reverted without taking them with it, see
[Revertability of 6b](#revertability-of-6b).

### Sequencing

The items are not mutually independent, so they run in waves.

**Wave 1, fully parallel.** Items 1, 2 and 5 touch disjoint files: `TaskStep.kt`,
`ChatClaimSummaryStep.kt`, and `FormStep.kt` plus new components, respectively. They share no
code and can be worked simultaneously on separate branches in separate worktrees.

**Wave 2, sequential.** Item 6a, then 6b. 6a builds the overlay that 6b has to host, and both
edit `StepBottomContent`/`StepContentSection` in `ClaimChatDestination.kt`, the very file whose
restructure is the point of 6b.

**Wave 3.** Item 7. It is a mechanical style swap across five files (`ContentSelectStep.kt`,
`UploadFilesStep.kt`, `FormStep.kt:357`, `AudioRecordingStepSections.kt:314`,
`ClaimChatDestination.kt`), overlapping items 5, 6a and 6b. Sequencing it last means it rebases
onto settled code instead of spraying conflicts across concurrent branches. This is why it is
no longer folded into 6a.

## 1. Animated loading text

Branch: `feat/claim-chat-animated-task-text`

The plumbing already exists. `StepContent.Task` carries `descriptions: List<String>`, and
`ObserveIncompleteTaskEffect` (`ClaimChatViewModel.kt:1089`) collects a polling flow, appending
each newly arrived description to the accumulated list and de-duplicating. `TaskStepTopContent`
(`TaskStep.kt`) renders `descriptions.lastOrNull()` inside an `AnimatedContent`, and the Rive
indicator sits in a sibling slot, so the indicator already survives a text change uninterrupted.

Three things remain:

1. **The transition.** The `AnimatedContent` has no `transitionSpec`, so it inherits Compose's
   default of fade plus `scaleIn(0.92f)` plus an animated `SizeTransform`, which reads as a pop
   and resize. Replace it with the designed motion: the outgoing text fades and slides up and
   out, the incoming one fades and slides in from below.
2. **Layout stability.** The default `SizeTransform` animates the content bounds, and the
   strings vary in length considerably ("Analyzing…" against "Going through the details…"), so
   the sibling Rive indicator is pushed around on every swap. Constrain the text slot so the
   indicator holds still.
3. **Pacing.** This is the only non-cosmetic part. `descriptions` accumulates and the UI renders
   `lastOrNull()`, so when a single poll returns several new descriptions at once, every
   intermediate one is dropped and the user sees only the last. The design is a sequence of
   messages intended to be read. Drive the display from a small queue that holds each
   description for a minimum dwell before advancing, rather than binding directly to the latest
   value.

Explicitly not doing: the "Done" terminal state. Today `isCompleted` flipping causes
`SubmitCompleteTaskEffect` to submit and `replaceTaskWithNextStep` to swap the task out with no
dwell. Adding a held "Done" frame means a client-synthesised string plus a delay inserted into
that effect chain. Deferred to a follow-up.

## 2. Summary sheet hierarchy

Branch: `feat/claim-chat-summary-answer-hierarchy`

`ClaimSummaryAnswersContent` (`ChatClaimSummaryStep.kt:182`) currently renders the question in
the default body style at primary colour and wraps the answer in `textSecondary`. The design
inverts this: question is label size at secondary colour, answer is body size at primary colour.

Apply the same hierarchy to every `AnswerValue` branch so `Text`, `Audio` and `Files` answers
stay consistent. The audio branch already renders a player row, which matches the design.

This is the smallest item and a good first landing.

## 5. Select insurance

Branch: `feat/claim-chat-single-select-sheet`

The capability exists. `FieldType.SINGLE_SELECT` (`FormStep.kt:240`) already renders
`SingleSelectBubbleWithDialog`: a `HedvigBigCard` showing the field label and the chosen value
with a chevron, opening a picker. Backend is confirmed to send the contract list through this
path, so no new `StepContent` type is needed.

Three presentation deltas:

1. The picker becomes a bottom sheet titled "Select insurance" with a primary Continue and a
   secondary Cancel, replacing `SingleSelectDialog`. `HedvigBottomSheet` and
   `rememberHedvigBottomSheetState` are already used elsewhere in `FormStep` for the search
   field, so follow that pattern.
2. Option rows gain a subtitle line ("Birger Jarlsgatan 57 · Only you"). The data path already
   exists: `RadioOption.label` is populated from `field.options[].subtitle`.
3. The collapsed card gains the same subtitle beneath the selected value. `HedvigBigCard`'s
   `inputText` is a single string today, so a subtitle slot does not exist. Default to a
   claim-chat-local card variant rather than widening the shared component, since this is the
   only known caller that needs it. Promote it into the design system later if a second caller
   appears.

Prefill is entirely backend-driven through `selectedOptions`. Nothing to build: if the backend
sends a best guess the card shows it, if it sends none the card shows the placeholder.

## 6a. Input mode row and overlays

Branch: `feat/claim-chat-input-mode-row`

### Button layout

Today `AudioRecorderBubble` (`AudioRecordingStepSections.kt:202`) renders three stacked
full-width buttons: "Record with voice" (primary, opens a `HedvigBottomSheet`), "Describe using
text" (secondary, flips state to an inline text section) and "Skip" (secondary).

The design replaces this with two equal-width Secondary Large buttons on one row (Skriv, Spela
in) at 16px insets with an 8px gap, and "Hoppa över" as a Ghost Large button beneath. Both
primary buttons open the same overlay.

The "Hoppa över" button in this composable takes the Ghost style.
`ButtonDefaults.ButtonStyle.Ghost` already exists. The rest of the flow's Skip buttons are item
7, handled separately in wave 3.

### Glass overlay

The design shows a translucent overlay with the question still legible above a card, for both
text and voice. The existing `FreeTextOverlay` is a full-screen opaque `Surface` with
`safeDrawingPadding`, and it has a second consumer in `feature-terminate-insurance`, so it is
**not** modified. Instead, add a claim-chat-local overlay that takes arbitrary card content: a
translucent scrim over the still-composed transcript, and a card pinned to the bottom with
`imePadding()`.

Both modes use it:

- Text: title, the text field, Avbryt (ghost) and Spara (primary) in the card footer.
- Voice: title, elapsed timer, waveform, and the Börja om / record-stop / Skicka control row,
  with an X to dismiss. This replaces the current `HedvigBottomSheet` presentation.

### Draft preservation, deferred

The meeting assumed drafts already survive a mode switch and that we only had to avoid
regressing it. They do not. `SwitchToFreeText` (`ClaimChatViewModel.kt:562`) explicitly
constructs `FreeTextDescription(freeText = null)` and `SwitchToAudioRecording` resets to
`NotRecording`, so today each switch destroys the other mode's draft, and leaving text for voice
and coming back loses what was typed.

Building it is not a UI tweak. `AudioRecordingStepState` models the two modes as alternative
branches of one sealed interface, so it structurally cannot hold both drafts at once. Supporting
it means widening that state to carry a text draft and a recording draft side by side, with a
separate notion of which mode is currently presented, and reworking the two switch handlers and
every consumer that pattern-matches on the sealed type.

Deferred to a follow-up. 6a ships the button row and the overlays only, and cancelling continues
to discard the draft exactly as it does today. This is a conscious hold rather than an
oversight: no behaviour regresses, the design intent is simply not yet met.

When it is picked up, the target behaviour is:

- Dismissing the text overlay via Avbryt keeps the typed text and returns to the two-button row.
- Dismissing the voice overlay via X keeps the recording and returns to the two-button row.
- Re-entering either mode restores that mode's draft.
- Submitting clears both.

## 6b. Sticky input

Branch: `feat/claim-chat-sticky-input`

### Current state

The input is not bottom-anchored. It is the last item of the transcript `LazyColumn`
(`ClaimChatDestination.kt:501`), stretched to at least viewport height by
`LastItemHeightAdjustingState` via `requiredHeightIn`, which makes it *appear* pinned while the
list sits at the end. Scrolling up carries it away. Three mechanisms exist to prop up that
illusion: the height-adjusting state itself, a `LaunchedEffect` firing
`animateScrollBy(3000f)` whenever the last item resizes, and the scroll-to-bottom arrow.

### Target

The screen becomes a column:

- A transcript `LazyColumn` holding each step's top content, and the bottom content of *past*
  steps only (an answered audio bubble with its "Ändra" chip, a skipped label, and so on). The
  `isCurrentStep` flag already threaded through `StepContentSection` and `StepBottomContent`
  marks this split.
- A pinned container below it holding the current step's bottom content, carrying
  `imePadding()` so it follows the keyboard up and settles back to the bottom edge on dismissal.

The list takes bottom content-padding equal to the measured height of the pinned container so
transcript content can scroll clear of it. `LastItemHeightAdjustingState` and the 3000f nudge
retire, replaced by a plain scroll-to-last-item when a new step arrives. The scroll arrow is
re-evaluated once the list no longer contains a viewport-height final item, and is likely
removed.

The voice overlay gets the same pinned treatment without requesting focus, so it never raises
the keyboard.

### Known risk

The handoff. When a step is answered, its input has to leave the pinned container and reappear
as a historical entry in the transcript. Expect to iterate on that transition. This is the part
of the whole effort most likely to need rework.

### Revertability of 6b

Explicitly required. 6b ships on its own branch touching only `ClaimChatDestination.kt` and
`LastItemHeightAdjustingState.kt`, with no changes shared with items 1, 2, 5 or 6a. If the
hoisted layout proves worse in practice, reverting this branch restores the current scroll model
without disturbing the other four. Do not fold any 6a work into this branch to keep that
property, even where it would be convenient.

The fallback if it is reverted: solve only the overlays, leaving the resting button row in the
list. That delivers 6a's keyboard-sticky text input but not the always-visible requirement.

## 7. Skip buttons to Ghost

Branch: `feat/claim-chat-ghost-skip-buttons`

Wave 3, after everything else has landed.

Every Skip button in the flow takes `ButtonDefaults.ButtonStyle.Ghost`. `claims_skip_button` is
referenced from `ContentSelectStep.kt`, `UploadFilesStep.kt`, `FormStep.kt:357`,
`AudioRecordingStepSections.kt:314` and `ClaimChatDestination.kt`. Mechanical, but it is exactly
those five files that items 5, 6a and 6b are rewriting, which is why it waits rather than
running alongside them.

## Testing without the backend

No fake data layer exists today. `isDevelopmentFlow` only forwards a flag to the backend in
`StartClaimIntentUseCase`, so it does not help. The module has 13 Compose previews.

Add a debug-only scripted fake behind the existing use-case interfaces (`GetClaimIntentUseCase`,
`StartClaimIntentUseCase` and the various `Submit*UseCase`s) that replays a hardcoded sequence
of steps on a timer. It must be able to drive:

- A task step emitting several descriptions in succession, for item 1.
- A `Form` step with a `SINGLE_SELECT` field over several contracts, with and without a prefill,
  for item 5.
- An audio recording step, for exercising mode switching and both overlays.
- A summary step with text, audio and file answers, for item 2.

Compose previews cover the static states. The scripted fake covers the transitions, which is
where all four risky items actually live.

## Out of scope

- Items 3 and 4, dropped in the meeting.
- The "Done" terminal beat of item 1.
- Draft preservation across input mode switches, deferred out of item 6a.
- Any change to `FreeTextOverlay`, which keeps serving `feature-terminate-insurance` unchanged.
