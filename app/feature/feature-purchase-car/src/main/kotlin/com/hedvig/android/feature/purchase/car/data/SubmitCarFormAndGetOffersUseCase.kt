package com.hedvig.android.feature.purchase.car.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.CarPriceIntentConfirmMutation
import octopus.CarPriceIntentDataUpdateMutation
import octopus.fragment.CarProductOfferFragment

internal interface SubmitCarFormAndGetOffersUseCase {
  suspend fun invoke(
    priceIntentId: String,
    ssn: String,
    registrationNumber: String,
    mileage: Int,
    street: String,
    zipCode: String,
    email: String,
  ): Either<ErrorMessage, CarOffers>
}

internal class SubmitCarFormAndGetOffersUseCaseImpl(
  private val apolloClient: ApolloClient,
) : SubmitCarFormAndGetOffersUseCase {
  override suspend fun invoke(
    priceIntentId: String,
    ssn: String,
    registrationNumber: String,
    mileage: Int,
    street: String,
    zipCode: String,
    email: String,
  ): Either<ErrorMessage, CarOffers> {
    return either {
      val formData = buildMap {
        put("ssn", ssn)
        put("registrationNumber", registrationNumber)
        put("mileage", mileage)
        put("street", street)
        put("zipCode", zipCode)
        put("email", email)
      }

      val updateResult = apolloClient
        .mutation(CarPriceIntentDataUpdateMutation(priceIntentId = priceIntentId, data = formData))
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
        .mutation(CarPriceIntentConfirmMutation(priceIntentId = priceIntentId))
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

      CarOffers(
        productDisplayName = offers.first().variant.displayName,
        offers = offers.map { it.toTierOffer() },
      )
    }
  }
}

internal fun CarProductOfferFragment.toTierOffer(): CarTierOffer {
  return CarTierOffer(
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
