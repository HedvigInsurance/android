package com.hedvig.app.feature.home.ui.claimstatus.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimProgressData
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimStatusCardData
import com.hedvig.app.feature.home.ui.claimstatus.data.PillData
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.compose.preview.previewData
import java.util.UUID

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ClaimStatusCards(claimStatusCardDataList: List<ClaimStatusCardData>) {
    val pagerState = rememberPagerState(claimStatusCardDataList.size)
    Column {
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        HorizontalPager(
            state = pagerState,
            itemSpacing = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page: Int ->
            val claimStatusData = claimStatusCardDataList[page]
            val itemWidth = screenWidth - (16 * 2).dp
            val itemSpacingPadding = PaddingValues(
                start = if (page == 0) 0.dp else 6.dp,
                end = if (page == claimStatusCardDataList.lastIndex) 0.dp else 6.dp,
            )
            ClaimStatusCard(
                claimStatusCardData = claimStatusData,
                modifier = Modifier
                    .width(itemWidth)
                    .padding(itemSpacingPadding)
            )
        }
        if (claimStatusCardDataList.size == 1) {
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

@Preview(name = "ClaimStatusCard", group = "Claim Status")
@Composable
fun ClaimStatusCardsPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            val claimStatusData = ClaimStatusCardData(
                id = UUID.randomUUID().toString(),
                pillData = PillData.previewData(),
                title = "All-risk",
                subtitle = "Contents insurance",
                claimProgressData = ClaimProgressData.previewData()
            )
            ClaimStatusCards(listOf(claimStatusData))
        }
    }
}
