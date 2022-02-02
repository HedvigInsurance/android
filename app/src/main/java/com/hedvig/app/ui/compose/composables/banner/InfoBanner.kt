package com.hedvig.app.ui.compose.composables.banner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.ui.compose.theme.hedvigBlack12percent

@Composable
fun InfoBanner(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSystemInDarkTheme()) {
        colorResource(R.color.lavender_400)
    } else {
        colorResource(R.color.lavender_200)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = backgroundColor,
        border = BorderStroke(1.dp, hedvigBlack12percent)
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = text,
            color = contentColorFor(backgroundColor).copy(alpha = 0.5f),
            style = MaterialTheme.typography.subtitle2,
        )
    }
}

@Preview
@Composable
fun InfoBannerPreview() {
    HedvigTheme {
        InfoBanner(onClick = { }, text = "Test info banner text")
    }
}
