package com.hedvig.android.feature.home.claims.commonclaim

import android.os.Parcelable
import com.hedvig.android.core.common.android.ThemedIconUrls
import giraffe.HomeQuery
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class BulletPoint(
  val title: String,
  val description: String,
  val iconUrls: ThemedIconUrls,
) : Parcelable {
  companion object {

    fun from(bulletPoints: List<HomeQuery.BulletPoint>) = bulletPoints.map { bp ->
      BulletPoint(
        bp.title,
        bp.description,
        ThemedIconUrls.from(bp.icon.variants.fragments.iconVariantsFragment),
      )
    }
  }
}
