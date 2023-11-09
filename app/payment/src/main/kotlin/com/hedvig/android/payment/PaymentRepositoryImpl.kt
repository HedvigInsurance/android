package com.hedvig.android.payment

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.apollo.toMonetaryAmount
import com.hedvig.android.language.LanguageService
import giraffe.ChargeHistoryQuery
import giraffe.PaymentQuery
import giraffe.type.PayoutMethodStatus
import giraffe.type.TypeOfContract
import java.time.LocalDate
import javax.money.MonetaryAmount

internal class PaymentRepositoryImpl(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) : PaymentRepository {
  override suspend fun getChargeHistory(): Either<OperationResult.Error, ChargeHistory> = either {
    apolloClient
      .query(ChargeHistoryQuery())
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
    val data = apolloClient
      .query(PaymentQuery(languageService.getGraphQLLocale()))
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute()
      .toEither()
      .bind()

    val costFragment = data.insuranceCost?.fragments?.costFragment
    PaymentData(
      nextCharge = data.chargeEstimation.subscription.fragments.monetaryAmountFragment.toMonetaryAmount(),
      monthlyCost = costFragment?.monthlyNet?.fragments?.monetaryAmountFragment?.toMonetaryAmount(),
      totalDiscount = costFragment?.monthlyDiscount?.fragments?.monetaryAmountFragment?.toMonetaryAmount(),
      nextChargeDate = data.nextChargeDate,
      contracts = data.contracts
        .filter { it.status.fragments.contractStatusFragment.asActiveStatus != null }
        .map {
          PaymentData.Contract(
            name = it.displayName,
            typeOfContract = it.typeOfContract,
          )
        },
      redeemedCampagins = data.redeemedCampaigns.map {
        PaymentData.Campaign(
          displayValue = it.fragments.incentiveFragment.displayValue,
          code = it.code,
        )
      },
      bankName = data.bankAccount?.fragments?.bankAccountFragment?.bankName,
      bankDescriptor = data.bankAccount?.fragments?.bankAccountFragment?.descriptor,
      paymentMethod = data.activePaymentMethodsV2
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
        } ?: data.activePaymentMethodsV2
        ?.fragments
        ?.activePaymentMethodsFragment
        ?.asStoredThirdPartyDetails
        ?.let {
          PaymentData.PaymentMethod.ThirdPartyPaymentMethd(
            name = it.name,
            type = it.type,
          )
        },
      payoutMethodStatus = data.activePayoutMethods?.status,
      bankAccount = data.bankAccount?.let {
        PaymentData.BankAccount(
          name = it.fragments.bankAccountFragment.bankName,
          accountNumber = it.fragments.bankAccountFragment.descriptor,
        )
      },
    )
  }
}

data class ChargeHistory(
  val charges: List<Charge>,
) {
  data class Charge(
    val amount: MonetaryAmount,
    val date: LocalDate,
  )
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

  data class Contract(
    val name: String,
    val typeOfContract: TypeOfContract,
  )

  data class BankAccount(
    val name: String,
    val accountNumber: String,
  )

  data class Campaign(
    val displayValue: String?,
    val code: String,
  )
}
