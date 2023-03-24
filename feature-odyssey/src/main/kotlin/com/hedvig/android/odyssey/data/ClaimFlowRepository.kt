package com.hedvig.android.odyssey.data

import arrow.core.Either
import arrow.core.continuations.either
import arrow.retrofit.adapter.either.networkhandling.CallError
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.odyssey.model.FlowId
import com.hedvig.android.odyssey.retrofit.toErrorMessage
import kotlinx.datetime.LocalDate
import octopus.FlowClaimAudioRecordingNextMutation
import octopus.FlowClaimDateOfOccurrenceNextMutation
import octopus.FlowClaimDateOfOccurrencePlusLocationNextMutation
import octopus.FlowClaimLocationNextMutation
import octopus.FlowClaimPhoneNumberNextMutation
import octopus.FlowClaimSingleItemNextMutation
import octopus.FlowClaimStartMutation
import octopus.FlowClaimSummaryNextMutation
import octopus.type.FlowClaimItemBrandInput
import octopus.type.FlowClaimItemModelInput
import octopus.type.FlowClaimSingleItemInput
import octopus.type.FlowClaimSummaryInput
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import slimber.log.d
import slimber.log.e
import java.io.File

internal interface ClaimFlowRepository {
  suspend fun startClaimFlow(entryPointId: String?): Either<ErrorMessage, ClaimFlowStep>
  suspend fun submitAudioRecording(flowId: FlowId, audioFile: File): Either<ErrorMessage, ClaimFlowStep>
  suspend fun submitDateOfOccurrence(dateOfOccurrence: LocalDate?): Either<ErrorMessage, ClaimFlowStep>
  suspend fun submitLocation(location: String?): Either<ErrorMessage, ClaimFlowStep>
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

  suspend fun submitSummary(
    dateOfOccurrence: LocalDate?,
    itemBrandInput: FlowClaimItemBrandInput?,
    itemModelInput: FlowClaimItemModelInput?,
    itemProblemIds: List<String>?,
    location: String?,
    purchaseDate: LocalDate?,
    purchasePrice: Double?,
  ): Either<ErrorMessage, ClaimFlowStep>
}

internal class ClaimFlowRepositoryImpl(
  private val apolloClient: ApolloClient,
  private val odysseyService: OdysseyService,
) : ClaimFlowRepository {
  private var claimFlowContext: Any? = null // todo clear this when leaving the of the Claim scope

  override suspend fun startClaimFlow(
    entryPointId: String?,
  ): Either<ErrorMessage, ClaimFlowStep> {
    return either {
      val result = apolloClient
        .mutation(FlowClaimStartMutation(entryPointId))
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowClaimStart
      claimFlowContext = result.context
      result.currentStep.toClaimFlowStep(FlowId(result.id))
    }
  }

  override suspend fun submitAudioRecording(flowId: FlowId, audioFile: File): Either<ErrorMessage, ClaimFlowStep> {
    return either {
      d { "Uploading file with flowId:$flowId and audio file name:${audioFile.name}" }
      val audioUrl = uploadAudioFile(flowId.value, audioFile).bind()
      d { "Uploaded audio file, resulting url:$audioUrl" }
      val result = apolloClient
        .mutation(FlowClaimAudioRecordingNextMutation(audioUrl.value, claimFlowContext!!))
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowClaimAudioRecordingNext
      d { "Submitted audio file to GQL with URL $audioUrl" }
      claimFlowContext = result.context
      result.currentStep.toClaimFlowStep(FlowId(result.id))
    }
  }

  override suspend fun submitDateOfOccurrence(
    dateOfOccurrence: LocalDate?,
  ): Either<ErrorMessage, ClaimFlowStep> {
    return either {
      val result = apolloClient
        .mutation(FlowClaimDateOfOccurrenceNextMutation(dateOfOccurrence, claimFlowContext!!))
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowClaimDateOfOccurrenceNext
      claimFlowContext = result.context
      result.currentStep.toClaimFlowStep(FlowId(result.id))
    }
  }

  override suspend fun submitLocation(
    location: String?,
  ): Either<ErrorMessage, ClaimFlowStep> {
    return either {
      val result = apolloClient
        .mutation(FlowClaimLocationNextMutation(location, claimFlowContext!!))
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowClaimLocationNext
      claimFlowContext = result.context
      result.currentStep.toClaimFlowStep(FlowId(result.id))
    }
  }

  override suspend fun submitDateOfOccurrenceAndLocation(
    dateOfOccurrence: LocalDate?,
    location: String?,
  ): Either<ErrorMessage, ClaimFlowStep> {
    return either {
      val result = apolloClient
        .mutation(FlowClaimDateOfOccurrencePlusLocationNextMutation(dateOfOccurrence, location, claimFlowContext!!))
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowClaimDateOfOccurrencePlusLocationNext
      claimFlowContext = result.context
      result.currentStep.toClaimFlowStep(FlowId(result.id))
    }
  }

  override suspend fun submitPhoneNumber(
    phoneNumber: String,
  ): Either<ErrorMessage, ClaimFlowStep> {
    return either {
      val result = apolloClient
        .mutation(FlowClaimPhoneNumberNextMutation(phoneNumber, claimFlowContext!!))
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowClaimPhoneNumberNext
      claimFlowContext = result.context
      result.currentStep.toClaimFlowStep(FlowId(result.id))
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
              customName = Optional.absent(), // Will be used when entering a free form text is supported
              itemBrandInput = Optional.presentIfNotNull(itemBrandInput),
              itemModelInput = Optional.presentIfNotNull(itemModelInput),
              itemProblemIds = Optional.presentIfNotNull(itemProblemIds),
              purchaseDate = Optional.presentIfNotNull(purchaseDate),
              purchasePrice = Optional.presentIfNotNull(purchasePrice),
            ),
            claimFlowContext!!,
          ),
        )
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowClaimSingleItemNext
      claimFlowContext = result.context
      result.currentStep.toClaimFlowStep(FlowId(result.id))
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
            claimFlowContext!!,
          ),
        )
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .flowClaimSummaryNext
      claimFlowContext = result.context
      result.currentStep.toClaimFlowStep(FlowId(result.id))
    }
  }

  private suspend fun uploadAudioFile(flowId: String, file: File): Either<ErrorMessage, AudioUrl> {
    return either {
      val result = odysseyService
        .uploadAudioRecordingFile(
          flowId = flowId,
          file = MultipartBody.Part.createFormData(
            name = "android_audio_file",
            filename = file.name,
            body = file.asRequestBody("audio/aac".toMediaType()),
          ),
        )
        .onLeft {
          e { "Failed to upload file for flowId:$flowId. Error:$it" }
        }
        .mapLeft(CallError::toErrorMessage)
        .bind()
      AudioUrl(result.audioUrl)
    }
  }
}

@JvmInline
private value class AudioUrl(val value: String)
