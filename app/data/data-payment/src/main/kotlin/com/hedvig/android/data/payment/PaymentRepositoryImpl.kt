package com.hedvig.android.data.payment

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.apollo.toMonetaryAmount
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.language.LanguageService
import giraffe.PaymentQuery
import giraffe.type.PayoutMethodStatus
import giraffe.type.TypeOfContract
import javax.money.MonetaryAmount
import kotlinx.datetime.LocalDate
import octopus.PaymentHistoryQuery
import octopus.type.MemberChargeHistoryEntryStatus

internal class PaymentRepositoryImpl(
  private val giraffeApolloClient: ApolloClient,
  private val octopusApolloClient: ApolloClient,
  private val languageService: LanguageService,
) : PaymentRepository {
  override suspend fun getChargeHistory(): Either<OperationResult.Error, ChargeHistory> = either {
    octopusApolloClient
      .query(PaymentHistoryQuery())
      .safeExecute()
      .toEither()
      .map { paymentData ->
        val chargeHistory = paymentData.currentMember.chargeHistory
        ChargeHistory(
          charges = chargeHistory.map { chargeHistoryEntry ->
            ChargeHistory.Charge(
              amount = UiMoney.fromMoneyFragment(chargeHistoryEntry.amount),
              date = chargeHistoryEntry.date,
              paymentStatus = when (chargeHistoryEntry.status) {
                MemberChargeHistoryEntryStatus.PENDING -> ChargeHistory.Charge.PaymentStatus.PENDING
                MemberChargeHistoryEntryStatus.FAILED -> ChargeHistory.Charge.PaymentStatus.FAILED
                MemberChargeHistoryEntryStatus.SUCCESS -> ChargeHistory.Charge.PaymentStatus.SUCCESSFUL
                else -> ChargeHistory.Charge.PaymentStatus.UNKNOWN
              },
            )
          },
        )
      }
      .bind()
  }

  override suspend fun getPaymentData(): Either<OperationResult.Error, PaymentData> = either {
    val data = giraffeApolloClient
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
    val amount: UiMoney,
    val date: LocalDate,
    val paymentStatus: PaymentStatus,
  ) {
    enum class PaymentStatus {
      PENDING,
      SUCCESSFUL,
      FAILED,
      UNKNOWN,
    }
  }
}

data class PaymentData(
  val nextCharge: MonetaryAmount,
  val monthlyCost: MonetaryAmount?,
  val totalDiscount: MonetaryAmount?,
  val nextChargeDate: java.time.LocalDate?,
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
