package com.hedvig.app.util.featureflags.paymenttype

import com.hedvig.hanalytics.PaymentType

interface PaymentTypeProvider {
  suspend fun getPaymentType(): PaymentType
}
