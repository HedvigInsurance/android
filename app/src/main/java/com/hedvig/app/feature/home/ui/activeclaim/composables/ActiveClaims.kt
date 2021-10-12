package com.hedvig.app.feature.home.ui.activeclaim

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.feature.home.ui.activeclaim.composables.ActiveClaimCard
import com.hedvig.app.feature.home.ui.activeclaim.data.ActiveClaimData

@Composable
fun ActiveClaimCards(homeQueryActiveClaimList: List<HomeQuery.ActiveClaim>) {
    require(homeQueryActiveClaimList.isNotEmpty())

    val activeClaimDataList: List<ActiveClaimData> = homeQueryActiveClaimList.map {
        ActiveClaimData.fromHomeQueryActiveClaim(it)
    }

    // TODO Horizontal Scroll Pager with dot indicators
    val activeClaimData = activeClaimDataList.first()
    ActiveClaimCard(
        activeClaimData = activeClaimData,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp)
    )
}
