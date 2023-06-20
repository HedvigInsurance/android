package com.hedvig.android.feature.home.claims.commonclaim

import android.os.Parcelable
import com.hedvig.android.core.common.android.ThemedIconUrls
import giraffe.HomeQuery
import giraffe.type.HedvigColor
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

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
      data: HomeQuery.CommonClaim,
      eligibleToClaim: Boolean,
    ): CommonClaimsData? {
      val layout = data.layout.asTitleAndBulletPoints ?: return null
      return CommonClaimsData(
        data.id,
        ThemedIconUrls.from(data.icon.variants.fragments.iconVariantsFragment),
        data.title,
        layout.color,
        layout.title,
        layout.buttonTitle,
        eligibleToClaim,
        BulletPoint.from(layout.bulletPoints),
      )
    }
  }
}

fun CommonClaimsData.isFirstVet() = id == "31" || id == "30"
