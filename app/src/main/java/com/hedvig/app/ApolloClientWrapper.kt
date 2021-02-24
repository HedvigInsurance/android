package com.hedvig.app

import com.apollographql.apollo.api.ScalarTypeAdapters
import com.hedvig.android.owldroid.type.CustomType
import com.hedvig.app.util.apollo.PaymentMethodsApiResponseAdapter
import com.hedvig.app.util.apollo.PromiscuousLocalDateAdapter

val CUSTOM_TYPE_ADAPTERS = ScalarTypeAdapters(
    mapOf(
        CustomType.LOCALDATE to PromiscuousLocalDateAdapter(),
        CustomType.PAYMENTMETHODSRESPONSE to PaymentMethodsApiResponseAdapter(),
        CustomType.JSONSTRING to JSONStringAdapter(),
    )
)
