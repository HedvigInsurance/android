package com.hedvig.app.ui.compose.composables.appbar

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TopAppBarWithBack(
    onClick: () -> Unit,
    title: String,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        onClick,
        title,
        TopAppBarActionType.BACK,
        modifier
    )
}

@Composable
fun TopAppBarWithClose(
    onClick: () -> Unit,
    title: String,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        onClick,
        title,
        TopAppBarActionType.CLOSE,
        modifier
    )
}

private enum class TopAppBarActionType {
    BACK, CLOSE
}

@Composable
private fun TopAppBar(
    onClick: () -> Unit,
    title: String,
    actionType: TopAppBarActionType,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.h6
            )
        },
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
        backgroundColor = MaterialTheme.colors.background,
        elevation = 0.dp,
    )
}
