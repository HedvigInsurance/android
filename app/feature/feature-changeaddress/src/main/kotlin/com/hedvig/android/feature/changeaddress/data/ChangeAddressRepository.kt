package com.hedvig.android.feature.changeaddress.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import octopus.MoveIntentCommitMutation
import octopus.MoveIntentCreateMutation
import octopus.MoveIntentRequestMutation

internal interface ChangeAddressRepository {
  suspend fun createMoveIntent(): Either<ErrorMessage, MoveIntent>
  suspend fun createQuotes(input: QuoteInput): Either<ErrorMessage, List<MoveQuote>>
  suspend fun commitMove(id: MoveIntentId): Either<ErrorMessage, SuccessfulMove>
}

internal class NetworkChangeAddressRepository(
  private val apolloClient: ApolloClient,
) : ChangeAddressRepository {
  override suspend fun createMoveIntent(): Either<ErrorMessage, MoveIntent> {
    return either {
      val result = apolloClient
        .mutation(MoveIntentCreateMutation())
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

  override suspend fun createQuotes(input: QuoteInput): Either<ErrorMessage, List<MoveQuote>> {
    return either {
      val result = apolloClient
        .mutation(input.toMoveIntentRequestMutation())
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

  override suspend fun commitMove(id: MoveIntentId): Either<ErrorMessage, SuccessfulMove> {
    return either {
      val result = apolloClient
        .mutation(MoveIntentCommitMutation(id.id))
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .moveIntentCommit

      val userError = result.userError

      when {
        userError != null -> raise(ErrorMessage(userError.message))
        else -> SuccessfulMove
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
  numberCoInsured = suggestedNumberCoInsured,
  extraBuildingTypes = extraBuildingTypes.map { it.toExtraBuildingType() },
)

private fun MoveIntentRequestMutation.Data.MoveIntentRequest.MoveIntent.toMoveQuotes() = quotes.map { quote ->
  MoveQuote(
    id = id,
    insuranceName = quote.productVariant.displayName,
    moveIntentId = MoveIntentId(id),
    address = Address(
      id = AddressId(quote.address.id),
      postalCode = quote.address.postalCode,
      street = quote.address.street,
    ),
    numberCoInsured = quote.numberCoInsured,
    premium = UiMoney(
      amount = quote.premium.amount,
      currencyCode = quote.premium.currencyCode,
    ),
    startDate = quote.startDate,
    productVariant = quote.productVariant,
  )
}
