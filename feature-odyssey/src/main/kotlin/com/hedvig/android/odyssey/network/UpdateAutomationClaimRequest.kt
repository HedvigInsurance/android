package com.hedvig.android.odyssey.network

import com.hedvig.android.odyssey.model.ClaimState
import com.hedvig.common.remote.money.MonetaryAmount
import java.time.LocalDate

@kotlinx.serialization.Serializable
data class UpdateAutomationClaimRequest(
  val dateOfOccurrence: String?,
  //val audioUrl: String?,
  val location: String?,
  val items: List<UpdateClaimItem>,
)

@kotlinx.serialization.Serializable
data class UpdateClaimItem(
  val problemIds: List<String>,
  val purchaseDate: String?,
  val purchasePrice: MonetaryAmount?,
)

fun ClaimState.toUpdateRequest() = UpdateAutomationClaimRequest(
  dateOfOccurrence = LocalDate.now().toString(),
//  audioUrl = audioUrl,
  location = location.name,
  items = listOf(
    UpdateClaimItem(
      problemIds = item.problemIds.map { it.name },
      purchaseDate = LocalDate.now().toString(),
      purchasePrice = item.purchasePrice,
    ),
  ),
)
