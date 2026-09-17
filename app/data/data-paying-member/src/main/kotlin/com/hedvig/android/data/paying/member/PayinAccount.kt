package com.hedvig.android.data.paying.member

import kotlinx.serialization.Serializable
import octopus.fragment.MemberPaymentMethodFragment
import octopus.fragment.MemberPaymentMethodFragment.Details.Companion.asPaymentMethodBankAccountDetails
import octopus.fragment.MemberPaymentMethodFragment.Details.Companion.asPaymentMethodInvoiceDetails
import octopus.fragment.MemberPaymentMethodFragment.Details.Companion.asPaymentMethodSwishDetails
import octopus.type.MemberPaymentMethodStatus
import octopus.type.MemberPaymentProvider
import octopus.type.PaymentMethodInvoiceDelivery

/** Serializable so that a chosen set of methods can be carried in a nav key across process death. */
@Serializable
sealed interface PayinAccount {
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

@Serializable
enum class InvoiceDelivery {
  Kivra,
  Mail,
}

/** Null for a provider the app has no screen for, which callers drop from the methods they show. */
fun MemberPaymentMethodFragment.toPayinAccount(): PayinAccount? {
  val isPending = status == MemberPaymentMethodStatus.PENDING
  // The backend also marks a pending method default once the member picks it during setup, but
  // nothing is charged to it until it activates, so it is not the default the app shows.
  val isDefaultAndActive = !isPending && isDefault
  return when (provider) {
    MemberPaymentProvider.SWISH -> {
      PayinAccount.SwishPayin(
        phoneNumber = details?.asPaymentMethodSwishDetails()?.phoneNumber,
        isPending = isPending,
        isDefault = isDefaultAndActive,
      )
    }

    MemberPaymentProvider.TRUSTLY -> {
      val bankAccountDetails = details?.asPaymentMethodBankAccountDetails()
      val account = bankAccountDetails?.account
      val dashIndex = account?.indexOf('-') ?: -1
      PayinAccount.Trustly(
        clearingNumber = if (dashIndex >= 0) account?.substring(0, dashIndex) else account,
        accountNumber = if (dashIndex >= 0) account?.substring(dashIndex + 1) else null,
        bankName = bankAccountDetails?.bank,
        isPending = isPending,
        isDefault = isDefaultAndActive,
      )
    }

    MemberPaymentProvider.INVOICE -> {
      val invoiceDetails = details?.asPaymentMethodInvoiceDetails()
      PayinAccount.Invoice(
        delivery = invoiceDetails?.delivery.toInvoiceDelivery(),
        email = invoiceDetails?.email,
        isPending = isPending,
        isDefault = isDefaultAndActive,
      )
    }

    else -> {
      null
    }
  }
}

/** Identifies the method to the backend: a member has at most one method per provider. */
val PayinAccount.provider: MemberPaymentProvider
  get() = when (this) {
    is PayinAccount.Trustly -> MemberPaymentProvider.TRUSTLY
    is PayinAccount.SwishPayin -> MemberPaymentProvider.SWISH
    is PayinAccount.Invoice -> MemberPaymentProvider.INVOICE
  }

/**
 * The order methods are shown in. Keyed on the provider, which a member has at most one method for, so a
 * method keeps its place as others are added, removed or made primary.
 */
fun List<PayinAccount>.sortedForDisplay(): List<PayinAccount> = sortedBy { it.provider.rawValue }

/** The last four digits of the account, the only part of it we ever put on screen. */
fun PayinAccount.Trustly.maskedAccountNumber(): String? {
  val clearing = clearingNumber?.takeIf { it.isNotBlank() }.orEmpty()
  val account = accountNumber?.takeIf { it.isNotBlank() }.orEmpty()
  val whole = clearing + account
  if (whole.length <= 8) return null
  return "**** ${whole.takeLast(4)}"
}

private fun PaymentMethodInvoiceDelivery?.toInvoiceDelivery(): InvoiceDelivery? {
  return when (this) {
    PaymentMethodInvoiceDelivery.KIVRA -> InvoiceDelivery.Kivra
    PaymentMethodInvoiceDelivery.MAIL -> InvoiceDelivery.Mail
    else -> null
  }
}
