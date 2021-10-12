package com.hedvig.app.feature.home.ui.activeclaim.data

import androidx.compose.runtime.Composable
import com.hedvig.android.owldroid.graphql.HomeQuery

data class ActiveClaimData(
    val chipButtonData: List<ChipButtonData>,
    val title: String,
    val subtitle: String,
    val progressItemData: List<ProgressItemData>,
) {
    companion object {
        @Composable
        fun fromHomeQueryActiveClaim(activeClaim: HomeQuery.ActiveClaim): ActiveClaimData {
            val chipButtons = ChipButtonData.buttonListFromActiveClaim(activeClaim)
            val progressItems = ProgressItemData.progressItemListFromActiveClaim(activeClaim)
            return ActiveClaimData(
                chipButtonData = chipButtons,
                // Todo why is this nullable? Proper localization?
                title = activeClaim.contract?.displayName ?: "Unknown Type",
                subtitle = activeClaim.contract?.displayName ?: "Unknown Type",
                progressItemData = progressItems
            )
        }
    }
}
