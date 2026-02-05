package com.hedvig.feature.remove.addons.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.logger.logcat
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import octopus.StartAddonRemovalMutation

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
      apolloClient
        .mutation(StartAddonRemovalMutation(contractId))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat { "StartAddonRemovalMutation returned error: $it" }
            raise(ErrorMessage())
          },
          ifRight = { result ->
            when (val addonRemoveStart = result.addonRemoveStart) {
              is StartAddonRemovalMutation.Data.AddonRemoveOfferAddonRemoveStart -> {
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
                )
              }

              is StartAddonRemovalMutation.Data.OtherAddonRemoveStart -> {
                raise(ErrorMessage())
              }

              is StartAddonRemovalMutation.Data.UserErrorAddonRemoveStart -> {
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
)
