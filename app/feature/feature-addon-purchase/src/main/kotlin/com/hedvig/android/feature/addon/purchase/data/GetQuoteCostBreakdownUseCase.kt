package com.hedvig.android.feature.addon.purchase.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.addon.purchase.navigation.AddonType
import com.hedvig.ui.tiersandaddons.CostBreakdownEntry
import octopus.AddonOfferCostQuery

internal interface GetQuoteCostBreakdownUseCase {
  suspend fun invoke(
    quoteId: String,
    existingAddons: List<CurrentlyActiveAddon>,
    newAddons: List<AddonQuote>,
    baseCost: ItemCost,
    insuranceDisplayName: String,
    addonType: AddonType
  ): Either<ErrorMessage, QuoteCostBreakdown>
}

internal data class QuoteCostBreakdown(
  val totalMonthlyNet: UiMoney,
  val totalMonthlyGross: UiMoney,
  val entries: List<CostBreakdownEntry>,
)

internal class GetQuoteCostBreakdownUseCaseImpl(private val apolloClient: ApolloClient) : GetQuoteCostBreakdownUseCase {
  override suspend fun invoke(
    quoteId: String,
    existingAddons: List<CurrentlyActiveAddon>,
    newAddons: List<AddonQuote>,
    baseCost: ItemCost,
    insuranceDisplayName: String,
    addonType: AddonType
  ): Either<ErrorMessage, QuoteCostBreakdown> {
    return either {
      apolloClient
        .query(AddonOfferCostQuery(newAddons.map { it.addonId }, quoteId))
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute()
        .fold(
          ifLeft = {
            raise(ErrorMessage())
          },
          ifRight = { result ->
            val list = buildList<CostBreakdownEntry> {
              add(
                CostBreakdownEntry(
                  displayName = insuranceDisplayName,
                  displayValue = baseCost.monthlyGross.toString(),
                  false,
                ),
              )
              existingAddons.forEach { existingAddon ->
                add(
                  CostBreakdownEntry(
                    displayName = existingAddon.displayTitle,
                    displayValue = existingAddon.cost.monthlyGross,
                    when (addonType) {
                      AddonType.SELECTABLE -> true
                      AddonType.TOGGLEABLE -> false
                    },
                  ),
                )
              }
              newAddons.forEach { existingAddon ->
                add(
                  CostBreakdownEntry(
                    displayName = existingAddon.displayTitle,
                    displayValue = existingAddon.itemCost.monthlyGross,
                    false,
                  ),
                )
              }
              result.addonOfferCost.discounts.forEach { discount ->
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
              UiMoney.fromMoneyFragment(result.addonOfferCost.monthlyNet),
              UiMoney.fromMoneyFragment(result.addonOfferCost.monthlyGross),
              list,
            )
          },
        )
    }
  }

}
