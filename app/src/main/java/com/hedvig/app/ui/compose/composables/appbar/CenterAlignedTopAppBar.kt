package com.hedvig.app.ui.compose.composables.appbar

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.app.ui.compose.theme.HedvigTheme

/**
 * Center aligned title implementation of [androidx.compose.material.TopAppBar] while also allowing for contentPadding
 * as [com.google.accompanist.insets.ui.TopAppBar] does.
 * Currently doesn't wrap when reaching the length to go under the close button. A [androidx.compose.ui.layout.Layout]
 * can be used in the future for that if needed.
 */
@Composable
fun CenterAlignedTopAppBar(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    elevation: Dp = 0.dp,
) {
    Surface(
        color = backgroundColor,
        contentColor = contentColor,
        elevation = elevation,
        modifier = modifier
    ) {
        TopAppBar(
            backgroundColor = Color.Transparent,
            contentColor = contentColor,
            elevation = 0.dp,
            contentPadding = contentPadding,
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .width(72.dp - AppBarHorizontalPadding)
                        .padding(start = AppBarHorizontalPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(
                        LocalContentAlpha provides ContentAlpha.high,
                        content = {
                            IconButton(onClick) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = null,
                                )
                            }
                        }
                    )
                }
                ProvideTextStyle(value = MaterialTheme.typography.h6) {
                    CompositionLocalProvider(
                        LocalContentAlpha provides ContentAlpha.high,
                        content = {
                            Text(
                                text = title,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    )
                }
            }
        }
    }
}

private val AppBarHorizontalPadding = 4.dp

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CenterAlignedTopAppBarPreview() {
    HedvigTheme {
        Surface(color = MaterialTheme.colors.background) {
            Column {
                CenterAlignedTopAppBar("Title", {}, backgroundColor = MaterialTheme.colors.surface)
            }
        }
    }
}
