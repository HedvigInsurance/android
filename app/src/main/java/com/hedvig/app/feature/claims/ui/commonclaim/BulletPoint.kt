package com.hedvig.app.feature.claims.ui.commonclaim

import android.os.Parcelable
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BulletPoint(
    val title: String,
    val description: String,
    val iconUrl: String
): Parcelable {
    companion object {
        fun from(bulletPoints: List<CommonClaimQuery.BulletPoint>) = bulletPoints.map { bp ->
            BulletPoint(
                bp.title,
                bp.description,
                bp.icon.svgUrl
            )
        }
    }
}
