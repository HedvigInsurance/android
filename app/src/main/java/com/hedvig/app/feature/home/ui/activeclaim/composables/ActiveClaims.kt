package com.hedvig.app.feature.home.ui.activeclaim.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.feature.home.ui.activeclaim.data.ClaimStatusData

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ClaimStatusCards(homeQueryActiveClaimList: List<HomeQuery.ClaimStatus>) {
    require(homeQueryActiveClaimList.isNotEmpty())

    val claimStatusDataList: List<ClaimStatusData> = homeQueryActiveClaimList.map {
        ClaimStatusData.fromHomeQueryClaimStatus(it)
    }

    if (claimStatusDataList.size == 1) {
        val activeClaimData = claimStatusDataList.first()
        ClaimStatusCard(
            claimStatusData = activeClaimData,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp)
        )
    } else {
        val pagerState = rememberPagerState()
        Column {
            HorizontalPager(
                count = claimStatusDataList.size,
                key = { page: Int -> claimStatusDataList[page].id },
                state = pagerState,
                itemSpacing = 12.dp,
                contentPadding = PaddingValues(top = 0.dp, start = 16.dp, end = 16.dp),
            ) { page: Int ->
                val activeClaimData = claimStatusDataList[page]
                ClaimStatusCard(
                    claimStatusData = activeClaimData,
                )
            }

            Spacer(Modifier.height(16.dp))

            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
