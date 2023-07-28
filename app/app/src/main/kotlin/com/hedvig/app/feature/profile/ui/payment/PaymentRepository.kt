package com.hedvig.app.feature.profile.ui.payment

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.watch
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.language.LanguageService
import com.hedvig.app.feature.offer.model.Campaign
import com.hedvig.app.feature.offer.model.toIncentive
import com.hedvig.app.feature.profile.data.PaymentMethod
import com.hedvig.android.apollo.toMonetaryAmount
import giraffe.ChargeHistoryQuery
import giraffe.PaymentQuery
import giraffe.type.PayoutMethodStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.money.MonetaryAmount

class PaymentRepository(
  private val apolloClient: ApolloClient,
  languageService: LanguageService,
) {
  private val paymentQuery = PaymentQuery(languageService.getGraphQLLocale())
  private val chargeHistoryQuery = ChargeHistoryQuery()
  fun payment(): Flow<ApolloResponse<PaymentQuery.Data>> = apolloClient
    .query(paymentQuery)
    .watch()

  suspend fun refresh(): ApolloResponse<PaymentQuery.Data> = apolloClient
    .query(paymentQuery)
    .fetchPolicy(FetchPolicy.NetworkOnly)
    .execute()

  suspend fun writeActivePayoutMethodStatus(status: PayoutMethodStatus) {
    val cachedData = apolloClient
      .apolloStore
      .readOperation(paymentQuery)

    apolloClient
      .apolloStore
      .writeOperation(
        paymentQuery,
        cachedData.copy(
          activePayoutMethods = PaymentQuery.ActivePayoutMethods(status = status),
        ),
      )
  }

  suspend fun getChargeHistory(): Either<OperationResult.Error, ChargeHistory> = either {
    apolloClient
      .query(chargeHistoryQuery)
      .safeExecute()
      .toEither()
      .map {
        ChargeHistory(
          charges = it.chargeHistory.map {
            ChargeHistory.Charge(
              amount = it.amount.fragments.monetaryAmountFragment.toMonetaryAmount(),
              date = it.date,
            )
          },
        )
      }
      .bind()
  }

  suspend fun getPaymentData(): Either<OperationResult.Error, PaymentData> = either {
    apolloClient
      .query(paymentQuery)
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute()
      .toEither()
      .map {
        PaymentData(
          nextCharge = it.chargeEstimation.subscription.fragments.monetaryAmountFragment.toMonetaryAmount(),
          monthlyCost = it.insuranceCost?.fragments?.costFragment?.monthlyNet?.fragments?.monetaryAmountFragment?.toMonetaryAmount(),
          totalDiscount = it.insuranceCost?.fragments?.costFragment?.monthlyDiscount?.fragments?.monetaryAmountFragment?.toMonetaryAmount(),
          nextChargeDate = it.nextChargeDate,
          contracts = it.contracts
            .filter { it.status.fragments.contractStatusFragment.asActiveStatus != null }
            .map { it.displayName },
          redeemedCampagins = it.redeemedCampaigns.map {
            Campaign(
              incentive = it.fragments.incentiveFragment.incentive.toIncentive(),
              displayValue = it.fragments.incentiveFragment.displayValue,
              code = it.code,
            )
          },
          bankName = it.bankAccount?.fragments?.bankAccountFragment?.bankName,
          bankDescriptor = it.bankAccount?.fragments?.bankAccountFragment?.descriptor,
          paymentMethod = it.activePaymentMethodsV2
            ?.fragments
            ?.activePaymentMethodsFragment
            ?.asStoredCardDetails
            ?.let {
              PaymentMethod.CardPaymentMethod(
                brand = it.brand,
                lastFourDigits = it.lastFourDigits,
                expiryMonth = it.expiryMonth,
                expiryYear = it.expiryYear,
              )
            } ?: it.activePaymentMethodsV2
            ?.fragments
            ?.activePaymentMethodsFragment
            ?.asStoredThirdPartyDetails
            ?.let {
              PaymentMethod.ThirdPartyPaymentMethd(
                name = it.name,
                type = it.type,
              )
            },
          payoutMethodStatus = it.activePayoutMethods?.status,
        )
      }
      .bind()
  }

  data class PaymentData(
    val nextCharge: MonetaryAmount,
    val monthlyCost: MonetaryAmount?,
    val totalDiscount: MonetaryAmount?,
    val nextChargeDate: LocalDate?,
    val redeemedCampagins: List<Campaign>,
    val bankName: String?,
    val bankDescriptor: String?,
    val paymentMethod: PaymentMethod?,
    val contracts: List<String>,
    val payoutMethodStatus: PayoutMethodStatus?,
  )

  data class ChargeHistory(
    val charges: List<Charge>,
  ) {
    data class Charge(
      val amount: MonetaryAmount,
      val date: LocalDate,
    )
  }
}
