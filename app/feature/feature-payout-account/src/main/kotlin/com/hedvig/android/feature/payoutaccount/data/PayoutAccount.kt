package com.hedvig.android.feature.payoutaccount.data

import com.hedvig.android.data.paying.member.PaymentProvider

internal sealed interface PayoutAccount {
  val isPending: Boolean
  val isDefault: Boolean

  data class Trustly(
    val clearingNumber: String?,
    val accountNumber: String?,
    val bankName: String?,
    override val isPending: Boolean,
    override val isDefault: Boolean,
  ) : PayoutAccount

  data class SwishPayout(
    val phoneNumber: String?,
    override val isPending: Boolean,
    override val isDefault: Boolean,
  ) : PayoutAccount

  data class BankAccount(
    val clearingNumber: String?,
    val accountNumber: String?,
    val bankName: String?,
    override val isPending: Boolean,
    override val isDefault: Boolean,
  ) : PayoutAccount
}

/** Identifies the method to the backend: a member has at most one method per provider. */
internal val PayoutAccount.provider: PaymentProvider
  get() = when (this) {
    is PayoutAccount.Trustly -> PaymentProvider.Trustly
    is PayoutAccount.SwishPayout -> PaymentProvider.Swish
    is PayoutAccount.BankAccount -> PaymentProvider.Nordea
  }

/**
 * The order methods are shown in. Keyed on the provider, which a member has at most one method for, so a
 * method keeps its place as others are added or made primary.
 */
internal fun List<PayoutAccount>.sortedForDisplay(): List<PayoutAccount> = sortedBy { it.provider.rawValue }
