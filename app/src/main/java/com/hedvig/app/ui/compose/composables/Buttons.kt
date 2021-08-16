package com.hedvig.app.ui.compose.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hedvig.app.ui.compose.theme.HedvigTypography

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
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 15.dp),
        elevation = ButtonDefaults.elevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20),
        onClick = onClick,
        content = content
    )
}

@Composable
fun SecondaryTextButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    SecondaryButton(
        modifier = modifier,
        content = {
            Text(
                text = text,
                style = MaterialTheme.typography.button
            )
        },
        onClick = onClick
    )
}

@Composable
fun SecondaryButton(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20),
        border = BorderStroke(width = 1.dp, color = Color.DarkGray),
        contentPadding = PaddingValues(vertical = 15.dp),
        onClick = onClick,
        content = content
    )
}
