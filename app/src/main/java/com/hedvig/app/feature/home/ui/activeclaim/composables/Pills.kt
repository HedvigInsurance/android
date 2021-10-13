package com.hedvig.app.feature.home.ui.activeclaim.composables

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.feature.home.ui.activeclaim.data.PillData
import com.hedvig.app.ui.compose.theme.HedvigTheme

@Composable
fun Pills(pillData: List<PillData>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            pillData.forEach { pillData: PillData ->
                Pill(
                    text = pillData.text,
                    pillType = pillData.type,
                )
            }
        }
        // Uncomment when the card is clickable to go to detail screen
//        Icon(Icons.Default.ArrowForward, contentDescription = null)
    }
}

@Composable
private fun Pill(
    text: String,
    pillType: PillData.PillType
) {
    val backgroundColor = when (pillType) {
        is PillData.PillType.Contained -> pillType.color
        PillData.PillType.Outlined -> Color.Transparent
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        contentColor = when (pillType) {
            is PillData.PillType.Contained -> contentColorFor(backgroundColor)
            PillData.PillType.Outlined -> contentColorFor(MaterialTheme.colors.background)
        },
        border = when (pillType) {
            is PillData.PillType.Contained -> null
            PillData.PillType.Outlined -> BorderStroke(
                width = ButtonDefaults.OutlinedBorderSize,
                color = MaterialTheme.colors.onSurface
            )
        }
    ) {
        Row(
            Modifier.padding(
                horizontal = 8.dp,
                vertical = 5.dp
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

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PillsPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            Pills(
                listOf(
                    PillData("Reopened", PillData.PillType.Contained(Color(0xFFFE9650))),
                    PillData("Claim", PillData.PillType.Outlined),
                )
            )
        }
    }
}
