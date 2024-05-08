package com.hedvig.android.sample.design.showcase.textfield

import android.content.res.Configuration
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.LayoutWithoutPlacement
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Cart
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.sample.design.showcase.util.ShowcaseLayout
import kotlin.math.max
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive

val showcaseJustAnimation = false

@Composable
internal fun ShowcaseTextField() {
  if (showcaseJustAnimation) {
    LabelAnimation(Modifier.safeContentPadding())
    return
  }
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
        HedvigTextField(text, { text = it }, "Label", size, Modifier.widthIn(max = 250.dp))
      }
    }
  }
}

private fun Modifier.clearFocusOnTap(): Modifier = this.composed {
  val focusManager = LocalFocusManager.current
  Modifier.pointerInput(Unit) {
    detectTapGestures(
      onTap = { focusManager.clearFocus() },
    )
  }
}

@Composable
private fun LabelAnimation(modifier: Modifier = Modifier) {
  Row(
    modifier
      .fillMaxSize()
      .clearFocusOnTap(),
    horizontalArrangement = Arrangement.spacedBy(20.dp),
  ) {
//    for (size in HedvigTextFieldDefaults.TextFieldSize.entries) {
    val size = HedvigTextFieldDefaults.TextFieldSize.Large
    val interactionSource = remember {
      MutableInteractionSource()
    }
//    var text by remember { mutableStateOf("") }
    val text by produceState("") {
      val delay: Long = 3000
      while (isActive) {
        value = "Input"
        delay(delay)
        value = ""
        delay(delay)
        val interaction = FocusInteraction.Focus()
        interactionSource.emit(interaction)
        delay(delay)
        interactionSource.emit(FocusInteraction.Unfocus(interaction))
      }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      HedvigTextField(
        text.takeIf { it != "-1" }.orEmpty(),
        { /*text = it*/ },
        "Label",
        size,
        Modifier.requiredWidth(150.dp),
        interactionSource = interactionSource,
      )
      HedvigTextField(
        text = text,
        onValueChange = { /*text = it*/ },
        labelText = "Label",
        textFieldSize = size,
        leadingContent = {
          IconButton({}, Modifier.size(32.dp)) {
            Icon(HedvigIcons.Cart, null)
          }
        },
        errorState = HedvigTextFieldDefaults.ErrorState.Error.WithoutMessage,
      )
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
  var maxWidth by remember { mutableIntStateOf(0) }
  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.then(
      if (maxWidth != 0) {
        with(LocalDensity.current) {
          Modifier.requiredWidth(maxWidth.toDp())
        }
      } else {
        Modifier
      },
    ),
  ) {
    if (maxWidth != 0) {
      ShowcaseTextField(
        "",
        type,
        size,
        Modifier.fillMaxWidth(),
      )
    }
    ShowcaseTextField(
      "Text Input",
      type,
      size,
      Modifier.onPlaced { maxWidth = max(maxWidth, it.size.width) },
    )
  }
}

@Composable
private fun ShowcaseTextField(
  input: String,
  type: ShowcaseTextFieldType,
  size: HedvigTextFieldDefaults.TextFieldSize,
  modifier: Modifier = Modifier,
) {
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
    modifier = modifier,
    errorState = when (type) {
      ShowcaseTextFieldType.ErrorMessage -> HedvigTextFieldDefaults.ErrorState.Error.WithMessage(
        "Something went wrong",
      )

      ShowcaseTextFieldType.ErrorPulsating -> HedvigTextFieldDefaults.ErrorState.Error.WithoutMessage
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
  Hover,
  TypePulsating,
  ErrorPulsating,
  ErrorMessage,
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
