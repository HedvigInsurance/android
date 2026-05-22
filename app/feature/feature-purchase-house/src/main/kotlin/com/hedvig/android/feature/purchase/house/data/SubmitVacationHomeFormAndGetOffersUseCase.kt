package com.hedvig.android.feature.purchase.house.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.purchase.house.ui.extrabuildings.ExtraBuildingInfo
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.HousePriceIntentConfirmMutation
import octopus.HousePriceIntentDataUpdateMutation
import octopus.fragment.HouseProductOfferFragment

internal interface SubmitVacationHomeFormAndGetOffersUseCase {
  suspend fun invoke(
    priceIntentId: String,
    ssn: String,
    email: String,
    street: String,
    zipCode: String,
    multipleOwners: Boolean,
    yearOfConstruction: Int,
    livingSpace: Int,
    hasWaterConnected: Boolean,
    numberOfBathrooms: Int,
    isSubleted: Boolean,
    extraBuildings: List<ExtraBuildingInfo>,
  ): Either<ErrorMessage, HouseOffers>
}

internal class SubmitVacationHomeFormAndGetOffersUseCaseImpl(
  private val apolloClient: ApolloClient,
) : SubmitVacationHomeFormAndGetOffersUseCase {
  override suspend fun invoke(
    priceIntentId: String,
    ssn: String,
    email: String,
    street: String,
    zipCode: String,
    multipleOwners: Boolean,
    yearOfConstruction: Int,
    livingSpace: Int,
    hasWaterConnected: Boolean,
    numberOfBathrooms: Int,
    isSubleted: Boolean,
    extraBuildings: List<ExtraBuildingInfo>,
  ): Either<ErrorMessage, HouseOffers> {
    return either {
      val formData = buildMap<String, Any> {
        put("ssn", ssn)
        put("email", email)
        put("street", street)
        put("zipCode", zipCode)
        put("multipleOwners", multipleOwners)
        put("yearOfConstruction", yearOfConstruction)
        put("livingSpace", livingSpace)
        put("hasWaterConnected", hasWaterConnected)
        put("numberOfBathrooms", numberOfBathrooms)
        put("isSubleted", isSubleted)
        put(
          "extraBuildings",
          extraBuildings.map { building ->
            mapOf(
              "type" to building.type,
              "area" to building.area,
              "hasWaterConnected" to building.hasWaterConnected,
            )
          },
        )
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

internal fun HouseProductOfferFragment.toHouseTierOffer(): HouseTierOffer {
  return HouseTierOffer(
    offerId = id,
    tierDisplayName = variant.displayNameTier ?: variant.displayName,
    tierDescription = variant.tierDescription ?: "",
    grossPrice = UiMoney.fromMoneyFragment(cost.gross),
    netPrice = UiMoney.fromMoneyFragment(cost.net),
    usps = usps,
    exposureDisplayName = exposure.displayNameShort,
    deductibleDisplayName = deductible?.displayName,
    hasDiscount = cost.net.amount < cost.gross.amount,
  )
}
