package com.hedvig.app.feature.offer.model

import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.hedvig.android.owldroid.graphql.fragment.QuoteCartFragment

@JvmInline
value class PaymentConnectionId(val id: String)

data class PaymentConnection(
  val id: PaymentConnectionId,
  val providers: List<PaymentProvider>,
)

sealed class PaymentProvider {
  data class Adyen(val availablePaymentOptions: PaymentMethodsApiResponse) : PaymentProvider()
  object Trustly : PaymentProvider()
}

fun QuoteCartFragment.PaymentConnection.toPaymentConnection() = PaymentConnection(
  id = PaymentConnectionId(id ?: ""),
  providers = if (providers.isEmpty()) {
    emptyList()
  } else {
    providers.mapNotNull {
      it.asAdyen?.let {
        PaymentProvider.Adyen(availablePaymentOptions = it.availablePaymentMethods)
      } ?: it.asTrustly?.let {
        PaymentProvider.Trustly
      }
    }
  },
)
