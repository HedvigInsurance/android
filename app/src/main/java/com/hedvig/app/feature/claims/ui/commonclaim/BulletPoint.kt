package com.hedvig.app.feature.claims.ui.commonclaim

import android.os.Parcelable
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.util.apollo.ThemedIconUrls
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BulletPoint(
    val title: String,
    val description: String,
    val iconUrls: ThemedIconUrls
) : Parcelable {
    companion object {

        fun from(bulletPoints: List<HomeQuery.BulletPoint>) = bulletPoints.map { bp ->
            BulletPoint(
                bp.title,
                bp.description,
                ThemedIconUrls.from(bp.icon.variants.fragments.iconVariantsFragment)
            )
        }
    }
}

