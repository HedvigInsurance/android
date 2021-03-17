package com.example.network

import com.apollographql.apollo.api.ScalarTypeAdapters
import com.hedvig.android.owldroid.type.CustomType

val CUSTOM_TYPE_ADAPTERS = ScalarTypeAdapters(
    mapOf(
        CustomType.LOCALDATE to PromiscuousLocalDateAdapter(),
        CustomType.PAYMENTMETHODSRESPONSE to PaymentMethodsApiResponseAdapter(),
        CustomType.JSONSTRING to JSONStringAdapter(),
    )
)
