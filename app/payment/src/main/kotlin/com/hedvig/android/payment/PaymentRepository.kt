package com.hedvig.android.payment

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
import com.hedvig.android.apollo.toMonetaryAmount
import com.hedvig.android.language.LanguageService
import com.hedvig.android.payment.model.Campaign
import com.hedvig.android.payment.model.toIncentive
import giraffe.ChargeHistoryQuery
import giraffe.PaymentQuery
import giraffe.type.PayoutMethodStatus
import giraffe.type.TypeOfContract
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.money.MonetaryAmount

interface PaymentRepository {
  fun payment(): Flow<ApolloResponse<PaymentQuery.Data>>
  suspend fun refresh(): ApolloResponse<PaymentQuery.Data>
  suspend fun writeActivePayoutMethodStatus(status: PayoutMethodStatus)
  suspend fun getChargeHistory(): Either<OperationResult.Error, ChargeHistory>
  suspend fun getPaymentData(): Either<OperationResult.Error, PaymentData>
}

class PaymentRepositoryImpl(
  private val apolloClient: ApolloClient,
  languageService: LanguageService,
) : PaymentRepository {
  private val paymentQuery = PaymentQuery(languageService.getGraphQLLocale())
  private val chargeHistoryQuery = ChargeHistoryQuery()
  override fun payment(): Flow<ApolloResponse<PaymentQuery.Data>> = apolloClient
    .query(paymentQuery)
    .watch()

  override suspend fun refresh(): ApolloResponse<PaymentQuery.Data> = apolloClient
    .query(paymentQuery)
    .fetchPolicy(FetchPolicy.NetworkOnly)
    .execute()

  override suspend fun writeActivePayoutMethodStatus(status: PayoutMethodStatus) {
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

  override suspend fun getChargeHistory(): Either<OperationResult.Error, ChargeHistory> = either {
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

  override suspend fun getPaymentData(): Either<OperationResult.Error, PaymentData> = either {
    apolloClient
      .query(paymentQuery)
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute()
      .toEither()
      .map {
        val costFragment = it.insuranceCost?.fragments?.costFragment
        PaymentData(
          nextCharge = it.chargeEstimation.subscription.fragments.monetaryAmountFragment.toMonetaryAmount(),
          monthlyCost = costFragment?.monthlyNet?.fragments?.monetaryAmountFragment?.toMonetaryAmount(),
          totalDiscount = costFragment?.monthlyDiscount?.fragments?.monetaryAmountFragment?.toMonetaryAmount(),
          nextChargeDate = it.nextChargeDate,
          contracts = it.contracts
            .filter { it.status.fragments.contractStatusFragment.asActiveStatus != null }
            .map {
              Contract(
                name = it.displayName,
                typeOfContract = it.typeOfContract,
              )
            },
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
              PaymentData.PaymentMethod.CardPaymentMethod(
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
              PaymentData.PaymentMethod.ThirdPartyPaymentMethd(
                name = it.name,
                type = it.type,
              )
            },
          payoutMethodStatus = it.activePayoutMethods?.status,
          bankAccount = it.bankAccount?.let {
            BankAccount(
              name = it.fragments.bankAccountFragment.bankName,
              accountNumber = it.fragments.bankAccountFragment.descriptor,
            )
          },
        )
      }
      .bind()
  }
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
  val bankAccount: BankAccount?,
  val contracts: List<Contract>,
  val payoutMethodStatus: PayoutMethodStatus?,
) {
  sealed interface PaymentMethod {
    data class CardPaymentMethod(
      val brand: String?,
      val lastFourDigits: String,
      val expiryMonth: String,
      val expiryYear: String,
    ) : PaymentMethod

    data class ThirdPartyPaymentMethd(
      val name: String,
      val type: String,
    ) : PaymentMethod
  }
}

data class Contract(
  val name: String,
  val typeOfContract: TypeOfContract,
)

data class BankAccount(
  val name: String,
  val accountNumber: String,
)

data class ChargeHistory(
  val charges: List<Charge>,
) {
  data class Charge(
    val amount: MonetaryAmount,
    val date: LocalDate,
  )
}
