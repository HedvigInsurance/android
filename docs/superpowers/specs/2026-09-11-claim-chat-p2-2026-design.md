# Claim chat P2-2026 design changes

Design source: [App P2-2026, "Updated Claims Submission Critique"](https://www.figma.com/design/SogcacjzOxkCC46XcZP8lQ/App-P2-2026?node-id=2274-11650&p=f&m=dev)

All work lands in `app/feature/feature-claim-chat`, with one change in
`app/design-system/design-system-hedvig`. The module is KMP (`commonMain`, with
`androidMain`/`nativeMain`/`jvmMain` actuals) but Android is its only consumer today. That
structure is preserved: new code goes in `commonMain` and stays platform-agnostic, using
expect/actual only where a platform API is genuinely needed, exactly as the module already does.
No iOS coordination is required.

## Status

Built and verified by running the flow on a device. This document has been updated to describe what was
actually implemented; where the original design differed, the divergence and its cause are recorded inline so
the mistakes are not repeated.

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

As built, in `TaskDescriptionQueue.kt`:

1. **Pacing.** `pacedDescriptions(descriptions: Flow<List<String>>, minimumDwell)` walks the backlog one at a
   time, holding each for a minimum dwell. `rememberPacedDescription` is a thin Compose wrapper over it, so
   there is one implementation and the tests cover the one that ships. This is the substantive part: the UI
   rendered `descriptions.lastOrNull()`, so when a single poll carried several new descriptions the
   intermediate ones were never shown at all.
2. **The transition.** The `AnimatedContent` had no `transitionSpec` and inherited Compose's default of fade
   plus `scaleIn` plus an animated `SizeTransform`. It now fades and slides vertically.
3. **Layout stability.** The strings vary a lot in length, so the animated size transform shifted the
   neighbouring Rive indicator on every swap. The size is snapped instead.

Verified on a device: consecutive frames show "Going through the details..." and then "Working out the next
step...", the two descriptions the demo script delivers in a single emission.

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

As built, two of the three expected deltas turned out to be unnecessary:

1. The picker is a `HedvigBottomSheet` with Continue and Cancel, replacing `SingleSelectDialog`. Beyond
   matching the design this fixes a real gap: the dialog committed the answer the instant an option was
   tapped, so a mis-tap could not be backed out of. The sheet holds the selection locally until Continue.
2. Option rows already rendered their subtitle. `RadioGroup` draws `option.label` and `FormStep` already
   populated it from `option.subtitle`. No work was needed.
3. The collapsed card gained the subtitle through a new optional `subtitleText` on the shared
   `HedvigBigCard`. The content-slot overload was the alternative, but `BigCardDefaults`, which holds the
   card's padding and text styles, is private, so that route meant copying design-system internals into a
   feature module. No existing caller changes.

No new Lokalise key was needed: the sheet title is the backend-supplied `field.title` the dialog already used.

**A sheet's payload must be non-null.** `HedvigBottomSheet` renders its content only while `state.data != null`,
so a payload typed to the selected id meant the picker never opened until something was already selected,
which could never happen. Sheets that are simply open or closed use `HedvigBottomSheetState<Unit>`, which is
the convention across the app; anything carrying a payload must ensure it is non-null when shown.

**Not done:** the design shows a chevron on the card. `HedvigBigCard` draws none today and adding one would
change its appearance for every caller, so that needs a design decision rather than a unilateral change.

Prefill is entirely backend-driven through `selectedOptions`. Nothing to build: if the backend
sends a best guess the card shows it, if it sends none the card shows the placeholder.

## 6a. The answer input

Branch: `feat/claim-chat-input-mode-row`

**Corrected during implementation.** This section originally described a translucent "glass overlay" hosting
both inputs, which came from paraphrasing the Figma annotation rather than reading the frames. The frames
(`S2 After`, `S3 Text — overlay above the keyboard`, `S4 Voice — recording inline`) show no sheet and no scrim
anywhere. The bottom of the screen swaps between three inline states and the question stays fully readable in
all of them.

### Resting

Two buttons side by side, each with its icon: `PenEdit` for the text option, `Mic` for the voice one. The
Figma measures them at 167.5 x 56 each, inside 343 of content width with an 8px gap, which is equal width at
`ButtonSize.Large` (whose 15 + 24 + 17 metrics give exactly 56dp) with `Modifier.weight(1f)`, an 8dp gap and
16dp insets. A ghost skip sits below them.

The labels are short: "Skriv" and "Spela in" in the design. No matching Lokalise key exists, and `strings.xml`
is generated, so they are hardcoded English with `// TODO: Add … to Lokalise` comments. **This blocks shipping
until real keys exist.**

### Text

A card whose text field *is* the input. Focusing it raises the keyboard and the card rides above it, so there
is no separate editor to open. The card carries the field label, the field, and Avbryt plus Spara aligned to
its trailing edge. Spara submits, which is what puts the answer into the transcript in frame `S6`.

The shared `FreeTextOverlay` is no longer used by this flow and its host is removed. The component itself is
untouched because `feature-terminate-insurance` still uses it.

### Voice

The recorder as an inline card with its own close, not a bottom sheet. Same content as before (title, timer,
waveform, and the restart / stop / send row); only the container changed.

### Draft preservation, deferred

The meeting assumed drafts already survive a mode switch. They do not. `SwitchToFreeText` explicitly
constructs `FreeTextDescription(freeText = null)` and `SwitchToAudioRecording` resets to `NotRecording`, so
each switch destroys the other mode's draft.

`AudioRecordingStepState` models the two modes as alternative branches of one sealed interface, so it
structurally cannot hold both drafts. Supporting it means widening that state and reworking every consumer
that pattern-matches on it. Deferred: cancelling still discards, exactly as before, so nothing regresses.

## 6b. Bottom attaching the answer input

Branch: `feat/claim-chat-sticky-input`

**Corrected during implementation.** This section originally said "the current step's bottom content", which
led to every step's actions being pinned. The requirement names the text input and the audio recording
specifically. Continue, Skip, the form fields and the summary's Submit all stay inline exactly as before.

### What the input looked like before

Not bottom-anchored at all. It was the last item of the transcript `LazyColumn`, stretched to at least
viewport height by `LastItemHeightAdjustingState` via `requiredHeightIn`, which only looked pinned while the
list sat at its end. Scrolling up to reread earlier answers carried it away.

### What changed

Only the step whose content is `StepContent.AudioRecording` has its answer input lifted out of its list item
into a container aligned to the bottom of the screen. That container carries the keyboard inset so it rides
up with the keyboard and settles back on dismissal, and the list takes bottom content padding equal to the
measured container height so the transcript can still scroll clear of it.

Everything else is untouched: `LastItemHeightAdjustingState`, the `requiredHeightIn` on the last item and the
autoscroll all remain, because every other step still positions its actions through them.

### Keyboard insets, once

`WindowInsets.safeDrawing` already includes the IME. Applying `Modifier.imePadding()` *and* padding derived
from `safeDrawing` to the same container applies the keyboard height twice, which shows up as a large gap
below the card and a text field squeezed into what is left. The bottom attached container takes its bottom
inset from one source only.

### Scroll to bottom arrow

The arrow occupies the same place as the bottom attached input, so it stands down for that one step. On every
other step its behaviour is unchanged.

### Revertability

The item stays on its own branch so it can be reverted without taking the other work with it. Nothing from
items 1, 2, 5 or 6a is folded into it.

## 7. Skip buttons to Ghost

Branch: `feat/claim-chat-ghost-skip-buttons`

Wave 3, after everything else has landed.

Every Skip button in the flow takes `ButtonDefaults.ButtonStyle.Ghost`. `claims_skip_button` is
referenced from `ContentSelectStep.kt`, `UploadFilesStep.kt`, `FormStep.kt:357`,
`AudioRecordingStepSections.kt:314` and `ClaimChatDestination.kt`. Mechanical, but it is exactly
those five files that items 5, 6a and 6b are rewriting, which is why it waits rather than
running alongside them.

## Testing without the backend

**Built and landed on `feat/claim-chat-demo-flow`.** This section originally assumed the use cases
already had interfaces to substitute behind. They did not: `GetClaimIntentUseCase`,
`StartClaimIntentUseCase` and most of the `Submit*UseCase`s were concrete `internal` classes, so
interfaces had to be extracted first. It also assumed the fake could be debug-only, which is not
possible here: these KMP modules use AGP's `androidLibrary {}` target, which has no build types, so
there is no debug source set to hide it in.

The fake therefore rides on demo mode, the one sanctioned way to have two implementations of a type.
`DemoClaimIntentScript` owns the canned flow; ten `*Demo` use cases delegate to it; ten `Switching*`
classes extending `DemoSwitcher` select between prod and demo and carry the only
`@ContributesBinding` for each type. The switchers live in `androidMain` because `core-demo-mode` is
a JVM library and `commonMain` must stay platform-agnostic.

Consequence worth stating plainly: this ships. Real demo-mode users get the scripted claim flow,
which is a product change rather than pure test scaffolding.

`isDevelopmentFlow` was investigated and does not help; it only forwards a flag to the backend in
`StartClaimIntentUseCase`.

The script drives:

- A task step emitting several descriptions in succession, for item 1.
- A `Form` step with a `SINGLE_SELECT` field over several contracts, with and without a prefill,
  for item 5.
- An audio recording step, for exercising mode switching and both overlays.
- A summary step with text, audio and file answers, for item 2.

Compose previews cover the static states. The scripted fake covers the transitions, which is
where all four risky items actually live.

`DemoClaimIntentScriptTest` covers the script itself: the walk to the outcome, regret not advancing,
progress increasing, the select field's options and lack of prefill, and that the task flow emits
every scheduled description. One test asserts that the schedule still delivers two descriptions in a
single emission, so the burst case cannot be flattened away by accident, which would let a consumer
that renders only the latest description look correct while still dropping messages in production.

## Out of scope

- Items 3 and 4, dropped in the meeting.
- The "Done" terminal beat of item 1.
- Draft preservation across input mode switches, deferred out of item 6a.
- Any change to `FreeTextOverlay`, which keeps serving `feature-terminate-insurance` unchanged.
