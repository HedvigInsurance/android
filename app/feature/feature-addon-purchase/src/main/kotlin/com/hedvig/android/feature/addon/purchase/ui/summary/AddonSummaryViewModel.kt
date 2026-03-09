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
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.addons.data.AddonBannerSource
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.feature.addon.purchase.data.AddonQuote
import com.hedvig.android.feature.addon.purchase.data.CurrentlyActiveAddon
import com.hedvig.android.feature.addon.purchase.data.GetInsuranceForTravelAddonUseCase
import com.hedvig.android.feature.addon.purchase.data.GetQuoteCostBreakdownUseCase
import com.hedvig.android.feature.addon.purchase.data.SubmitAddonPurchaseUseCase
import com.hedvig.android.feature.addon.purchase.navigation.AddonType
import com.hedvig.android.feature.addon.purchase.navigation.SummaryParameters
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonLogInfo.AddonEventType
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState.Content
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState.Loading
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.ui.tiersandaddons.CostBreakdownEntry
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.LocalDate

internal class AddonSummaryViewModel(
  summaryParameters: SummaryParameters,
  addonPurchaseSource: AddonBannerSource,
  submitAddonPurchaseUseCase: SubmitAddonPurchaseUseCase,
  getQuoteCostBreakdownUseCase: GetQuoteCostBreakdownUseCase,
  getInsuranceForTravelAddonUseCase: GetInsuranceForTravelAddonUseCase,
) : MoleculeViewModel<AddonSummaryEvent, AddonSummaryState>(
    initialState = Loading(),
    presenter = AddonSummaryPresenter(
      summaryParameters,
      submitAddonPurchaseUseCase,
      addonPurchaseSource,
      getQuoteCostBreakdownUseCase,
      getInsuranceForTravelAddonUseCase,
    ),
  )

internal class AddonSummaryPresenter(
  private val summaryParameters: SummaryParameters,
  private val submitAddonPurchaseUseCase: SubmitAddonPurchaseUseCase,
  private val addonPurchaseSource: AddonBannerSource,
  private val getQuoteCostBreakdownUseCase: GetQuoteCostBreakdownUseCase,
  private val getInsuranceForTravelAddonUseCase: GetInsuranceForTravelAddonUseCase,
) : MoleculePresenter<AddonSummaryEvent, AddonSummaryState> {
  @Composable
  override fun MoleculePresenterScope<AddonSummaryEvent>.present(lastState: AddonSummaryState): AddonSummaryState {
    var submitIteration by remember { mutableIntStateOf(0) }
    var loadIteration by remember { mutableIntStateOf(0) }
    var currentState by remember { mutableStateOf(lastState) }
    var activationDateForNavigation by remember { mutableStateOf<LocalDate?>(null) }
    var errorForNavigation by remember { mutableStateOf<ErrorMessage?>(null) }

    CollectEvents { event ->
      when (event) {
        AddonSummaryEvent.Submit -> {
          submitIteration++
        }

        AddonSummaryEvent.ReturnToInitialState -> {
          activationDateForNavigation = null
          errorForNavigation = null
        }

        AddonSummaryEvent.Reload -> {
          loadIteration++
        }
      }
    }

    LaunchedEffect(loadIteration) {
      val exposureName = getInsuranceForTravelAddonUseCase
        .invoke(listOf(summaryParameters.contractId))
        .firstOrNull()?.getOrNull()?.firstOrNull()?.contractExposure
      if (exposureName == null) {
        currentState = AddonSummaryState.Error
        return@LaunchedEffect
      }
      getQuoteCostBreakdownUseCase.invoke(
        quoteId = summaryParameters.quoteId,
        existingAddons = summaryParameters.currentlyActiveAddons,
        newAddons = summaryParameters.chosenQuotes,
        baseCost = summaryParameters.baseInsuranceCost,
        insuranceDisplayName = summaryParameters.productVariant.displayName,
        addonType = summaryParameters.addonType,
      ).fold(
        ifLeft = {
          currentState = AddonSummaryState.Error
        },
        ifRight = {
          val baseCurrency = summaryParameters.baseInsuranceCost.monthlyNet.currencyCode
          val extraSum = summaryParameters.chosenQuotes.sumOf { chosenQuote ->
            chosenQuote.itemCost.monthlyNet.amount
          }.let { sum ->
            when (summaryParameters.addonType) {
              AddonType.SELECTABLE -> sum - summaryParameters.currentlyActiveAddons.sumOf { existing ->
                existing.cost.monthlyNet.amount
              }

              AddonType.TOGGLEABLE -> sum
            }
          }
          val totalExtra = UiMoney(extraSum, baseCurrency)
          currentState = getInitialState(
            summaryParameters = summaryParameters,
            exposureName = exposureName,
            costBreakdownWithExtras = CostBreakdownWithExtras(
              totalMonthlyNet =
                it.totalMonthlyNet,
              totalMonthlyGross =
                it.totalMonthlyGross,
              totalExtra = totalExtra,
              displayItems = it.entries,
            ),
          )
        },
      )
    }

    LaunchedEffect(submitIteration) {
      val state = currentState as? Content ?: return@LaunchedEffect
      if (submitIteration > 0) {
        currentState = Loading()
        submitAddonPurchaseUseCase.invoke(
          quoteId = summaryParameters.quoteId,
          addonIds = summaryParameters.chosenQuotes.map {
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
    return when (val state = currentState) {
      is Content -> state.copy(
        navigateToFailure = errorForNavigation,
      )

      is Loading -> state.copy(
        activationDateToNavigateToSuccess = activationDateForNavigation,
      )

      AddonSummaryState.Error -> state
    }
  }
}

internal fun getInitialState(
  summaryParameters: SummaryParameters,
  exposureName: String,
  costBreakdownWithExtras: CostBreakdownWithExtras,
): Content {
  return Content(
    insuranceDisplayName = summaryParameters.productVariant.displayName,
    quotes = summaryParameters.chosenQuotes,
    activationDate = summaryParameters.activationDate,
    currentlyActiveAddons = summaryParameters.currentlyActiveAddons,
    insuranceExposure = exposureName,
    notificationMessage = summaryParameters.notificationMessage,
    documents = summaryParameters.productVariant.documents,
    costBreakdownWithExtras = costBreakdownWithExtras,
//    displayItems = summaryParameters.chosenQuotes.flatMap {
//      it.displayDetails
//    },
    displayItems = emptyList(), // todo: check on test session
    navigateToFailure = null,
    contractGroup = summaryParameters.productVariant.contractGroup,
  )
}

internal data class CostBreakdownWithExtras(
  val totalMonthlyNet: UiMoney,
  val totalMonthlyGross: UiMoney,
  val totalExtra: UiMoney,
  val displayItems: List<CostBreakdownEntry>,
)

internal sealed interface AddonSummaryState {
  data class Loading(
    val activationDateToNavigateToSuccess: LocalDate? = null,
  ) : AddonSummaryState

  data object Error : AddonSummaryState

  data class Content(
    val insuranceDisplayName: String,
    val insuranceExposure: String?,
    val contractGroup: ContractGroup?,
    val quotes: List<AddonQuote>,
    val activationDate: LocalDate,
    val currentlyActiveAddons: List<CurrentlyActiveAddon>,
    val notificationMessage: String?,
    val documents: List<InsuranceVariantDocument>,
    val costBreakdownWithExtras: CostBreakdownWithExtras,
    val displayItems: List<Pair<String, String>>, // todo: check how those look
    val navigateToFailure: ErrorMessage? = null,
  ) : AddonSummaryState
}

internal sealed interface AddonSummaryEvent {
  data object Submit : AddonSummaryEvent

  data object Reload : AddonSummaryEvent

  data object ReturnToInitialState : AddonSummaryEvent
}

private fun logSuccessfulAddonPurchaseAction(
  summaryParameters: SummaryParameters,
  addonPurchaseSource: AddonBannerSource,
) {
  summaryParameters.chosenQuotes.forEach { chosenQuote ->
    chosenQuote.addonSubtype?.let {
      val logInfo = AddonLogInfo(
        flow = addonPurchaseSource,
        subType = chosenQuote.addonSubtype,
        type = chosenQuote.addonVariant.product
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
  val subType: String,
  val type: String
) {

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
