package com.hedvig.android.typeadapter

import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.apollographql.apollo3.annotations.ApolloInternal
import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import com.apollographql.apollo3.api.json.readAny
import org.json.JSONObject

/**
 * PaymentMethodsApiResponse is sometimes (?) read as a map from the JsonReader, therefore this special deserialization
 * is required.
 */
@Suppress("unused") // Used inside the `apollo {}` block inside build.gradle.kts
object PaymentMethodsApiResponseAdapter : Adapter<PaymentMethodsApiResponse> {
    @OptIn(ApolloInternal::class)
    override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): PaymentMethodsApiResponse {
        val data = reader.readAny()
        return if (data is LinkedHashMap<*, *>) {
            PaymentMethodsApiResponse.SERIALIZER.deserialize(JSONObject(data.toMap()))
        } else {
            val jsonString = data.toString().replace("\\", "")
            PaymentMethodsApiResponse.SERIALIZER.deserialize(JSONObject(jsonString))
        }
    }

    override fun toJson(
        writer: JsonWriter,
        customScalarAdapters: CustomScalarAdapters,
        value: PaymentMethodsApiResponse,
    ) {
        val jsonString = PaymentMethodsApiResponse.SERIALIZER.serialize(value).toString()
        writer.value(jsonString)
    }
}
