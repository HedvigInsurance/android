package com.hedvig.android.feature.addon.purchase.ui.summary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.tracking.ActionType
import com.hedvig.android.core.tracking.logAction
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.addons.data.AddonBannerSource
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.feature.addon.purchase.data.CurrentlyActiveAddon
import com.hedvig.android.feature.addon.purchase.data.SubmitAddonPurchaseUseCase
import com.hedvig.android.feature.addon.purchase.data.AddonQuote
import com.hedvig.android.feature.addon.purchase.navigation.AddonType
import com.hedvig.android.feature.addon.purchase.navigation.SummaryParameters
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonLogInfo.AddonEventType
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState.Content
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState.Loading
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import kotlin.math.log
import kotlinx.datetime.LocalDate

internal class AddonSummaryViewModel(
    summaryParameters: SummaryParameters,
    addonPurchaseSource: AddonBannerSource,
    submitAddonPurchaseUseCase: SubmitAddonPurchaseUseCase,
) : MoleculeViewModel<AddonSummaryEvent, AddonSummaryState>(
    initialState = getInitialState(summaryParameters),
    presenter = AddonSummaryPresenter(
      summaryParameters,
      submitAddonPurchaseUseCase,
      addonPurchaseSource,
    ),
  )

internal class AddonSummaryPresenter(
    private val summaryParameters: SummaryParameters,
    private val submitAddonPurchaseUseCase: SubmitAddonPurchaseUseCase,
    private val addonPurchaseSource: AddonBannerSource,
) : MoleculePresenter<AddonSummaryEvent, AddonSummaryState> {
  @Composable
  override fun MoleculePresenterScope<AddonSummaryEvent>.present(lastState: AddonSummaryState): AddonSummaryState {
    var submitIteration by remember { mutableIntStateOf(0) }
    var currentState by remember { mutableStateOf(lastState) }
    var activationDateForNavigation  by remember { mutableStateOf<LocalDate?>(null) }
    var errorForNavigation  by remember { mutableStateOf<ErrorMessage?>(null) }

    CollectEvents { event ->
      when (event) {
        AddonSummaryEvent.Submit -> submitIteration++
        AddonSummaryEvent.ReturnToInitialState -> {
          activationDateForNavigation = null
          errorForNavigation = null
        }
      }
    }

    LaunchedEffect(submitIteration) {
      val state = currentState as? Content ?: return@LaunchedEffect
      if (submitIteration > 0) {
        currentState = Loading()
        submitAddonPurchaseUseCase.invoke(
          quoteId = summaryParameters.quoteId,
          addonIds = summaryParameters.chosenQuotes.map{
            it.addonId
          },
        ).fold(
          ifLeft = {
            errorForNavigation = it
            currentState = state
          },
          ifRight = {
            logSuccessfulAddonPurchaseAction(summaryParameters, addonPurchaseSource)
            errorForNavigation = null
            activationDateForNavigation = summaryParameters.activationDate
          },
        )
      }
    }
    return when(val state = currentState) {
      is Content -> state.copy (
        navigateToFailure = errorForNavigation)
      is Loading -> state.copy(
        activationDateToNavigateToSuccess = activationDateForNavigation
      )
    }
  }
}

internal fun getInitialState(summaryParameters: SummaryParameters): Content {
  return Content(
    insuranceDisplayName = summaryParameters.productVariant.displayName,
    quotes = summaryParameters.chosenQuotes,
    activationDate = summaryParameters.activationDate,
    currentlyActiveAddons = summaryParameters.currentlyActiveAddons,
    insuranceExposure = null, //todo
    notificationMessage = summaryParameters.notificationMessage,
    documents = summaryParameters.productVariant.documents,
    costBreakdownWithExtras = getCostBreakdownWithExtras(
      baseCost = summaryParameters.baseInsuranceCost,
      quotes = summaryParameters.chosenQuotes,
      insuranceDisplayName = summaryParameters.productVariant.displayName,
      existingAddons = summaryParameters.currentlyActiveAddons,
      addonType = summaryParameters.addonType
    ),
    displayItems = summaryParameters.chosenQuotes.flatMap {
      it.displayDetails
    },
    navigateToFailure = null,
    contractGroup = summaryParameters.productVariant.contractGroup
  )
}

internal fun getCostBreakdownWithExtras(
  insuranceDisplayName: String,
  baseCost: ItemCost,
  existingAddons: List<CurrentlyActiveAddon>,
  quotes: List<AddonQuote>,
  addonType: AddonType
): CostBreakdownWithExtras {
  val baseInsuranceGross = insuranceDisplayName to baseCost.monthlyGross
  val addonsGross = quotes.map {
    it.displayTitle to it.itemCost.monthlyGross
  } //todo: continue here for display items
  val baseCurrency = baseCost.monthlyNet.currencyCode
  val extraSum = quotes.sumOf {
    it.itemCost.monthlyNet.amount
  }.let {
    when (addonType) {
      AddonType.SELECTABLE -> it - existingAddons.sumOf { existing ->
        existing.cost.monthlyNet.amount
      }
      AddonType.TOGGLEABLE -> it
    }
  }

  val totalExtra = UiMoney(extraSum, baseCurrency)
  val totalGross = baseCost.monthlyGross.amount + existingAddons.sumOf {
    it.cost.monthlyGross.amount
  } + quotes.sumOf { it.itemCost.monthlyGross.amount }
  val totalNet = baseCost.monthlyNet.amount + existingAddons.sumOf {
    it.cost.monthlyNet.amount
  } + quotes.sumOf { it.itemCost.monthlyNet.amount }
  return CostBreakdownWithExtras(
    totalCost = ItemCost(
      monthlyNet = UiMoney(
        totalNet, baseCurrency
      ),
      monthlyGross = UiMoney(
        totalGross, baseCurrency
      ),
      discounts = emptyList() //todo: change when BE allows!!!
    ),
    totalExtra = totalExtra,
    displayItems = emptyList() //todo: change when BE allows!!!
  )
}

internal data class CostBreakdownWithExtras(
  val totalCost: ItemCost,
  val totalExtra: UiMoney,
  val displayItems: List<Pair<String, UiMoney>>
)

internal sealed interface AddonSummaryState {
  data class Loading(
    val activationDateToNavigateToSuccess: LocalDate? = null
  ) : AddonSummaryState

  data class Content(
    val insuranceDisplayName: String,
    val insuranceExposure: String?, //todo: add separate query
    val contractGroup: ContractGroup?,
    val quotes: List<AddonQuote>,
    val activationDate: LocalDate,
    val currentlyActiveAddons: List<CurrentlyActiveAddon>,
    val notificationMessage: String?,
    val documents: List<InsuranceVariantDocument>,
    val costBreakdownWithExtras: CostBreakdownWithExtras?,
    val displayItems: List<Pair<String,String>>, //todo: check how those look
    val navigateToFailure: ErrorMessage? = null,
  ) : AddonSummaryState
}



internal sealed interface AddonSummaryEvent {
  data object Submit : AddonSummaryEvent

  data object ReturnToInitialState : AddonSummaryEvent
}

private fun logSuccessfulAddonPurchaseAction(
    summaryParameters: SummaryParameters,
    addonPurchaseSource: AddonBannerSource,
) {
  summaryParameters.chosenQuotes.forEach { chosenQuote ->
    chosenQuote.addonSubtype?.let {
      //todo: review later when will have new entrypoints. Prob new addonPurchaseSource, "product"?
      val logInfo = AddonLogInfo(
        flow = addonPurchaseSource,
        subType = chosenQuote.addonSubtype
      )
      val eventType = if (summaryParameters.currentlyActiveAddons.isEmpty()) {
        AddonEventType.ADDON_PURCHASED
      } else {
        AddonEventType.ADDON_UPGRADED
      }
      logAction(type = ActionType.CUSTOM, name = eventType.name, attributes = logInfo.asAddonAttributes())
    }
  }

}

private data class AddonLogInfo(
    val flow: AddonBannerSource,
    val subType: String
) {
  val type = "travelAddon"

  enum class AddonEventType {
    ADDON_PURCHASED,
    ADDON_UPGRADED,
  }
}

private fun AddonLogInfo.asAddonAttributes(): Map<String, Map<String, String>> {
  return mapOf(
    "addon" to
      mapOf(
        "flow" to this.flow.name,
        "subType" to this.subType,
        "type" to this.type,
      ),
  )
}
