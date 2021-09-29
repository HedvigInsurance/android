package com.hedvig.app.ui.compose.composables.buttons

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.HedvigTheme

@Composable
fun LageOutlinedTextButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    LageOutlinedButton(
        modifier = modifier,
        content = {
            Text(text = text)
        },
        onClick = onClick
    )
}

@Composable
fun LageOutlinedButton(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        border = ButtonDefaults.outlinedBorder.copy(brush = SolidColor(MaterialTheme.colors.primary)),
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(dimensionResource(R.dimen.base_margin_double)),
        content = content,
    )
}

@Preview(
    name = "Outlined Button (Large)",
    group = "Buttons",
)
@Composable
fun LageOutlinedButtonPreview() {
    HedvigTheme {
        LageOutlinedTextButton(text = "Outlined Button (Large)") {}
    }
}
