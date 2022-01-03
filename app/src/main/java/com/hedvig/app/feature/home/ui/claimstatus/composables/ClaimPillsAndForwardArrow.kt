package com.hedvig.app.feature.home.ui.claimstatus.composables

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimStatusColors
import com.hedvig.app.feature.home.ui.claimstatus.data.PillData
import com.hedvig.app.ui.compose.composables.pill.OutlinedPill
import com.hedvig.app.ui.compose.composables.pill.Pill
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.compose.preview.previewData

@Composable
fun ClaimPillsAndForwardArrow(
    pillData: List<PillData>,
    modifier: Modifier = Modifier,
    isClickable: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            pillData.forEach { pillData: PillData ->
                ClaimPill(
                    text = pillData.text,
                    pillType = pillData.type,
                )
            }
        }
        if (isClickable) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_forward),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ClaimPill(
    text: String,
    pillType: PillData.PillType,
) {
    when (pillType) {
        PillData.PillType.OPEN -> OutlinedPill(text)
        PillData.PillType.CLOSED -> Pill(text, MaterialTheme.colors.primary)
        PillData.PillType.REOPENED -> Pill(text, ClaimStatusColors.Pill.reopened)
        PillData.PillType.PAYMENT -> Pill(text, ClaimStatusColors.Pill.paid)
        PillData.PillType.UNKNOWN -> OutlinedPill(text)
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PillsPreview(
    @PreviewParameter(PillsPreviewDataProvider::class) pillData: List<PillData>,
) {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            ClaimPillsAndForwardArrow(pillData, isClickable = true)
        }
    }
}

class PillsPreviewDataProvider : CollectionPreviewParameterProvider<List<PillData>>(
    listOf(
        PillData.previewData(),
        listOf(PillData.PillType.CLOSED, PillData.PillType.PAYMENT).map { pillType ->
            PillData(pillType.name, pillType)
        },
        listOf(PillData.PillType.REOPENED, PillData.PillType.OPEN).map { pillType ->
            PillData(pillType.name, pillType)
        }
    )
)
