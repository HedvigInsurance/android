package com.hedvig.android.data.claimflow

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.retrofit.adapter.either.networkhandling.CallError
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.appreview.SelfServiceCompletedEventManager
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.retrofit.toErrorMessage
import com.hedvig.android.data.claimflow.model.AudioUrl
import com.hedvig.android.data.claimflow.model.FlowId
import com.hedvig.android.data.claimtriaging.EntryPointId
import com.hedvig.android.data.claimtriaging.EntryPointOptionId
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import java.io.File
import kotlinx.datetime.LocalDate
import octopus.FlowClaimAudioRecordingNextMutation
import octopus.FlowClaimConfirmEmergencyMutation
import octopus.FlowClaimContractNextMutation
import octopus.FlowClaimDateOfOccurrenceNextMutation
import octopus.FlowClaimDateOfOccurrencePlusLocationNextMutation
import octopus.FlowClaimFileUploadNextMutation
import octopus.FlowClaimLocationNextMutation
import octopus.FlowClaimPhoneNumberNextMutation
import octopus.FlowClaimSingleItemCheckoutNextMutation
import octopus.FlowClaimSingleItemNextMutation
import octopus.FlowClaimStartMutation
import octopus.FlowClaimSummaryNextMutation
import octopus.fragment.ClaimFlowStepFragment
import octopus.type.FlowClaimFileUploadInput
import octopus.type.FlowClaimItemBrandInput
import octopus.type.FlowClaimItemModelInput
import octopus.type.FlowClaimSingleItemInput
import octopus.type.FlowClaimSummaryInput
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

interface ClaimFlowRepository {
  suspend fun startClaimFlow(
    entryPointId: EntryPointId?,
    entryPointOptionId: EntryPointOptionId?,
  ): Either<ErrorMessage, ClaimFlowStep>

  suspend fun submitAudioRecording(flowId: FlowId, audioFile: File): Either<ErrorMessage, ClaimFlowStep>

  /**
   * Used when we already got an audio Url ready, and we haven't made a new audio recording
   */
  suspend fun submitAudioUrl(flowId: FlowId, audioUrl: AudioUrl): Either<ErrorMessage, ClaimFlowStep>

  suspend fun submitDateOfOccurrence(dateOfOccurrence: LocalDate?): Either<ErrorMessage, ClaimFlowStep>

  suspend fun submitLocation(location: String?): Either<ErrorMessage, ClaimFlowStep>

  suspend fun submitContract(contract: String?): Either<ErrorMessage, ClaimFlowStep>

  suspend fun submitDateOfOccurrenceAndLocation(
    dateOfOccurrence: LocalDate?,
    location: String?,
  ): Either<ErrorMessage, ClaimFlowStep>

  suspend fun submitPhoneNumber(phoneNumber: String): Either<ErrorMessage, ClaimFlowStep>

  suspend fun submitSingleItem(
    itemBrandInput: FlowClaimItemBrandInput?,
    itemModelInput: FlowClaimItemModelInput?,
    itemProblemIds: List<String>?,
    purchaseDate: LocalDate?,
    purchasePrice: Double?,
  ): Either<ErrorMessage, ClaimFlowStep>

  suspend fun submitSingleItemCheckout(amount: Double): Either<ErrorMessage, Unit>

  suspend fun submitSummary(
    dateOfOccurrence: LocalDate?,
    itemBrandInput: FlowClaimItemBrandInput?,
    itemModelInput: FlowClaimItemModelInput?,
    itemProblemIds: List<String>?,
    location: String?,
    purchaseDate: LocalDate?,
    purchasePrice: Double?,
  ): Either<ErrorMessage, ClaimFlowStep>

  suspend fun submitUrgentEmergency(isUrgentEmergency: Boolean): Either<ErrorMessage, ClaimFlowStep>

  suspend fun submitFiles(fileIds: List<String>): Either<ErrorMessage, ClaimFlowStep>
}

internal class ClaimFlowRepositoryImpl(
  private val apolloClient: ApolloClient,
  private val odysseyService: OdysseyService,
  private val claimFlowContextStorage: ClaimFlowContextStorage,
  private val networkCacheManager: NetworkCacheManager,
  private val selfServiceCompletedEventManager: SelfServiceCompletedEventManager,
) : ClaimFlowRepository {
  override suspend fun startClaimFlow(
    entryPointId: EntryPointId?,
    entryPointOptionId: EntryPointOptionId?,
  ): Either<ErrorMessage, ClaimFlowStep> {
    return either {
      val result = apolloClient
        .mutation(FlowClaimStartMutation(entryPointId?.id, entryPointOptionId?.id))
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowClaimStart
      claimFlowContextStorage.saveContext(result.context)
      result.currentStep.toClaimFlowStep(FlowId(result.id), selfServiceCompletedEventManager)
    }
  }

  override suspend fun submitAudioRecording(flowId: FlowId, audioFile: File): Either<ErrorMessage, ClaimFlowStep> {
    return either {
      logcat { "Uploading file with flowId:$flowId and audio file name:${audioFile.name}" }
      val audioUrl: AudioUrl = uploadAudioFile(flowId.value, audioFile)
      logcat { "Uploaded audio file, resulting url:$audioUrl" }
      nextClaimFlowStepWithAudioUrl(audioUrl)
    }
  }

  override suspend fun submitAudioUrl(flowId: FlowId, audioUrl: AudioUrl): Either<ErrorMessage, ClaimFlowStep> {
    return either {
      nextClaimFlowStepWithAudioUrl(audioUrl)
    }
  }

  override suspend fun submitDateOfOccurrence(dateOfOccurrence: LocalDate?): Either<ErrorMessage, ClaimFlowStep> {
    return either {
      val result = apolloClient
        .mutation(FlowClaimDateOfOccurrenceNextMutation(dateOfOccurrence, claimFlowContextStorage.getContext()))
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowClaimDateOfOccurrenceNext
      claimFlowContextStorage.saveContext(result.context)
      result.currentStep.toClaimFlowStep(FlowId(result.id), selfServiceCompletedEventManager)
    }
  }

  override suspend fun submitLocation(location: String?): Either<ErrorMessage, ClaimFlowStep> {
    return either {
      val result = apolloClient
        .mutation(FlowClaimLocationNextMutation(location, claimFlowContextStorage.getContext()))
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowClaimLocationNext
      claimFlowContextStorage.saveContext(result.context)
      result.currentStep.toClaimFlowStep(FlowId(result.id), selfServiceCompletedEventManager)
    }
  }

  override suspend fun submitContract(contract: String?): Either<ErrorMessage, ClaimFlowStep> {
    return either {
      val result = apolloClient
        .mutation(FlowClaimContractNextMutation(contract, claimFlowContextStorage.getContext()))
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowClaimContractSelectNext
      claimFlowContextStorage.saveContext(result.context)
      result.currentStep.toClaimFlowStep(FlowId(result.id), selfServiceCompletedEventManager)
    }
  }

  override suspend fun submitDateOfOccurrenceAndLocation(
    dateOfOccurrence: LocalDate?,
    location: String?,
  ): Either<ErrorMessage, ClaimFlowStep> {
    return either {
      val result = apolloClient
        .mutation(
          FlowClaimDateOfOccurrencePlusLocationNextMutation(
            dateOfOccurrence,
            location,
            claimFlowContextStorage.getContext(),
          ),
        )
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowClaimDateOfOccurrencePlusLocationNext
      claimFlowContextStorage.saveContext(result.context)
      result.currentStep.toClaimFlowStep(FlowId(result.id), selfServiceCompletedEventManager)
    }
  }

  override suspend fun submitPhoneNumber(phoneNumber: String): Either<ErrorMessage, ClaimFlowStep> {
    return either {
      val result = apolloClient
        .mutation(FlowClaimPhoneNumberNextMutation(phoneNumber, claimFlowContextStorage.getContext()))
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowClaimPhoneNumberNext
      claimFlowContextStorage.saveContext(result.context)
      result.currentStep.toClaimFlowStep(FlowId(result.id), selfServiceCompletedEventManager)
    }
  }

  override suspend fun submitSingleItem(
    itemBrandInput: FlowClaimItemBrandInput?,
    itemModelInput: FlowClaimItemModelInput?,
    itemProblemIds: List<String>?,
    purchaseDate: LocalDate?,
    purchasePrice: Double?,
  ): Either<ErrorMessage, ClaimFlowStep> {
    return either {
      val result = apolloClient
        .mutation(
          FlowClaimSingleItemNextMutation(
            FlowClaimSingleItemInput(
              // Will be used when entering a free form text is supported
              customName = Optional.absent(),
              itemBrandInput = Optional.presentIfNotNull(itemBrandInput),
              itemModelInput = Optional.presentIfNotNull(itemModelInput),
              itemProblemIds = Optional.presentIfNotNull(itemProblemIds),
              purchaseDate = Optional.presentIfNotNull(purchaseDate),
              purchasePrice = Optional.presentIfNotNull(purchasePrice.takeIf { it: Double? -> it != 0.0 }),
            ),
            claimFlowContextStorage.getContext(),
          ),
        )
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowClaimSingleItemNext
      claimFlowContextStorage.saveContext(result.context)
      result.currentStep.toClaimFlowStep(FlowId(result.id), selfServiceCompletedEventManager)
    }
  }

  override suspend fun submitSingleItemCheckout(amount: Double): Either<ErrorMessage, Unit> {
    return either {
      val result = apolloClient
        .mutation(
          FlowClaimSingleItemCheckoutNextMutation(
            amount,
            claimFlowContextStorage.getContext(),
          ),
        )
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowClaimSingleItemCheckoutNext
      claimFlowContextStorage.saveContext(result.context)
    }
  }

  override suspend fun submitSummary(
    dateOfOccurrence: LocalDate?,
    itemBrandInput: FlowClaimItemBrandInput?,
    itemModelInput: FlowClaimItemModelInput?,
    itemProblemIds: List<String>?,
    location: String?,
    purchaseDate: LocalDate?,
    purchasePrice: Double?,
  ): Either<ErrorMessage, ClaimFlowStep> {
    return either {
      val result = apolloClient
        .mutation(
          FlowClaimSummaryNextMutation(
            FlowClaimSummaryInput(
              customName = Optional.absent(),
              dateOfOccurrence = Optional.presentIfNotNull(dateOfOccurrence),
              itemBrandInput = Optional.presentIfNotNull(itemBrandInput),
              itemModelInput = Optional.presentIfNotNull(itemModelInput),
              itemProblemIds = Optional.presentIfNotNull(itemProblemIds),
              location = Optional.presentIfNotNull(location),
              purchaseDate = Optional.presentIfNotNull(purchaseDate),
              purchasePrice = Optional.presentIfNotNull(purchasePrice),
            ),
            claimFlowContextStorage.getContext(),
          ),
        )
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowClaimSummaryNext
      claimFlowContextStorage.saveContext(result.context)
      networkCacheManager.clearCache()
      result.currentStep.toClaimFlowStep(FlowId(result.id), selfServiceCompletedEventManager)
    }
  }

  override suspend fun submitUrgentEmergency(isUrgentEmergency: Boolean): Either<ErrorMessage, ClaimFlowStep> {
    return either {
      val result = apolloClient
        .mutation(
          FlowClaimConfirmEmergencyMutation(
            isUrgentEmergency = isUrgentEmergency,
            claimFlowContextStorage.getContext(),
          ),
        )
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowClaimConfirmEmergencyNext

      claimFlowContextStorage.saveContext(result.context)
      result.currentStep.toClaimFlowStep(FlowId(result.id), selfServiceCompletedEventManager)
    }
  }

  override suspend fun submitFiles(fileIds: List<String>): Either<ErrorMessage, ClaimFlowStep> {
    return either {
      val result = apolloClient
        .mutation(
          FlowClaimFileUploadNextMutation(
            input = FlowClaimFileUploadInput(fileIds),
            context = claimFlowContextStorage.getContext(),
          ),
        )
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowClaimFileUploadNext

      claimFlowContextStorage.saveContext(result.context)
      result.currentStep.toClaimFlowStep(FlowId(result.id), selfServiceCompletedEventManager)
    }
  }

  private suspend fun Raise<ErrorMessage>.uploadAudioFile(flowId: String, file: File): AudioUrl {
    val result = odysseyService
      .uploadAudioRecordingFile(
        flowId = flowId,
        file = MultipartBody.Part.createFormData(
          // Same name for both due to this: https://hedviginsurance.slack.com/archives/C03RP2M458V/p1680004365854429
          name = file.name,
          filename = file.name,
          body = file.asRequestBody("audio/aac".toMediaType()),
        ),
      )
      .onLeft {
        logcat(LogPriority.ERROR) { "Failed to upload file for flowId:$flowId. Error:$it" }
      }
      .mapLeft(CallError::toErrorMessage)
      .bind()
    return AudioUrl(result.audioUrl)
  }

  private suspend fun Raise<ErrorMessage>.nextClaimFlowStepWithAudioUrl(audioUrl: AudioUrl): ClaimFlowStep {
    val result = apolloClient
      .mutation(FlowClaimAudioRecordingNextMutation(audioUrl.value, claimFlowContextStorage.getContext()))
      .safeExecute()
      .toEither(::ErrorMessage)
      .bind()
      .flowClaimAudioRecordingNext
    logcat { "Submitted audio file to GQL with URL $audioUrl" }
    claimFlowContextStorage.saveContext(result.context)
    return result.currentStep.toClaimFlowStep(FlowId(result.id), selfServiceCompletedEventManager)
  }
}

private suspend fun ClaimFlowStepFragment.CurrentStep.toClaimFlowStep(
  flowId: FlowId,
  selfServiceCompletedEventManager: SelfServiceCompletedEventManager,
): ClaimFlowStep {
  return when (this) {
    is ClaimFlowStepFragment.FlowClaimAudioRecordingStepCurrentStep -> {
      ClaimFlowStep.ClaimAudioRecordingStep(flowId, questions, audioContent)
    }
    is ClaimFlowStepFragment.FlowClaimDateOfOccurrenceStepCurrentStep -> {
      ClaimFlowStep.ClaimDateOfOccurrenceStep(flowId, dateOfOccurrence, maxDate)
    }
    is ClaimFlowStepFragment.FlowClaimLocationStepCurrentStep -> {
      ClaimFlowStep.ClaimLocationStep(flowId, location, options)
    }
    is ClaimFlowStepFragment.FlowClaimDateOfOccurrencePlusLocationStepCurrentStep -> {
      ClaimFlowStep.ClaimDateOfOccurrencePlusLocationStep(
        flowId,
        dateOfOccurrenceStep.dateOfOccurrence,
        dateOfOccurrenceStep.maxDate,
        locationStep.location,
        locationStep.options,
      )
    }
    is ClaimFlowStepFragment.FlowClaimPhoneNumberStepCurrentStep -> {
      ClaimFlowStep.ClaimPhoneNumberStep(flowId, phoneNumber)
    }
    is ClaimFlowStepFragment.FlowClaimSingleItemStepCurrentStep -> {
      ClaimFlowStep.ClaimSingleItemStep(
        flowId,
        preferredCurrency,
        purchaseDate,
        purchasePrice,
        availableItemBrands,
        selectedItemBrand,
        availableItemModels,
        selectedItemModel,
        availableItemProblems,
        selectedItemProblems,
      )
    }

    is ClaimFlowStepFragment.FlowClaimSingleItemCheckoutStepCurrentStep -> {
      ClaimFlowStep.ClaimResolutionSingleItemStep(
        flowId,
        price,
        depreciation,
        deductible,
        payoutAmount,
        availableCheckoutMethods,
      )
    }

    is ClaimFlowStepFragment.FlowClaimSummaryStepCurrentStep -> {
      ClaimFlowStep.ClaimSummaryStep(
        flowId,
        title,
        locationStep.location,
        locationStep.options,
        dateOfOccurrenceStep.dateOfOccurrence,
        dateOfOccurrenceStep.maxDate,
        singleItemStep?.preferredCurrency,
        singleItemStep?.purchaseDate,
        singleItemStep?.purchasePrice,
        singleItemStep?.availableItemBrands,
        singleItemStep?.selectedItemBrand,
        singleItemStep?.availableItemModels,
        singleItemStep?.selectedItemModel,
        singleItemStep?.availableItemProblems,
        singleItemStep?.selectedItemProblems,
        fileUploads = fileUploadStep?.uploads,
        signedAudioUrl = audioRecordingStep?.audioContent?.signedUrl,
      )
    }

    is ClaimFlowStepFragment.FlowClaimFailedStepCurrentStep -> ClaimFlowStep.ClaimFailedStep(flowId)
    is ClaimFlowStepFragment.FlowClaimSuccessStepCurrentStep -> {
      selfServiceCompletedEventManager.completedSelfServiceSuccessfully()
      ClaimFlowStep.ClaimSuccessStep(flowId)
    }
    is ClaimFlowStepFragment.FlowClaimContractSelectStepCurrentStep -> ClaimFlowStep.ClaimSelectContractStep(
      flowId,
      options,
    )

    is ClaimFlowStepFragment.FlowClaimDeflectGlassDamageStepCurrentStep -> ClaimFlowStep.ClaimDeflectGlassDamageStep(
      flowId,
      partners,
    )

    is ClaimFlowStepFragment.FlowClaimDeflectTowingStepCurrentStep -> ClaimFlowStep.ClaimDeflectTowingStep(
      flowId,
      partners,
    )

    is ClaimFlowStepFragment.FlowClaimDeflectEirStepCurrentStep -> ClaimFlowStep.ClaimDeflectEirStep(
      flowId,
      partners,
    )

    is ClaimFlowStepFragment.FlowClaimConfirmEmergencyStepCurrentStep -> ClaimFlowStep.ClaimConfirmEmergencyStep(
      flowId,
      text,
      confirmEmergency,
      options,
    )

    is ClaimFlowStepFragment.FlowClaimDeflectEmergencyStepCurrentStep -> ClaimFlowStep.ClaimDeflectEmergencyStep(
      flowId,
      partners,
    )

    is ClaimFlowStepFragment.FlowClaimDeflectPestsStepCurrentStep -> ClaimFlowStep.ClaimDeflectPestsStep(
      flowId,
      partners,
    )

    is ClaimFlowStepFragment.FlowClaimFileUploadStepCurrentStep -> ClaimFlowStep.ClaimFileUploadStep(
      flowId,
      title,
      targetUploadUrl,
      uploads,
    )

    else -> ClaimFlowStep.UnknownStep(flowId)
  }
}
