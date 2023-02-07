package com.hedvig.app.feature.claims.ui.commonclaim

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import com.google.android.gms.common.internal.service.Common
import com.hedvig.android.apollo.graphql.HomeQuery
import com.hedvig.android.apollo.graphql.type.HedvigColor
import com.hedvig.app.util.apollo.ThemedIconUrls
import kotlinx.parcelize.Parcelize

@Parcelize
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

fun CommonClaimsData.isFirstVet() = id == "25" || id == "26"

fun getFirstVetIntent(context: Context) = try {
  context.packageManager.getLaunchIntentForPackage("com.firstvet.firstvet");
} catch (e: ActivityNotFoundException) {
  Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.firstvet.firstvet"))
}
