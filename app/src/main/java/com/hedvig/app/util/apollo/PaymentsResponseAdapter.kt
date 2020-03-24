package com.hedvig.app.util.apollo

import com.apollographql.apollo.response.CustomTypeAdapter
import com.apollographql.apollo.response.CustomTypeValue
import org.json.JSONObject

class PaymentsResponseAdapter : CustomTypeAdapter<JSONObject> {
    override fun encode(value: JSONObject): CustomTypeValue<*> =
        CustomTypeValue.fromRawValue(value.toString())

    override fun decode(value: CustomTypeValue<*>) = JSONObject(value.value as String)
}
