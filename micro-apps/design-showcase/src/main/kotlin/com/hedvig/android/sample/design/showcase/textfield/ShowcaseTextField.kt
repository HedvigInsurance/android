package com.hedvig.android.sample.design.showcase.textfield

import android.content.res.Configuration
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.LayoutWithoutPlacement
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TextFieldDefaults
import com.hedvig.android.sample.design.showcase.util.ShowcaseLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive

@Composable
internal fun ShowcaseTextField() {
  ShowcaseLayout {
    Column(verticalArrangement = Arrangement.spacedBy(80.dp)) {
      for ((index, type) in ShowcaseTextFieldType.entries.withIndex()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Box(modifier = Modifier.align(Alignment.CenterVertically)) {
            LayoutWithoutPlacement(
              sizeAdjustingContent = {
                HedvigText(
                  text = ShowcaseTextFieldType.entries.map { it.name }.maxBy { it.length },
                  style = HedvigTheme.typography.bodyMedium,
                )
              },
            ) {
              HedvigText(text = type.name, style = HedvigTheme.typography.bodyMedium)
            }
          }
          TextFieldTypeRowWithAllSizes(type, index == 0)
        }
      }
    }
  }
}

@Composable
private fun TextFieldTypeRowWithAllSizes(type: ShowcaseTextFieldType, isFirst: Boolean) {
  Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
    for (size in TextFieldDefaults.TextFieldSize.entries) {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (isFirst) {
          HedvigText(
            text = type.name,
            style = HedvigTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally),
          )
        }
        TextFieldWithAndWithoutInputColumn(type, size)
      }
    }
  }
}

@Composable
private fun TextFieldWithAndWithoutInputColumn(type: ShowcaseTextFieldType, size: TextFieldDefaults.TextFieldSize) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ShowcaseTextField("", type, size)
    ShowcaseTextField("Text Input", type, size)
  }
}

@Composable
private fun ShowcaseTextField(input: String, type: ShowcaseTextFieldType, size: TextFieldDefaults.TextFieldSize) {
  val inputValue = if (type == ShowcaseTextFieldType.ErrorPulsating || type == ShowcaseTextFieldType.TypePulsating) {
    val blinkingInput by produceState(input) {
      while (isActive) {
        value = input
        delay(200)
        value = input + " "
        delay(200)
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
    errorMessage = "Something went wrong".takeIf {
      type == ShowcaseTextFieldType.Error || type == ShowcaseTextFieldType.ErrorPulsating
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
      remember { MutableInteractionSource() }
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
