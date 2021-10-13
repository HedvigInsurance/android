package com.hedvig.app.feature.home.ui.activeclaim.composables

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.feature.home.ui.activeclaim.data.ChipButtonData
import com.hedvig.app.ui.compose.theme.HedvigTheme

@Composable
fun ChipAndArrowRow(chipButtonData: List<ChipButtonData>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            chipButtonData.forEach { chipButtonData: ChipButtonData ->
                FlatNonClickableButtonLookingSurface(
                    text = chipButtonData.text,
                    chipType = chipButtonData.type,
                )
            }
        }
        Icon(Icons.Default.ArrowForward, contentDescription = null)
    }
}

// TODO what kind of name would fit here, no clue 😂
@Composable
fun FlatNonClickableButtonLookingSurface(
    text: String,
    chipType: ChipButtonData.ButtonType
) {
    val backgroundColor = when (chipType) {
        is ChipButtonData.ButtonType.Contained -> chipType.color
        ChipButtonData.ButtonType.Outlined -> Color.Transparent
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        contentColor = when (chipType) {
            is ChipButtonData.ButtonType.Contained -> contentColorFor(backgroundColor)
            ChipButtonData.ButtonType.Outlined -> contentColorFor(MaterialTheme.colors.background)
        },
        border = when (chipType) {
            is ChipButtonData.ButtonType.Contained -> null
            ChipButtonData.ButtonType.Outlined -> BorderStroke(
                width = ButtonDefaults.OutlinedBorderSize,
                color = MaterialTheme.colors.onSurface
            )
        }
    ) {
        ProvideTextStyle(MaterialTheme.typography.button) {
            Row(
                Modifier.padding(
                    horizontal = 5.dp,
                    vertical = 8.dp
                ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // TODO uppercase it in a different way? Check user's preference on language maybe?
                val currentLocale = Locale.current
                Text(
                    text.uppercase(java.util.Locale(currentLocale.language, currentLocale.region)),
                    style = MaterialTheme.typography.caption,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ChipAndArrowRowPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            ChipAndArrowRow(
                listOf(
                    ChipButtonData("Reopened", ChipButtonData.ButtonType.Contained(Color(0xFFFE9650))),
                    ChipButtonData("Claim", ChipButtonData.ButtonType.Outlined),
                )
            )
        }
    }
}
