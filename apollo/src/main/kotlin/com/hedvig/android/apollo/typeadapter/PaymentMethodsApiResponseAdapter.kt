package com.hedvig.android.apollo.typeadapter

import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.AnyAdapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import org.json.JSONObject

/**
 * PaymentMethodsApiResponse is sometimes read as a map from the JsonReader.
 * This adapter handles both cases, therefore this special deserialization checking for a Map is required.
 */
@Suppress("unused") // Used inside the `apollo` block inside build.gradle.kts
object PaymentMethodsApiResponseAdapter : Adapter<PaymentMethodsApiResponse> {
  override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): PaymentMethodsApiResponse {
    val data = AnyAdapter.fromJson(reader, customScalarAdapters)
    return if (data is Map<*, *>) {
      PaymentMethodsApiResponse.SERIALIZER.deserialize(JSONObject(data))
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
