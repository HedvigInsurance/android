package com.hedvig.app.util.apollo

import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.apollographql.apollo.api.CustomTypeAdapter
import com.apollographql.apollo.api.CustomTypeValue
import com.hedvig.app.util.toJsonObject
import org.json.JSONObject

class PaymentMethodsApiResponseAdapter : CustomTypeAdapter<PaymentMethodsApiResponse> {
    override fun encode(value: PaymentMethodsApiResponse) = CustomTypeValue.fromRawValue(
        PaymentMethodsApiResponse.SERIALIZER.serialize(value).toString()
    )

    override fun decode(value: CustomTypeValue<*>): PaymentMethodsApiResponse {
        return if (value.value is LinkedHashMap<*, *>) {
            PaymentMethodsApiResponse.SERIALIZER.deserialize((value.value as LinkedHashMap<*, *>).toJsonObject())
        } else {
            PaymentMethodsApiResponse.SERIALIZER.deserialize(JSONObject(value.value as String))
        }
    }
}
