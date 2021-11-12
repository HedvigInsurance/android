package com.hedvig.app.feature.claimstatus.ui.composables

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.HedvigTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChatIcon(openChat: () -> Unit) {
    Surface(
        color = MaterialTheme.colors.background,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(40.dp),
        onClick = { openChat() }

    ) {
        Layout(
            modifier = Modifier.size(40.dp),
            content = {
                Image(
                    painterResource(R.drawable.ic_chat_on_background),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.background)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDD2727))
                ) {}
            },
        ) { measurables, constraints ->
            val image = measurables[0].measure(constraints.copy(minWidth = 0, minHeight = 0))
            val circle = measurables[1].measure(constraints.copy(minWidth = 0, minHeight = 0))

            val totalWidth = constraints.maxWidth
            val totalHeight = constraints.maxHeight

            layout(totalWidth, totalHeight) {
                val imageX = (totalWidth - image.width) / 2
                val imageY = (totalHeight - image.height) / 2
                image.place(
                    x = imageX,
                    y = imageY,
                )
                circle.place(
                    x = imageX + image.width - circle.width + 2.dp.roundToPx(),
                    y = imageY - 2.dp.roundToPx(),
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
            ChatIcon {}
        }
    }
}
