package com.hedvig.android.sample.design.showcase.textfield

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.LayoutWithoutPlacement
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Cart
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.sample.design.showcase.util.ShowcaseLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive

@Composable
internal fun ShowcaseTextField() {
  ShowcaseLayout {
    Column(verticalArrangement = Arrangement.spacedBy(80.dp)) {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.align(Alignment.Bottom)) {
          WithWidthOfTypeLabel {
            WithHeightOfTextField {
              HedvigText(
                text = "Interactive",
                style = HedvigTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterStart),
              )
            }
          }
        }
        InteractiveTextFieldWithAllSizes()
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.align(Alignment.CenterVertically)) {
          WithWidthOfTypeLabel {
            HedvigText(
              text = "Animation",
              style = HedvigTheme.typography.bodyMedium,
              modifier = Modifier.align(Alignment.CenterStart),
            )
          }
        }
        LabelAnimation()
      }
      for (type in ShowcaseTextFieldType.entries) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Box(modifier = Modifier.align(Alignment.CenterVertically)) {
            WithWidthOfTypeLabel {
              HedvigText(text = type.name, style = HedvigTheme.typography.bodyMedium)
            }
          }
          TextFieldTypeRowWithAllSizes(type)
        }
      }
    }
  }
}

@Composable
fun InteractiveTextFieldWithAllSizes(modifier: Modifier = Modifier) {
  Row(modifier, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
    for (size in HedvigTextFieldDefaults.TextFieldSize.entries) {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        HedvigText(
          text = size.name,
          style = HedvigTheme.typography.bodyMedium,
          modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        var text by remember { mutableStateOf("") }
        HedvigTextField(text, { text = it }, "Label", size)
      }
    }
  }
}

@Composable
private fun LabelAnimation(modifier: Modifier = Modifier) {
  Row(modifier, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
    for (size in HedvigTextFieldDefaults.TextFieldSize.entries) {
      val text by produceState("") {
        val delay: Long = 2000
        while (isActive) {
          value = "Input"
          delay(delay)
          value = ""
          delay(delay)
        }
      }
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HedvigTextField(text, {}, "Label", size)
        HedvigTextField(
          text = text,
          onValueChange = {},
          labelText = "Label",
          textFieldSize = size,
          leadingIcon = {
            IconButton({}) {
              Image(HedvigIcons.Cart, null)
            }
          },
          trailingIcon = {
            IconButton({}) {
              Image(HedvigIcons.Cart, null)
            }
          },
        )
      }
    }
  }
}

@Composable
private fun TextFieldTypeRowWithAllSizes(type: ShowcaseTextFieldType) {
  Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
    for (size in HedvigTextFieldDefaults.TextFieldSize.entries) {
      TextFieldWithAndWithoutInputColumn(type, size)
    }
  }
}

@Composable
private fun TextFieldWithAndWithoutInputColumn(
  type: ShowcaseTextFieldType,
  size: HedvigTextFieldDefaults.TextFieldSize,
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ShowcaseTextField("", type, size)
    ShowcaseTextField("Text Input", type, size)
  }
}

@Composable
private fun ShowcaseTextField(input: String, type: ShowcaseTextFieldType, size: HedvigTextFieldDefaults.TextFieldSize) {
  val inputValue = if (type == ShowcaseTextFieldType.ErrorPulsating || type == ShowcaseTextFieldType.TypePulsating) {
    val blinkingInput by produceState(input) {
      while (isActive) {
        val textWithAtLeastOneChar = input.takeIf { it.isNotEmpty() } ?: "A"
        value = textWithAtLeastOneChar
        delay(400 - 10)
        value = textWithAtLeastOneChar + " "
        delay(400 - 10)
      }
    }
    blinkingInput
  } else {
    input
  }
  HedvigTextField(
    text = inputValue,
    onValueChange = {},
    textFieldSize = size,
    labelText = "Label",
    errorState = when (type) {
      ShowcaseTextFieldType.Error -> HedvigTextFieldDefaults.ErrorState.Error
      ShowcaseTextFieldType.ErrorPulsating -> HedvigTextFieldDefaults.ErrorState.ErrorWithMessage(
        "Something went wrong",
      )

      else -> HedvigTextFieldDefaults.ErrorState.NoError
    },
    enabled = type == ShowcaseTextFieldType.Disabled,
    readOnly = type == ShowcaseTextFieldType.ReadOnly,
    interactionSource = if (type == ShowcaseTextFieldType.Hover) {
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
    } else if (type == ShowcaseTextFieldType.Focused) {
      remember {
        object : MutableInteractionSource {
          override val interactions: Flow<Interaction>
            get() = flowOf(FocusInteraction.Focus())

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
  )
}

private enum class ShowcaseTextFieldType {
  Resting,
  Focused,
  ReadOnly,
  Disabled,
  TypePulsating,
  Hover,
  Error,
  ErrorPulsating,
}

@Composable
private fun WithWidthOfTypeLabel(content: @Composable BoxScope.() -> Unit) {
  LayoutWithoutPlacement(
    sizeAdjustingContent = {
      HedvigText(
        text = ShowcaseTextFieldType.entries.map { it.name }.maxBy { it.length },
        style = HedvigTheme.typography.bodyMedium,
      )
    },
    content = content,
  )
}

@Composable
private fun WithHeightOfTextField(content: @Composable BoxScope.() -> Unit) {
  LayoutWithoutPlacement(
    sizeAdjustingContent = {
      HedvigTextField("", {}, "", HedvigTextFieldDefaults.TextFieldSize.Large, Modifier.requiredWidth(1.dp))
    },
    content = content,
  )
}

@Preview(
  name = "lightMode portrait",
  uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
  device = "spec:width=2200px,height=4200px,dpi=440",
)
@Composable
private fun PreviewShowcaseTextField() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ShowcaseTextField()
    }
  }
}
