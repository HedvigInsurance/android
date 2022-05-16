package com.hedvig.app.util.apollo

import com.apollographql.apollo3.api.CustomTypeAdapter
import com.apollographql.apollo3.api.CustomTypeValue
import org.json.JSONObject

class JSONStringAdapter : CustomTypeAdapter<JSONObject> {
    override fun decode(value: CustomTypeValue<*>) = JSONObject(value.value as String)
    override fun encode(value: JSONObject) = CustomTypeValue.fromRawValue(value.toString())
}
