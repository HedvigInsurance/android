package com.hedvig.app.ui.compose.designsystem

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
import com.hedvig.app.ui.compose.HedvigTheme

@Composable
fun LargeContainedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().then(modifier),
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
        LargeContainedButton(
            onClick = {},
        ) {
            Text("Contained Button (Large)")
        }
    }
}
