package com.hedvig.android.feature.payoutaccount.data

import octopus.type.PaymentMethodInvoiceDelivery

// Swap the initial value to test each display scenario in the Overview:
//   null                                                                     → "Connect payout account" button
//   PayoutAccount.Trustly                                                    → Trustly display
//   PayoutAccount.SwishPayout("0701234567")                                  → Swish display
//   PayoutAccount.BankAccount("8327", "12345678", "Swedbank")                → Bank account display
//   PayoutAccount.Invoice(PaymentMethodInvoiceDelivery.KIVRA, null)          → Invoice display
internal object FakePayoutAccountStorage {
  var currentMethod: PayoutAccount? = null
}
