package com.hedvig.android.feature.odyssey.step.singleitem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import arrow.core.nonEmptyListOf
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigBigCard
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.dialog.MultiSelectDialog
import com.hedvig.android.core.ui.dialog.SingleSelectDialog
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.scaffold.ClaimFlowScaffold
import com.hedvig.android.core.ui.snackbar.ErrorSnackbarState
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.ItemBrand
import com.hedvig.android.data.claimflow.ItemModel
import com.hedvig.android.data.claimflow.ItemProblem
import com.hedvig.android.feature.odyssey.ui.DatePickerUiState
import com.hedvig.android.feature.odyssey.ui.DatePickerWithDialog
import com.hedvig.android.feature.odyssey.ui.MonetaryAmountInput
import hedvig.resources.R
import java.util.Locale
import octopus.type.CurrencyCode

@Composable
internal fun SingleItemDestination(
  viewModel: SingleItemViewModel,
  windowSizeClass: WindowSizeClass,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val nextStep = uiState.nextStep
  LaunchedEffect(nextStep) {
    if (nextStep != null) {
      navigateToNextStep(nextStep)
    }
  }
  SingleItemScreen(
    uiState = uiState,
    windowSizeClass = windowSizeClass,
    submitSelections = viewModel::submitSelections,
    selectBrand = viewModel::selectBrand,
    selectModel = viewModel::selectModel,
    selectProblem = viewModel::selectProblem,
    showedError = viewModel::showedError,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
  )
}

@Composable
private fun SingleItemScreen(
  uiState: SingleItemUiState,
  windowSizeClass: WindowSizeClass,
  submitSelections: () -> Unit,
  selectBrand: (ItemBrand) -> Unit,
  selectModel: (ItemModel) -> Unit,
  selectProblem: (ItemProblem) -> Unit,
  showedError: () -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
    errorSnackbarState = ErrorSnackbarState(
      error = uiState.hasError,
      showedError = showedError,
    ),
    modifier = Modifier.clearFocusOnTap(),
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(R.string.CLAIMS_SINGLE_ITEM_DETAILS),
      style = MaterialTheme.typography.headlineMedium,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(30.dp))
    Spacer(Modifier.weight(1f))

    uiState.itemBrandsUiState.asContent()?.let { itemBrandsUiState ->
      Spacer(Modifier.height(2.dp))
      Brands(
        uiState = itemBrandsUiState,
        enabled = uiState.canSubmit,
        selectBrand = {
          selectBrand(it)
        },
        modifier = sideSpacingModifier.fillMaxWidth(),
      )
    }
    Spacer(Modifier.height(2.dp))
    when (uiState.itemModelsUiState.modelUi) {
      is ModelUi.JustCustomModel -> {
        Spacer(Modifier.height(2.dp))
        CustomModelInput(
          initialValue = uiState.itemModelsUiState.initialCustomValue,
          onInput = { input ->
            if (input != null) {
              selectModel(ItemModel.New(input))
            }
          },
          modifier = sideSpacingModifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(2.dp))
      }
      ModelUi.JustModelDialog -> {
        ModelPicker(
          uiState = uiState,
          selectModel = selectModel,
          modifier = sideSpacingModifier,
        )
      }
      is ModelUi.BothDialogAndCustom -> {
        ModelPicker(
          uiState = uiState,
          selectModel = selectModel,
          modifier = sideSpacingModifier,
        )
        Spacer(Modifier.height(2.dp))
        CustomModelInput(
          initialValue = uiState.itemModelsUiState.initialCustomValue,
          onInput = { input ->
            if (input != null) {
              selectModel(ItemModel.New(input))
            }
          },
          modifier = sideSpacingModifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(2.dp))
      }
    }

    Spacer(Modifier.height(2.dp))
    DateOfPurchase(uiState.datePickerUiState, uiState.canSubmit, sideSpacingModifier.fillMaxWidth())
    Spacer(Modifier.height(2.dp))
    if (uiState.purchasePriceApplicable) {
      Spacer(Modifier.height(2.dp))
      PriceOfPurchase(
        uiState = uiState.purchasePriceUiState,
        canInteract = uiState.canSubmit,
        modifier = sideSpacingModifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(2.dp))
    }
    uiState.itemProblemsUiState.asContent()?.let { itemProblemsUiState ->
      Spacer(Modifier.height(2.dp))
      ItemProblems(
        uiState = itemProblemsUiState,
        enabled = uiState.canSubmit,
        selectProblem = selectProblem,
        modifier = sideSpacingModifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(2.dp))
    }
    Spacer(Modifier.height(14.dp))
    VectorInfoCard(
      stringResource(R.string.CLAIMS_SINGLE_ITEM_NOTICE_LABEL),
      sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      text = stringResource(R.string.general_continue_button),
      onClick = submitSelections,
      isLoading = uiState.isLoading,
      enabled = uiState.canSubmit,
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@Composable
private fun ModelPicker(uiState: SingleItemUiState, selectModel: (ItemModel) -> Unit, modifier: Modifier = Modifier) {
  Column(modifier = modifier.fillMaxWidth()) {
    Spacer(Modifier.height(2.dp))
    Models(
      uiState = uiState.itemModelsUiState,
      enabled = uiState.canSubmit,
      selectModel = selectModel,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(2.dp))
  }
}

@Composable
private fun Models(
  uiState: ItemModelsUiState,
  enabled: Boolean,
  selectModel: (ItemModel) -> Unit,
  modifier: Modifier = Modifier,
) {
  LocalConfiguration.current
  val resources = LocalContext.current.resources

  var showDialog by rememberSaveable { mutableStateOf(false) }
  if (showDialog) {
    SelectDialogWithFreeTextField(
      uiState = uiState,
      onDismissRequest = { showDialog = false },
      selectModel = selectModel,
    )
  }
  HedvigBigCard(
    onClick = { showDialog = true },
    hintText = stringResource(R.string.claims_item_screen_model_button),
    inputText = if (uiState.selectedItemModel is ItemModel.New) {
      stringResource(id = R.string.claims_item_model_other)
    } else {
      uiState.selectedItemModel?.displayName(resources)
    },
    modifier = modifier,
    enabled = enabled,
  )
}

@Composable
private fun SelectDialogWithFreeTextField(
  uiState: ItemModelsUiState,
  onDismissRequest: () -> Unit,
  selectModel: (ItemModel) -> Unit,
) {
  val resources = LocalContext.current.resources
  SingleSelectDialog(
    title = stringResource(R.string.claims_item_screen_model_button),
    optionsList = uiState.availableItemModels,
    onSelected =
      { selectedModel ->
        selectModel(selectedModel)
      },
    getDisplayText = { it.displayName(resources) },
    getIsSelected = { it: ItemModel -> it == uiState.selectedItemModel },
    getId = { it.asKnown()?.itemModelId ?: "id" },
    onDismissRequest = onDismissRequest,
    smallSelectionItems = true,
  )
}

@Composable
private fun Brands(
  uiState: ItemBrandsUiState.Content,
  enabled: Boolean,
  selectBrand: (ItemBrand) -> Unit,
  modifier: Modifier = Modifier,
) {
  LocalConfiguration.current
  val resources = LocalContext.current.resources
  var showDialog by rememberSaveable { mutableStateOf(false) }
  if (showDialog) {
    SingleSelectDialog(
      title = stringResource(R.string.SINGLE_ITEM_INFO_BRAND),
      optionsList = uiState.availableItemBrands,
      onSelected = selectBrand,
      getDisplayText = { it.displayName(resources) },
      getId = { it.asKnown()?.itemBrandId ?: "id" },
      getIsSelected = { it: ItemBrand -> it == uiState.selectedItemBrand },
      onDismissRequest = { showDialog = false },
      smallSelectionItems = true,
    )
  }

  HedvigBigCard(
    onClick = { showDialog = true },
    hintText = stringResource(R.string.SINGLE_ITEM_INFO_BRAND),
    inputText = uiState.selectedItemBrand?.displayName(resources),
    modifier = modifier,
    enabled = enabled,
  )
}

@Composable
private fun DateOfPurchase(uiState: DatePickerUiState, canInteract: Boolean, modifier: Modifier = Modifier) {
  DatePickerWithDialog(
    uiState = uiState,
    canInteract = canInteract,
    startText = stringResource(R.string.claims_item_screen_date_of_purchase_button),
    modifier = modifier,
  )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PriceOfPurchase(uiState: PurchasePriceUiState, canInteract: Boolean, modifier: Modifier = Modifier) {
  val focusRequester = remember { FocusRequester() }
  MonetaryAmountInput(
    value = uiState.uiMoney.amount?.toString() ?: "",
    hintText = stringResource(R.string.claims_payout_purchase_price),
    canInteract = canInteract,
    onInput = { uiState.updateAmount(it) },
    currency = uiState.uiMoney.currencyCode.rawValue,
    focusRequester = focusRequester,
    modifier = modifier,
  )
}

@Composable
private fun CustomModelInput(initialValue: String, onInput: (String?) -> Unit, modifier: Modifier = Modifier) {
  var text by remember { mutableStateOf(initialValue) }
  val focusRequester = remember { FocusRequester() }
  val focusManager = LocalFocusManager.current
  HedvigTextField(
    value = initialValue,
    onValueChange = { newValue ->
      text = newValue
      onInput(newValue)
    },
    withNewDesign = true,
    modifier = modifier.focusRequester(focusRequester),
    enabled = true,
    textStyle = LocalTextStyle.current,
    label = { Text(stringResource(id = R.string.claims_item_enter_model_name)) },
    keyboardOptions = KeyboardOptions(
      autoCorrect = false,
      keyboardType = KeyboardType.Text,
      imeAction = ImeAction.Done,
    ),
    keyboardActions = KeyboardActions(
      onDone = {
        focusManager.clearFocus()
      },
    ),
  )
}

@Composable
private fun ItemProblems(
  uiState: ItemProblemsUiState.Content,
  enabled: Boolean,
  selectProblem: (ItemProblem) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showDialog: Boolean by rememberSaveable { mutableStateOf(false) }
  if (showDialog) {
    MultiSelectDialog(
      title = stringResource(R.string.claims_item_screen_type_of_damage_button),
      optionsList = uiState.availableItemProblems,
      onSelected = selectProblem,
      getDisplayText = { it.displayName },
      getIsSelected = { uiState.selectedItemProblems.contains(it) },
      getId = { it.itemProblemId },
    ) {
      showDialog = false
    }
  }

  HedvigBigCard(
    onClick = { showDialog = true },
    hintText = stringResource(R.string.claims_item_screen_type_of_damage_button),
    inputText = when {
      uiState.selectedItemProblems.isEmpty() -> null
      else -> uiState.selectedItemProblems.map(ItemProblem::displayName).joinToString()
    },
    modifier = modifier,
    enabled = enabled,
  )
}

@HedvigPreview
@Composable
private fun PreviewSingleItemScreen(
  @PreviewParameter(SingleItemScreenStatePreviewProvider::class) state: Pair<Boolean, Boolean>,
) {
  val (isLoading: Boolean, hasPriceInput: Boolean) = state
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      SingleItemScreen(
        SingleItemUiState(
          datePickerUiState = remember { DatePickerUiState(Locale.ENGLISH, null) },
          purchasePriceUiState = PurchasePriceUiState(if (hasPriceInput) 299.90 else null, CurrencyCode.SEK),
          itemBrandsUiState = ItemBrandsUiState.Content(
            nonEmptyListOf(ItemBrand.Known("Item Brand", "", "")),
            ItemBrand.Known("Item Brand #1", "", ""),
          ),
          itemModelsUiState = ItemModelsUiState(
            nonEmptyListOf(ItemModel.Known("Item Model", "", "", "")),
            ItemModel.Known("Item Model #2", "", "", ""),
            modelUi = ModelUi.BothDialogAndCustom,
            initialCustomValue = "",
          ),
          itemProblemsUiState = ItemProblemsUiState.Content(
            nonEmptyListOf(ItemProblem("Item Problem", "")),
            listOf(ItemProblem("Item Problem #3", "")),
          ),
          isLoading = isLoading,
          hasError = false,
          nextStep = null,
          purchasePriceApplicable = true,
        ),
        WindowSizeClass.calculateForPreview(),
        {},
        {},
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}

private class SingleItemScreenStatePreviewProvider : CollectionPreviewParameterProvider<Pair<Boolean, Boolean>>(
  listOf(
    false to true,
    true to false,
    true to true,
  ),
)
