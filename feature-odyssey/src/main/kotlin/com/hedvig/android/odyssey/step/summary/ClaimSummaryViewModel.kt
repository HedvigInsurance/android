package com.hedvig.android.odyssey.step.summary

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.odyssey.data.ClaimFlowRepository
import com.hedvig.android.odyssey.data.ClaimFlowStep
import com.hedvig.android.odyssey.navigation.ClaimFlowDestination
import com.hedvig.android.odyssey.navigation.ItemBrand
import com.hedvig.android.odyssey.navigation.ItemModel
import com.hedvig.android.odyssey.navigation.ItemProblem
import com.hedvig.android.odyssey.navigation.LocationOption
import com.hedvig.android.odyssey.navigation.UiNullableMoney
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import octopus.type.FlowClaimItemBrandInput
import octopus.type.FlowClaimItemModelInput
import kotlin.time.Duration.Companion.seconds

internal class ClaimSummaryViewModel(
  private val summary: ClaimFlowDestination.Summary,
  private val claimFlowRepository: ClaimFlowRepository,
) : ViewModel() {

  private val infoUiState: MutableStateFlow<ClaimSummaryInfoUiState> =
    MutableStateFlow(ClaimSummaryInfoUiState.fromSummary(summary))
  private val statusUiState: MutableStateFlow<ClaimSummaryStatusUiState> = MutableStateFlow(ClaimSummaryStatusUiState())

  val uiState: StateFlow<ClaimSummaryUiState> = combine(infoUiState, statusUiState) { infoUiState, statusUiState ->
    ClaimSummaryUiState(infoUiState, statusUiState)
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5.seconds),
    ClaimSummaryUiState(infoUiState.value, statusUiState.value),
  )

  fun submitSummary() {
    if (uiState.value.canSubmit.not()) return
    statusUiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      val itemModelInput: FlowClaimItemModelInput? = run {
        summary.availableItemModels ?: return@run null
        val selectedItemModel = summary.selectedItemModel ?: return@run null
        FlowClaimItemModelInput(selectedItemModel)
      }
      val itemBrandInput: FlowClaimItemBrandInput? = run {
        if (itemModelInput != null) return@run null
        val availableItemBrands = summary.availableItemBrands ?: return@run null
        val selectedItemBrand = summary.selectedItemBrand ?: return@run null
        val selectedItemType = availableItemBrands
          .filterIsInstance<ItemBrand.Known>()
          .firstOrNull { itemBrand ->
            itemBrand.asKnown()?.itemBrandId == selectedItemBrand
          } ?: return@run null
        FlowClaimItemBrandInput(
          itemTypeId = selectedItemType.itemTypeId,
          itemBrandId = selectedItemBrand,
        )
      }
      claimFlowRepository.submitSummary(
        summary.dateOfOccurrence,
        itemBrandInput,
        itemModelInput,
        summary.selectedItemProblems,
        summary.selectedLocation,
        summary.purchaseDate,
        summary.purchasePrice?.amount,
      ).fold(
        ifLeft = {
          statusUiState.update {
            it.copy(isLoading = false, hasError = true)
          }
        },
        ifRight = { claimFlowStep ->
          statusUiState.update {
            it.copy(isLoading = false, nextStep = claimFlowStep)
          }
        },
      )
    }
  }

  fun handledNextStepNavigation() {
    statusUiState.update {
      it.copy(nextStep = null)
    }
  }

  fun showedError() {
    statusUiState.update {
      it.copy(hasError = false)
    }
  }
}

internal data class ClaimSummaryUiState(
  val claimSummaryInfoUiState: ClaimSummaryInfoUiState,
  val claimSummaryStatusUiState: ClaimSummaryStatusUiState,
) {
  val canSubmit: Boolean
    get() = claimSummaryStatusUiState.canSubmit
}

internal data class ClaimSummaryStatusUiState(
  val isLoading: Boolean = false,
  val hasError: Boolean = false,
  val nextStep: ClaimFlowStep? = null,
) {
  val canSubmit: Boolean
    get() = !isLoading && !hasError && nextStep == null
}

internal data class ClaimSummaryInfoUiState(
  val imageUrl: String?,
  val claimTypeTitle: String?, // e.g "Broken Phone"
  val dateOfIncident: LocalDate?,
  val locationOption: LocationOption?,
  val itemType: ItemType?,
  val dateOfPurchase: LocalDate?,
  val priceOfPurchase: UiNullableMoney?,
  val itemProblems: List<ItemProblem>,
) {
  sealed interface ItemType {

    fun displayName(resources: Resources): String {
      return when (this) {
        is Brand -> itemBrand.displayName(resources)
        is Model -> itemModel.displayName(resources)
      }
    }

    data class Brand(val itemBrand: ItemBrand) : ItemType
    data class Model(val itemModel: ItemModel) : ItemType

    companion object {
      fun fromSummary(summary: ClaimFlowDestination.Summary): ItemType? {
        val selectedModel = summary.availableItemModels?.firstOrNull {
          it.asKnown()?.itemModelId == summary.selectedItemModel
        }
        if (selectedModel != null) {
          return Model(selectedModel)
        }
        val selectedBrand = summary.availableItemBrands?.firstOrNull {
          it.asKnown()?.itemBrandId == summary.selectedItemBrand
        }
        if (selectedBrand != null) return Brand(selectedBrand)
        return null
      }
    }
  }

  companion object {
    fun fromSummary(
      summary: ClaimFlowDestination.Summary,
    ): ClaimSummaryInfoUiState {
      return ClaimSummaryInfoUiState(
        imageUrl = summary.availableItemModels
          ?.firstOrNull { it.asKnown()?.itemModelId == summary.selectedItemModel }
          ?.asKnown()
          ?.imageUrl,
        claimTypeTitle = summary.claimTypeTitle,
        dateOfIncident = summary.dateOfOccurrence,
        locationOption = summary.locationOptions.firstOrNull { it.value == summary.selectedLocation },
        itemType = ItemType.fromSummary(summary),
        dateOfPurchase = summary.purchaseDate,
        priceOfPurchase = summary.purchasePrice,
        itemProblems = summary.availableItemProblems
          ?.filter { summary.selectedItemProblems?.contains(it.itemProblemId) == true }
          ?: emptyList(),
      )
    }
  }
}
