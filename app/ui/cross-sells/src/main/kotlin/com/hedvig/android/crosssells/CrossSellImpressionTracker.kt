package com.hedvig.android.crosssells

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.tracking.EventTrackingClient
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

/**
 * Fires the `cross_sell_shown` analytics event, once for each cross-sell offer surfaced to a member.
 *
 * Call [crossSellShown] per individual offer that becomes visible, so a sheet or list showing several offers
 * produces several events. [CrossSellUserFlow] records which surface the offer appeared on; consumers segment on it
 * because the surfaces fire at very different rates (the insurances tab far more often than the after-flow sheet).
 */
fun interface CrossSellImpressionTracker {
  fun crossSellShown(
    userFlow: CrossSellUserFlow,
    crossSellType: CrossSellType,
    offerId: String,
    flowSource: CrossSellFlowSource?,
  )
}

/**
 * Fires one impression per offer shown in a [CrossSellSheetData], mirroring the sheet's render priority: an addon
 * recommendation takes the primary slot over a cross-sell recommendation, and every [CrossSellSheetData.otherCrossSells]
 * entry is a new-insurance offer. Used by the after-flow and home sheets, which render the same data.
 */
fun CrossSellImpressionTracker.crossSellSheetShown(
  data: CrossSellSheetData,
  userFlow: CrossSellUserFlow,
  flowSource: CrossSellFlowSource? = null,
) {
  when {
    data.recommendedAddon != null -> {
      crossSellShown(userFlow, CrossSellType.Addon, data.recommendedAddon.id, flowSource)
    }

    data.recommendedCrossSell != null -> {
      crossSellShown(userFlow, CrossSellType.NewPromise, data.recommendedCrossSell.crossSell.id, flowSource)
    }
  }
  data.otherCrossSells.forEach { crossSell ->
    crossSellShown(userFlow, CrossSellType.NewPromise, crossSell.id, flowSource)
  }
}

/** The surface a cross-sell offer was shown on. */
enum class CrossSellUserFlow(val analyticsValue: String) {
  SmartXSell("smart_x_sell"),
  InsuranceCard("insurance_card"),
  InsuranceScreen("insurance_screen"),
  HomeScreen("home_screen"),
}

/** Whether the offer is an add-on to an existing insurance or a new insurance. */
enum class CrossSellType(val analyticsValue: String) {
  Addon("addon"),
  NewPromise("new_promise"),
}

/** The self-service flow a member just completed when a [CrossSellUserFlow.SmartXSell] offer is shown. */
enum class CrossSellFlowSource(val analyticsValue: String) {
  ChangeTier("change_tier"),
  ClosedClaim("closed_claim"),
  Addon("addon"),
  Moving("moving"),
  EditCoInsured("edit_co_insured"),
}

@ContributesBinding(AppScope::class)
@Inject
internal class CrossSellImpressionTrackerImpl(
  private val eventTrackingClient: EventTrackingClient,
) : CrossSellImpressionTracker {
  override fun crossSellShown(
    userFlow: CrossSellUserFlow,
    crossSellType: CrossSellType,
    offerId: String,
    flowSource: CrossSellFlowSource?,
  ) {
    eventTrackingClient.trackEvent(
      name = CROSS_SELL_SHOWN_EVENT,
      parameters = buildMap {
        put("user_flow", userFlow.analyticsValue)
        put("offer_type", crossSellType.analyticsValue)
        put("offer_id", offerId)
        if (flowSource != null) {
          put("flow_source", flowSource.analyticsValue)
        }
      },
    )
  }
}

private const val CROSS_SELL_SHOWN_EVENT = "cross_sell_shown"

/** An offer counts as shown once at least half of it has been on screen for this long. */
const val CROSS_SELL_IMPRESSION_MIN_DURATION_MS = 500L
const val CROSS_SELL_IMPRESSION_MIN_FRACTION_VISIBLE = 0.5f
