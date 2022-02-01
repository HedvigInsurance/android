package com.hedvig.app.ui.compose.composables.claimprogress

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimStatusColors
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.compose.ContentAlpha
import com.hedvig.app.util.compose.fillWithColor

@Composable
fun ClaimProgressRow(
    claimProgressItemsUiState: List<ClaimProgressUiState>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
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

@Composable
private fun ClaimProgress(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    contentAlpha: ContentAlpha = ContentAlpha.HIGH,
) {
    CompositionLocalProvider(LocalContentAlpha provides contentAlpha.value) {
        Column(modifier = modifier) {
            val progressColor = color.copy(alpha = LocalContentAlpha.current)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            ) {
                fillWithColor(progressColor)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
fun ClaimProgressRowPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            ClaimProgressRow(
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

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ClaimProgressPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            ClaimProgress(
                "Text",
                MaterialTheme.colors.primary,
            )
        }
    }
}
