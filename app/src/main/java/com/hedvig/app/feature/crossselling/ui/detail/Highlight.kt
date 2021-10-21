package com.hedvig.app.feature.crossselling.ui.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.HedvigTheme

@Composable
fun Highlight(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.ic_checkmark),
            contentDescription = null,
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                text = description,
                style = MaterialTheme.typography.body2,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HighlightPreview() {
    HedvigTheme {
        Highlight(
            title = "Covers dental injuries",
            description = "Up to 100 000 SEK per damage."
        )
    }
}
