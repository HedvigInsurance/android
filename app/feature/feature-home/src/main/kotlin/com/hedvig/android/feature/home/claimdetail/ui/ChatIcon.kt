package com.hedvig.android.feature.home.claimdetail.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.colored.hedvig.Chat

@Composable
internal fun ChatIcon(
  onClick: () -> Unit,
  contentDescription: String?,
  showRedDot: Boolean = false,
) {
  val paddingAroundRedDot = 2
  Layout(
    modifier = Modifier
      .size(40.dp)
      .clip(MaterialTheme.shapes.squircleMedium)
      .clickable { onClick() },
    content = {
      Image(
        imageVector = Icons.Hedvig.Chat,
        contentDescription = contentDescription,
        modifier = Modifier
          .layoutId("chatImage")
      )
      if (showRedDot) {
        Box(
          modifier = Modifier
            .layoutId("redDot")
            .size(12.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingAroundRedDot.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.error),
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

@HedvigPreview
@Composable
private fun PreviewChatIcon() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      Column {
        ChatIcon({}, null)
        ChatIcon({}, null, true)
      }
    }
  }
}
