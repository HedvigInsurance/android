package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
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
import kotlin.collections.listOf

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
      modifier = Modifier.fillMaxWidth(),
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
    optionsList = optionsList,
    onDismissRequest = onDismissRequest,
    style = DialogStyle.TitleNoButtons(title = title),
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
  buttonText: String? = null,
) {
  MultiSelectDialog(
    title = title,
    buttonText = buttonText,
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
  buttonText: String? = null,
  checkboxStyle: CheckboxStyle = CheckboxStyle.Default,
  checkboxSize: CheckboxSize = Medium,
) {
  CoreSelectDialog(
    optionsList = optionsList,
    onDismissRequest = onDismissRequest,
    style = DialogStyle.TitlePlusButton(
      title = title,
      onButtonClick = onDismissRequest,
      buttonText = buttonText ?: stringResource(R.string.general_close_button),
    ),
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

/**
 * [contentPadding] is used if a custom content padding needs to be applied. When [null] is passed, the default DS
 * paddings are used instead
 */
@Composable
fun HedvigDialog(
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  dialogProperties: DialogProperties = DialogDefaults.defaultProperties,
  style: DialogStyle = DialogDefaults.defaultDialogStyle,
  contentPadding: PaddingValues? = null,
  content: @Composable () -> Unit,
) {
  Dialog(
    onDismissRequest = onDismissRequest,
    properties = dialogProperties,
  ) {
    HedvigDialogContent(
      style = style,
      contentPadding = contentPadding,
      modifier = modifier.then(
        if (dialogProperties.usePlatformDefaultWidth) {
          Modifier
        } else {
          Modifier.padding(horizontal = 16.dp)
        },
      ),
      content = content,
    )
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

      is DialogStyle.TitleNoButtons -> {
        DialogTokens.TitledPadding
      }

      is DialogStyle.TitlePlusButton -> {
        DialogTokens.TitledPadding
      }

      NoButtons -> DialogTokens.NoButtonsPadding
    }
  }

  internal fun contentToButtonPaddingHeight(style: DialogStyle): Dp {
    return when (style) {
      is Buttons -> {
        DialogTokens.ContentToButtonsPaddingHeight
      }

      is DialogStyle.TitlePlusButton -> {
        DialogTokens.ContentToTitlePlusButtonPaddingHeight
      }

      is DialogStyle.TitleNoButtons -> 0.dp
      NoButtons -> 0.dp
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

    /**
     * A dialog stlye which has a title at the top, therefore it needs a small top padding, but also contains a single
     * button at the bottom like [Buttons] do.
     * TODO no direct match in the design system
     *  https://www.figma.com/design/5kmmDdh6StpXzbEfr7WevV/Hedvig-UI-Kit?node-id=15981-24294&t=OAapnM8EnyEKhUSw-1
     *  however this is required for dialogs which need a button at the bottom for a11y reasons
     */
    data class TitlePlusButton(
      val title: String,
      val onButtonClick: () -> Unit,
      val buttonText: String,
    ) : DialogStyle()

    data class TitleNoButtons(
      val title: String,
    ) : DialogStyle()

    data object NoButtons : DialogStyle()
  }

  enum class ButtonSize {
    BIG,
    SMALL,
  }
}

@Composable
private fun HedvigDialogContent(
  style: DialogStyle,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues? = null,
  content: @Composable () -> Unit,
) {
  Surface(
    shape = DialogDefaults.shape,
    color = DialogDefaults.containerColor,
    modifier = modifier,
  ) {
    val padding = contentPadding ?: DialogDefaults.padding(style)
    Column(
      Modifier.padding(padding),
    ) {
      if (style is NoButtons) {
        content()
      } else {
        if (style is DialogStyle.TitlePlusButton) {
          DialogContentTitle(padding, style, style.title)
        }
        if (style is DialogStyle.TitleNoButtons) {
          DialogContentTitle(padding, style, style.title)
        }
        Box(
          Modifier
            .fillMaxWidth()
            .weight(1f, fill = false),
          propagateMinConstraints = true,
        ) {
          content()
        }
        Spacer(Modifier.height(DialogDefaults.contentToButtonPaddingHeight(style)))
        when (style) {
          NoButtons -> error("NoButtons is covered in the outer when statement")
          is DialogStyle.TitleNoButtons -> {}
          is Buttons -> {
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
                SmallHorizontalPreferringButtons(
                  onDismissRequest = style.onDismissRequest,
                  dismissButtonText = style.dismissButtonText,
                  onConfirmButtonClick = style.onConfirmButtonClick,
                  confirmButtonText = style.confirmButtonText,
                )
              }
            }
          }

          is DialogStyle.TitlePlusButton -> {
            HedvigButton(
              modifier = Modifier.fillMaxWidth(),
              onClick = style.onButtonClick,
              text = style.buttonText,
              enabled = true,
              buttonStyle = ButtonDefaults.ButtonStyle.Primary,
              buttonSize = ButtonDefaults.ButtonSize.Large,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun DialogContentTitle(contentPadding: PaddingValues, style: DialogStyle, title: String) {
  Spacer(
    Modifier.height(
      (DialogDefaults.padding(style).calculateTopPadding() - contentPadding.calculateTopPadding()).coerceAtLeast(0.dp),
    ),
  )
  HedvigText(
    text = title,
    style = HedvigTheme.typography.bodySmall,
    textAlign = TextAlign.Center,
    modifier = Modifier
      .fillMaxWidth()
      .semantics { heading() },
  )
}

@Composable
private fun CoreSelectDialog(
  optionsList: List<RadioOptionData>,
  onDismissRequest: () -> Unit,
  style: DialogStyle = DialogDefaults.defaultDialogStyle,
  itemContent: @Composable (RadioOptionData) -> Unit,
) {
  HedvigDialog(
    onDismissRequest = { onDismissRequest.invoke() },
    style = style,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Spacer(Modifier.height(8.dp))
      val state = rememberLazyListState()
      val lazyColumnContentPadding = PaddingValues(vertical = 16.dp)
      val density = LocalDensity.current
      val drawTopBorder by remember {
        derivedStateOf {
          state.firstVisibleItemIndex != 0 ||
            state.firstVisibleItemScrollOffset > with(density) {
              lazyColumnContentPadding.calculateTopPadding().roundToPx()
            }
        }
      }
      val borderColor = HedvigTheme.colorScheme.borderSecondary
      LazyColumn(
        state = state,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = lazyColumnContentPadding,
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

/**
 * Prefer an horizontal placement if they both fit, otherwise vertical
 */
@Composable
private fun SmallHorizontalPreferringButtons(
  onDismissRequest: () -> Unit,
  dismissButtonText: String,
  onConfirmButtonClick: () -> Unit,
  confirmButtonText: String,
) {
  val textMeasurer = rememberTextMeasurer(cacheSize = 2)
  val textStyle = LocalTextStyle.current
  Layout(
    {
      HedvigButton(
        onClick = onDismissRequest,
        text = dismissButtonText,
        enabled = true,
        buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
        buttonSize = ButtonDefaults.ButtonSize.Medium,
      )
      HedvigButton(
        onClick = onConfirmButtonClick,
        text = confirmButtonText,
        enabled = true,
        buttonStyle = ButtonDefaults.ButtonStyle.Primary,
        buttonSize = ButtonDefaults.ButtonSize.Medium,
      )
    },
  ) { measurables, constraints ->
    val spaceBetween = 8.dp
    val innerButtonHorizontalPadding = 16.dp
    val spaceAvailableForEachTextWhenPlacedHorizontallyInsideButton = constraints.maxWidth / 2 -
      (innerButtonHorizontalPadding * 2).toPx() -
      (spaceBetween / 2).toPx()
    val doNeedSecondLine = listOf(dismissButtonText, confirmButtonText).any { text ->
      textMeasurer.measure(
        text = text,
        style = textStyle,
        constraints = Constraints(maxWidth = spaceAvailableForEachTextWhenPlacedHorizontallyInsideButton.toInt()),
      ).lineCount > 1
    }
    val buttonConstraints = if (doNeedSecondLine) {
      constraints.copy(minHeight = 0, minWidth = constraints.maxWidth)
    } else {
      val fixedWidth = constraints.maxWidth / 2 - (spaceBetween / 2).toPx().toInt()
      constraints.copy(minHeight = 0, minWidth = fixedWidth, maxWidth = fixedWidth)
    }
    val (dismissButton, confirmButton) = measurables
    val dismissButtonPlaceable = dismissButton.measure(buttonConstraints)
    val confirmButtonPlaceable = confirmButton.measure(buttonConstraints)
    val layoutWidth = constraints.maxWidth
    val layoutHeight = if (doNeedSecondLine) {
      confirmButtonPlaceable.height + spaceBetween.roundToPx() + dismissButtonPlaceable.height
    } else {
      maxOf(confirmButtonPlaceable.height, dismissButtonPlaceable.height)
    }
    layout(layoutWidth, layoutHeight) {
      if (doNeedSecondLine) {
        confirmButtonPlaceable.place(0, 0)
        dismissButtonPlaceable.place(0, confirmButtonPlaceable.height + spaceBetween.roundToPx())
      } else {
        dismissButtonPlaceable.place(0, 0)
        confirmButtonPlaceable.place(dismissButtonPlaceable.width + spaceBetween.roundToPx(), 0)
      }
    }
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

@HedvigPreview
@Composable
private fun PreviewSingleSelectDialog() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      val optionsList = listOf("optionsList")
      SingleSelectDialog(
        title = "title",
        optionsList = optionsList,
        onSelected = {},
        getDisplayText = { "getDisplayText" },
        getIsSelected = { true },
        getId = { "getId" },
        getItemForId = { optionsList.first() },
        onDismissRequest = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewMultiSelectDialog() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      val optionsList = listOf("optionsList").run {
        this + this + this + this + this
      }
      MultiSelectDialog(
        title = "title",
        optionsList = optionsList,
        onSelected = {},
        getDisplayText = { "getDisplayText" },
        getIsSelected = { true },
        getId = { "getId" },
        getItemForId = { optionsList.first() },
        onDismissRequest = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHedvigDialogContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HedvigDialogContent(
        style = Buttons({}, "Cancel", {}, "Confirm text with upgrade and everything"),
        content = {
          HedvigText("Some varying content", textAlign = TextAlign.Center)
        },
      )
    }
  }
}

@HedvigPreview
@Preview(widthDp = 150, heightDp = 150)
@Preview(widthDp = 200, heightDp = 150)
@Preview(widthDp = 250, heightDp = 150)
@Preview(widthDp = 280, heightDp = 150)
@Preview(widthDp = 300, heightDp = 150)
@Preview(widthDp = 400, heightDp = 150)
@Composable
private fun PreviewSmallHorizontalPreferringButtons() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SmallHorizontalPreferringButtons(
        {},
        "Cancel",
        {},
        "Confirm text",
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewErrorDialog() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ErrorDialog("title", "message", {})
    }
  }
}
