package com.hedvig.app.feature.home.ui.claimstatus.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimProgressData
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimStatusColors
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimStatusData
import com.hedvig.app.feature.home.ui.claimstatus.data.PillData
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.compose.DisplayableText
import java.util.UUID

@Composable
fun ClaimStatusCard(
    claimStatusData: ClaimStatusData,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        elevation = 4.dp
    ) {
        Column {
            TopInfo(
                pillDataList = claimStatusData.pillData,
                title = claimStatusData.title.text(),
                subtitle = claimStatusData.subtitle.text(),
                modifier = Modifier.padding(16.dp)
            )
            Divider()
            ClaimProgress(
                claimProgressData = claimStatusData.claimProgressData,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ClaimStatusCardPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            val claimStatusData = ClaimStatusData(
                id = UUID.randomUUID().toString(),
                pillData = listOf(
                    PillData(DisplayableText("Reopened"), PillData.PillType.Contained(ClaimStatusColors.Pill.reopened)),
                    PillData(DisplayableText("Claim"), PillData.PillType.Outlined),
                ),
                title = DisplayableText("All-risk"),
                subtitle = DisplayableText("Contents insurance"),
                claimProgressData = listOf(
                    ClaimProgressData(DisplayableText("Submitted"), ClaimProgressData.ClaimProgressType.PastInactive),
                    ClaimProgressData(DisplayableText("Being Handled"), ClaimProgressData.ClaimProgressType.Paid),
                    ClaimProgressData(DisplayableText("Closed"), ClaimProgressData.ClaimProgressType.FutureInactive),
                )
            )
            ClaimStatusCard(claimStatusData)
        }
    }
}
