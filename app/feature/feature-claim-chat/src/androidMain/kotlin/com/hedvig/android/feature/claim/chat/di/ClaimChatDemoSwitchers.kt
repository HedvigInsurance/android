package com.hedvig.android.feature.claim.chat.di

import arrow.core.Either
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.DemoSwitcher
import com.hedvig.android.core.fileupload.CommonFile
import com.hedvig.android.feature.claim.chat.data.ClaimChatErrorMessage
import com.hedvig.android.feature.claim.chat.data.ClaimIntent
import com.hedvig.android.feature.claim.chat.data.ClaimIntentId
import com.hedvig.android.feature.claim.chat.data.FormSubmissionData
import com.hedvig.android.feature.claim.chat.data.GetClaimIntentUseCase
import com.hedvig.android.feature.claim.chat.data.GetClaimIntentUseCaseDemo
import com.hedvig.android.feature.claim.chat.data.GetClaimIntentUseCaseImpl
import com.hedvig.android.feature.claim.chat.data.RegretStepUseCase
import com.hedvig.android.feature.claim.chat.data.RegretStepUseCaseDemo
import com.hedvig.android.feature.claim.chat.data.RegretStepUseCaseImpl
import com.hedvig.android.feature.claim.chat.data.ResumeClaimUseCase
import com.hedvig.android.feature.claim.chat.data.ResumeClaimUseCaseDemo
import com.hedvig.android.feature.claim.chat.data.ResumeClaimUseCaseImpl
import com.hedvig.android.feature.claim.chat.data.SkipStepUseCase
import com.hedvig.android.feature.claim.chat.data.SkipStepUseCaseDemo
import com.hedvig.android.feature.claim.chat.data.SkipStepUseCaseImpl
import com.hedvig.android.feature.claim.chat.data.StartClaimIntentUseCase
import com.hedvig.android.feature.claim.chat.data.StartClaimIntentUseCaseDemo
import com.hedvig.android.feature.claim.chat.data.StartClaimIntentUseCaseImpl
import com.hedvig.android.feature.claim.chat.data.StepId
import com.hedvig.android.feature.claim.chat.data.SubmitAudioRecordingUseCase
import com.hedvig.android.feature.claim.chat.data.SubmitAudioRecordingUseCaseDemo
import com.hedvig.android.feature.claim.chat.data.SubmitAudioRecordingUseCaseImpl
import com.hedvig.android.feature.claim.chat.data.SubmitFormUseCase
import com.hedvig.android.feature.claim.chat.data.SubmitFormUseCaseDemo
import com.hedvig.android.feature.claim.chat.data.SubmitFormUseCaseImpl
import com.hedvig.android.feature.claim.chat.data.SubmitSelectUseCase
import com.hedvig.android.feature.claim.chat.data.SubmitSelectUseCaseDemo
import com.hedvig.android.feature.claim.chat.data.SubmitSelectUseCaseImpl
import com.hedvig.android.feature.claim.chat.data.SubmitSummaryUseCase
import com.hedvig.android.feature.claim.chat.data.SubmitSummaryUseCaseDemo
import com.hedvig.android.feature.claim.chat.data.SubmitSummaryUseCaseImpl
import com.hedvig.android.feature.claim.chat.data.SubmitTaskUseCase
import com.hedvig.android.feature.claim.chat.data.SubmitTaskUseCaseDemo
import com.hedvig.android.feature.claim.chat.data.SubmitTaskUseCaseImpl
import com.hedvig.android.feature.claim.chat.data.TaskStepContent
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.flow.Flow

/**
 * Demo-mode selection for the claim chat use cases. Each switcher implements the use case interface and forwards every
 * member through [DemoSwitcher.pick], so the presenter injects the plain interface and never learns demo mode exists.
 *
 * These carry the only `@ContributesBinding` for their type. Neither the prod `Impl` nor the `Demo` implementation is
 * bound directly, which is what keeps the graph unambiguous.
 */

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<StartClaimIntentUseCase>())
internal class SwitchingStartClaimIntentUseCase(
  override val demoManager: DemoManager,
  override val prodImpl: StartClaimIntentUseCaseImpl,
  override val demoImpl: StartClaimIntentUseCaseDemo,
) : StartClaimIntentUseCase, DemoSwitcher<StartClaimIntentUseCase>() {
  override suspend fun invoke(developmentFlow: Boolean): Either<ClaimChatErrorMessage, ClaimIntent> =
    pick().invoke(developmentFlow)
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<ResumeClaimUseCase>())
internal class SwitchingResumeClaimUseCase(
  override val demoManager: DemoManager,
  override val prodImpl: ResumeClaimUseCaseImpl,
  override val demoImpl: ResumeClaimUseCaseDemo,
) : ResumeClaimUseCase, DemoSwitcher<ResumeClaimUseCase>() {
  override suspend fun invoke(): Either<ClaimChatErrorMessage, ClaimIntent?> = pick().invoke()
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<GetClaimIntentUseCase>())
internal class SwitchingGetClaimIntentUseCase(
  override val demoManager: DemoManager,
  override val prodImpl: GetClaimIntentUseCaseImpl,
  override val demoImpl: GetClaimIntentUseCaseDemo,
) : GetClaimIntentUseCase, DemoSwitcher<GetClaimIntentUseCase>() {
  override fun invoke(claimIntentId: ClaimIntentId): Flow<Either<ClaimChatErrorMessage, TaskStepContent>> =
    pickFlow { it.invoke(claimIntentId) }
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<SubmitTaskUseCase>())
internal class SwitchingSubmitTaskUseCase(
  override val demoManager: DemoManager,
  override val prodImpl: SubmitTaskUseCaseImpl,
  override val demoImpl: SubmitTaskUseCaseDemo,
) : SubmitTaskUseCase, DemoSwitcher<SubmitTaskUseCase>() {
  override suspend fun invoke(stepId: String): Either<ClaimChatErrorMessage, ClaimIntent> = pick().invoke(stepId)
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<SubmitAudioRecordingUseCase>())
internal class SwitchingSubmitAudioRecordingUseCase(
  override val demoManager: DemoManager,
  override val prodImpl: SubmitAudioRecordingUseCaseImpl,
  override val demoImpl: SubmitAudioRecordingUseCaseDemo,
) : SubmitAudioRecordingUseCase, DemoSwitcher<SubmitAudioRecordingUseCase>() {
  override suspend fun invoke(stepId: StepId, freeText: String): Either<ClaimChatErrorMessage, ClaimIntent> =
    pick().invoke(stepId, freeText)

  override suspend fun invoke(
    stepId: StepId,
    commonFile: CommonFile,
    uploadUrl: String,
  ): Either<ClaimChatErrorMessage, ClaimIntent> = pick().invoke(stepId, commonFile, uploadUrl)
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<SubmitFormUseCase>())
internal class SwitchingSubmitFormUseCase(
  override val demoManager: DemoManager,
  override val prodImpl: SubmitFormUseCaseImpl,
  override val demoImpl: SubmitFormUseCaseDemo,
) : SubmitFormUseCase, DemoSwitcher<SubmitFormUseCase>() {
  override suspend fun invoke(formData: FormSubmissionData): Either<ClaimChatErrorMessage, ClaimIntent> =
    pick().invoke(formData)
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<SubmitSelectUseCase>())
internal class SwitchingSubmitSelectUseCase(
  override val demoManager: DemoManager,
  override val prodImpl: SubmitSelectUseCaseImpl,
  override val demoImpl: SubmitSelectUseCaseDemo,
) : SubmitSelectUseCase, DemoSwitcher<SubmitSelectUseCase>() {
  override suspend fun invoke(id: StepId, selectedId: String): Either<ClaimChatErrorMessage, ClaimIntent> =
    pick().invoke(id, selectedId)
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<SubmitSummaryUseCase>())
internal class SwitchingSubmitSummaryUseCase(
  override val demoManager: DemoManager,
  override val prodImpl: SubmitSummaryUseCaseImpl,
  override val demoImpl: SubmitSummaryUseCaseDemo,
) : SubmitSummaryUseCase, DemoSwitcher<SubmitSummaryUseCase>() {
  override suspend fun invoke(stepId: StepId): Either<ClaimChatErrorMessage, ClaimIntent> = pick().invoke(stepId)
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<SkipStepUseCase>())
internal class SwitchingSkipStepUseCase(
  override val demoManager: DemoManager,
  override val prodImpl: SkipStepUseCaseImpl,
  override val demoImpl: SkipStepUseCaseDemo,
) : SkipStepUseCase, DemoSwitcher<SkipStepUseCase>() {
  override suspend fun invoke(id: StepId): Either<ClaimChatErrorMessage, ClaimIntent> = pick().invoke(id)
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<RegretStepUseCase>())
internal class SwitchingRegretStepUseCase(
  override val demoManager: DemoManager,
  override val prodImpl: RegretStepUseCaseImpl,
  override val demoImpl: RegretStepUseCaseDemo,
) : RegretStepUseCase, DemoSwitcher<RegretStepUseCase>() {
  override suspend fun invoke(id: StepId): Either<ClaimChatErrorMessage, ClaimIntent> = pick().invoke(id)
}
