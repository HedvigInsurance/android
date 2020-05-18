package com.hedvig.app

import androidx.lifecycle.MutableLiveData
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.hedvig.app.feature.adyen.AdyenViewModel
import org.json.JSONObject

class MockAdyenViewModel : AdyenViewModel() {
    override val paymentMethods = MutableLiveData<PaymentMethodsApiResponse>()
    override fun loadPaymentMethods() {
        paymentMethods.postValue(
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
