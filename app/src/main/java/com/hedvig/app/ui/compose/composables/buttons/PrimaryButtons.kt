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
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.HedvigTheme

@Composable
fun LargeContainedTextButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    LargeContainedButton(
        modifier = modifier,
        content = {
            Text(text = text)
        },
        onClick = onClick
    )
}

@Composable
fun LargeContainedButton(
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
    name = "Contained Button (Large)",
    group = "Buttons",
)
@Composable
fun LargeContainedButtonPreview() {
    HedvigTheme {
        LargeContainedTextButton(text = "Contained Button (Large)") {}
    }
}

@ShowkaseComposable(skip = true)
@Preview(
    name = "Contained Button (Large) • dark",
    group = "Buttons",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun LargeContainedButtonPreviewDark() {
    HedvigTheme {
        LargeContainedTextButton(text = "Contained Button Dark (Large)") {}
    }
}
