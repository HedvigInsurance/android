package com.hedvig.android.feature.purchase.apartment.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.ApartmentPriceIntentConfirmMutation
import octopus.ApartmentPriceIntentDataUpdateMutation
import octopus.fragment.ApartmentProductOfferFragment

internal interface SubmitFormAndGetOffersUseCase {
  suspend fun invoke(
    priceIntentId: String,
    street: String,
    zipCode: String,
    livingSpace: Int,
    numberCoInsured: Int,
  ): Either<ErrorMessage, ApartmentOffers>
}

internal class SubmitFormAndGetOffersUseCaseImpl(
  private val apolloClient: ApolloClient,
) : SubmitFormAndGetOffersUseCase {
  override suspend fun invoke(
    priceIntentId: String,
    street: String,
    zipCode: String,
    livingSpace: Int,
    numberCoInsured: Int,
  ): Either<ErrorMessage, ApartmentOffers> {
    return either {
      val formData = buildMap {
        put("street", street)
        put("zipCode", zipCode)
        put("livingSpace", livingSpace)
        put("numberCoInsured", numberCoInsured)
      }

      val updateResult = apolloClient
        .mutation(ApartmentPriceIntentDataUpdateMutation(priceIntentId = priceIntentId, data = formData))
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
        .mutation(ApartmentPriceIntentConfirmMutation(priceIntentId = priceIntentId))
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

      ApartmentOffers(
        productDisplayName = offers.first().variant.displayName,
        offers = offers.map { it.toTierOffer() },
      )
    }
  }
}

internal fun ApartmentProductOfferFragment.toTierOffer(): ApartmentTierOffer {
  return ApartmentTierOffer(
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
