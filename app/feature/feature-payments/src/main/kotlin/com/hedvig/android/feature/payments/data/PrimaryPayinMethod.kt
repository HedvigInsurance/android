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
 */
data class PrimaryPayinMethod(
  val id: PayinMethodId,
  val descriptor: String?,
)

internal fun MemberPaymentMethodFragment.toPrimaryPayinMethod(): PrimaryPayinMethod? {
  val id = when (provider) {
    MemberPaymentProvider.TRUSTLY -> PayinMethodId.Trustly
    MemberPaymentProvider.SWISH -> PayinMethodId.Swish
    MemberPaymentProvider.INVOICE -> PayinMethodId.Invoice
    MemberPaymentProvider.NORDEA, MemberPaymentProvider.UNKNOWN__ -> return null
  }
  return PrimaryPayinMethod(id = id, descriptor = toPayinAccount()?.descriptor())
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
