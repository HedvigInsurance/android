package com.hedvig.android.feature.home.claims.commonclaim

import android.os.Parcelable
import com.hedvig.android.core.common.android.ThemedIconUrls
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import octopus.HomeQuery.Data.CurrentMember.ActiveContract.CurrentAgreement.ProductVariant.CommonClaimDescription.CommonClaimLayoutTitleAndBulletPointsLayout

@Serializable
@Parcelize
data class BulletPoint(
  val title: String,
  val description: String,
  val iconUrls: ThemedIconUrls,
) : Parcelable {
  companion object {

    fun from(
      bulletPoints: List<CommonClaimLayoutTitleAndBulletPointsLayout.BulletPoint>,
    ): List<BulletPoint> = bulletPoints.map { bulletPoint ->
      BulletPoint(
        bulletPoint.title,
        bulletPoint.description,
        ThemedIconUrls.from(bulletPoint.icon),
      )
    }
  }
}
