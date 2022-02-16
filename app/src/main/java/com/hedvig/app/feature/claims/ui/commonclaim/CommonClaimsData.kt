package com.hedvig.app.feature.claims.ui.commonclaim

import android.os.Parcelable
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.HedvigColor
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
    val bulletPoints: List<BulletPoint>
) : Parcelable {
    companion object {
        fun from(
            data: HomeQuery.CommonClaim,
            eligibleToClaim: Boolean
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
                BulletPoint.from(layout.bulletPoints)
            )
        }
    }
}
