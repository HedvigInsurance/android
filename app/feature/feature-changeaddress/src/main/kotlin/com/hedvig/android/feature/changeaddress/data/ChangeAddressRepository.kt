package com.hedvig.android.feature.changeaddress.data

import CreateQuoteInput
import HousingType
import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import octopus.MoveIntentCommitMutation
import octopus.MoveIntentCreateMutation
import octopus.MoveIntentRequestMutation
import octopus.type.MoveApartmentSubType
import octopus.type.MoveIntentCreateInput
import octopus.type.MoveToAddressInput
import octopus.type.MoveToApartmentInput

internal interface ChangeAddressRepository {
  suspend fun createMoveIntent(): Either<ErrorMessage, MoveIntent>
  suspend fun createQuotes(input: CreateQuoteInput): Either<ErrorMessage, List<MoveQuote>>
  suspend fun commitMove(id: MoveIntentId): Either<ErrorMessage, MoveResult>
}

internal class NetworkChangeAddressRepository(
  private val apolloClient: ApolloClient,
) : ChangeAddressRepository {
  override suspend fun createMoveIntent(): Either<ErrorMessage, MoveIntent> {
    return either {
      val result = apolloClient
        .mutation(MoveIntentCreateMutation(MoveIntentCreateInput(toType = octopus.type.MoveToType.APARTMENT)))
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .moveIntentCreate

      val moveIntent = result.moveIntent
      val userError = result.userError

      when {
        moveIntent != null -> moveIntent.toMoveIntent()
        userError != null -> raise(ErrorMessage(userError.message))
        else -> raise(ErrorMessage("No data found in MoveIntent"))
      }
    }
  }

  override suspend fun createQuotes(input: CreateQuoteInput): Either<ErrorMessage, List<MoveQuote>> {
    return either {
      val result = apolloClient
        .mutation(
          MoveIntentRequestMutation(
            intentId = input.moveIntentId.id,
            input = octopus.type.MoveIntentRequestInput(
              moveToAddress = MoveToAddressInput(
                street = input.address.street,
                postalCode = input.address.postalCode,
                city = Optional.absent(),
                bbrId = Optional.absent(),
                apartmentNumber = Optional.absent(),
                floor = Optional.absent(),
              ),
              moveFromAddressId = input.moveFromAddressId.id,
              movingDate = input.movingDate,
              numberCoInsured = input.numberCoInsured,
              squareMeters = input.squareMeters,
              apartment = Optional.present(
                MoveToApartmentInput(
                  subType = when (input.apartmentOwnerType) {
                    HousingType.APARTMENT_RENT -> MoveApartmentSubType.RENT
                    HousingType.APARTMENT_OWN -> MoveApartmentSubType.OWN
                    HousingType.VILLA -> throw IllegalArgumentException("Can not create request with villa type")
                  },
                  isStudent = input.isStudent,
                ),
              ),
            ),
          ),
        )
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .moveIntentRequest

      val moveIntent = result.moveIntent
      val userError = result.userError

      when {
        userError != null -> raise(ErrorMessage(userError.message))
        moveIntent != null -> moveIntent.toMoveQuotes()
        else -> raise(ErrorMessage("No data found in MoveIntent"))
      }
    }
  }

  override suspend fun commitMove(id: MoveIntentId): Either<ErrorMessage, MoveResult> {
    return either {
      val result = apolloClient
        .mutation(MoveIntentCommitMutation(id.id))
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .moveIntentCommit

      val moveIntent = result.moveIntent
      val userError = result.userError

      when {
        userError != null -> raise(ErrorMessage(userError.message))
        moveIntent != null -> moveIntent.toMoveResult()
        else -> raise(ErrorMessage("No data found in MoveIntent"))
      }
    }
  }
}

private fun MoveIntentCreateMutation.Data.MoveIntentCreate.MoveIntent.toMoveIntent() = MoveIntent(
  id = MoveIntentId(id),
  currentHomeAddresses = currentHomeAddresses.map { currentHomeAddress ->
    Address(
      id = AddressId(currentHomeAddress.id),
      street = currentHomeAddress.street,
      postalCode = currentHomeAddress.postalCode,
    )
  },
  movingDateRange = minMovingDate..maxMovingDate,
  numberCoInsured = numberCoInsured,
)

private fun MoveIntentRequestMutation.Data.MoveIntentRequest.MoveIntent.toMoveQuotes() = quotes.map { quote ->
  MoveQuote(
    insuranceName = quote.termsVersion.id,
    moveIntentId = MoveIntentId(id),
    address = Address(
      id = AddressId(quote.address.id),
      apartmentNumber = quote.address.apartmentNumber,
      bbrId = quote.address.bbrId,
      city = quote.address.city,
      floor = quote.address.floor,
      postalCode = quote.address.postalCode,
      street = quote.address.street,
    ),
    numberCoInsured = quote.numberCoInsured,
    premium = UiMoney(
      amount = quote.premium.amount,
      currencyCode = quote.premium.currencyCode,
    ),
    startDate = quote.startDate,
    termsVersion = quote.termsVersion.id,
  )
}

private fun MoveIntentCommitMutation.Data.MoveIntentCommit.MoveIntent.toMoveResult() = MoveResult(
  addressId = AddressId(id),
)
