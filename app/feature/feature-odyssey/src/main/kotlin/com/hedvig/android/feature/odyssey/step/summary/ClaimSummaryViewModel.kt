package com.hedvig.android.feature.odyssey.step.summary

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.core.ui.hedvigDateTimeFormatter
import com.hedvig.android.core.uidata.UiNullableMoney
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.ItemBrand
import com.hedvig.android.data.claimflow.ItemModel
import com.hedvig.android.data.claimflow.ItemProblem
import com.hedvig.android.data.claimflow.LocationOption
import hedvig.resources.R
import java.util.Locale
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import octopus.type.FlowClaimItemBrandInput
import octopus.type.FlowClaimItemModelInput

internal class ClaimSummaryViewModel(
  private val summary: ClaimFlowDestination.Summary,
  private val claimFlowRepository: ClaimFlowRepository,
) : ViewModel() {
  private val infoUiState: ClaimSummaryInfoUiState = ClaimSummaryInfoUiState.fromSummary(summary)
  private val statusUiState: MutableStateFlow<ClaimSummaryStatusUiState> = MutableStateFlow(ClaimSummaryStatusUiState())

  val uiState: StateFlow<ClaimSummaryUiState> = statusUiState.map { statusUiState ->
    ClaimSummaryUiState(infoUiState, statusUiState)
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5.seconds),
    ClaimSummaryUiState(infoUiState, statusUiState.value),
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
  // e.g "Broken Phone"
  val claimTypeTitle: String?,
  val dateOfIncident: LocalDate?,
  val locationOption: LocationOption?,
  val itemType: ItemType?,
  val dateOfPurchase: LocalDate?,
  val priceOfPurchase: UiNullableMoney?,
  val itemProblems: List<ItemProblem>,
) {
  fun itemDetailPairs(resources: Resources, locale: Locale): List<Pair<String, String>> {
    return buildList {
      // Ärende
      if (claimTypeTitle != null) {
        add(resources.getString(R.string.CLAIMS_CASE) to claimTypeTitle)
      }
      // Skadetyp
      val incidentTypeText = if (itemProblems.isNotEmpty()) {
        itemProblems.joinToString { it.displayName }
      } else {
        "-"
      }
      add(resources.getString(R.string.CLAIMS_DAMAGES) to incidentTypeText)
      // Skadedatum
      val incidentDateText = if (dateOfIncident != null) {
        dateOfIncident.toJavaLocalDate().format(hedvigDateTimeFormatter(locale))
      } else {
        "-"
      }
      add(resources.getString(R.string.claims_item_screen_date_of_incident_button) to incidentDateText)
      // Plats
      val locationText = if (locationOption != null) {
        locationOption.displayName
      } else {
        "-"
      }
      add(resources.getString(R.string.claims_location_screen_title) to locationText)
      // Modell
      if (itemType != null) {
        val isKnownBrand = (itemType as? ItemType.Brand)?.itemBrand?.asKnown() != null
        val isKnownModel = (itemType as? ItemType.Model)?.itemModel?.asKnown() != null
        if (isKnownBrand || isKnownModel) {
          add(resources.getString(R.string.claims_item_screen_model_button) to itemType.displayName(resources))
        }
      }
      // Inköpsdatum
      val purchaseDateText = if (dateOfPurchase != null) {
        dateOfPurchase.toJavaLocalDate().format(hedvigDateTimeFormatter(locale))
      } else {
        "-"
      }
      add(resources.getString(R.string.claims_item_screen_date_of_purchase_button) to purchaseDateText)
      // Inköpspris
      val purchasePriceText = if (priceOfPurchase?.amount != null) {
        "${priceOfPurchase.amount!!.toInt()} ${priceOfPurchase.currencyCode}"
      } else {
        "-"
      }
      add(resources.getString(R.string.claims_payout_purchase_price) to purchasePriceText)
    }
  }

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
    fun fromSummary(summary: ClaimFlowDestination.Summary): ClaimSummaryInfoUiState {
      return ClaimSummaryInfoUiState(
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
