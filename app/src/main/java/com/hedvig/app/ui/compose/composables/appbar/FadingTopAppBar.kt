package com.hedvig.app.ui.compose.composables.appbar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.ui.TopAppBar

@Composable
fun FadingTopAppBar(
    backgroundAlpha: Float,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    title: @Composable () -> Unit = {},
    navigationIcon: @Composable () -> Unit,
    backgroundColor: Color = MaterialTheme.colors.surface,
) {
    TopAppBar(
        title = title,
        modifier = modifier,
        contentPadding = contentPadding,
        navigationIcon = navigationIcon,
        backgroundColor = backgroundColor.copy(alpha = backgroundAlpha),
        elevation = 0.dp
    )
}
