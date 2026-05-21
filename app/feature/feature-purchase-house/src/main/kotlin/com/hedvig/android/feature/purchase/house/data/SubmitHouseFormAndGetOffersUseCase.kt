package com.hedvig.android.feature.purchase.house.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.HousePriceIntentConfirmMutation
import octopus.HousePriceIntentDataUpdateMutation

internal interface SubmitHouseFormAndGetOffersUseCase {
  suspend fun invoke(
    priceIntentId: String,
    ssn: String,
    email: String,
    street: String,
    zipCode: String,
    livingSpace: Int,
    ancillaryArea: Int,
    numberCoInsured: Int,
    yearOfConstruction: Int,
    numberOfBathrooms: Int,
    isSubleted: Boolean,
  ): Either<ErrorMessage, HouseOffers>
}

internal class SubmitHouseFormAndGetOffersUseCaseImpl(
  private val apolloClient: ApolloClient,
) : SubmitHouseFormAndGetOffersUseCase {
  override suspend fun invoke(
    priceIntentId: String,
    ssn: String,
    email: String,
    street: String,
    zipCode: String,
    livingSpace: Int,
    ancillaryArea: Int,
    numberCoInsured: Int,
    yearOfConstruction: Int,
    numberOfBathrooms: Int,
    isSubleted: Boolean,
  ): Either<ErrorMessage, HouseOffers> {
    return either {
      val formData = buildMap<String, Any> {
        put("ssn", ssn)
        put("email", email)
        put("street", street)
        put("zipCode", zipCode)
        put("livingSpace", livingSpace)
        put("ancillaryArea", ancillaryArea)
        put("numberCoInsured", numberCoInsured)
        put("yearOfConstruction", yearOfConstruction)
        put("numberOfBathrooms", numberOfBathrooms)
        put("isSubleted", isSubleted)
        put("extraBuildings", emptyList<Map<String, Any>>())
      }

      val updateResult = apolloClient
        .mutation(HousePriceIntentDataUpdateMutation(priceIntentId = priceIntentId, data = formData))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to update price intent data: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.priceIntentDataUpdate },
        )

      if (updateResult.userError != null) {
        raise(ErrorMessage(updateResult.userError?.message))
      }

      val confirmResult = apolloClient
        .mutation(HousePriceIntentConfirmMutation(priceIntentId = priceIntentId))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to confirm price intent: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.priceIntentConfirm },
        )

      if (confirmResult.userError != null) {
        raise(ErrorMessage(confirmResult.userError?.message))
      }

      val offers = confirmResult.priceIntent?.offers.orEmpty()
      if (offers.isEmpty()) {
        logcat(LogPriority.ERROR) { "No offers returned after confirming price intent" }
        raise(ErrorMessage())
      }

      HouseOffers(
        productDisplayName = offers.first().variant.displayName,
        offers = offers.map { it.toHouseTierOffer() },
      )
    }
  }
}
