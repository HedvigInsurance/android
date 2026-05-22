package com.hedvig.android.feature.purchase.pet.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.toContractGroup
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.datetime.LocalDate
import octopus.PetPriceIntentConfirmMutation
import octopus.PetPriceIntentDataUpdateMutation
import octopus.fragment.PetProductOfferFragment

internal data class SubmitInput(
  val priceIntentId: String,
  val productName: String,
  val ssn: String,
  val email: String,
  val name: String,
  val breedId: String,
  val isMixedBreed: Boolean,
  val birthDate: LocalDate,
  val gender: PetGender,
  val isNeutered: Boolean,
  val speciesAnswer: Boolean,
  val street: String,
  val zipCode: String,
)

internal interface SubmitPetFormAndGetOffersUseCase {
  suspend fun invoke(input: SubmitInput): Either<ErrorMessage, PetOffers>
}

internal class SubmitPetFormAndGetOffersUseCaseImpl(
  private val apolloClient: ApolloClient,
) : SubmitPetFormAndGetOffersUseCase {
  override suspend fun invoke(input: SubmitInput): Either<ErrorMessage, PetOffers> {
    return either {
      val speciesKey = if (input.productName == PRODUCT_NAME_CAT) "hasOutsideAccess" else "isPreviousDogOwner"
      val formData: Map<String, Any> = buildMap {
        put("ssn", input.ssn)
        put("name", input.name)
        put("breeds", if (input.isMixedBreed) emptyList<String>() else listOf(input.breedId))
        put("birthDate", input.birthDate.toString())
        put("gender", input.gender.name)
        put("isNeutered", input.isNeutered.toString())
        put(speciesKey, input.speciesAnswer.toString())
        put("street", input.street)
        put("zipCode", input.zipCode)
        put("email", input.email)
      }

      val updateResult = apolloClient
        .mutation(PetPriceIntentDataUpdateMutation(priceIntentId = input.priceIntentId, data = formData))
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
        .mutation(PetPriceIntentConfirmMutation(priceIntentId = input.priceIntentId))
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

      PetOffers(
        productDisplayName = offers.first().variant.displayName,
        contractGroup = offers.first().variant.typeOfContract.toContractGroup(),
        offers = offers.map { it.toTierOffer() },
      )
    }
  }
}

internal fun PetProductOfferFragment.toTierOffer(): PetTierOffer {
  return PetTierOffer(
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
