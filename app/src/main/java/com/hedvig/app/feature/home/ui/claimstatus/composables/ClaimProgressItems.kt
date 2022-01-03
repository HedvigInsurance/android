package com.hedvig.app.feature.home.ui.claimstatus.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimProgressUiState
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimStatusColors
import com.hedvig.app.ui.compose.composables.ClaimProgress
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.compose.ContentAlpha

@Composable
fun ClaimProgressItems(
    claimProgressItemsUiState: List<ClaimProgressUiState>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        claimProgressItemsUiState.forEach { claimProgressUiState: ClaimProgressUiState ->
            ClaimProgress(
                text = claimProgressUiState.text,
                type = claimProgressUiState.type,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ClaimProgress(
    text: String,
    type: ClaimProgressUiState.ClaimProgressType,
    modifier: Modifier,
) {
    val color = when (type) {
        ClaimProgressUiState.ClaimProgressType.PAID -> ClaimStatusColors.Progress.paid
        ClaimProgressUiState.ClaimProgressType.REOPENED -> ClaimStatusColors.Progress.reopened
        ClaimProgressUiState.ClaimProgressType.UNKNOWN,
        ClaimProgressUiState.ClaimProgressType.PAST_INACTIVE,
        ClaimProgressUiState.ClaimProgressType.CURRENTLY_ACTIVE,
        ClaimProgressUiState.ClaimProgressType.FUTURE_INACTIVE,
        -> MaterialTheme.colors.primary
    }
    val contentAlpha = when (type) {
        ClaimProgressUiState.ClaimProgressType.PAST_INACTIVE -> ContentAlpha.MEDIUM
        ClaimProgressUiState.ClaimProgressType.CURRENTLY_ACTIVE -> ContentAlpha.HIGH
        ClaimProgressUiState.ClaimProgressType.FUTURE_INACTIVE -> ContentAlpha.DISABLED
        ClaimProgressUiState.ClaimProgressType.PAID -> ContentAlpha.HIGH
        ClaimProgressUiState.ClaimProgressType.REOPENED -> ContentAlpha.HIGH
        ClaimProgressUiState.ClaimProgressType.UNKNOWN -> ContentAlpha.HIGH
    }
    ClaimProgress(
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
            ClaimProgressItems(
                listOf(
                    ClaimProgressUiState(
                        "Submitted",
                        ClaimProgressUiState.ClaimProgressType.PAST_INACTIVE
                    ),
                    ClaimProgressUiState(
                        "Being Handled",
                        ClaimProgressUiState.ClaimProgressType.REOPENED
                    ),
                    ClaimProgressUiState(
                        "Closed",
                        ClaimProgressUiState.ClaimProgressType.FUTURE_INACTIVE
                    ),
                ),
            )
        }
    }
}
