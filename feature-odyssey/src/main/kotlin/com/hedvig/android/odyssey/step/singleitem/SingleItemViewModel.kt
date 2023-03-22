package com.hedvig.android.odyssey.step.singleitem

import android.content.res.Resources
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import com.hedvig.android.odyssey.data.ClaimFlowRepository
import com.hedvig.android.odyssey.data.ClaimFlowStep
import com.hedvig.android.odyssey.navigation.ClaimFlowDestination
import com.hedvig.android.odyssey.navigation.ItemBrand
import com.hedvig.android.odyssey.navigation.ItemModel
import com.hedvig.android.odyssey.navigation.ItemProblem
import com.hedvig.android.odyssey.navigation.UiMoney
import com.hedvig.android.odyssey.ui.DatePickerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octopus.type.CurrencyCode
import octopus.type.FlowClaimItemBrandInput
import octopus.type.FlowClaimItemModelInput

internal class SingleItemViewModel(
  singleItem: ClaimFlowDestination.SingleItem,
  private val claimFlowRepository: ClaimFlowRepository,
  resources: Resources,
  clock: Clock = Clock.System,
) : ViewModel() {
  private val _uiState = MutableStateFlow(SingleItemUiState.fromInitialSingleItem(singleItem, resources, clock))
  val uiState: StateFlow<SingleItemUiState> = _uiState.asStateFlow()

  fun submitSelections() {
    val uiState = _uiState.value
    if (uiState.canSubmit.not()) return
    _uiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      claimFlowRepository.submitSingleItem(
        itemBrandInput = run {
          val itemBrandsUiState = uiState.itemBrandsUiState.asContent()
          val selectedItemBrand = itemBrandsUiState?.selectedItemBrand?.asKnown() ?: return@run null
          FlowClaimItemBrandInput(
            itemTypeId = selectedItemBrand.itemTypeId,
            itemBrandId = selectedItemBrand.itemBrandId,
          )
        },
        itemModelInput = run {
          val itemModelUiState = uiState.itemModelsUiState.asContent()
          val selectedItemModel = itemModelUiState?.selectedItemModel?.asKnown() ?: return@run null
          FlowClaimItemModelInput(selectedItemModel.itemModelId)
        },
        itemProblemIds = run {
          val itemProblemsUiState = uiState.itemProblemsUiState.asContent() ?: return@run null
          val selectedItemProblems =
            itemProblemsUiState.selectedItemProblems.toNonEmptyListOrNull() ?: return@run emptyList()
          selectedItemProblems.map { it.itemProblemId }
        },
        purchaseDate = uiState.datePickerUiState.datePickerState.selectedDateMillis?.let { epochMilliseconds ->
          Instant.fromEpochMilliseconds(epochMilliseconds).toLocalDateTime(TimeZone.UTC).date
        },
        purchasePrice = uiState.purchasePriceUiState.uiMoney.amount,
      ).fold(
        ifLeft = {
          _uiState.update { it.copy(isLoading = false, hasError = true) }
        },
        ifRight = { claimFlowStep ->
          _uiState.update { it.copy(isLoading = false, nextStep = claimFlowStep) }
        },
      )
    }
  }

  fun selectBrand(itemBrand: ItemBrand) {
    val oldItemBrandsUiState = _uiState.value.itemBrandsUiState.asContent() ?: return
    _uiState.update {
      it.copy(
        itemBrandsUiState = oldItemBrandsUiState.copy(
          selectedItemBrand = itemBrand,
        ),
      )
    }
  }

  fun selectModel(itemModel: ItemModel) {
    val oldItemModelsUiState = _uiState.value.itemModelsUiState.asContent() ?: return
    _uiState.update {
      it.copy(
        itemModelsUiState = oldItemModelsUiState.copy(
          selectedItemModel = itemModel,
        ),
      )
    }
  }

  fun selectProblem(itemProblem: ItemProblem) {
    val oldItemProblemsUiState = _uiState.value.itemProblemsUiState.asContent() ?: return
    val oldSelectedItemProblems = oldItemProblemsUiState.selectedItemProblems
    val newSelectedItemProblems = if (itemProblem in oldSelectedItemProblems) {
      oldSelectedItemProblems.minus(itemProblem)
    } else {
      oldSelectedItemProblems.plus(itemProblem)
    }
    _uiState.update {
      it.copy(
        itemProblemsUiState = oldItemProblemsUiState.copy(
          selectedItemProblems = newSelectedItemProblems,
        ),
      )
    }
  }

  fun handledNextStepNavigation() {
    _uiState.update {
      it.copy(nextStep = null)
    }
  }

  fun showedError() {
    _uiState.update {
      it.copy(hasError = false)
    }
  }
}

internal data class SingleItemUiState(
  val datePickerUiState: DatePickerUiState,
  val purchasePriceUiState: PurchasePriceUiState,
  val itemBrandsUiState: ItemBrandsUiState,
  val itemModelsUiState: ItemModelsUiState,
  val itemProblemsUiState: ItemProblemsUiState,
  val isLoading: Boolean = false,
  val hasError: Boolean = false,
  val nextStep: ClaimFlowStep? = null,
) {
  val canSubmit: Boolean
    get() = !isLoading && !hasError && nextStep == null

  companion object {
    fun fromInitialSingleItem(singleItem: ClaimFlowDestination.SingleItem, resources: Resources, clock: Clock): SingleItemUiState {
      return SingleItemUiState(
        datePickerUiState = DatePickerUiState(
          initiallySelectedDate = singleItem.purchaseDate,
          maxDate = clock.now().toLocalDateTime(TimeZone.UTC).date,
        ),
        purchasePriceUiState = PurchasePriceUiState(singleItem.purchasePrice?.amount, singleItem.preferredCurrency),
        itemBrandsUiState = ItemBrandsUiState.fromSingleItem(singleItem, resources),
        itemModelsUiState = ItemModelsUiState.fromSingleItem(singleItem, resources),
        itemProblemsUiState = ItemProblemsUiState.fromSingleItem(singleItem),
      )
    }
  }
}

internal class PurchasePriceUiState(amount: Double?, preferredCurrency: CurrencyCode) {
  var uiMoney: UiMoney by mutableStateOf(UiMoney(amount, preferredCurrency))
    private set

  fun updateAmount(amount: String?) {
    uiMoney = if (amount == null) {
      uiMoney.copy(amount = null)
    } else {
      val amountAsDouble = amount.toDoubleOrNull() ?: return
      uiMoney.copy(amount = amountAsDouble)
    }
  }
}

internal sealed interface ItemBrandsUiState {
  fun asContent(): Content? = this as? Content

  object NotApplicable : ItemBrandsUiState

  data class Content(
    val availableItemBrands: NonEmptyList<ItemBrand>,
    val selectedItemBrand: ItemBrand?,
  ) : ItemBrandsUiState

  companion object {
    fun fromSingleItem(singleItem: ClaimFlowDestination.SingleItem, resources: Resources): ItemBrandsUiState {
      val availableItemBrands = singleItem.availableItemBrands?.toNonEmptyListOrNull() ?: return NotApplicable
      val selectedItemBrand = availableItemBrands.firstOrNull { availableItemBrand: ItemBrand ->
        availableItemBrand.asKnown()?.itemBrandId == singleItem.selectedItemBrand
      }
      val notSureBrand = ItemBrand.Unknown(
        resources.getString(hedvig.resources.R.string.GENERAL_NOT_SURE),
      )
      return Content(
        availableItemBrands.plus(notSureBrand),
        selectedItemBrand,
      )
    }
  }
}

internal sealed interface ItemModelsUiState {
  fun asContent(): Content? = this as? Content

  object NotApplicable : ItemModelsUiState

  data class Content(
    val availableItemModels: NonEmptyList<ItemModel>,
    val selectedItemModel: ItemModel?,
  ) : ItemModelsUiState

  companion object {
    fun fromSingleItem(singleItem: ClaimFlowDestination.SingleItem, resources: Resources): ItemModelsUiState {
      val availableItemModels = singleItem.availableItemModels?.toNonEmptyListOrNull() ?: return NotApplicable
      val selectedItemModel = availableItemModels.firstOrNull { availableItemModel ->
        availableItemModel.asKnown()?.itemModelId == singleItem.selectedItemModel
      }
      val notSureModel = ItemModel.Unknown(
        resources.getString(hedvig.resources.R.string.GENERAL_NOT_SURE),
      )
      return Content(
        availableItemModels.plus(notSureModel),
        selectedItemModel,
      )
    }
  }
}

internal sealed interface ItemProblemsUiState {
  fun asContent(): Content? = this as? Content

  object NotApplicable : ItemProblemsUiState

  data class Content(
    val availableItemProblems: NonEmptyList<ItemProblem>,
    val selectedItemProblems: List<ItemProblem>,
  ) : ItemProblemsUiState

  companion object {
    fun fromSingleItem(singleItem: ClaimFlowDestination.SingleItem): ItemProblemsUiState {
      val availableItemProblems: NonEmptyList<ItemProblem> =
        singleItem.availableItemProblems?.toNonEmptyListOrNull() ?: return NotApplicable
      val selectedItemProblems: List<ItemProblem> = if (singleItem.selectedItemProblems == null) {
        emptyList()
      } else {
        singleItem.selectedItemProblems.mapNotNull { selectedItemProblemId ->
          availableItemProblems.firstOrNull { it.itemProblemId == selectedItemProblemId }
        }
      }
      return Content(
        availableItemProblems,
        selectedItemProblems,
      )
    }
  }
}
