package com.hedvig.android.feature.odyssey.data

import app.cash.turbine.Turbine
import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.model.AudioUrl
import com.hedvig.android.data.claimflow.model.FlowId
import com.hedvig.android.data.claimtriaging.EntryPointId
import com.hedvig.android.data.claimtriaging.EntryPointOptionId
import java.io.File
import kotlinx.datetime.LocalDate
import octopus.type.FlowClaimItemBrandInput
import octopus.type.FlowClaimItemModelInput

internal class TestClaimFlowRepository : ClaimFlowRepository {
  override suspend fun startClaimFlow(
    entryPointId: EntryPointId?,
    entryPointOptionId: EntryPointOptionId?,
  ): Either<ErrorMessage, ClaimFlowStep> {
    error("Not implemented")
  }

  override suspend fun submitAudioRecording(flowId: FlowId, audioFile: File): Either<ErrorMessage, ClaimFlowStep> {
    error("Not implemented")
  }

  override suspend fun submitAudioUrl(flowId: FlowId, audioUrl: AudioUrl): Either<ErrorMessage, ClaimFlowStep> {
    error("Not implemented")
  }

  override suspend fun submitDateOfOccurrence(dateOfOccurrence: LocalDate?): Either<ErrorMessage, ClaimFlowStep> {
    error("Not implemented")
  }

  override suspend fun submitLocation(location: String?): Either<ErrorMessage, ClaimFlowStep> {
    error("Not implemented")
  }

  override suspend fun submitContract(contract: String?): Either<ErrorMessage, ClaimFlowStep> {
    error("Not implemented")
  }

  override suspend fun submitDateOfOccurrenceAndLocation(
    dateOfOccurrence: LocalDate?,
    location: String?,
  ): Either<ErrorMessage, ClaimFlowStep> {
    error("Not implemented")
  }

  override suspend fun submitPhoneNumber(phoneNumber: String): Either<ErrorMessage, ClaimFlowStep> {
    error("Not implemented")
  }

  val submitSingleItemBrandAndModelInput = Turbine<Pair<FlowClaimItemBrandInput?, FlowClaimItemModelInput?>>()
  val submitSingleItemResponse = Turbine<Either<ErrorMessage, ClaimFlowStep>>()

  override suspend fun submitSingleItem(
    itemBrandInput: FlowClaimItemBrandInput?,
    itemModelInput: FlowClaimItemModelInput?,
    itemProblemIds: List<String>?,
    purchaseDate: LocalDate?,
    purchasePrice: Double?,
  ): Either<ErrorMessage, ClaimFlowStep> {
    submitSingleItemBrandAndModelInput.add(itemBrandInput to itemModelInput)
    return submitSingleItemResponse.awaitItem()
  }

  val submitSingleItemCheckoutInput = Turbine<Double>()
  val submitSingleItemCheckoutResponse = Turbine<Either<ErrorMessage, Unit>>()

  override suspend fun submitSingleItemCheckout(amount: Double): Either<ErrorMessage, Unit> {
    submitSingleItemCheckoutInput.add(amount)
    return submitSingleItemCheckoutResponse.awaitItem()
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
    error("Not implemented")
  }

  override suspend fun submitUrgentEmergency(isUrgentEmergency: Boolean): Either<ErrorMessage, ClaimFlowStep> {
    error("Not implemented")
  }

  override suspend fun submitFiles(fileIds: List<String>): Either<ErrorMessage, ClaimFlowStep> {
    error("Not implemented")
  }
}
