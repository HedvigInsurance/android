package com.hedvig.android.feature.claim.chat.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.feature.claim.chat.data.GetClaimIntentUseCase
import com.hedvig.android.feature.claim.chat.data.GetClaimIntentUseCaseImpl
import com.hedvig.android.feature.claim.chat.data.RegretStepUseCase
import com.hedvig.android.feature.claim.chat.data.RegretStepUseCaseImpl
import com.hedvig.android.feature.claim.chat.data.ResumeClaimUseCase
import com.hedvig.android.feature.claim.chat.data.ResumeClaimUseCaseImpl
import com.hedvig.android.feature.claim.chat.data.SkipStepUseCase
import com.hedvig.android.feature.claim.chat.data.SkipStepUseCaseImpl
import com.hedvig.android.feature.claim.chat.data.StartClaimIntentUseCase
import com.hedvig.android.feature.claim.chat.data.StartClaimIntentUseCaseImpl
import com.hedvig.android.feature.claim.chat.data.SubmitAudioRecordingUseCase
import com.hedvig.android.feature.claim.chat.data.SubmitAudioRecordingUseCaseImpl
import com.hedvig.android.feature.claim.chat.data.SubmitFormUseCase
import com.hedvig.android.feature.claim.chat.data.SubmitFormUseCaseImpl
import com.hedvig.android.feature.claim.chat.data.SubmitInformationUseCase
import com.hedvig.android.feature.claim.chat.data.SubmitInformationUseCaseImpl
import com.hedvig.android.feature.claim.chat.data.SubmitSelectUseCase
import com.hedvig.android.feature.claim.chat.data.SubmitSelectUseCaseImpl
import com.hedvig.android.feature.claim.chat.data.SubmitSummaryUseCase
import com.hedvig.android.feature.claim.chat.data.SubmitSummaryUseCaseImpl
import com.hedvig.android.feature.claim.chat.data.SubmitTaskUseCase
import com.hedvig.android.feature.claim.chat.data.SubmitTaskUseCaseImpl
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

/**
 * iOS has no demo mode for the claim chat, so each use case binds straight to its prod implementation. These carry
 * the iOS binding for their type, the way the switchers in androidMain carry the Android one.
 */

@Inject
@ContributesBinding(AppScope::class, binding = binding<StartClaimIntentUseCase>())
internal class NativeStartClaimIntentUseCase(impl: StartClaimIntentUseCaseImpl) : StartClaimIntentUseCase by impl

@Inject
@ContributesBinding(AppScope::class, binding = binding<ResumeClaimUseCase>())
internal class NativeResumeClaimUseCase(impl: ResumeClaimUseCaseImpl) : ResumeClaimUseCase by impl

@Inject
@ContributesBinding(AppScope::class, binding = binding<GetClaimIntentUseCase>())
internal class NativeGetClaimIntentUseCase(impl: GetClaimIntentUseCaseImpl) : GetClaimIntentUseCase by impl

@Inject
@ContributesBinding(AppScope::class, binding = binding<SubmitTaskUseCase>())
internal class NativeSubmitTaskUseCase(impl: SubmitTaskUseCaseImpl) : SubmitTaskUseCase by impl

@Inject
@ContributesBinding(AppScope::class, binding = binding<SubmitAudioRecordingUseCase>())
internal class NativeSubmitAudioRecordingUseCase(
  impl: SubmitAudioRecordingUseCaseImpl,
) : SubmitAudioRecordingUseCase by impl

@Inject
@ContributesBinding(AppScope::class, binding = binding<SubmitFormUseCase>())
internal class NativeSubmitFormUseCase(impl: SubmitFormUseCaseImpl) : SubmitFormUseCase by impl

@Inject
@ContributesBinding(AppScope::class, binding = binding<SubmitSelectUseCase>())
internal class NativeSubmitSelectUseCase(impl: SubmitSelectUseCaseImpl) : SubmitSelectUseCase by impl

@Inject
@ContributesBinding(AppScope::class, binding = binding<SubmitSummaryUseCase>())
internal class NativeSubmitSummaryUseCase(impl: SubmitSummaryUseCaseImpl) : SubmitSummaryUseCase by impl

@Inject
@ContributesBinding(AppScope::class, binding = binding<SubmitInformationUseCase>())
internal class NativeSubmitInformationUseCase(impl: SubmitInformationUseCaseImpl) : SubmitInformationUseCase by impl

@Inject
@ContributesBinding(AppScope::class, binding = binding<SkipStepUseCase>())
internal class NativeSkipStepUseCase(impl: SkipStepUseCaseImpl) : SkipStepUseCase by impl

@Inject
@ContributesBinding(AppScope::class, binding = binding<RegretStepUseCase>())
internal class NativeRegretStepUseCase(impl: RegretStepUseCaseImpl) : RegretStepUseCase by impl
