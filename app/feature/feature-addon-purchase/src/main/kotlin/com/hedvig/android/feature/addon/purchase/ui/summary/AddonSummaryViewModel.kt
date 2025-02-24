package com.hedvig.android.feature.addon.purchase.ui.summary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.tracking.ActionType
import com.hedvig.android.core.tracking.logAction
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.addons.data.TravelAddonBannerSource
import com.hedvig.android.feature.addon.purchase.data.CurrentTravelAddon
import com.hedvig.android.feature.addon.purchase.data.SubmitAddonPurchaseUseCase
import com.hedvig.android.feature.addon.purchase.data.TravelAddonQuote
import com.hedvig.android.feature.addon.purchase.navigation.SummaryParameters
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonLogInfo.AddonEventType
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState.Content
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState.Loading
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.datetime.LocalDate

internal class AddonSummaryViewModel(
  summaryParameters: SummaryParameters,
  addonPurchaseSource: TravelAddonBannerSource,
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
  private val addonPurchaseSource: TravelAddonBannerSource,
) :
  MoleculePresenter<AddonSummaryEvent, AddonSummaryState> {
  @Composable
  override fun MoleculePresenterScope<AddonSummaryEvent>.present(lastState: AddonSummaryState): AddonSummaryState {
    var submitIteration by remember { mutableIntStateOf(0) }
    var currentState by remember { mutableStateOf(lastState) }

    val initialState = getInitialState(summaryParameters)

    CollectEvents { event ->
      when (event) {
        AddonSummaryEvent.Submit -> submitIteration++
        AddonSummaryEvent.ReturnToInitialState -> currentState = initialState
      }
    }

    LaunchedEffect(submitIteration) {
      if (submitIteration > 0) {
        currentState = Loading
        submitAddonPurchaseUseCase.invoke(
          quoteId = summaryParameters.quote.quoteId,
          addonId = summaryParameters.quote.addonId,
        ).fold(
          ifLeft = {
            currentState = initialState.copy(navigateToFailure = true)
            // todo: not really passing UserError message here. Should we? Or should we maybe redirect to chat in
            // the case of final failure?
          },
          ifRight = { date ->
            logSuccessfulAddonPurchaseAction(summaryParameters, addonPurchaseSource)
            currentState =
              initialState.copy(activationDateForSuccessfullyPurchasedAddon = summaryParameters.activationDate)
          },
        )
      }
    }
    return currentState
  }
}

internal fun getInitialState(summaryParameters: SummaryParameters): Content {
  val total = if (summaryParameters.currentTravelAddon == null) {
    summaryParameters.quote.price
  } else {
    val amountDiff = summaryParameters.quote.price.amount - summaryParameters.currentTravelAddon.price.amount
    UiMoney(amountDiff, summaryParameters.quote.price.currencyCode)
  }
  return Content(
    offerDisplayName = summaryParameters.offerDisplayName,
    quote = summaryParameters.quote,
    activationDate = summaryParameters.activationDate,
    currentTravelAddon = summaryParameters.currentTravelAddon,
    totalPriceChange = total,
  )
}

internal sealed interface AddonSummaryState {
  data object Loading : AddonSummaryState

  data class Content(
    val offerDisplayName: String,
    val quote: TravelAddonQuote,
    val activationDate: LocalDate,
    val currentTravelAddon: CurrentTravelAddon?,
    val totalPriceChange: UiMoney,
    val activationDateForSuccessfullyPurchasedAddon: LocalDate? = null,
    val navigateToFailure: Boolean = false,
  ) : AddonSummaryState
}

internal sealed interface AddonSummaryEvent {
  data object Submit : AddonSummaryEvent

  data object ReturnToInitialState : AddonSummaryEvent
}

private fun logSuccessfulAddonPurchaseAction(
  summaryParameters: SummaryParameters,
  addonPurchaseSource: TravelAddonBannerSource,
) {
  val logInfo = AddonLogInfo(
    flow = addonPurchaseSource,
    subType = summaryParameters.quote.addonSubtype,
  )
  val eventType = if (summaryParameters.currentTravelAddon == null) {
    AddonEventType.ADDON_PURCHASED
  } else {
    AddonEventType.ADDON_UPGRADED
  }
  logAction(type = ActionType.CUSTOM, name = eventType.name, attributes = logInfo.asAddonAttributes())
}

private data class AddonLogInfo(
  val flow: TravelAddonBannerSource,
  val subType: String,
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
