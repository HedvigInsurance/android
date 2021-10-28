package com.hedvig.app.feature.home.ui.claimstatus.composables

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimProgressData
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.compose.DisplayableText
import com.hedvig.app.util.compose.fillWithColor

@Composable
fun ClaimProgress(
    claimProgressData: List<ClaimProgressData>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        claimProgressData.forEach { claimProgressData: ClaimProgressData ->
            ClaimProgress(
                text = claimProgressData.displayableText.text(),
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
    CompositionLocalProvider(LocalContentAlpha provides type.contentAlpha.toComposableAlpha()) {
        Column(modifier = modifier) {
            val progressColor = type.color.toComposableColor().copy(alpha = LocalContentAlpha.current)
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
fun BottomProgressPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            ClaimProgress(
                listOf(
                    ClaimProgressData(
                        DisplayableText("Submitted"),
                        ClaimProgressData.ClaimProgressType.PastInactive
                    ),
                    ClaimProgressData(
                        DisplayableText("Being Handled"),
                        ClaimProgressData.ClaimProgressType.Reopened
                    ),
                    ClaimProgressData(
                        DisplayableText("Closed"),
                        ClaimProgressData.ClaimProgressType.FutureInactive
                    ),
                ),
            )
        }
    }
}
