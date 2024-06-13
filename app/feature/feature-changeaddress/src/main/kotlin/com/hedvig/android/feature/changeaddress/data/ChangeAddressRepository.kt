package com.hedvig.android.feature.changeaddress.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.appreview.SelfServiceCompletedEventManager
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productVariant.android.toProductVariant
import com.hedvig.android.logger.logcat
import octopus.MoveIntentCommitMutation
import octopus.MoveIntentCreateMutation
import octopus.MoveIntentRequestMutation

internal interface ChangeAddressRepository {
  suspend fun createMoveIntent(): Either<ErrorMessage, MoveIntent>

  suspend fun createQuotes(input: QuoteInput): Either<ErrorMessage, List<MoveQuote>>

  suspend fun commitMove(id: MoveIntentId): Either<ErrorMessage, SuccessfulMove>
}

internal data object SuccessfulMove

internal class NetworkChangeAddressRepository(
  private val apolloClient: ApolloClient,
  private val selfServiceCompletedEventManager: SelfServiceCompletedEventManager,
) : ChangeAddressRepository {
  override suspend fun createMoveIntent(): Either<ErrorMessage, MoveIntent> {
    logcat { "Moving Flow: createMoveIntent" }
    return either {
      val result = apolloClient
        .mutation(MoveIntentCreateMutation())
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .moveIntentCreate

      val userError = result.userError
      if (userError != null) {
        raise(ErrorMessage(userError.message))
      }
      val moveIntent = result.moveIntent
      ensureNotNull(moveIntent) {
        ErrorMessage("No data found in MoveIntent")
      }
      moveIntent.toMoveIntent()
    }
  }

  override suspend fun createQuotes(input: QuoteInput): Either<ErrorMessage, List<MoveQuote>> {
    logcat { "Moving Flow: createQuotes with input:$input" }
    return either {
      val result = apolloClient
        .mutation(input.toMoveIntentRequestMutation())
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .moveIntentRequest

      val userError = result.userError
      if (userError != null) {
        raise(ErrorMessage(userError.message))
      }

      val moveIntent = result.moveIntent
      ensureNotNull(moveIntent) {
        ErrorMessage("No data found in MoveIntent")
      }
      moveIntent.toMoveQuotes()
    }
  }

  override suspend fun commitMove(id: MoveIntentId): Either<ErrorMessage, SuccessfulMove> {
    logcat { "Moving Flow: commitMove with id:$id" }
    return either {
      val result = apolloClient
        .mutation(MoveIntentCommitMutation(id.id))
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .moveIntentCommit

      val userError = result.userError
      if (userError != null) {
        raise(ErrorMessage(userError.message))
      }
      selfServiceCompletedEventManager.completedSelfServiceSuccessfully()
      SuccessfulMove
    }
  }
}

private fun MoveIntentCreateMutation.Data.MoveIntentCreate.MoveIntent.toMoveIntent(): MoveIntent = MoveIntent(
  id = MoveIntentId(id),
  currentHomeAddresses = currentHomeAddresses.map { currentHomeAddress ->
    Address(
      id = AddressId(currentHomeAddress.id),
      street = currentHomeAddress.street,
      postalCode = currentHomeAddress.postalCode,
    )
  },
  movingDateRange = minMovingDate..maxMovingDate,
  // numberInsured = numberCoInsured + member,
  suggestedNumberInsured = suggestedNumberCoInsured.plus(1),
  isApartmentAvailableforStudent = isApartmentAvailableforStudent,
  maxApartmentSquareMeters = maxApartmentSquareMeters,
  maxHouseSquareMeters = maxHouseSquareMeters,
  maxApartmentNumberCoInsured = maxApartmentNumberCoInsured,
  maxHouseNumberCoInsured = maxHouseNumberCoInsured,
  extraBuildingTypes = extraBuildingTypes.map { it.toExtraBuildingType() },
)

private fun MoveIntentRequestMutation.Data.MoveIntentRequest.MoveIntent.toMoveQuotes(): List<MoveQuote> {
  return quotes.map { quote ->
    MoveQuote(
      id = id,
      insuranceName = quote.exposureName ?: quote.productVariant.displayName,
      moveIntentId = MoveIntentId(id),
      premium = UiMoney(
        amount = quote.premium.amount,
        currencyCode = quote.premium.currencyCode,
      ),
      startDate = quote.startDate,
      productVariant = quote.productVariant.toProductVariant(),
      displayItems = quote.displayItems
        .map { it.displayTitle to it.displayValue },
    )
  }
}
