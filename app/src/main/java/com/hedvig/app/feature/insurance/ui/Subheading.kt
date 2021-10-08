package com.hedvig.app.feature.insurance.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.ui.compose.theme.HedvigTheme

@Composable
fun Subheading(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.h6,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                top = 48.dp,
                end = 16.dp,
                bottom = 8.dp,
            ),
    )
}

@Preview(
    name = "Subheading",
    group = "Insurance Tab",
    showBackground = true,
)
@Composable
fun SubheadingPreview() {
    HedvigTheme {
        Subheading("Add more coverage")
    }
}
