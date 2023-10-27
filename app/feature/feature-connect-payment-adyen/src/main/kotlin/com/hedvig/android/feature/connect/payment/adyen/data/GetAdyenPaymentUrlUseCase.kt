package com.hedvig.android.feature.connect.payment.adyen.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage

internal class GetAdyenPaymentUrlUseCase {
  suspend fun invoke(): Either<ErrorMessage, AdyenPaymentUrl> {
    return AdyenPaymentUrl("").right()
  }
}

@JvmInline
internal value class AdyenPaymentUrl(val url: String)
