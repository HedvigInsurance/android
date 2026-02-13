package com.hedvig.feature.remove.addons.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.data.productvariant.toProductVariant
import com.hedvig.android.logger.logcat
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import octopus.InsurancesWithRemovableAddonsQuery
import octopus.StartAddonRemovalQuery

@Serializable
internal data class CurrentlyActiveAddon(
  val displayTitle: String,
  val displayDescription: String?,
  val cost: ItemCost,
  val id: String,
)

internal interface StartAddonRemovalUseCase {
  suspend fun invoke(contractId: String): Either<ErrorMessage, StartAddonRemovalResponse>
}

internal class StartAddonRemovalUseCaseImpl(
  private val apolloClient: ApolloClient,
) : StartAddonRemovalUseCase {
  override suspend fun invoke(contractId: String): Either<ErrorMessage, StartAddonRemovalResponse> {
    return either {
      val productVariant = apolloClient
        .query(InsurancesWithRemovableAddonsQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute()
        .getOrNull()
        ?.currentMember
        ?.activeContracts
        ?.firstOrNull { it.id == contractId }
        ?.currentAgreement?.productVariant?.toProductVariant()
      if (productVariant == null) {
        logcat { "InsurancesWithRemovableAddonsQuery returned null productVariant" }
        raise(ErrorMessage())
      }
      apolloClient
        .query(StartAddonRemovalQuery(contractId))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat { "StartAddonRemovalQuery returned error: $it" }
            raise(ErrorMessage())
          },
          ifRight = { result ->
            when (val addonRemoveStart = result.addonRemoveStart) {
              is StartAddonRemovalQuery.Data.AddonRemoveOfferAddonRemoveStart -> {
                val removableAddons = addonRemoveStart.removableAddons.map { addon ->
                  CurrentlyActiveAddon(
                    displayTitle = addon.displayTitle,
                    displayDescription = addon.displayDescription,
                    cost = ItemCost.fromItemCostFragment(addon.cost),
                    id = addon.id,
                  )
                }
                StartAddonRemovalResponse(
                  existingAddonsToRemove = removableAddons,
                  activationDate = addonRemoveStart.activationDate,
                  baseCost = ItemCost.fromItemCostFragment(addonRemoveStart.baseCost),
                  currentTotalCost = ItemCost.fromItemCostFragment(addonRemoveStart.currentTotalCost),
                  pageDescription = addonRemoveStart.pageDescription,
                  pageTitle = addonRemoveStart.pageTitle,
                  productVariant = productVariant,
                )
              }

              is StartAddonRemovalQuery.Data.OtherAddonRemoveStart -> {
                raise(ErrorMessage())
              }

              is StartAddonRemovalQuery.Data.UserErrorAddonRemoveStart -> {
                raise(ErrorMessage(addonRemoveStart.message))
              }
            }

          },
        )
    }
  }
}


internal data class StartAddonRemovalResponse(
  val existingAddonsToRemove: List<CurrentlyActiveAddon>,
  val activationDate: LocalDate,
  val baseCost: ItemCost,
  val currentTotalCost: ItemCost,
  val pageDescription: String,
  val pageTitle: String,
  val productVariant: ProductVariant,
)
