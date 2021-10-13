package com.hedvig.app.feature.home.ui.activeclaim.data

import androidx.compose.runtime.Composable
import com.hedvig.android.owldroid.graphql.HomeQuery

data class ActiveClaimData(
    val pillData: List<PillData>,
    val title: String,
    val subtitle: String,
    val claimProgressData: List<ClaimProgressData>,
) {
    companion object {
        @Composable
        fun fromHomeQueryActiveClaim(activeClaim: HomeQuery.ActiveClaim): ActiveClaimData {
            val chipButtons = PillData.pillDataListFromActiveClaim(activeClaim)
            val progressItems = ClaimProgressData.progressItemListFromActiveClaim(activeClaim)
            return ActiveClaimData(
                pillData = chipButtons,
                // Todo why is this nullable? Proper localization?
                title = activeClaim.contract?.displayName ?: "Unknown Type",
                subtitle = activeClaim.contract?.displayName ?: "Unknown Type",
                claimProgressData = progressItems
            )
        }
    }
}
