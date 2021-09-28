package com.hedvig.app.ui.compose.composables.buttons

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.HedvigTheme

@Composable
fun PrimaryTextButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    PrimaryButton(
        modifier = modifier,
        content = {
            Text(
                text = text,
                style = MaterialTheme.typography.button,
                color = MaterialTheme.colors.onSecondary
            )
        },
        onClick = onClick
    )
}

@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(dimensionResource(R.dimen.base_margin_double)),
        content = content,
    )
}

@Preview(
    name = "PrimaryButton",
    group = "Buttons",
)
@Composable
fun PrimaryButtonPreview() {
    HedvigTheme {
        PrimaryTextButton(text = "Contained Button (Large)") {}
    }
}

@Preview(
    name = "PrimaryButton • dark",
    group = "Buttons",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun PrimaryButtonPreviewDark() {
    HedvigTheme {
        PrimaryTextButton(text = "Contained Button Dark (Large)") {}
    }
}
