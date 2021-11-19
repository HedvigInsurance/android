package com.hedvig.app.ui.compose.composables

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
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
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.compose.ContentAlpha
import com.hedvig.app.util.compose.fillWithColor

@Composable
fun ProgressItem(
    text: String,
    color: Color,
    contentAlpha: ContentAlpha,
    modifier: Modifier = Modifier,
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

@Preview()
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ProgressItemPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            ProgressItem(
                "Text",
                MaterialTheme.colors.primary,
                ContentAlpha.HIGH,
            )
        }
    }
}
