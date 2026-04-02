package com.hedvig.android.feature.chip.id.ui

import android.text.TextUtils
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.then
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setText
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
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
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.pillowResource
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.clearFocusOnTap
import com.hedvig.android.feature.chip.id.data.PetContractForChipId
import com.hedvig.android.feature.chip.id.ui.AddChipIdEvent.RetryLoadData
import com.hedvig.android.feature.chip.id.ui.AddChipIdEvent.SubmitData
import com.hedvig.android.feature.chip.id.ui.AddChipIdUiState.Content
import hedvig.resources.CHIP_ID_LABEL
import hedvig.resources.CHIP_ID_TOP_TITLE
import hedvig.resources.CHIP_ID_WRONG_INPUT
import hedvig.resources.CONTACT_INFO_CHANGES_SAVED
import hedvig.resources.Res
import hedvig.resources.general_save_button
import hedvig.resources.something_went_wrong
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.core.text.isDigitsOnly

@Composable
internal fun AddChipIdDestination(
  viewModel: AddChipIdViewModel,
  globalSnackBarState: GlobalSnackBarState,
  navigateUp: () -> Unit,
  popFlowOnSuccess: () -> Unit,
) {
  val uiState: AddChipIdUiState by viewModel.uiState.collectAsStateWithLifecycle()
  AddChipIdScreen(
    uiState = uiState,
    globalSnackBarState = globalSnackBarState,
    submitChipId = {
      viewModel.emit(SubmitData)
    },
    reload = {
      viewModel.emit(RetryLoadData)
    },
    navigateUp = navigateUp,
    showedSnackBar = {
      viewModel.emit(AddChipIdEvent.ShowedMessage)
      popFlowOnSuccess()
    },
    updateText = {
      viewModel.emit(AddChipIdEvent.UpdateText(it))
    },
  )
}

@Composable
private fun AddChipIdScreen(
  uiState: AddChipIdUiState,
  globalSnackBarState: GlobalSnackBarState,
  submitChipId: () -> Unit,
  reload: () -> Unit,
  navigateUp: () -> Unit,
  showedSnackBar: () -> Unit,
  updateText: (String) -> Unit,
) {
  val focusManager = LocalFocusManager.current
  HedvigScaffold(
    topAppBarText = stringResource(Res.string.CHIP_ID_TOP_TITLE),
    navigateUp = navigateUp,
    modifier = Modifier
      .fillMaxSize()
      .clearFocusOnTap(),
  ) {
    when (uiState) {
      AddChipIdUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced(
          Modifier
            .weight(1f)
            .wrapContentHeight(),
        )
      }

      AddChipIdUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = reload,
          modifier = Modifier
            .weight(1f)
            .wrapContentHeight(),
        )
      }

      is Content -> {
        AddChipIdContent(
          uiState = uiState,
          globalSnackBarState = globalSnackBarState,
          submitChipId = submitChipId,
          focusManager = focusManager,
          showedSnackBar = showedSnackBar,
          updateText = updateText,
        )
      }
    }
  }
}

@Composable
private fun ColumnScope.AddChipIdContent(
  uiState: Content,
  globalSnackBarState: GlobalSnackBarState,
  submitChipId: () -> Unit,
  focusManager: FocusManager,
  showedSnackBar: () -> Unit,
  updateText: (String) -> Unit,
) {
  val successMessage = stringResource(Res.string.CONTACT_INFO_CHANGES_SAVED)
  LaunchedEffect(uiState.showSuccessSnackBar) {
    if (!uiState.showSuccessSnackBar) return@LaunchedEffect
    globalSnackBarState.show(successMessage, NotificationPriority.Campaign)
    showedSnackBar()
  }

  Spacer(Modifier.weight(1f))
  Spacer(Modifier.height(16.dp))

  InsuranceInfoCard(
    uiState.contract,
    modifier = Modifier.padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(16.dp))
  ChipIdTextField(
    text = uiState.chipIdText,
    labelText = stringResource(Res.string.CHIP_ID_LABEL),
    updateText = updateText,
  )
  Spacer(Modifier.height(16.dp))
  ExperimentalChipIdTextField()
  AnimatedContent(
    targetState = uiState.errorType,
    transitionSpec = { fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically() },
    modifier = Modifier.padding(top = 4.dp),
  ) { errorType ->
    if (errorType != null) {
      val errorMessage = when (errorType) {
        ChipIdErrorType.WrongInput -> stringResource(Res.string.CHIP_ID_WRONG_INPUT)
        ChipIdErrorType.GeneralError -> stringResource(Res.string.something_went_wrong)
        is ChipIdErrorType.ErrorWithMessage -> errorType.message
      }
      HedvigNotificationCard(
        message = errorMessage,
        priority = NotificationPriority.Error,
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth()
          .semantics {
            liveRegion = LiveRegionMode.Assertive
          },
      )
    }
  }

  Spacer(Modifier.height(16.dp))
  HedvigButton(
    text = stringResource(Res.string.general_save_button),
    enabled = !uiState.submittingData,
    onClick = {
      focusManager.clearFocus()
      submitChipId()
    },
    isLoading = uiState.submittingData,
    modifier = Modifier
      .padding(horizontal = 16.dp)
      .fillMaxSize(),
  )
  Spacer(Modifier.height(16.dp))
}

@Composable
private fun ExperimentalChipIdTextField() {
  val textFieldState = remember {
    TextFieldState("")
  }
  val maskColor = HedvigTheme.colorScheme.textTertiary
  HedvigTextField(
    state = textFieldState,
    textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
    outputTransformation = ChipIdOutputTransformation(maskColor),
    modifier = Modifier.padding(horizontal = 16.dp),
    labelText = "Chip-ID OutputTransformation",
    inputTransformation = InputTransformation.then (
      object: InputTransformation {
        override fun SemanticsPropertyReceiver.applySemantics() {
          maxLength(30)
        }

        override fun TextFieldBuffer.transformInput() {
          if (!asCharSequence().isDigitsOnly() || length > 15) {
            revertAllChanges()
          }
        }
      }
    ),
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Number,
      imeAction = ImeAction.Done,
    ),
  )
}

@Stable
data class ChipIdOutputTransformation(
  val color: Color
) : OutputTransformation {
  override fun TextFieldBuffer.transformOutput() {
// Find dash


    // Pad the text with placeholder chars if too short.
    // ___-___-___-___-___
    val padCount = 15 - length
    repeat(padCount) {
      append('x')
    }


    // OOO-OOO-OOO-OOO-OOO
    if (length > 3) insert(3, "-")
    if (length > 7) insert(7, "-")
    if (length > 11) insert(11, "-")
    if (length > 15) insert(15, "-")

    val regex = Regex("x|(?<=x)-|-(?=x)")
    regex
      .findAll(asCharSequence())
      .forEach { match ->
        addStyle(SpanStyle(color = color), match.range.start, match.range.last + 1)
      }

  }
}

@Composable
private fun ChipIdTextField(
  text: String,
  labelText: String,
  updateText: (String) -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }
  var input by remember { mutableStateOf(text) }
  val mask = "xxx-xxx-xxx-xxx-xxx"
  val maskColor = HedvigTheme.colorScheme.textTertiary
  val visualTransformation = ChipIdVisualTransformation(mask, maskColor)
  HedvigTextField(
    text = input,
    labelText = "ChipId VisualTransformation", //todo!!!
    errorState = HedvigTextFieldDefaults.ErrorState.NoError,
    onValueChange = {
      if (it.length <= 15) {
        updateText(it)
        input = it
      }
    },
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Number,
      imeAction = ImeAction.Done,
    ),
    visualTransformation = visualTransformation,
    textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
    interactionSource = interactionSource,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
//      .clearAndSetSemantics {
//        contentDescription = "Enter ChipId. " +
//          "Edit box. Input: ${if (!input.isEmpty()) input else "empty"} Double tap to activate"
//      },
  )
}

private class ChipIdVisualTransformation(
  private val mask: String,
  private val maskColor: Color,
) : VisualTransformation {
  override fun filter(text: AnnotatedString): TransformedText {
    val trimmed = if (text.text.length >= 15) text.text.substring(0..14) else text.text

    val annotatedString = buildAnnotatedString {
      for (i in trimmed.indices) {
        append(trimmed[i])
        if (i in listOf(2, 5, 8, 11)) {
          append("-")
        }
      }
      withStyle(SpanStyle(color = maskColor)) {
        append(mask.takeLast(mask.length - length))
      }
    }

    val personalNumberOffsetTranslator = object : OffsetMapping {
      override fun originalToTransformed(offset: Int): Int {
        return when {
          offset <= 2 -> offset
          offset <= 5 -> offset + 1
          offset <= 8 -> offset + 2
          offset <= 11 -> offset + 3
          offset <= 15 -> offset + 4
          else -> 19
        }
      }

      override fun transformedToOriginal(offset: Int): Int {
        return when {
          offset <= 3 -> offset
          offset <= 7 -> offset - 1
          offset <= 11 -> offset - 2
          offset <= 15 -> offset - 3
          else -> offset - 4
        }.coerceAtMost(text.length)
      }
    }
    return TransformedText(annotatedString, personalNumberOffsetTranslator)
  }
}


@Composable
private fun InsuranceInfoCard(
  insuranceInfo: PetContractForChipId,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    modifier
      .border(
        width = 1.dp,
        color = HedvigTheme.colorScheme.borderPrimary,
        shape = HedvigTheme.shapes.cornerXLarge,
      ),
    color = HedvigTheme.colorScheme.backgroundPrimary,
  ) {
    Column(Modifier.padding(16.dp)) {
      Row {
        Image(
          painter = painterResource(insuranceInfo.contractGroup.pillowResource()),
          contentDescription = null,
          modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
          HedvigText(insuranceInfo.displayName)
          HedvigText(insuranceInfo.contractExposure, color = HedvigTheme.colorScheme.textSecondary)
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminationConfirmationScreen(
  @PreviewParameter(AddChipIdScreenStateProvider::class) state: AddChipIdUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      AddChipIdScreen(
        state,
        globalSnackBarState = GlobalSnackBarState(),
        submitChipId = { },
        reload = { },
        navigateUp = { },
        showedSnackBar = {},
        {},
      )
    }
  }
}


private class AddChipIdScreenStateProvider : CollectionPreviewParameterProvider<AddChipIdUiState>(
  listOf(
    AddChipIdUiState.Error,
    AddChipIdUiState.Loading,
    Content(
      chipIdText = "",
      contract = PetContractForChipId(
        id = "sdf",
        displayName = "Display name",
        contractExposure = "Kitty",
        contractGroup = ContractGroup.CAT,
      ),
      showSuccessSnackBar = false,
      submittingData = false,
    ),
    Content(
      chipIdText = "123456789012345",
      contract = PetContractForChipId(
        id = "sdf",
        displayName = "Display name",
        contractExposure = "Kitty",
        contractGroup = ContractGroup.CAT,
      ),
      showSuccessSnackBar = false,
      submittingData = false,
    ),
    Content(
      chipIdText = "",
      contract = PetContractForChipId(
        id = "sdf",
        displayName = "Display name",
        contractExposure = "Kitty",
        contractGroup = ContractGroup.CAT,
      ),
      showSuccessSnackBar = false,
      submittingData = false,
      errorType = ChipIdErrorType.WrongInput,
    ),
    Content(
      chipIdText = "",
      contract = PetContractForChipId(
        id = "sdf",
        displayName = "Display name",
        contractExposure = "Kitty",
        contractGroup = ContractGroup.CAT,
      ),
      showSuccessSnackBar = false,
      submittingData = false,
      errorType = ChipIdErrorType.GeneralError,
    ),
  ),
)
