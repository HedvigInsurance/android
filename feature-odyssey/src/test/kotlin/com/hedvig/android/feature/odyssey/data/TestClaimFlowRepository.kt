package com.hedvig.android.feature.odyssey.data

import app.cash.turbine.Turbine
import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.odyssey.data.ClaimFlowRepository
import com.hedvig.android.odyssey.data.ClaimFlowStep
import com.hedvig.android.odyssey.model.AudioUrl
import com.hedvig.android.odyssey.model.FlowId
import kotlinx.datetime.LocalDate
import octopus.type.FlowClaimItemBrandInput
import octopus.type.FlowClaimItemModelInput
import java.io.File

internal class TestClaimFlowRepository : ClaimFlowRepository {
  override suspend fun startClaimFlow(entryPointId: String?): Either<ErrorMessage, ClaimFlowStep> {
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
  val submitSingleItemCheckoutResponse = Turbine<Either<ErrorMessage, ClaimFlowStep>>()

  override suspend fun submitSingleItemCheckout(amount: Double): Either<ErrorMessage, ClaimFlowStep> {
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
}
