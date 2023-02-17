package com.hedvig.android.odyssey.network

import com.hedvig.android.odyssey.model.ClaimState
import com.hedvig.odyssey.remote.money.MonetaryAmount

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
  val brandId: String?,
  val typeId: String?,
  val customName: String?,
  val purchaseDate: String?,
  val purchasePrice: MonetaryAmount?,
)

fun ClaimState.toUpdateRequest() = UpdateAutomationClaimRequest(
  dateOfOccurrence = dateOfOccurrence?.toString(),
//  audioUrl = audioUrl,
  location = location.name,
  items = listOf(
    UpdateClaimItem(
      problemIds = item.problemIds.map { it.name },
      purchaseDate = item.purchaseDate?.toString(),
      purchasePrice = item.purchasePrice,
      brandId = item.selectedModelOption?.brandId,
      typeId = item.selectedModelOption?.typeId,
      customName = item.selectedModelOption?.modelName
    ),
  ),
)
