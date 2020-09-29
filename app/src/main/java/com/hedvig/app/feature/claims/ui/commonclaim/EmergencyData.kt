package com.hedvig.app.feature.claims.ui.commonclaim

import android.os.Parcelable
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.HedvigColor
import com.hedvig.app.util.apollo.ThemedIconUrls
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EmergencyData(
    val iconUrls: ThemedIconUrls,
    val color: HedvigColor,
    val title: String,
    val eligibleToClaim: Boolean
) : Parcelable {
    companion object {
        fun from(data: CommonClaimQuery.CommonClaim, eligibleToClaim: Boolean): EmergencyData? {
            TODO("Remove")
        }

        fun from(data: HomeQuery.CommonClaim, eligibleToClaim: Boolean): EmergencyData? {
            val layout = data.layout.asEmergency ?: return null
            return EmergencyData(
                ThemedIconUrls.from(data.icon.variants.fragments.iconVariantsFragment),
                layout.color,
                data.title,
                eligibleToClaim
            )
        }
    }
}
