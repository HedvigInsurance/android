package com.hedvig.app.feature.claims.ui.commonclaim

import android.os.Parcelable
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import com.hedvig.android.owldroid.type.HedvigColor
import com.hedvig.android.owldroid.type.InsuranceStatus
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CommonClaimsData(
    val iconUrl: String,
    val title: String,
    val color: HedvigColor,
    val layoutTitle: String,
    val buttonText: String,
    val insuranceStatus: InsuranceStatus,
    val bulletPoints: List<BulletPoint>
) : Parcelable {
    companion object {
        fun from(data: CommonClaimQuery.CommonClaim, insuranceStatus: InsuranceStatus): CommonClaimsData? {
            val layout = (data.layout as? CommonClaimQuery.AsTitleAndBulletPoints) ?: return null
            return CommonClaimsData(
                data.icon.svgUrl,
                data.title,
                layout.color,
                layout.title,
                layout.buttonTitle,
                insuranceStatus,
                BulletPoint.from(layout.bulletPoints)
            )
        }
    }
}
