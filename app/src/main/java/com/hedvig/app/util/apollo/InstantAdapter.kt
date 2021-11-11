package com.hedvig.app.util.apollo

import com.apollographql.apollo.api.CustomTypeAdapter
import com.apollographql.apollo.api.CustomTypeValue
import java.time.Instant

class InstantAdapter : CustomTypeAdapter<Instant> {
    override fun decode(value: CustomTypeValue<*>): Instant = Instant.parse(value.value as String)
    override fun encode(value: Instant): CustomTypeValue<*> = CustomTypeValue.fromRawValue(value.toString())
}
