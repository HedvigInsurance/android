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
import com.hedvig.app.feature.home.ui.activeclaim.data.ActiveClaimData
import com.hedvig.app.feature.home.ui.activeclaim.data.ProgressItemData
import com.hedvig.app.ui.compose.theme.HedvigTheme

@Composable
fun ActiveClaimCard(
    activeClaimData: ActiveClaimData,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        elevation = 4.dp
    ) {
        Column {
            TopInfo(
                chipButtonDataList = activeClaimData.chipButtonData,
                title = activeClaimData.title,
                subtitle = activeClaimData.subtitle,
                modifier = Modifier.padding(16.dp)
            )
            Divider()
            BottomProgress(
                progressItemData = activeClaimData.progressItemData,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
fun ActiveClaimCardPreview(
    @PreviewParameter(ActiveClaimDataProvider::class) activeClaimData: ActiveClaimData
) {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            ActiveClaimCard(activeClaimData)
        }
    }
}

class ActiveClaimDataProvider : PreviewParameterProvider<ActiveClaimData> {

    override val values: Sequence<ActiveClaimData>
        get() {
            val title = "All-risk"
            val subTitle = "Contents insurance"
            return sequenceOf(
                ActiveClaimData(
                    chipButtonData = listOf(),
                    title = title,
                    subtitle = subTitle,
                    progressItemData = listOf(
                        ProgressItemData("Submitted", ProgressItemData.ProgressItemType.Paid)
                    )
                ),
            )
        }
}
