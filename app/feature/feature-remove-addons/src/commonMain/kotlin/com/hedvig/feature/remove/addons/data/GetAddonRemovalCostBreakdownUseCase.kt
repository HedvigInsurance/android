package com.hedvig.feature.remove.addons.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.AddonId
import com.hedvig.android.data.contract.ContractId
import com.hedvig.android.logger.logcat
import com.hedvig.ui.tiersandaddons.CostBreakdownEntry
import com.hedvig.ui.tiersandaddons.QuoteCostBreakdown
import octopus.AddonRemovalCostBreakdownQuery

internal interface GetAddonRemovalCostBreakdownUseCase {
  suspend fun invoke(
    contractId: ContractId,
    addonsToRemove: List<CurrentlyActiveAddon>,
    addonsLeft: List<CurrentlyActiveAddon>,
    baseCost: ItemCost,
    insuranceDisplayName: String,
  ): Either<ErrorMessage, QuoteCostBreakdown>
}

internal class GetAddonRemovalCostBreakdownUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetAddonRemovalCostBreakdownUseCase {
  override suspend fun invoke(
    contractId: ContractId,
    addonsToRemove: List<CurrentlyActiveAddon>,
    addonsLeft: List<CurrentlyActiveAddon>,
    baseCost: ItemCost,
    insuranceDisplayName: String,
  ): Either<ErrorMessage, QuoteCostBreakdown> {
    return either {
      apolloClient
        .query(
          AddonRemovalCostBreakdownQuery(
            contractId = contractId.id,
            addonIds = addonsToRemove.map(CurrentlyActiveAddon::id).map(AddonId::id),
          ),
        )
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute()
        .fold(
          ifLeft = {
            logcat { "AddonRemovalCostBreakdownQuery returned error: $it" }
            raise(ErrorMessage())
          },
          ifRight = { result ->
            val costBreakdownEntries = buildList {
              add(
                CostBreakdownEntry(
                  displayName = insuranceDisplayName,
                  displayValue = baseCost.monthlyGross.toString(),
                  false,
                ),
              )
              addonsLeft.forEach { leftAddon ->
                add(
                  CostBreakdownEntry(
                    displayName = leftAddon.displayTitle,
                    displayValue = leftAddon.cost.monthlyGross.toString(),
                    false,
                  ),
                )
              }
              addonsToRemove.forEach { addonToRemove ->
                add(
                  CostBreakdownEntry(
                    displayName = addonToRemove.displayTitle,
                    displayValue = addonToRemove.cost.monthlyGross.toString(),
                    true,
                  ),
                )
              }
              result.addonRemoveOfferCost.discounts.forEach { discount ->
                add(
                  CostBreakdownEntry(
                    displayName = discount.displayName,
                    displayValue = discount.displayValue,
                    false,
                  ),
                )
              }
            }
            QuoteCostBreakdown(
              totalMonthlyNet = UiMoney.fromMoneyFragment(result.addonRemoveOfferCost.monthlyNet),
              totalMonthlyGross = UiMoney.fromMoneyFragment(result.addonRemoveOfferCost.monthlyGross),
              entries = costBreakdownEntries,
            )
          },
        )
    }
  }
}
