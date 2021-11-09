package com.hedvig.app.feature.home.ui.claimstatus.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimStatusData

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ClaimStatusCards(
    claimStatusDataList: List<ClaimStatusData>,
    onClick: (id: String) -> Unit,
) {
    val pagerState = rememberPagerState()
    Column {
        HorizontalPager(
            count = claimStatusDataList.size,
            key = { page: Int -> claimStatusDataList[page].id },
            state = pagerState,
            itemSpacing = 0.dp,
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) { page: Int ->
            val claimStatusData = claimStatusDataList[page]
            // TODO use normal itemSpacing on pager after https://github.com/google/accompanist/issues/793 is fixed
            val itemSpacingPadding = PaddingValues(
                start = if (page == 0) 0.dp else 6.dp,
                end = if (page == claimStatusDataList.lastIndex) 0.dp else 6.dp,
            )
            ClaimStatusCard(
                claimStatusData = claimStatusData,
                onClick = onClick,
                modifier = Modifier.padding(itemSpacingPadding),
            )
        }
        if (claimStatusDataList.size == 1) {
            Spacer(Modifier.height(14.dp))
        } else {
            Spacer(Modifier.height(16.dp))
            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
