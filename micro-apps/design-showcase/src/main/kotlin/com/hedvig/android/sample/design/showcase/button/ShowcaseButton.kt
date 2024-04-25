package com.hedvig.android.sample.design.showcase.button

import android.content.res.Configuration
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.LayoutWithoutPlacement
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.sample.design.showcase.util.dashedBorder
import com.hedvig.android.sample.design.showcase.util.freeScroll
import com.hedvig.android.sample.design.showcase.util.rememberFreeScrollState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun ShowcaseButton() {
  Column(
    verticalArrangement = Arrangement.spacedBy(80.dp),
    modifier = Modifier
      .freeScroll(rememberFreeScrollState())
      .windowInsetsPadding(WindowInsets.safeDrawing)
      .padding(16.dp)
      .dashedBorder(Color(0xFF9747FF), HedvigTheme.shapes.cornerXXLarge)
      .padding(8.dp),
  ) {
    for ((index, size) in ButtonDefaults.ButtonSize.entries.withIndex()) {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.align(Alignment.CenterVertically)) {
          LayoutWithoutPlacement(
            sizeAdjustingContent = {
              HedvigText(
                text = ButtonDefaults.ButtonSize.entries.map { it.name }.maxBy { it.length },
                style = HedvigTheme.typography.bodyMedium,
              )
            },
          ) {
            HedvigText(text = size.name, style = HedvigTheme.typography.bodyMedium)
          }
        }
        ButtonSizesRow(size, index == 0)
      }
    }
  }
}

@Composable
private fun ButtonSizesRow(buttonSize: ButtonDefaults.ButtonSize, isFirst: Boolean) {
  Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
    for (type in ShowcaseButtonType.entries) {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (isFirst) {
          HedvigText(
            text = type.name,
            style = HedvigTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally),
          )
        }
        ButtonStylesColumn(type, buttonSize)
      }
    }
  }
}

@Composable
private fun ButtonStylesColumn(type: ShowcaseButtonType, buttonSize: ButtonDefaults.ButtonSize) {
  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    for (style in ButtonDefaults.ButtonStyle.entries) {
      ShowcaseButton(style, type, buttonSize)
    }
  }
}

@Composable
private fun ShowcaseButton(
  buttonStyle: ButtonDefaults.ButtonStyle,
  showcaseButtonType: ShowcaseButtonType,
  buttonSize: ButtonDefaults.ButtonSize,
  modifier: Modifier = Modifier,
) {
  HedvigButton(
    text = "Button label",
    onClick = {},
    enabled = showcaseButtonType != ShowcaseButtonType.Disabled,
    buttonStyle = buttonStyle,
    buttonSize = buttonSize,
    isLoading = showcaseButtonType == ShowcaseButtonType.Loading,
    interactionSource = if (showcaseButtonType == ShowcaseButtonType.Hover) {
      remember {
        object : MutableInteractionSource {
          override val interactions: Flow<Interaction>
            get() = flowOf(HoverInteraction.Enter())

          override suspend fun emit(interaction: Interaction) {
          }

          override fun tryEmit(interaction: Interaction): Boolean {
            return false
          }
        }
      }
    } else {
      null
    },
    modifier = modifier,
  )
}

private enum class ShowcaseButtonType {
  Resting,
  Hover,
  Disabled,
  Loading,
}

@Preview(
  name = "lightMode portrait",
  uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
  device = "spec:width=2200px,height=4200px,dpi=440",
)
// @Preview(
//  name = "nightMode portrait",
//  uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
// )
@Composable
private fun PreviewShowcaseButton() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ShowcaseButton()
    }
  }
}
