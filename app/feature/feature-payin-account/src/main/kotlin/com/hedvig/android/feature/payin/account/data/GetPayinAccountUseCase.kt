package com.hedvig.android.feature.payin.account.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.Serializable
import octopus.GetPayinMethodsQuery
import octopus.GetPayinMethodsQuery.Data.CurrentMember.PaymentMethods.PayinMethod.Details.Companion.asPaymentMethodBankAccountDetails
import octopus.GetPayinMethodsQuery.Data.CurrentMember.PaymentMethods.PayinMethod.Details.Companion.asPaymentMethodInvoiceDetails
import octopus.GetPayinMethodsQuery.Data.CurrentMember.PaymentMethods.PayinMethod.Details.Companion.asPaymentMethodSwishDetails
import octopus.type.MemberPaymentMethodStatus
import octopus.type.MemberPaymentProvider
import octopus.type.PaymentMethodInvoiceDelivery

internal data class PayinAccountData(
  val currentMethods: List<PayinAccount>,
  val availablePayinMethods: List<MemberPaymentProvider>,
)

@SingleIn(AppScope::class)
@Inject
internal class GetPayinAccountUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(): Either<ErrorMessage, PayinAccountData> = either {
    val result = apolloClient
      .query(GetPayinMethodsQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute(::ErrorMessage)
      .bind()

    val paymentMethods = result.currentMember.paymentMethods

    val currentMethods: List<PayinAccount> = paymentMethods.payinMethods.mapNotNull { method ->
      val isPending = method.status == MemberPaymentMethodStatus.PENDING
      val isDefault = !isPending && method.isDefault
      when (method.provider) {
        MemberPaymentProvider.SWISH -> {
          val phoneNumber = method.details?.asPaymentMethodSwishDetails()?.phoneNumber
          PayinAccount.SwishPayin(
            phoneNumber = phoneNumber,
            isPending = isPending,
            isDefault = isDefault,
          )
        }

        MemberPaymentProvider.TRUSTLY -> {
          val (clearingNumber, accountNumber, bankName) = parseBankAccountDetails(method)
          PayinAccount.Trustly(
            clearingNumber = clearingNumber,
            accountNumber = accountNumber,
            bankName = bankName,
            isPending = isPending,
            isDefault = isDefault,
          )
        }

        MemberPaymentProvider.INVOICE -> {
          val invoiceDetails = method.details?.asPaymentMethodInvoiceDetails()
          PayinAccount.Invoice(
            delivery = invoiceDetails?.delivery.toInvoiceDelivery(),
            email = invoiceDetails?.email,
            isPending = isPending,
            isDefault = isDefault,
          )
        }

        else -> {
          null
        }
      }
    }
    logcat { "availablePayinMethods: before filter ${paymentMethods.availableMethods}" }
    val availablePayinMethods = paymentMethods.availableMethods
      .filter { it.supportsPayin }
      .map { it.provider }
    logcat { "availablePayinMethods: $availablePayinMethods" }
    PayinAccountData(
      currentMethods = currentMethods,
      availablePayinMethods = availablePayinMethods,
    )
  }
}

private data class ParsedBankAccountDetails(
  val clearingNumber: String?,
  val accountNumber: String?,
  val bankName: String?,
)

private fun parseBankAccountDetails(
  method: GetPayinMethodsQuery.Data.CurrentMember.PaymentMethods.PayinMethod,
): ParsedBankAccountDetails {
  val bankAccountDetails = method.details?.asPaymentMethodBankAccountDetails()
  val account = bankAccountDetails?.account
  val dashIndex = account?.indexOf('-') ?: -1
  val clearingNumber = if (dashIndex >= 0) account?.substring(0, dashIndex) else account
  val accountNumber = if (dashIndex >= 0) account?.substring(dashIndex + 1) else null
  return ParsedBankAccountDetails(
    clearingNumber = clearingNumber,
    accountNumber = accountNumber,
    bankName = bankAccountDetails?.bank,
  )
}

/** Serializable so that a chosen set of methods can be carried in a nav key across process death. */
@Serializable
internal sealed interface PayinAccount {
  val isPending: Boolean
  val isDefault: Boolean

  @Serializable
  data class Trustly(
    val clearingNumber: String?,
    val accountNumber: String?,
    val bankName: String?,
    override val isPending: Boolean,
    override val isDefault: Boolean,
  ) : PayinAccount

  @Serializable
  data class SwishPayin(
    val phoneNumber: String?,
    override val isPending: Boolean,
    override val isDefault: Boolean,
  ) : PayinAccount

  @Serializable
  data class Invoice(
    val delivery: InvoiceDelivery?,
    val email: String?,
    override val isPending: Boolean,
    override val isDefault: Boolean,
  ) : PayinAccount
}

internal val PayinAccount.provider: MemberPaymentProvider
  get() = when (this) {
    is PayinAccount.Trustly -> MemberPaymentProvider.TRUSTLY
    is PayinAccount.SwishPayin -> MemberPaymentProvider.SWISH
    is PayinAccount.Invoice -> MemberPaymentProvider.INVOICE
  }

@Serializable
internal enum class InvoiceDelivery {
  Kivra,
  Mail,
}

private fun PaymentMethodInvoiceDelivery?.toInvoiceDelivery(): InvoiceDelivery? {
  return when (this) {
    PaymentMethodInvoiceDelivery.KIVRA -> InvoiceDelivery.Kivra
    PaymentMethodInvoiceDelivery.MAIL -> InvoiceDelivery.Mail
    else -> null
  }
}

internal fun InvoiceDelivery?.toDeliveryString(): String? {
  return when (this) {
    // TODO: Add "Kivra" / "Kivra" to Lokalise
    InvoiceDelivery.Kivra -> "Kivra"

    // TODO: Add "Email" / "E-post" to Lokalise
    InvoiceDelivery.Mail -> "Email"

    null -> null
  }
}
