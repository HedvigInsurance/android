package com.hedvig.app.ui.compose.composables.pill

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.ui.compose.theme.hedvigContentColorFor

@Composable
fun Pill(
    text: String,
    backgroundColor: Color,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        contentColor = hedvigContentColorFor(backgroundColor),
        modifier = Modifier.heightIn(min = 24.dp)
    ) {
        Row(
            Modifier.padding(
                horizontal = 8.dp,
                vertical = 5.dp
            ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text,
                style = MaterialTheme.typography.caption,
                maxLines = 1
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PillPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            Pill("PillText", MaterialTheme.colors.primary)
        }
    }
}
