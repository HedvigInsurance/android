package com.hedvig.app.feature.claims.ui.commonclaim

import android.os.Parcelable
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import com.hedvig.app.util.apollo.ThemedIconUrls
import kotlinx.android.parcel.Parcelize
import type.HedvigColor
import type.InsuranceStatus

@Parcelize
data class EmergencyData(
    val iconUrls: ThemedIconUrls,
    val color: HedvigColor,
    val title: String,
    val insuranceStatus: InsuranceStatus
) : Parcelable {
    companion object {
        fun from(data: CommonClaimQuery.CommonClaim, status: InsuranceStatus): EmergencyData? {
            val layout = data.layout?.inlineFragment as? CommonClaimQuery.AsEmergency ?: return null
            return EmergencyData(
                ThemedIconUrls.from(data.icon.variants.fragments.iconVariantsFragment),
                layout.color,
                data.title,
                status
            )
        }
    }
}
