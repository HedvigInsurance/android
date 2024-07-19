package com.hedvig.android.feature.home.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.infoContainer
import com.hedvig.android.core.designsystem.material3.onInfoContainer
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.home.home.ChatTooltipMessage.GotQuestions
import com.hedvig.android.feature.home.home.ChatTooltipMessage.NewMessage
import hedvig.resources.R
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

@Composable
internal fun ChatTooltip(
  chatTooltipMessage: ChatTooltipMessage,
  showTooltip: Boolean,
  tooltipShown: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var transientShowTooltip by remember { mutableStateOf(false) }
  LaunchedEffect(showTooltip) {
    if (!showTooltip) return@LaunchedEffect
    delay(0.5.seconds)
    transientShowTooltip = showTooltip
    tooltipShown()
    delay(5.seconds)
    transientShowTooltip = false
  }
  InnerChatTooltip(
    chatTooltipMessage = chatTooltipMessage,
    show = transientShowTooltip,
    onClick = {
      transientShowTooltip = false
      tooltipShown()
    },
    modifier = modifier,
  )
}

internal sealed interface ChatTooltipMessage {
  data object GotQuestions : ChatTooltipMessage

  data object NewMessage : ChatTooltipMessage
}

@Composable
private fun InnerChatTooltip(
  chatTooltipMessage: ChatTooltipMessage,
  show: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier
      .size(width = 40.dp, height = 0.dp)
      .wrapContentHeight(Alignment.Top, true)
      .wrapContentWidth(Alignment.End, true)
      .offset(y = -arrowHeightDp),
  ) {
    Crossfade(show, label = "chat tooltip") { crossfadeShow ->
      if (crossfadeShow) {
        val squircleMedium = MaterialTheme.shapes.squircleMedium
        Surface(
          onClick = onClick,
          color = MaterialTheme.colorScheme.infoContainer,
          contentColor = MaterialTheme.colorScheme.onInfoContainer,
          shape = remember(squircleMedium) { squircleMedium.withTopRightPointingArrow() },
          modifier = Modifier.widthIn(max = 200.dp),
        ) {
          Text(
            text = when (chatTooltipMessage) {
              GotQuestions -> stringResource(R.string.home_tab_chat_hint_text)
              NewMessage -> stringResource(R.string.CHAT_NEW_MESSAGE)
            },
            modifier = Modifier
              .padding(horizontal = 12.dp, vertical = 7.dp)
              .padding(top = arrowHeightDp),
          )
        }
      }
    }
  }
}

private fun Shape.withTopRightPointingArrow(): Shape {
  return object : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
      val iconWidth: Float = with(density) { 40.dp.toPx() }
      val arrowWidth = with(density) { 15.dp.toPx() }
      val arrowHeight = with(density) { arrowHeightDp.toPx() }
      val squircleOutline = this@withTopRightPointingArrow.createOutline(
        size.copy(height = size.height - arrowHeight),
        layoutDirection,
        density,
      )
      val squirclePath: Path = (squircleOutline as Outline.Generic).path
      val arrowPath: Path = Path().apply {
        relativeLineTo(-(arrowWidth / 2), 0f)
        relativeLineTo(arrowWidth / 2, -arrowHeight)
        relativeLineTo(arrowWidth / 2, arrowHeight)
        relativeLineTo(0f, 20f)
        close()
      }
      return Outline.Generic(
        Path().apply {
          addPath(path = squirclePath, offset = Offset(0f, arrowHeight))
          addPath(path = arrowPath, offset = Offset(size.width - (iconWidth / 2), arrowHeight))
        },
      )
    }
  }
}

private val arrowHeightDp = 8.dp

@HedvigPreview
@Composable
private fun PreviewChatTooltip() {
  HedvigTheme {
    Surface(
      color = MaterialTheme.colorScheme.background,
      modifier = Modifier
        .width(300.dp)
        .height(150.dp),
    ) {
      Box(
        contentAlignment = Alignment.TopEnd,
        modifier = Modifier.padding(40.dp),
      ) {
        InnerChatTooltip(ChatTooltipMessage.NewMessage, true, {})
      }
    }
  }
}
