package com.hedvig.android.feature.claim.chat.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.fileupload.CommonFile
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Demo-mode implementations of the claim chat use cases. Every one of them is a thin delegate over
 * [DemoClaimIntentScript], which owns the canned flow and the position within it.
 *
 * These are selected by the `Switching*` bindings in the `di` package, so nothing outside demo mode can reach them.
 */

@Inject
internal class StartClaimIntentUseCaseDemo(
  private val script: DemoClaimIntentScript,
) : StartClaimIntentUseCase {
  override suspend fun invoke(developmentFlow: Boolean): Either<ClaimChatErrorMessage, ClaimIntent> =
    script.start().right()
}

@Inject
internal class ResumeClaimUseCaseDemo(
  private val script: DemoClaimIntentScript,
) : ResumeClaimUseCase {
  /**
   * Demo mode keeps no draft across process death, so there is nothing to genuinely resume. Returning a fresh start
   * rather than null keeps the resume entry point usable instead of dropping the user on the failed-to-start screen.
   */
  override suspend fun invoke(): Either<ClaimChatErrorMessage, ClaimIntent?> = script.start().right()
}

@Inject
internal class GetClaimIntentUseCaseDemo(
  private val script: DemoClaimIntentScript,
) : GetClaimIntentUseCase {
  override fun invoke(claimIntentId: ClaimIntentId): Flow<Either<ClaimChatErrorMessage, TaskStepContent>> = flow {
    val step = ClaimIntentStep(
      id = DemoClaimIntentScript.taskStepId,
      text = null,
      hint = null,
      isRegrettable = false,
      stepContent = incompleteTask(emptyList()),
    )
    for ((waitBefore, descriptions) in DemoClaimIntentScript.taskDescriptionSchedule) {
      delay(waitBefore)
      emit(TaskStepContent(step, incompleteTask(descriptions)).right())
    }
    delay(DemoClaimIntentScript.taskCompletionDelay)
    // The caller accumulates descriptions across emissions, so the completing emission need not repeat them.
    emit(
      TaskStepContent(
        step,
        StepContent.Task(descriptions = emptyList(), isCompleted = true, failedToSubmit = false),
      ).right(),
    )
  }

  private fun incompleteTask(descriptions: List<String>) = StepContent.Task(
    descriptions = descriptions,
    isCompleted = false,
    failedToSubmit = false,
  )
}

@Inject
internal class SubmitTaskUseCaseDemo(
  private val script: DemoClaimIntentScript,
) : SubmitTaskUseCase {
  override suspend fun invoke(stepId: String): Either<ClaimChatErrorMessage, ClaimIntent> = script.advance().right()
}

@Inject
internal class SubmitAudioRecordingUseCaseDemo(
  private val script: DemoClaimIntentScript,
) : SubmitAudioRecordingUseCase {
  override suspend fun invoke(stepId: StepId, freeText: String): Either<ClaimChatErrorMessage, ClaimIntent> =
    script.advance().right()

  override suspend fun invoke(
    stepId: StepId,
    commonFile: CommonFile,
    uploadUrl: String,
  ): Either<ClaimChatErrorMessage, ClaimIntent> = script.advance().right()
}

@Inject
internal class SubmitFormUseCaseDemo(
  private val script: DemoClaimIntentScript,
) : SubmitFormUseCase {
  override suspend fun invoke(formData: FormSubmissionData): Either<ClaimChatErrorMessage, ClaimIntent> =
    script.advance().right()
}

@Inject
internal class SubmitSelectUseCaseDemo(
  private val script: DemoClaimIntentScript,
) : SubmitSelectUseCase {
  override suspend fun invoke(id: StepId, selectedId: String): Either<ClaimChatErrorMessage, ClaimIntent> =
    script.advance().right()
}

@Inject
internal class SubmitSummaryUseCaseDemo(
  private val script: DemoClaimIntentScript,
) : SubmitSummaryUseCase {
  override suspend fun invoke(stepId: StepId): Either<ClaimChatErrorMessage, ClaimIntent> = script.advance().right()
}

@Inject
internal class SkipStepUseCaseDemo(
  private val script: DemoClaimIntentScript,
) : SkipStepUseCase {
  override suspend fun invoke(id: StepId): Either<ClaimChatErrorMessage, ClaimIntent> = script.advance().right()
}

@Inject
internal class RegretStepUseCaseDemo(
  private val script: DemoClaimIntentScript,
) : RegretStepUseCase {
  override suspend fun invoke(id: StepId): Either<ClaimChatErrorMessage, ClaimIntent> = script.current().right()
}
