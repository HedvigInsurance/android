package com.hedvig.app.feature.claims.ui.commonclaim

import android.os.Parcelable
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import com.hedvig.android.owldroid.type.HedvigColor
import com.hedvig.android.owldroid.type.InsuranceStatus
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EmergencyData(
    val iconUrl: String,
    val color: HedvigColor,
    val title: String,
    val insuranceStatus: InsuranceStatus
) : Parcelable {
    companion object {
        fun from(data: CommonClaimQuery.CommonClaim, status: InsuranceStatus): EmergencyData? {
            val layout = data.layout as? CommonClaimQuery.AsEmergency ?: return null
            return EmergencyData(
                data.icon.svgUrl,
                layout.color,
                data.title,
                status
            )
        }
    }
}
