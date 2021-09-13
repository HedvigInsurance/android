package com.hedvig.app.ui.compose.designsystem

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.ui.compose.HedvigTheme

@Composable
fun LargeOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.border(
            1.dp,
            color = MaterialTheme.colors.primary,
            shape = MaterialTheme.shapes.large
        ),
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(dimensionResource(R.dimen.base_margin_double)),
        content = content,
    )
}

@Preview
@Composable
private fun LargeOutlinedButtonPreview() {
    HedvigTheme {
        LargeOutlinedButton(
            onClick = {},
            content = { Text("Outlined Button (Large)") },
        )
    }
}
