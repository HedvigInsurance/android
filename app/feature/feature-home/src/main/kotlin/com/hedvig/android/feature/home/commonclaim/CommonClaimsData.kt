package com.hedvig.android.feature.home.commonclaim

import kotlinx.serialization.Serializable
import octopus.HomeQuery
import octopus.HomeQuery.Data.CurrentMember.ActiveContract.CurrentAgreement.ProductVariant.CommonClaimDescription.Layout.Companion.asCommonClaimLayoutTitleAndBulletPoints

@Serializable
data class CommonClaimsData(
  val id: String,
  val title: String,
  val bulletPoints: List<BulletPoint>,
) {
  val isFirstVet = id == "30" || id == "31" || id == "32"

  companion object {
    fun from(
      data: HomeQuery.Data.CurrentMember.ActiveContract.CurrentAgreement.ProductVariant.CommonClaimDescription,
    ): CommonClaimsData? {
      val layout = data.layout.asCommonClaimLayoutTitleAndBulletPoints() ?: return null
      return CommonClaimsData(
        id = data.id,
        title = data.title,
        bulletPoints = layout.bulletPoints.map { bulletPoint ->
          BulletPoint(
            bulletPoint.title,
            bulletPoint.description,
          )
        },
      )
    }
  }
}

@Serializable
data class BulletPoint(
  val title: String,
  val description: String,
)
