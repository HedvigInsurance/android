package com.hedvig.android.feature.payin.account.ui.setupswish

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.colored.Swish
import com.hedvig.android.feature.payin.account.ui.components.PayinMethodMarkSize
import com.hedvig.android.feature.payin.account.ui.components.PayinMethodTile
import com.hedvig.android.feature.payin.account.ui.components.PayinMethodTileBadge
import com.hedvig.android.feature.payin.account.ui.components.formatSwishPhoneNumber
import hedvig.resources.CONTACT_INFO_CHANGES_SAVED
import hedvig.resources.GENERAL_CONFIRM
import hedvig.resources.ODYSSEY_PHONE_NUMBER_LABEL
import hedvig.resources.PAYMENT_OPEN_SWISH_BUTTON
import hedvig.resources.PAYMENT_SWISH_APPROVE_TITLE
import hedvig.resources.PAYMENT_SWISH_EXPLANATION_BUTTON
import hedvig.resources.PAYMENT_SWISH_SUBTITLE
import hedvig.resources.PAYMENT_SWISH_TITLE
import hedvig.resources.Res
import hedvig.resources.general_cancel_button
import hedvig.resources.something_went_wrong
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SetupSwishPayinDestination(
  viewModel: SetupSwishPayinViewModel,
  globalSnackBarState: GlobalSnackBarState,
  onSuccessfullyConnected: () -> Unit,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
  openUrl: (String) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  SetupSwishPayinScreen(
    uiState = uiState,
    globalSnackBarState = globalSnackBarState,
    onSave = { viewModel.emit(SetupSwishPayoutEvent.Save) },
    showedSnackBar = {
      viewModel.emit(SetupSwishPayoutEvent.ShowedSnackBar)
      onSuccessfullyConnected()
    },
    navigateUp = navigateUp,
    navigateBack = navigateBack,
    openUrl = openUrl,
    onLearnMoreAboutRecurringSwish = {
      // TODO: open the recurring-Swish explanation once we know where it lives.
    },
    updateText = {
      viewModel.emit(SetupSwishPayoutEvent.UpdateText(it))
    },
  )
}

// todo fetch payment methods continuously to see if it already not in pending state

@Composable
private fun SetupSwishPayinScreen(
  uiState: SetupSwishPayoutUiState,
  globalSnackBarState: GlobalSnackBarState,
  onSave: () -> Unit,
  showedSnackBar: () -> Unit,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
  openUrl: (String) -> Unit,
  onLearnMoreAboutRecurringSwish: () -> Unit,
  updateText: (String) -> Unit,
) {
  val changesSaved = stringResource(Res.string.CONTACT_INFO_CHANGES_SAVED)
  LaunchedEffect(uiState.showSuccessSnackBar) {
    if (!uiState.showSuccessSnackBar) return@LaunchedEffect
    globalSnackBarState.show(changesSaved, NotificationPriority.Campaign)
    showedSnackBar()
  }

  val successUrl = uiState.successUrl
  HedvigScaffold(
    topAppBarText = null,
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    Spacer(Modifier.height(8.dp))
    FlowHeading(
      title = if (successUrl == null) {
        stringResource(Res.string.PAYMENT_SWISH_TITLE)
      } else {
        stringResource(Res.string.PAYMENT_SWISH_APPROVE_TITLE)
      },
      description = if (successUrl == null) {
        stringResource(Res.string.PAYMENT_SWISH_SUBTITLE)
      } else {
        null
      },
      baseStyle = HedvigTheme.typography.bodySmall,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    PayinMethodTile(
      modifier = Modifier.align(Alignment.CenterHorizontally),
      badge = if (successUrl == null) {
        null
      } else {
        {
          PayinMethodTileBadge(
            icon = HedvigIcons.ArrowNorthEast,
            containerColor = HedvigTheme.colorScheme.signalBlueElement,
            contentColor = HedvigTheme.colorScheme.fillWhite,
          )
        }
      },
      mark = {
        Image(HedvigIcons.Swish, EmptyContentDescription, Modifier.size(PayinMethodMarkSize))
      },
    )
    Spacer(Modifier.weight(1f))
    if (uiState.error != null) {
      HedvigNotificationCard(
        message = uiState.error.message ?: stringResource(Res.string.something_went_wrong),
        priority = NotificationPriority.Error,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(8.dp))
    }
    if (successUrl == null) {
      EnterPhoneNumberSection(
        uiState = uiState,
        onSave = onSave,
        onLearnMoreAboutRecurringSwish = onLearnMoreAboutRecurringSwish,
        updateText = updateText,
      )
    } else {
      HedvigButton(
        text = stringResource(Res.string.PAYMENT_OPEN_SWISH_BUTTON),
        onClick = { openUrl(successUrl) },
        enabled = true,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        text = stringResource(Res.string.general_cancel_button),
        onClick = navigateBack,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
    }
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun ColumnScope.EnterPhoneNumberSection(
  uiState: SetupSwishPayoutUiState,
  onSave: () -> Unit,
  onLearnMoreAboutRecurringSwish: () -> Unit,
  updateText: (String) -> Unit,
) {
  val focusManager = LocalFocusManager.current
  HedvigText(
    text = stringResource(Res.string.PAYMENT_SWISH_EXPLANATION_BUTTON),
    style = HedvigTheme.typography.label,
    color = HedvigTheme.colorScheme.textSecondary,
    textAlign = TextAlign.Center,
    textDecoration = TextDecoration.Underline,
    modifier = Modifier
      .align(Alignment.CenterHorizontally)
      .clip(HedvigTheme.shapes.cornerSmall)
      .clickable(onClick = onLearnMoreAboutRecurringSwish)
      .padding(horizontal = 8.dp, vertical = 4.dp),
  )
  Spacer(Modifier.height(16.dp))
  var input by remember { mutableStateOf(uiState.phoneNumber) }
  val visualTransformation = SwishPhoneNumberVisualTransformation(
    mask = "000-000-00-00",
    maskColor = HedvigTheme.colorScheme.textTertiary,
  )
  HedvigTextField(
    text = input,
    labelText = stringResource(Res.string.ODYSSEY_PHONE_NUMBER_LABEL),
    textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    visualTransformation = visualTransformation,
    errorState = HedvigTextFieldDefaults.ErrorState.NoError,
    interactionSource = remember { MutableInteractionSource() },
    onValueChange = {
      val digitsOnly = it.filter { char -> char.isDigit() }
      if (digitsOnly.length <= 15) {
        updateText(digitsOnly)
        input = digitsOnly
      }
    },
  )
  Spacer(Modifier.height(16.dp))
  HedvigButton(
    text = stringResource(Res.string.GENERAL_CONFIRM),
    onClick = {
      focusManager.clearFocus()
      onSave()
    },
    enabled = !uiState.isLoading &&
      uiState.phoneNumber.length >= 8 &&
      uiState.phoneNumber.length <= 15,
    isLoading = uiState.isLoading,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
}

private class SwishPhoneNumberVisualTransformation(
  private val mask: String,
  private val maskColor: Color,
) : VisualTransformation {
  override fun filter(text: AnnotatedString): TransformedText {
    val trimmed = if (text.text.length >= 15) text.text.substring(0..14) else text.text

    val annotatedString = buildAnnotatedString {
      append(formatSwishPhoneNumber(trimmed))
      withStyle(SpanStyle(color = maskColor)) {
        append(mask.takeLast((mask.length - length).coerceAtLeast(0)))
      }
    }

    val personalNumberOffsetTranslator = object : OffsetMapping {
      override fun originalToTransformed(offset: Int): Int {
        return when {
          offset <= 2 -> offset
          offset <= 5 -> offset + 1
          offset <= 7 -> offset + 2
          else -> offset + 3
        }
      }

      override fun transformedToOriginal(offset: Int): Int {
        return when {
          offset <= 3 -> offset
          offset <= 7 -> offset - 1
          offset <= 10 -> offset - 2
          else -> offset - 3
        }.coerceAtMost(text.length)
      }
    }
    return TransformedText(annotatedString, personalNumberOffsetTranslator)
  }
}

@Composable
@HedvigShortMultiScreenPreview
private fun PreviewSetupSwishPayinScreen(
  @PreviewParameter(SetupSwishPayinUiStateProvider::class) uiState: SetupSwishPayoutUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SetupSwishPayinScreen(
        uiState = uiState,
        globalSnackBarState = GlobalSnackBarState(),
        onSave = {},
        showedSnackBar = {},
        navigateUp = {},
        navigateBack = {},
        openUrl = {},
        onLearnMoreAboutRecurringSwish = {},
        updateText = {},
      )
    }
  }
}

private class SetupSwishPayinUiStateProvider : CollectionPreviewParameterProvider<SetupSwishPayoutUiState>(
  listOf(
    SetupSwishPayoutUiState(
      phoneNumber = "287334432273",
      isLoading = false,
      error = null,
      showSuccessSnackBar = false,
    ),
    SetupSwishPayoutUiState(
      phoneNumber = "",
      isLoading = false,
      error = ErrorMessage(),
      showSuccessSnackBar = false,
    ),
    SetupSwishPayoutUiState(
      phoneNumber = "837286428",
      isLoading = true,
      error = null,
      showSuccessSnackBar = false,
    ),
    SetupSwishPayoutUiState(
      phoneNumber = "83728644428",
      isLoading = false,
      error = null,
      showSuccessSnackBar = false,
      successUrl = "hwdjhew",
    ),
  ),
)
