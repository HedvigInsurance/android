package com.hedvig.android.hanalytics.featureflags.paymenttype

import com.hedvig.hanalytics.PaymentType

interface PaymentTypeProvider {
  suspend fun getPaymentType(): PaymentType
}
