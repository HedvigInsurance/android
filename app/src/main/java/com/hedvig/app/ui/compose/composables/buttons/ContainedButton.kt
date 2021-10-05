package com.hedvig.app.ui.compose.composables.buttons

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.HedvigTheme

@Composable
fun LargeContainedTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LargeContainedButton(
        content = {
            Text(text = text)
        },
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
fun LargeContainedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        backgroundColor = if (MaterialTheme.colors.isLight) {
            MaterialTheme.colors.primary
        } else {
            MaterialTheme.colors.secondary
        },
        contentColor = MaterialTheme.colors.onPrimary,
    ),
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(dimensionResource(R.dimen.base_margin_double)),
        colors = colors,
        content = content,
    )
}

@Preview(
    name = "Contained Button (Large)",
    group = "Buttons",
)
@Composable
fun LargeContainedButtonPreview() {
    HedvigTheme {
        LargeContainedTextButton(text = "Contained Button (Large)", onClick = {})
    }
}
