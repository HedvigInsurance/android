package com.hedvig.app.ui.compose.composables.appbar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.ui.TopAppBar

@Composable
fun TopAppBarWithBack(
    onClick: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.background,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    TopAppBar(
        onClick,
        title,
        TopAppBarActionType.BACK,
        modifier,
        backgroundColor,
        contentPadding,
    )
}

@Composable
fun TopAppBarWithClose(
    onClick: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.background,
    contentPadding: PaddingValues,
) {
    TopAppBar(
        onClick,
        title,
        TopAppBarActionType.CLOSE,
        modifier,
        backgroundColor,
        contentPadding
    )
}

private enum class TopAppBarActionType {
    BACK, CLOSE
}

@Composable
private inline fun TopAppBar(
    crossinline onClick: () -> Unit,
    title: String,
    actionType: TopAppBarActionType,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.background,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.h6
            )
        },
        contentPadding = contentPadding,
        navigationIcon = {
            IconButton(
                onClick = { onClick() },
                content = {
                    Icon(
                        imageVector = when (actionType) {
                            TopAppBarActionType.BACK -> Icons.Filled.ArrowBack
                            TopAppBarActionType.CLOSE -> Icons.Filled.Close
                        },
                        contentDescription = null
                    )
                }
            )
        },
        backgroundColor = backgroundColor,
        elevation = 0.dp,
    )
}
