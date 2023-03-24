package com.hedvig.android.odyssey.step.singleitem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import arrow.core.nonEmptyListOf
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.button.FormRowCard
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.core.ui.snackbar.ErrorSnackbarState
import com.hedvig.android.odyssey.data.ClaimFlowStep
import com.hedvig.android.odyssey.navigation.ItemBrand
import com.hedvig.android.odyssey.navigation.ItemModel
import com.hedvig.android.odyssey.navigation.ItemProblem
import com.hedvig.android.odyssey.ui.ClaimFlowScaffold
import com.hedvig.android.odyssey.ui.DatePickerRowCard
import com.hedvig.android.odyssey.ui.DatePickerUiState
import com.hedvig.android.odyssey.ui.MonetaryAmountInput
import com.hedvig.android.odyssey.ui.SingleSelectDialog
import hedvig.resources.R
import octopus.type.CurrencyCode

@Composable
internal fun SingleItemDestination(
  viewModel: SingleItemViewModel,
  windowSizeClass: WindowSizeClass,
  imageLoader: ImageLoader,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  navigateBack: () -> Unit,
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
    imageLoader = imageLoader,
    submitSelections = viewModel::submitSelections,
    selectBrand = viewModel::selectBrand,
    selectModel = viewModel::selectModel,
    selectProblem = viewModel::selectProblem,
    showedError = viewModel::showedError,
    navigateBack = navigateBack,
  )
}

@Composable
private fun SingleItemScreen(
  uiState: SingleItemUiState,
  windowSizeClass: WindowSizeClass,
  imageLoader: ImageLoader,
  submitSelections: () -> Unit,
  selectBrand: (ItemBrand) -> Unit,
  selectModel: (ItemModel) -> Unit,
  selectProblem: (ItemProblem) -> Unit,
  showedError: () -> Unit,
  navigateBack: () -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateBack = navigateBack,
    topAppBarText = stringResource(R.string.claims_item_screen_title),
    isLoading = uiState.isLoading,
    errorSnackbarState = ErrorSnackbarState(
      error = uiState.hasError,
      showedError = showedError,
    ),
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(22.dp))
    uiState.itemBrandsUiState.asContent()?.let { itemBrandsUiState ->
      Spacer(Modifier.height(10.dp))
      Brands(itemBrandsUiState, selectBrand, imageLoader, sideSpacingModifier)
      Spacer(Modifier.height(10.dp))
    }
    val itemModelsUiStateContent = uiState.itemModelsUiState.asContent()
    AnimatedVisibility(
      visible = itemModelsUiStateContent != null,
      enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically, clip = false),
      exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically, clip = false),
    ) {
      Column {
        Spacer(Modifier.height(10.dp))
        Models(itemModelsUiStateContent, selectModel, imageLoader, sideSpacingModifier)
        Spacer(Modifier.height(10.dp))
      }
    }
    Spacer(Modifier.height(10.dp))
    DateOfPurchase(uiState.datePickerUiState, uiState.canSubmit, sideSpacingModifier)
    Spacer(Modifier.height(20.dp))
    PriceOfPurchase(
      uiState = uiState.purchasePriceUiState,
      enabled = uiState.canSubmit,
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(10.dp))
    uiState.itemProblemsUiState.asContent()?.let { itemProblemsUiState ->
      Spacer(Modifier.height(10.dp))
      ItemProblems(itemProblemsUiState, selectProblem, imageLoader, sideSpacingModifier)
      Spacer(Modifier.height(10.dp))
    }
    Spacer(Modifier.height(10.dp))
    Spacer(Modifier.weight(1f))
    LargeContainedTextButton(
      onClick = submitSelections,
      enabled = uiState.canSubmit,
      text = stringResource(R.string.general_continue_button),
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(20.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@Composable
private fun Models(
  uiState: ItemModelsUiState.Content?,
  selectModel: (ItemModel) -> Unit,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  LocalConfiguration.current
  val resources = LocalContext.current.resources
  var showDialog by rememberSaveable { mutableStateOf(false) }
  if (showDialog && uiState != null) {
    SingleSelectDialog(
      title = stringResource(R.string.claims_item_screen_model_button),
      optionsList = uiState.availableItemModels,
      onSelected = selectModel,
      getDisplayText = { it.displayName(resources) },
      getImageUrl = { it.asKnown()?.imageUrl },
      getId = { it.asKnown()?.itemModelId ?: "id" },
      imageLoader = imageLoader,
    ) {
      showDialog = false
    }
  }

  FormRowCard(
    onClick = { showDialog = true },
    modifier = modifier,
  ) {
    Text(stringResource(R.string.claims_item_screen_model_button))
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.width(8.dp))
    CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = ContentAlpha.medium)) {
      Text(uiState?.selectedItemModel?.displayName(resources) ?: "")
      Spacer(Modifier.width(8.dp))
      Icon(Icons.Default.ArrowForward, null)
    }
    Spacer(Modifier.width(8.dp))
  }
}

@Composable
private fun Brands(
  uiState: ItemBrandsUiState.Content,
  selectBrand: (ItemBrand) -> Unit,
  imageLoader: ImageLoader,
  modifier: Modifier,
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
      getImageUrl = { null },
      getId = { it.asKnown()?.itemBrandId ?: "id" },
      imageLoader = imageLoader,
    ) {
      showDialog = false
    }
  }

  FormRowCard(
    onClick = { showDialog = true },
    modifier = modifier,
  ) {
    Text(stringResource(R.string.SINGLE_ITEM_INFO_BRAND))
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.width(8.dp))
    CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = ContentAlpha.medium)) {
      Text(uiState.selectedItemBrand?.displayName(resources) ?: "")
      Spacer(Modifier.width(8.dp))
      Icon(Icons.Default.ArrowForward, null)
    }
    Spacer(Modifier.width(8.dp))
  }
}

@Composable
private fun DateOfPurchase(
  uiState: DatePickerUiState,
  canInteract: Boolean,
  modifier: Modifier,
) {
  DatePickerRowCard(
    uiState = uiState,
    canInteract = canInteract,
    startText = stringResource(R.string.claims_item_screen_date_of_purchase_button),
    modifier = modifier,
  )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PriceOfPurchase(
  uiState: PurchasePriceUiState,
  enabled: Boolean,
  modifier: Modifier = Modifier,
) {
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  FormRowCard(
    modifier = modifier,
    enabled = enabled,
    onClick = {
      focusRequester.requestFocus()
      keyboardController?.show()
    },
  ) {
    Text("Price of purchase") // todo string resource
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.width(8.dp))
    CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = ContentAlpha.medium)) {
      MonetaryAmountInput(
        value = uiState.uiMoney.amount?.toString() ?: "",
        onInput = { uiState.updateAmount(it) },
        currency = uiState.uiMoney.currencyCode.rawValue,
        maximumFractionDigits = 0,
        focusRequester = focusRequester,
      )
    }
  }
}

@Composable
private fun ItemProblems(
  uiState: ItemProblemsUiState.Content,
  selectProblem: (ItemProblem) -> Unit,
  imageLoader: ImageLoader,
  modifier: Modifier,
) {
  var showDialog: Boolean by rememberSaveable { mutableStateOf(false) }
  if (showDialog) {
    SingleSelectDialog(
      title = stringResource(R.string.claims_item_screen_type_of_damage_button),
      optionsList = uiState.availableItemProblems,
      onSelected = selectProblem,
      getDisplayText = { it.displayName },
      getImageUrl = { null },
      getId = { it.itemProblemId },
      imageLoader = imageLoader,
    ) {
      showDialog = false
    }
  }

  FormRowCard(
    onClick = { showDialog = true },
    modifier = modifier,
  ) {
    Text("Damage") // todo string resource "Damage"
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.width(8.dp))
    CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = ContentAlpha.medium)) {
      Text(
        when {
          uiState.selectedItemProblems.isEmpty() -> ""
          uiState.selectedItemProblems.size == 1 -> uiState.selectedItemProblems.first().displayName
          else -> stringResource(R.string.OFFER_START_DATE_MULTIPLE)
        },
      )
      Spacer(Modifier.width(8.dp))
      Icon(Icons.Default.ArrowForward, null)
    }
    Spacer(Modifier.width(8.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewSingleItemScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      SingleItemScreen(
        SingleItemUiState(
          datePickerUiState = remember { DatePickerUiState(null) },
          purchasePriceUiState = PurchasePriceUiState(299.90, CurrencyCode.SEK),
          itemBrandsUiState = ItemBrandsUiState.Content(
            nonEmptyListOf(ItemBrand.Known("Item Brand", "", "")),
            ItemBrand.Known("Item Brand", "", ""),
          ),
          itemModelsUiState = ItemModelsUiState.Content(
            nonEmptyListOf(ItemModel.Known("Item Model", null, "", "", "")),
            ItemModel.Known("Item Model", null, "", "", ""),
          ),
          itemProblemsUiState = ItemProblemsUiState.Content(
            nonEmptyListOf(ItemProblem("Item Problem", "")),
            listOf(ItemProblem("Item Problem", "")),
          ),
          isLoading = false,
          hasError = false,
          nextStep = null,
        ),
        WindowSizeClass.calculateForPreview(),
        rememberPreviewImageLoader(),
        {}, {}, {}, {}, {}, {},
      )
    }
  }
}
