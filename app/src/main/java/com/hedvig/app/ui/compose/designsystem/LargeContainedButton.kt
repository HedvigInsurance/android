package com.hedvig.app.ui.compose.designsystem

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
fun LargeContainedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
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
    name = "LargeContainedButton",
    group = "Design System",
)
@Composable
fun LargeContainedButtonPreview() {
    HedvigTheme {
        LargeContainedButton({}) {
            Text("Contained Button (Large)")
        }
    }
}

@ShowkaseComposable(skip = true)
@Preview(
    name = "LargeContainedButton • dark",
    group = "Design System",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun LargeContainedButtonPreviewDark() {
    HedvigTheme {
        LargeContainedButton({}) {
            Text("Contained Button (Large)")
        }
    }
}
