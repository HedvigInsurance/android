package com.hedvig.android.feature.home.claims.commonclaim

import android.os.Parcelable
import com.hedvig.android.core.common.android.ThemedIconUrls
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import octopus.HomeQuery
import octopus.HomeQuery.Data.CurrentMember.ActiveContract.CurrentAgreement.ProductVariant.CommonClaimDescription.Layout.Companion.asCommonClaimLayoutTitleAndBulletPoints
import octopus.type.HedvigColor

@Parcelize
@Serializable
data class CommonClaimsData(
  val id: String,
  val iconUrls: ThemedIconUrls,
  val title: String,
  val color: HedvigColor,
  val layoutTitle: String,
  val buttonText: String,
  val eligibleToClaim: Boolean,
  val bulletPoints: List<BulletPoint>,
) : Parcelable {
  companion object {
    fun from(
      data: HomeQuery.Data.CurrentMember.ActiveContract.CurrentAgreement.ProductVariant.CommonClaimDescription,
      eligibleToClaim: Boolean,
    ): CommonClaimsData? {
      val layout = data.layout.asCommonClaimLayoutTitleAndBulletPoints() ?: return null
      return CommonClaimsData(
        id = data.id,
        iconUrls = ThemedIconUrls.from(data.icon),
        title = data.title,
        color = layout.color,
        layoutTitle = layout.title,
        buttonText = layout.buttonTitle,
        eligibleToClaim = eligibleToClaim,
        bulletPoints = BulletPoint.from(layout.bulletPoints),
      )
    }
  }
}

fun CommonClaimsData.isFirstVet() = id == "32" || id == "31" || id == "30"
