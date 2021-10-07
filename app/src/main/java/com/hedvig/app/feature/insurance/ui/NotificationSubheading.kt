package com.hedvig.app.feature.insurance.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.ui.compose.theme.HedvigTheme

@Composable
fun NotificationSubheading(text: String) {
    Subheading(
        text = text,
        contentBeforeText = {
            Canvas(Modifier.size(8.dp)) {
                drawCircle(Color.Red)
            }
            Spacer(modifier = Modifier.width(8.dp))
        },
    )
}

@Preview(
    name = "Subheading with a notification Badge",
    group = "Insurance Tab",
    showBackground = true,
)
@Composable
fun UnseenBadgeSubheadingPreview() {
    HedvigTheme {
        NotificationSubheading("Add more coverage")
    }
}
