package com.hedvig.app.util.apollo.adapter

import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.apollographql.apollo3.api.CustomTypeAdapter
import com.apollographql.apollo3.api.CustomTypeValue
import com.hedvig.app.util.toJsonObject
import org.json.JSONObject

// TODO: Not sure why we're doing the LinkedHashMap check and how to migrate this to apollo3 Adapter. We can use the
//  compat Version2CustomTypeAdapterToAdapter for now.
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
