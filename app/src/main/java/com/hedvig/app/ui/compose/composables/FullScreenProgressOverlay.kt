package com.hedvig.app.ui.compose.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.HedvigTheme

@OptIn(ExperimentalUnitApi::class)
@Composable
fun FullScreenProgressOverlay() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorResource(id = R.color.progress_overlay)
    ) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "H", fontSize = TextUnit(26f, TextUnitType.Sp))
            CircularProgressIndicator(strokeWidth = 2.dp)
        }
    }
}

@Preview
@Composable
fun FullScreenProgressOverlayPreview() {
    HedvigTheme {
        FullScreenProgressOverlay()
    }
}
