package com.hedvig.app.util.apollo

import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.apollographql.apollo.response.CustomTypeAdapter
import com.apollographql.apollo.response.CustomTypeValue
import org.json.JSONObject

class PaymentMethodsApiResponseAdapter : CustomTypeAdapter<PaymentMethodsApiResponse> {
    override fun encode(value: PaymentMethodsApiResponse): CustomTypeValue<*> {
        return CustomTypeValue.fromRawValue(
            PaymentMethodsApiResponse.SERIALIZER.serialize(value).toString()
        )
    }

    override fun decode(value: CustomTypeValue<*>): PaymentMethodsApiResponse? {
        return PaymentMethodsApiResponse.SERIALIZER.deserialize(JSONObject(value.value as String))
    }
}
