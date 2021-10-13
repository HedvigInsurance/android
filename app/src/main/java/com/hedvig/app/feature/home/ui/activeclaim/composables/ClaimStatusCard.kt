package com.hedvig.app.feature.home.ui.activeclaim.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.app.feature.home.ui.activeclaim.data.ClaimProgressData
import com.hedvig.app.feature.home.ui.activeclaim.data.ClaimStatusData
import com.hedvig.app.ui.compose.theme.HedvigTheme

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
                title = claimStatusData.title,
                subtitle = claimStatusData.subtitle,
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
@Composable
fun ClaimStatusCardPreview(
    @PreviewParameter(ClaimStatusDataProvider::class) claimStatusData: ClaimStatusData
) {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            ClaimStatusCard(claimStatusData)
        }
    }
}

class ClaimStatusDataProvider : PreviewParameterProvider<ClaimStatusData> {

    override val values: Sequence<ClaimStatusData>
        get() {
            val title = "All-risk"
            val subTitle = "Contents insurance"
            return sequenceOf(
                ClaimStatusData(
                    pillData = listOf(),
                    title = title,
                    subtitle = subTitle,
                    claimProgressData = listOf(
                        ClaimProgressData("Submitted", ClaimProgressData.ClaimProgressType.Paid)
                    )
                ),
            )
        }
}
