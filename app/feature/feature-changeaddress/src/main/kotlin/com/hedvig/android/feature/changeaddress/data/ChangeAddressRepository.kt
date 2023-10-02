package com.hedvig.android.feature.changeaddress.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.insurance.Document
import com.hedvig.android.core.insurance.InsurableLimit
import com.hedvig.android.core.insurance.Peril
import com.hedvig.android.core.insurance.Product
import com.hedvig.android.core.insurance.ProductVariant
import com.hedvig.android.core.uidata.UiMoney
import octopus.MoveIntentCommitMutation
import octopus.MoveIntentCreateMutation
import octopus.MoveIntentRequestMutation
import octopus.type.InsurableLimitType
import octopus.type.InsuranceDocumentType

internal interface ChangeAddressRepository {
  suspend fun createMoveIntent(): Either<ErrorMessage, MoveIntent>
  suspend fun createQuotes(input: QuoteInput): Either<ErrorMessage, List<MoveQuote>>
  suspend fun commitMove(id: MoveIntentId): Either<ErrorMessage, SuccessfulMove>
}

data object SuccessfulMove

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
//    numberCoInsured = quote.numberCoInsured,
    numberCoInsured = quote.numberCoInsured ?: 0, // todo https://hedviginsurance.slack.com/archives/C03HT2JRDPG/p1696263992552259
    premium = UiMoney(
      amount = quote.premium.amount,
      currencyCode = quote.premium.currencyCode,
    ),
    startDate = quote.startDate,
    productVariant = quote.productVariant.toProductVariant(),
  )
}

private fun MoveIntentRequestMutation.Data.MoveIntentRequest.MoveIntent.Quote.ProductVariant.toProductVariant() =
  ProductVariant(
    displayName = this.displayName,
    typeOfContract = this.typeOfContract,
    partner = this.partner,
    product = Product(
      displayNameFull = "", // this.product.displayNameFull, todo https://hedviginsurance.slack.com/archives/C03HT2JRDPG/p1696263992552259
      pillowImageUrl = "", // this.product.pillowImage.src, todo https://hedviginsurance.slack.com/archives/C03HT2JRDPG/p1696263992552259
    ),
    perils = this.perils.map { peril ->
      Peril(
        id = peril.id,
        title = peril.title,
        description = peril.description,
        info = peril.info,
        covered = peril.covered,
        exceptions = peril.exceptions,
        colorCode = peril.colorCode,
      )
    },
    insurableLimits = this.insurableLimits.map { insurableLimit ->
      InsurableLimit(
        label = insurableLimit.label,
        limit = insurableLimit.limit,
        description = insurableLimit.description,
        type = when (insurableLimit.type) {
          InsurableLimitType.DEDUCTIBLE -> InsurableLimit.InsurableLimitType.DEDUCTIBLE
          InsurableLimitType.DEDUCTIBLE_NATURE_DAMAGE -> InsurableLimit.InsurableLimitType.DEDUCTIBLE_NATURE_DAMAGE
          InsurableLimitType.DEDUCTIBLE_ALL_RISK -> InsurableLimit.InsurableLimitType.DEDUCTIBLE_ALL_RISK
          InsurableLimitType.INSURED_AMOUNT -> InsurableLimit.InsurableLimitType.INSURED_AMOUNT
          InsurableLimitType.GOODS_INDIVIDUAL -> InsurableLimit.InsurableLimitType.GOODS_INDIVIDUAL
          InsurableLimitType.GOODS_FAMILY -> InsurableLimit.InsurableLimitType.GOODS_FAMILY
          InsurableLimitType.TRAVEL_DAYS -> InsurableLimit.InsurableLimitType.TRAVEL_DAYS
          InsurableLimitType.MEDICAL_EXPENSES -> InsurableLimit.InsurableLimitType.MEDICAL_EXPENSES
          InsurableLimitType.LOST_LUGGAGE -> InsurableLimit.InsurableLimitType.LOST_LUGGAGE
          InsurableLimitType.BIKE -> InsurableLimit.InsurableLimitType.BIKE
          InsurableLimitType.PERMANENT_INJURY -> InsurableLimit.InsurableLimitType.PERMANENT_INJURY
          InsurableLimitType.TREATMENT -> InsurableLimit.InsurableLimitType.TREATMENT
          InsurableLimitType.DENTAL_TREATMENT -> InsurableLimit.InsurableLimitType.DENTAL_TREATMENT
          InsurableLimitType.TRAVEL_ILLNESS_INJURY_TRANSPORTATION_HOME -> InsurableLimit.InsurableLimitType.TRAVEL_ILLNESS_INJURY_TRANSPORTATION_HOME
          InsurableLimitType.TRAVEL_DELAYED_ON_TRIP -> InsurableLimit.InsurableLimitType.TRAVEL_DELAYED_ON_TRIP
          InsurableLimitType.TRAVEL_DELAYED_LUGGAGE -> InsurableLimit.InsurableLimitType.TRAVEL_DELAYED_LUGGAGE
          InsurableLimitType.TRAVEL_CANCELLATION -> InsurableLimit.InsurableLimitType.TRAVEL_CANCELLATION
          InsurableLimitType.UNKNOWN__ -> InsurableLimit.InsurableLimitType.UNKNOWN
        },
      )
    },
    documents = this.documents.map { document ->
      Document(
        displayName = document.displayName,
        url = document.url,
        type = when (document.type) {
          InsuranceDocumentType.TERMS_AND_CONDITIONS -> Document.InsuranceDocumentType.TERMS_AND_CONDITIONS
          InsuranceDocumentType.PRE_SALE_INFO_EU_STANDARD -> Document.InsuranceDocumentType.PRE_SALE_INFO_EU_STANDARD
          InsuranceDocumentType.PRE_SALE_INFO -> Document.InsuranceDocumentType.PRE_SALE_INFO
          InsuranceDocumentType.GENERAL_TERMS -> Document.InsuranceDocumentType.GENERAL_TERMS
          InsuranceDocumentType.PRIVACY_POLICY -> Document.InsuranceDocumentType.PRIVACY_POLICY
          InsuranceDocumentType.UNKNOWN__ -> Document.InsuranceDocumentType.UNKNOWN__
        },
      )
    },
  )
