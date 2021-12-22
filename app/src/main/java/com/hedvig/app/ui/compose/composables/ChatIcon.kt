package com.hedvig.app.ui.compose.composables

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.HedvigTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChatIcon(
    onClick: () -> Unit,
    contentDescription: String?,
    showRedDot: Boolean = true,
) {
    Surface(
        color = MaterialTheme.colors.background,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.size(40.dp),
        onClick = onClick
    ) {
        val paddingAroundRedDot = 2
        Layout(
            modifier = Modifier.size(40.dp),
            content = {
                Image(
                    painterResource(R.drawable.ic_chat_on_background),
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .layoutId("chatImage")
                        .size(24.dp)
                )
                if (showRedDot) {
                    Box(
                        modifier = Modifier
                            .layoutId("redDot")
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colors.background)
                            .padding(paddingAroundRedDot.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colors.error),
                        content = {},
                    )
                }
            },
        ) { measurables, initialConstraints ->
            val constraints = initialConstraints.copy(minWidth = 0, minHeight = 0)
            val image = measurables.first { it.layoutId == "chatImage" }.measure(constraints)
            val redDot = measurables.firstOrNull { it.layoutId == "redDot" }?.measure(constraints)

            val maxWidth = constraints.maxWidth
            val maxHeight = constraints.maxHeight

            layout(maxWidth, maxHeight) {
                val imageX = (maxWidth - image.width) / 2
                val imageY = (maxHeight - image.height) / 2
                image.place(
                    x = imageX,
                    y = imageY,
                )
                redDot?.place(
                    x = imageX + image.width - redDot.width + paddingAroundRedDot.dp.roundToPx(),
                    y = imageY - paddingAroundRedDot.dp.roundToPx(),
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ChatIconPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.surface,
        ) {
            Column() {
                ChatIcon({}, null,)
                ChatIcon({}, null, false)
            }
        }
    }
}
