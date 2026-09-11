package com.hedvig.android.feature.claim.chat.data

import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * The canned claim flow used when the app is in demo mode.
 *
 * Holds the position within [steps] and hands out a [ClaimIntent] pointing at whatever comes next, so every demo use
 * case can stay a thin delegate. Answers are not recorded: submitting anything simply advances the script, which is
 * enough to exercise the presentation of each step type.
 *
 * The steps are chosen to cover the cases that are awkward to get a real backend to produce on demand:
 *
 * - An audio recording step, for the two input modes and their overlays.
 * - A task step whose descriptions arrive unevenly, including two at once, see [taskDescriptionSchedule].
 * - A single-select form over several contracts, for the insurance picker.
 * - A summary carrying a text, an audio and a file answer, for the answer hierarchy.
 */
@SingleIn(AppScope::class)
@Inject
internal class DemoClaimIntentScript {
  private var index = 0

  fun start(): ClaimIntent {
    index = 0
    return intentAt(0)
  }

  fun advance(): ClaimIntent {
    index = (index + 1).coerceAtMost(steps.size)
    return intentAt(index)
  }

  /** Re-presents the step the script is already on, for regretting an answer. */
  fun current(): ClaimIntent = intentAt(index)

  fun currentStep(): ClaimIntentStep? = steps.getOrNull(index)

  private fun intentAt(position: Int): ClaimIntent {
    val next = steps.getOrNull(position)
      ?.let { ClaimIntent.Next.Step(it) }
      ?: ClaimIntent.Next.Outcome(
        ClaimIntentOutcome.Claim(
          claimId = "demo-claim",
          claimSubmissionDate = Clock.System.now(),
        ),
      )
    return ClaimIntent(
      id = intentId,
      next = next,
      progress = position.toFloat() / steps.size,
      displayName = "Claim",
      resumable = false,
      previousSteps = steps.take(position),
    )
  }

  companion object {
    val intentId = ClaimIntentId("demo-claim-intent")

    private val describeStepId = StepId("demo-describe")
    val taskStepId = StepId("demo-task")
    private val selectInsuranceStepId = StepId("demo-select-insurance")
    private val travellingStepId = StepId("demo-travelling")
    private val summaryStepId = StepId("demo-summary")

    /**
     * What the task step emits, and how long to wait before each emission. The gaps are deliberately uneven and one
     * entry carries two descriptions at once, because a burst is exactly the case where showing only the most recent
     * description drops the ones in between.
     */
    val taskDescriptionSchedule: List<Pair<Duration, List<String>>> = listOf(
      0.milliseconds to listOf("Analyzing..."),
      1200.milliseconds to listOf("Reading your answer..."),
      200.milliseconds to listOf("Going through the details...", "Working out the next step..."),
      1500.milliseconds to listOf("One moment..."),
    )

    /** How long the task sits complete before the flow moves on. */
    val taskCompletionDelay: Duration = 1000.milliseconds

    private val steps: List<ClaimIntentStep> = listOf(
      ClaimIntentStep(
        id = describeStepId,
        text = "In order to help you faster we would like you to describe the situation.",
        hint = "Please answer the following questions:\n- What happened?\n- When did it happen?\n" +
          "- Where did it happen?",
        isRegrettable = true,
        stepContent = StepContent.AudioRecording(
          uploadUri = "https://example.invalid/demo-upload",
          isSkippable = true,
          recordingState = AudioRecordingStepState.AudioRecording.NotRecording,
          freeTextMinLength = 10,
          freeTextMaxLength = 2000,
        ),
      ),
      ClaimIntentStep(
        id = taskStepId,
        text = null,
        hint = null,
        isRegrettable = false,
        stepContent = StepContent.Task(
          descriptions = emptyList(),
          isCompleted = false,
          failedToSubmit = false,
        ),
      ),
      ClaimIntentStep(
        id = selectInsuranceStepId,
        text = "Which insurance is this about?",
        hint = null,
        isRegrettable = true,
        stepContent = StepContent.Form(
          isSkippable = true,
          fields = listOf(
            StepContent.Form.Field(
              id = FieldId("demo-insurance"),
              isRequired = true,
              suffix = null,
              title = "Select insurance...",
              defaultValues = emptyList(),
              maxValue = null,
              minValue = null,
              type = StepContent.Form.FieldType.SINGLE_SELECT,
              options = contractOptions,
              // No prefill. Swap in `listOf(contractOptions.first())` to exercise the best-guess case instead.
              selectedOptions = emptyList(),
              datePickerUiState = null,
              searchData = null,
            ),
          ),
        ),
      ),
      ClaimIntentStep(
        id = travellingStepId,
        text = "Were you traveling at the time of the theft?",
        hint = null,
        isRegrettable = true,
        stepContent = StepContent.ContentSelect(
          options = listOf(
            StepContent.ContentSelect.Option(id = "yes", title = "Yes"),
            StepContent.ContentSelect.Option(id = "no", title = "No"),
          ),
          selectedOptionId = null,
          style = StepContent.ContentSelectStyle.BINARY,
          isSkippable = false,
        ),
      ),
      ClaimIntentStep(
        id = summaryStepId,
        text = "Here is everything we have. Does it look right?",
        hint = null,
        isRegrettable = false,
        stepContent = StepContent.Summary(
          items = emptyList(),
          audioRecordings = emptyList(),
          fileUploads = emptyList(),
          keyDetails = listOf(
            StepContent.Summary.Item(title = "Type", value = "Theft"),
            StepContent.Summary.Item(title = "Insurance", value = "Homeowners Insurance Max"),
          ),
          answers = listOf(
            StepContent.Summary.Answer(
              title = "Were you traveling at the time of the theft?",
              value = StepContent.Summary.Answer.Value.Text("No"),
            ),
            StepContent.Summary.Answer(
              title = "Have you reported the theft to the police?",
              value = StepContent.Summary.Answer.Value.Text("No"),
            ),
            StepContent.Summary.Answer(
              title = "Tell us how exactly where and when you left your phone when it got stolen?",
              value = StepContent.Summary.Answer.Value.Audio(
                url = "https://example.invalid/demo-recording.m4a",
                transcript = "I left it on a bench in the park and when I came back it was gone.",
              ),
            ),
            StepContent.Summary.Answer(
              title = "Did you ever leave your phone unsupervised?",
              value = StepContent.Summary.Answer.Value.Text(
                "Yes I did, i left it on a bench in the park.",
              ),
            ),
            StepContent.Summary.Answer(
              title = "Receipts",
              value = StepContent.Summary.Answer.Value.Files(
                files = listOf(
                  StepContent.Summary.FileUpload(
                    url = "https://example.invalid/demo-receipt.pdf",
                    contentType = "application/pdf",
                    fileName = "receipt.pdf",
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    )

    private val contractOptions: List<StepContent.Form.FieldOption>
      get() = listOf(
        StepContent.Form.FieldOption(
          value = "homeowners-max",
          text = "Homeowners Insurance Max",
          subtitle = "Birger Jarlsgatan 57 • Only you",
        ),
        StepContent.Form.FieldOption(
          value = "rent-standard",
          text = "Home Insurance Rent Standard",
          subtitle = "Hyrgatan 15 • Only you",
        ),
        StepContent.Form.FieldOption(
          value = "villa-standard",
          text = "Villa Insurance Standard",
          subtitle = "Villagatan 22 • Only you",
        ),
        StepContent.Form.FieldOption(
          value = "accident",
          text = "Accident Insurance",
          subtitle = "Only you",
        ),
      )
  }
}
