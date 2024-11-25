package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hedvig.android.design.system.hedvig.CheckboxDefaults.CheckboxSize
import com.hedvig.android.design.system.hedvig.CheckboxDefaults.CheckboxSize.Medium
import com.hedvig.android.design.system.hedvig.CheckboxDefaults.CheckboxStyle
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.DialogDefaults.ButtonSize.BIG
import com.hedvig.android.design.system.hedvig.DialogDefaults.ButtonSize.SMALL
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle.Buttons
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle.NoButtons
import com.hedvig.android.design.system.hedvig.DialogDefaults.defaultButtonSize
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.ERROR
import com.hedvig.android.design.system.hedvig.LockedState.NotLocked
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionSize.Small
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle
import com.hedvig.android.design.system.hedvig.tokens.DialogTokens
import hedvig.resources.R

@Composable
fun ErrorDialog(
  title: String,
  message: String?,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  buttonText: String = stringResource(R.string.general_close_button),
  onButtonClick: (() -> Unit)? = null,
) {
  HedvigDialog(
    style = NoButtons,
    onDismissRequest = onDismiss,
    modifier = modifier,
  ) {
    EmptyState(
      text = title,
      description = message,
      iconStyle = ERROR,
      buttonStyle = EmptyStateButtonStyle.Button(
        buttonText = buttonText,
        onButtonClick = onButtonClick ?: onDismiss,
      ),
    )
  }
}

@Composable
fun HedvigAlertDialog(
  title: String,
  text: String?,
  onConfirmClick: () -> Unit,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  confirmButtonLabel: String = stringResource(R.string.GENERAL_YES),
  dismissButtonLabel: String = stringResource(R.string.GENERAL_NO),
  buttonSize: DialogDefaults.ButtonSize = defaultButtonSize,
) {
  HedvigDialog(
    style = Buttons(
      confirmButtonText = confirmButtonLabel,
      dismissButtonText = dismissButtonLabel,
      onDismissRequest = onDismissRequest,
      onConfirmButtonClick = {
        onConfirmClick()
        onDismissRequest()
      },
      buttonSize = buttonSize,
    ),
    onDismissRequest = onDismissRequest,
    modifier = modifier,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      HedvigText(
        text = title,
        textAlign = TextAlign.Center,
      )
      if (text != null) {
        HedvigText(
          text = text,
          textAlign = TextAlign.Center,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
    }
  }
}

@Composable
fun <T> SingleSelectDialog(
  title: String,
  optionsList: List<T>,
  onSelected: (T) -> Unit,
  getDisplayText: (T) -> String,
  getIsSelected: ((T) -> Boolean)?,
  getId: (T) -> String,
  getItemForId: (String) -> T,
  onDismissRequest: () -> Unit,
) {
  SingleSelectDialog(
    title = title,
    optionsList = optionsList.map {
      RadioOptionData(
        id = getId(it),
        optionText = getDisplayText(it),
        chosenState = if (getIsSelected?.invoke(it) ?: false) Chosen else NotChosen,
      )
    },
    onSelected = {
      onSelected(getItemForId(it.id))
    },
    onDismissRequest = onDismissRequest,
    radioOptionStyle = RadioOptionStyle.LeftAligned,
    radioOptionSize = Small,
  )
}

@Composable
fun SingleSelectDialog(
  title: String,
  optionsList: List<RadioOptionData>,
  onSelected: (RadioOptionData) -> Unit,
  onDismissRequest: () -> Unit,
  radioOptionStyle: RadioOptionStyle = RadioOptionStyle.LeftAligned,
  radioOptionSize: RadioOptionDefaults.RadioOptionSize = RadioOptionDefaults.RadioOptionSize.Medium,
) {
  CoreSelectDialog(
    title = title,
    optionsList = optionsList,
    onDismissRequest = onDismissRequest,
  ) { radioOptionData ->
    RadioOption(
      data = radioOptionData,
      radioOptionStyle = radioOptionStyle,
      radioOptionSize = radioOptionSize,
      groupLockedState = NotLocked,
      onOptionClick = {
        onSelected(radioOptionData)
        onDismissRequest()
      },
    )
  }
}


@Composable
fun <T> MultiSelectDialog(
  title: String,
  optionsList: List<T>,
  onSelected: (T) -> Unit,
  getDisplayText: (T) -> String,
  getIsSelected: ((T) -> Boolean)?,
  getId: (T) -> String,
  getItemForId: (String) -> T,
  onDismissRequest: () -> Unit,
) {
  MultiSelectDialog(
    title = title,
    optionsList = optionsList.map {
      RadioOptionData(
        id = getId(it),
        optionText = getDisplayText(it),
        chosenState = if (getIsSelected?.invoke(it) ?: false) Chosen else NotChosen,
      )
    },
    onSelected = { onSelected(getItemForId(it.id)) },
    onDismissRequest = onDismissRequest,
    checkboxStyle = CheckboxStyle.LeftAligned,
    checkboxSize = CheckboxSize.Small,
  )
}

@Composable
fun MultiSelectDialog(
  title: String,
  optionsList: List<RadioOptionData>,
  onSelected: (RadioOptionData) -> Unit,
  onDismissRequest: () -> Unit,
  checkboxStyle: CheckboxStyle = CheckboxStyle.Default,
  checkboxSize: CheckboxDefaults.CheckboxSize = Medium,
) {
  CoreSelectDialog(
    title = title,
    optionsList = optionsList,
    onDismissRequest = onDismissRequest,
  ) { radioOptionData ->
    Checkbox(
      data = radioOptionData,
      checkboxStyle = checkboxStyle,
      checkboxSize = checkboxSize,
      lockedState = NotLocked,
      onClick = { onSelected(radioOptionData) },
    )
  }
}

@Composable
fun HedvigDialog(
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  applyDefaultPadding: Boolean = true,
  dialogProperties: DialogProperties = DialogDefaults.defaultProperties,
  style: DialogStyle = DialogDefaults.defaultDialogStyle,
  content: @Composable () -> Unit,
) {
  Dialog(
    onDismissRequest = onDismissRequest,
    properties = dialogProperties,
  ) {
    Surface(
      shape = DialogDefaults.shape,
      color = DialogDefaults.containerColor,
      modifier = modifier.then(
        if (dialogProperties.usePlatformDefaultWidth) {
          Modifier
        } else {
          Modifier.padding(horizontal = 16.dp)
        },
      ),
    ) {
      val padding = if (applyDefaultPadding) DialogDefaults.padding(style) else PaddingValues()
      Column(
        Modifier.padding(padding),
      ) {
        when (style) {
          is Buttons -> {
            content()
            Spacer(Modifier.height(40.dp))
            when (style.buttonSize) {
              BIG -> {
                BigVerticalButtons(
                  onDismissRequest = style.onDismissRequest,
                  dismissButtonText = style.dismissButtonText,
                  onConfirmButtonClick = style.onConfirmButtonClick,
                  confirmButtonText = style.confirmButtonText,
                )
              }

              SMALL -> {
                SmallHorizontalButtons(
                  onDismissRequest = style.onDismissRequest,
                  dismissButtonText = style.dismissButtonText,
                  onConfirmButtonClick = style.onConfirmButtonClick,
                  confirmButtonText = style.confirmButtonText,
                )
              }
            }
          }

          NoButtons -> content()
        }
      }
    }
  }
}

object DialogDefaults {
  internal val defaultButtonSize = SMALL
  internal val defaultDialogStyle = NoButtons
  internal val defaultProperties = DialogProperties()

  internal val shape: Shape
    @Composable
    @ReadOnlyComposable
    get() = DialogTokens.ContainerShape.value

  internal val containerColor: Color
    @Composable
    get() = with(HedvigTheme.colorScheme) {
      remember(this) {
        fromToken(DialogTokens.ContainerColor)
      }
    }

  internal fun padding(style: DialogStyle): PaddingValues {
    return when (style) {
      is Buttons -> {
        when (style.buttonSize) {
          BIG -> DialogTokens.BigButtonsPadding
          SMALL -> DialogTokens.SmallButtonsPadding
        }
      }

      NoButtons -> DialogTokens.NoButtonsPadding
    }
  }

  sealed class DialogStyle {
    data class Buttons(
      val onDismissRequest: () -> Unit,
      val dismissButtonText: String,
      val onConfirmButtonClick: () -> Unit,
      val confirmButtonText: String,
      val buttonSize: ButtonSize = defaultButtonSize,
    ) : DialogStyle()

    data object NoButtons : DialogStyle()
  }

  enum class ButtonSize {
    BIG,
    SMALL,
  }
}

@Composable
private fun CoreSelectDialog(
  title: String,
  optionsList: List<RadioOptionData>,
  onDismissRequest: () -> Unit,
  itemContent: @Composable (RadioOptionData) -> Unit,
) {
  HedvigDialog(
    onDismissRequest = { onDismissRequest.invoke() },
    applyDefaultPadding = false,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Spacer(Modifier.height(20.dp))
      HedvigText(title, style = HedvigTheme.typography.bodySmall, textAlign = TextAlign.Center)
      Spacer(Modifier.height(8.dp))
      val state = rememberLazyListState()
      val lazyColumnContentPadding = 16.dp
      val density = LocalDensity.current
      val drawTopBorder by remember {
        derivedStateOf {
          state.firstVisibleItemIndex != 0 ||
            state.firstVisibleItemScrollOffset > with(density) { lazyColumnContentPadding.roundToPx() }
        }
      }
      val borderColor = HedvigTheme.colorScheme.borderSecondary
      LazyColumn(
        state = state,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(lazyColumnContentPadding),
        modifier = Modifier.drawWithContent {
          drawContent()
          if (drawTopBorder) {
            drawLine(borderColor, Offset.Zero, Offset(size.width, 0f))
          }
        },
      ) {
        items(
          items = optionsList,
          key = { data -> data.id },
        ) { radioOptionData ->
          itemContent(radioOptionData)
        }
      }
    }
  }
}

@Composable
private fun SmallHorizontalButtons(
  onDismissRequest: () -> Unit,
  dismissButtonText: String,
  onConfirmButtonClick: () -> Unit,
  confirmButtonText: String,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
  ) {
    HedvigButton(
      modifier = Modifier.weight(1f),
      onClick = onDismissRequest,
      text = dismissButtonText,
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      buttonSize = ButtonDefaults.ButtonSize.Medium,
    )
    Spacer(Modifier.width(8.dp))
    HedvigButton(
      modifier = Modifier.weight(1f),
      onClick = onConfirmButtonClick,
      text = confirmButtonText,
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Primary,
      buttonSize = ButtonDefaults.ButtonSize.Medium,
    )
  }
}

@Composable
private fun BigVerticalButtons(
  onDismissRequest: () -> Unit,
  dismissButtonText: String,
  onConfirmButtonClick: () -> Unit,
  confirmButtonText: String,
) {
  Column {
    HedvigButton(
      modifier = Modifier.fillMaxWidth(),
      onClick = onConfirmButtonClick,
      text = confirmButtonText,
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Primary,
      buttonSize = ButtonDefaults.ButtonSize.Large,
    )
    Spacer(Modifier.height(8.dp))
    HedvigButton(
      modifier = Modifier.fillMaxWidth(),
      onClick = onDismissRequest,
      text = dismissButtonText,
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      buttonSize = ButtonDefaults.ButtonSize.Large,
    )
  }
}
