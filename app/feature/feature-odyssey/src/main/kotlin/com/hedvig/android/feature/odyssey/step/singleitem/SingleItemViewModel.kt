package com.hedvig.android.feature.odyssey.step.singleitem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import com.hedvig.android.core.uidata.UiNullableMoney
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.ItemBrand
import com.hedvig.android.data.claimflow.ItemModel
import com.hedvig.android.data.claimflow.ItemProblem
import com.hedvig.android.feature.odyssey.ui.DatePickerUiState
import com.hedvig.android.language.LanguageService
import java.util.Locale
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
  clock: Clock,
  languageService: LanguageService,
) : ViewModel() {
  private val itemBrandsUiState: MutableStateFlow<ItemBrandsUiState> =
    MutableStateFlow(ItemBrandsUiState.fromSingleItem(singleItem))
  private val itemModelsUiState: MutableStateFlow<ItemModelsUiState> =
    MutableStateFlow(ItemModelsUiState.fromSingleItem(singleItem))

  // Holds most of what [uiState] does, minus what's inside brands and models UiState above
  private val partialUiState: MutableStateFlow<PartialSingleItemUiState> =
    MutableStateFlow(PartialSingleItemUiState.fromSingleItem(singleItem, languageService.getLocale(), clock))

  val uiState: StateFlow<SingleItemUiState> = combine(
    itemBrandsUiState,
    itemModelsUiState,
    ::transformItemModelsToOnlyContainModelsOfTheSelectedBrand,
  ).combine(partialUiState) { (itemBrandsUiState, itemModelsUiState), partialUiState ->
    SingleItemUiState(
      partialUiState.datePickerUiState,
      partialUiState.purchasePriceUiState,
      itemBrandsUiState,
      itemModelsUiState,
      partialUiState.itemProblemsUiState,
      partialUiState.isLoading,
      partialUiState.hasError,
      partialUiState.nextStep,
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5.seconds),
    SingleItemUiState.fromInitialSingleItem(singleItem, languageService.getLocale(), clock),
  )

  private fun transformItemModelsToOnlyContainModelsOfTheSelectedBrand(
    itemBrandsUiState: ItemBrandsUiState,
    itemModelsUiState: ItemModelsUiState,
  ): Pair<ItemBrandsUiState, ItemModelsUiState> {
    val selectedBrandId = itemBrandsUiState
      .asContent()
      ?.selectedItemBrand
      ?.asKnown()
      ?.itemBrandId
      ?: return itemBrandsUiState to itemModelsUiState

    val availableItemModelsWithSelectedBrands: List<ItemModel> = itemModelsUiState
      .availableItemModels
      .filter { itemModel ->
        when (itemModel) {
          is ItemModel.Unknown -> true
          is ItemModel.Known -> itemModel.itemBrandId == selectedBrandId
          is ItemModel.New -> true // todo: but it's never in this list, it can only be selectedItemModel
        }
      }
      .toList()

    return itemBrandsUiState to itemModelsUiState.copy(
      availableItemModels = availableItemModelsWithSelectedBrands,
      selectedItemModel = itemModelsUiState.selectedItemModel,
    )
  }

  fun submitSelections() {
    val uiState = uiState.value
    if (uiState.canSubmit.not()) return
    partialUiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      val itemModelInput = run {
        val itemModelUiState = uiState.itemModelsUiState
        val selectedItemModel = itemModelUiState.selectedItemModel?.asKnown() ?: return@run null
        FlowClaimItemModelInput(selectedItemModel.itemModelId)
      }
      val customNameInput = run {
        val selectedItemModel = uiState.itemModelsUiState.selectedItemModel?.asNew() ?: return@run null
        selectedItemModel.displayName
      }
      val itemBrandInput = run {
        // If there is a specific model selected, brand must be null as the input should only have one or the other and
        //  we prefer the more specific, meaning the model instead of just the generic brand.
        // todo: this comment above: do we still want it or not? why must it be one or another?
        // and I guess we do not want it, if the model is custom -
        // because we would not have the brand name if our member chooses a brand, then writes a model themselves - they
        // won't write the brand name again
        if (itemModelInput != null) return@run null
        val itemBrandsUiState = uiState.itemBrandsUiState.asContent()
        val selectedItemBrand = itemBrandsUiState?.selectedItemBrand?.asKnown() ?: return@run null
        FlowClaimItemBrandInput(
          itemTypeId = selectedItemBrand.itemTypeId,
          itemBrandId = selectedItemBrand.itemBrandId,
        )
      }
      // todo: should we make sure that we have at least one of itemModelInput, customNameInput, itemBrandInput? all nullable
      claimFlowRepository.submitSingleItem(
        itemBrandInput = itemBrandInput,
        itemModelInput = itemModelInput,
        customName = customNameInput,
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
          partialUiState.update { it.copy(isLoading = false, hasError = true) }
        },
        ifRight = { claimFlowStep ->
          partialUiState.update { it.copy(isLoading = false, nextStep = claimFlowStep) }
        },
      )
    }
  }

  /**
   * Selects the ItemBrand. If there was a selected itemModel, it clears it so that there are no possible issues with
   * selecting a brand which does not match the selected [itemBrand]
   */
  fun selectBrand(itemBrand: ItemBrand) {
    val currentItemBrandsUiState = itemBrandsUiState.value.asContent() ?: return
    itemBrandsUiState.update { currentItemBrandsUiState.copy(selectedItemBrand = itemBrand) }
    itemModelsUiState.update { modelsUiState ->
      modelsUiState.copy(
        selectedItemModel = null,
      )
    }
  }

  /**
   * Selects the ItemModel. Also makes sure that the correct brand is selected if there is one available matching the
   * brandId inside the [itemModel] passed here.
   */
  fun selectModel(itemModel: ItemModel) {
    itemModelsUiState.update { itemModelsUiState ->
      itemModelsUiState.copy(selectedItemModel = itemModel)
    }
    if (itemModel !is ItemModel.Known) return
    val currentItemBrandsUiState: ItemBrandsUiState.Content = itemBrandsUiState.value.asContent() ?: return
    val matchingBrandToSelectedItemModel: ItemBrand.Known = currentItemBrandsUiState
      .availableItemBrands
      .filterIsInstance<ItemBrand.Known>()
      .firstOrNull { itemBrand ->
        itemBrand.itemBrandId == itemModel.itemBrandId
      } ?: return
    itemBrandsUiState.update {
      currentItemBrandsUiState.copy(
        selectedItemBrand = matchingBrandToSelectedItemModel,
      )
    }
  }

  fun selectProblem(itemProblem: ItemProblem) {
    val oldItemProblemsUiState = partialUiState.value.itemProblemsUiState.asContent() ?: return
    val oldSelectedItemProblems = oldItemProblemsUiState.selectedItemProblems
    val newSelectedItemProblems = if (itemProblem in oldSelectedItemProblems) {
      oldSelectedItemProblems.minus(itemProblem)
    } else {
      oldSelectedItemProblems.plus(itemProblem)
    }
    partialUiState.update {
      it.copy(
        itemProblemsUiState = oldItemProblemsUiState.copy(
          selectedItemProblems = newSelectedItemProblems,
        ),
      )
    }
  }

  fun handledNextStepNavigation() {
    partialUiState.update {
      it.copy(nextStep = null)
    }
  }

  fun showedError() {
    partialUiState.update {
      it.copy(hasError = false)
    }
  }
}

internal data class PartialSingleItemUiState(
  val datePickerUiState: DatePickerUiState,
  val purchasePriceUiState: PurchasePriceUiState,
  val itemProblemsUiState: ItemProblemsUiState,
  val isLoading: Boolean = false,
  val hasError: Boolean = false,
  val nextStep: ClaimFlowStep? = null,
) {
  companion object {
    fun fromSingleItem(
      singleItem: ClaimFlowDestination.SingleItem,
      locale: Locale,
      clock: Clock,
    ): PartialSingleItemUiState {
      return PartialSingleItemUiState(
        datePickerUiState = DatePickerUiState(
          locale = locale,
          initiallySelectedDate = singleItem.purchaseDate,
          maxDate = clock.now().toLocalDateTime(TimeZone.UTC).date,
        ),
        purchasePriceUiState = PurchasePriceUiState(singleItem.purchasePrice?.amount, singleItem.preferredCurrency),
        itemProblemsUiState = ItemProblemsUiState.fromSingleItem(singleItem),
      )
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
    fun fromInitialSingleItem(
      singleItem: ClaimFlowDestination.SingleItem,
      locale: Locale,
      clock: Clock,
    ): SingleItemUiState {
      return SingleItemUiState(
        datePickerUiState = DatePickerUiState(
          locale = locale,
          initiallySelectedDate = singleItem.purchaseDate,
          maxDate = clock.now().toLocalDateTime(TimeZone.UTC).date,
        ),
        purchasePriceUiState = PurchasePriceUiState(singleItem.purchasePrice?.amount, singleItem.preferredCurrency),
        itemBrandsUiState = ItemBrandsUiState.fromSingleItem(singleItem),
        itemModelsUiState = ItemModelsUiState.fromSingleItem(singleItem),
        itemProblemsUiState = ItemProblemsUiState.fromSingleItem(singleItem),
      )
    }
  }
}

internal class PurchasePriceUiState(amount: Double?, preferredCurrency: CurrencyCode) {
  var uiMoney: UiNullableMoney by mutableStateOf(UiNullableMoney(amount, preferredCurrency))
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
    fun fromSingleItem(singleItem: ClaimFlowDestination.SingleItem): ItemBrandsUiState {
      val availableItemBrands = singleItem.availableItemBrands?.toNonEmptyListOrNull() ?: return NotApplicable
      val selectedItemBrand = availableItemBrands.firstOrNull { availableItemBrand: ItemBrand ->
        availableItemBrand.asKnown()?.itemBrandId == singleItem.selectedItemBrand
      }
      val notSureBrand = ItemBrand.Unknown
      return Content(
        availableItemBrands.plus(notSureBrand),
        selectedItemBrand,
      )
    }
  }
}

internal data class ItemModelsUiState(
  val availableItemModels: List<ItemModel>,
  val selectedItemModel: ItemModel?,
) {
  companion object {
    fun fromSingleItem(singleItem: ClaimFlowDestination.SingleItem): ItemModelsUiState {
      val availableItemModels = singleItem.availableItemModels?.toMutableList() ?: listOf()
      val selectedItemModel = availableItemModels.firstOrNull { availableItemModel ->
        availableItemModel.asKnown()?.itemModelId == singleItem.selectedItemModel
      }
      val otherModel = ItemModel.Unknown
      return ItemModelsUiState(
        availableItemModels.plus(otherModel),
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
      val selectedItemProblems: List<ItemProblem> = singleItem.selectedItemProblems
        .orEmpty()
        .mapNotNull { selectedItemProblemId ->
          availableItemProblems.firstOrNull { it.itemProblemId == selectedItemProblemId }
        }
      return Content(
        availableItemProblems,
        selectedItemProblems,
      )
    }
  }
}
