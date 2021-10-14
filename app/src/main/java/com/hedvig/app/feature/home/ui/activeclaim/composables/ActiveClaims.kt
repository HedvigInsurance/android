package com.hedvig.app.feature.home.ui.activeclaim.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.feature.home.ui.activeclaim.data.ClaimStatusData

@Composable
fun ClaimStatusCards(homeQueryActiveClaimList: List<HomeQuery.ClaimStatus>) {
    require(homeQueryActiveClaimList.isNotEmpty())

    val claimStatusDataList: List<ClaimStatusData> = homeQueryActiveClaimList.map {
        ClaimStatusData.fromHomeQueryClaimStatus(it)
    }

    // TODO Horizontal Scroll Pager with dot indicators
    val activeClaimData = claimStatusDataList.first()
    ClaimStatusCard(
        claimStatusData = activeClaimData,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp)
    )
}
