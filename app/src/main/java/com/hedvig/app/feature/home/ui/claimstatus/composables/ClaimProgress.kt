package com.hedvig.app.feature.home.ui.claimstatus.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimProgressData
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimStatusColors
import com.hedvig.app.ui.compose.composables.ClaimProgressItem
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.compose.ContentAlpha

@Composable
fun ClaimProgress(
    claimProgressData: List<ClaimProgressData>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        claimProgressData.forEach { claimProgressData: ClaimProgressData ->
            ClaimProgress(
                text = claimProgressData.text,
                type = claimProgressData.type,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ClaimProgress(
    text: String,
    type: ClaimProgressData.ClaimProgressType,
    modifier: Modifier,
) {
    val color = when (type) {
        ClaimProgressData.ClaimProgressType.PAID -> ClaimStatusColors.Progress.paid
        ClaimProgressData.ClaimProgressType.REOPENED -> ClaimStatusColors.Progress.reopened
        ClaimProgressData.ClaimProgressType.UNKNOWN,
        ClaimProgressData.ClaimProgressType.PAST_INACTIVE,
        ClaimProgressData.ClaimProgressType.CURRENTLY_ACTIVE,
        ClaimProgressData.ClaimProgressType.FUTURE_INACTIVE,
        -> MaterialTheme.colors.primary
    }
    val contentAlpha = when (type) {
        ClaimProgressData.ClaimProgressType.PAST_INACTIVE -> ContentAlpha.MEDIUM
        ClaimProgressData.ClaimProgressType.CURRENTLY_ACTIVE -> ContentAlpha.HIGH
        ClaimProgressData.ClaimProgressType.FUTURE_INACTIVE -> ContentAlpha.DISABLED
        ClaimProgressData.ClaimProgressType.PAID -> ContentAlpha.HIGH
        ClaimProgressData.ClaimProgressType.REOPENED -> ContentAlpha.HIGH
        ClaimProgressData.ClaimProgressType.UNKNOWN -> ContentAlpha.HIGH
    }
    ClaimProgressItem(
        text = text,
        color = color,
        contentAlpha = contentAlpha,
        modifier = modifier,
    )
}

@Preview
@Composable
fun BottomProgressPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            ClaimProgress(
                listOf(
                    ClaimProgressData(
                        "Submitted",
                        ClaimProgressData.ClaimProgressType.PAST_INACTIVE
                    ),
                    ClaimProgressData(
                        "Being Handled",
                        ClaimProgressData.ClaimProgressType.REOPENED
                    ),
                    ClaimProgressData(
                        "Closed",
                        ClaimProgressData.ClaimProgressType.FUTURE_INACTIVE
                    ),
                ),
            )
        }
    }
}
