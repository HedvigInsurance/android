package com.hedvig.android.feature.payin.account.ui.setupswish

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.simulateHotReload
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.payin.account.ui.overview.formatSwishPhoneNumber
import hedvig.resources.CONTACT_INFO_CHANGES_SAVED
import hedvig.resources.ODYSSEY_PHONE_NUMBER_LABEL
import hedvig.resources.Res
import hedvig.resources.TIER_FLOW_COMMIT_PROCESSING_ERROR_DESCRIPTION
import hedvig.resources.general_save_button
import hedvig.resources.something_went_wrong
import hedvig.resources.swish
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SetupSwishPayinDestination(
  viewModel: SetupSwishPayinViewModel,
  globalSnackBarState: GlobalSnackBarState,
  onSuccessfullyConnected: () -> Unit,
  navigateUp: () -> Unit,
  openUrl: (String) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  SetupSwishPayoutScreen(
    uiState = uiState,
    globalSnackBarState = globalSnackBarState,
    onSave = { viewModel.emit(SetupSwishPayoutEvent.Save) },
    showedSnackBar = {
      viewModel.emit(SetupSwishPayoutEvent.ShowedSnackBar)
      onSuccessfullyConnected()
    },
    navigateUp = navigateUp,
    openUrl = openUrl,
    updateText = {
      viewModel.emit(SetupSwishPayoutEvent.UpdateText(it))
    },
  )
}

// todo fetch payment methods continuously to see if it already not in pending state

@Composable
private fun SetupSwishPayoutScreen(
  uiState: SetupSwishPayoutUiState,
  globalSnackBarState: GlobalSnackBarState,
  onSave: () -> Unit,
  showedSnackBar: () -> Unit,
  navigateUp: () -> Unit,
  openUrl: (String) -> Unit,
  updateText: (String) -> Unit,
) {
  val focusManager = LocalFocusManager.current
  val changesSaved = stringResource(Res.string.CONTACT_INFO_CHANGES_SAVED)
  LaunchedEffect(uiState.showSuccessSnackBar) {
    if (!uiState.showSuccessSnackBar) return@LaunchedEffect
    globalSnackBarState.show(changesSaved, NotificationPriority.Campaign)
    showedSnackBar()
  }

  HedvigScaffold(
    topAppBarText = stringResource(Res.string.swish),
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    Spacer(Modifier.weight(1f))
    if (uiState.error != null) {
      EmptyState(
        text = stringResource(Res.string.something_went_wrong),
        description = uiState.error.message,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        iconStyle = EmptyStateDefaults.EmptyStateIconStyle.ERROR,
      )
    }
    AnimatedVisibility(uiState.successUrl != null) {
      if (uiState.successUrl != null)
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        EmptyState(
          text = "Please give your approval in the Swish app",
          description = null,
          modifier = Modifier.fillMaxWidth(),
          iconStyle = EmptyStateDefaults.EmptyStateIconStyle.SWISH,
        )
        Spacer(Modifier.height(16.dp))
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Surface(
            color = Color.White,
            shape = HedvigTheme.shapes.cornerMedium,
            border = HedvigTheme.colorScheme.borderPrimary,
            modifier = Modifier.padding(16.dp),
          ) {
            QRCode(
              token = uiState.successUrl,
              modifier = Modifier
                .size(180.dp)
                .padding(16.dp),
            )
          }
        }
        Spacer(Modifier.height(32.dp))
        HedvigButton(
          "Open Swish",
          enabled = true,
          onClick = {
            openUrl(uiState.successUrl)
          },
          buttonStyle = ButtonDefaults.ButtonStyle.PrimaryAlt,
          buttonSize = ButtonDefaults.ButtonSize.Medium,
        )
      }
    }
    Spacer(Modifier.weight(1f))
    AnimatedVisibility(uiState.successUrl == null) {
      Column {
        Spacer(Modifier.height(16.dp))
        Column(Modifier.padding(horizontal = 16.dp)) {
          var input by remember { mutableStateOf(uiState.phoneNumber) }
          val mask = "000-000-00-00"
          val maskColor = HedvigTheme.colorScheme.textTertiary
          val visualTransformation = ChipIdVisualTransformation(mask, maskColor)
          val interactionSource = remember { MutableInteractionSource() }
          HedvigTextField(
            text = input,
            labelText = stringResource(Res.string.ODYSSEY_PHONE_NUMBER_LABEL),
            textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Phone,
            ),
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = visualTransformation,
            errorState = HedvigTextFieldDefaults.ErrorState.NoError,
            interactionSource = interactionSource,
            onValueChange = {
              if (it.length <= 15) {
                updateText(it)
                input = it
              }
            },
          )
        }
        Spacer(Modifier.height(16.dp))
        HedvigButton(
          text = stringResource(Res.string.general_save_button),
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
    }

    Spacer(Modifier.height(16.dp))
  }
}

private class ChipIdVisualTransformation(
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
@HedvigPreview
private fun PreviewSetupSwishPayinScreen(
  @PreviewParameter(SetupSwishPayinUiStateProvider::class) uiState: SetupSwishPayoutUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SetupSwishPayoutScreen(
        uiState = uiState,
        globalSnackBarState = GlobalSnackBarState(),
        onSave = {},
        showedSnackBar = {},
        navigateUp = {},
        {},
        {},
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
