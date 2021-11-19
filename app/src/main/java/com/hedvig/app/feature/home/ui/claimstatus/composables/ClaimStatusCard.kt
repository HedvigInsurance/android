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
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimStatusCardData
import com.hedvig.app.feature.home.ui.claimstatus.data.PillData
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.compose.preview.previewData
import java.util.UUID

@Composable
fun ClaimStatusCard(
    claimStatusCardData: ClaimStatusCardData,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        elevation = 4.dp
    ) {
        Column {
            TopInfo(
                pillDataList = claimStatusCardData.pillData,
                title = claimStatusCardData.title,
                subtitle = claimStatusCardData.subtitle,
                modifier = Modifier.padding(16.dp)
            )
            Divider()
            ClaimProgress(
                claimProgressData = claimStatusCardData.claimProgressData,
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
            val claimStatusData = ClaimStatusCardData(
                id = UUID.randomUUID().toString(),
                pillData = PillData.previewData(),
                title = "All-risk",
                subtitle = "Home Insurance Renter",
                claimProgressData = ClaimProgressData.previewData()
            )
            ClaimStatusCard(claimStatusData)
        }
    }
}
