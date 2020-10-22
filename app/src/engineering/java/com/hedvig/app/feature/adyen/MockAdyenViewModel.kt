package com.hedvig.app.feature.adyen

import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import org.json.JSONObject

class MockAdyenViewModel : AdyenViewModel() {
    init {
        _paymentMethods.postValue(
            PaymentMethodsApiResponse.SERIALIZER.deserialize(
                JSONObject(
                    """{
  "groups": [
    {
      "name": "Credit Card",
      "types": [
        "mc",
        "visa",
        "amex",
        "maestro",
        "diners",
        "discover"
      ]
    }
  ],
  "paymentMethods": [
    {
      "brands": [
        "mc",
        "visa",
        "amex",
        "maestro",
        "diners",
        "discover"
      ],
      "details": [
        {
          "key": "encryptedCardNumber",
          "type": "cardToken"
        },
        {
          "key": "encryptedSecurityCode",
          "type": "cardToken"
        },
        {
          "key": "encryptedExpiryMonth",
          "type": "cardToken"
        },
        {
          "key": "encryptedExpiryYear",
          "type": "cardToken"
        },
        {
          "key": "holderName",
          "optional": true,
          "type": "text"
        }
      ],
      "name": "Credit Card",
      "type": "scheme"
    }
  ]
}"""
                )
            )
        )
    }
}
