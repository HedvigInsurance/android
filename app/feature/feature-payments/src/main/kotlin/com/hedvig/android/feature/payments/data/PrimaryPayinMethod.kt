package com.hedvig.android.feature.payments.data

import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.data.paying.member.bankAndMaskedAccount
import com.hedvig.android.data.paying.member.formatSwishPhoneNumber
import com.hedvig.android.data.paying.member.toDeliveryString
import com.hedvig.android.data.paying.member.toPayinAccount
import com.hedvig.android.feature.payin.account.navigation.PayinMethodId
import octopus.fragment.MemberPaymentMethodFragment
import octopus.type.MemberPaymentProvider

/**
 * The method the member is currently charged with, summarised for the payments overview.
 *
 * [descriptor] is the method's account in the member's own terms — the Swish number, the masked bank account, or
 * the invoice delivery channel. Invoice has nothing else worth showing, so the row uses it as the title there and
 * as the subtitle for the other providers.
 *
 * A [isPending] method is still being set up, so there is nothing to manage on it yet.
 */
data class PrimaryPayinMethod(
  val id: PayinMethodId,
  val descriptor: String?,
  val isPending: Boolean = false,
)

internal fun MemberPaymentMethodFragment.toPrimaryPayinMethod(): PrimaryPayinMethod? {
  val id = when (provider) {
    MemberPaymentProvider.TRUSTLY -> PayinMethodId.Trustly
    MemberPaymentProvider.SWISH -> PayinMethodId.Swish
    MemberPaymentProvider.INVOICE -> PayinMethodId.Invoice
    MemberPaymentProvider.NORDEA, MemberPaymentProvider.UNKNOWN__ -> return null
  }
  val payinAccount = toPayinAccount()
  return PrimaryPayinMethod(
    id = id,
    descriptor = payinAccount?.descriptor(),
    isPending = payinAccount?.isPending == true,
  )
}

private fun PayinAccount.descriptor(): String? = when (this) {
  is PayinAccount.Trustly -> {
    bankAndMaskedAccount()
  }

  is PayinAccount.SwishPayin -> {
    phoneNumber?.let(::formatSwishPhoneNumber)
  }

  is PayinAccount.Invoice -> {
    delivery.toDeliveryString()
  }
}
