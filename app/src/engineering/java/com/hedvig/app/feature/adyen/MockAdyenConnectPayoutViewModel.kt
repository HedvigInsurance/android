package com.hedvig.app.feature.adyen

import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.hedvig.app.feature.adyen.payout.AdyenConnectPayoutViewModel
import org.json.JSONObject

class MockAdyenConnectPayoutViewModel : AdyenConnectPayoutViewModel() {
    init {
        _payoutMethods.postValue(
            PaymentMethodsApiResponse.SERIALIZER.deserialize(
                JSONObject(
                    """
                {
                  "paymentMethods": [
                    {
                      "name": "Trustly",
                      "supportsRecurring": true,
                      "type": "trustly"
                    }
                  ],
                  "groups": [
                    {
                      "name": "Credit Card",
                      "types": [
                        "visa",
                        "mc",
                        "amex",
                        "amex_applepay",
                        "diners",
                        "discover",
                        "maestro",
                        "mc_applepay",
                        "visa_applepay"
                      ]
                    }
                  ]
                }
                    """.trimIndent()
                )
            )
        )
    }
}
