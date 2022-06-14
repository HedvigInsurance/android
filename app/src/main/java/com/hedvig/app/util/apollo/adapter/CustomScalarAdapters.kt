package com.hedvig.app.util.apollo.adapter

import com.apollographql.apollo3.api.CustomScalarAdapters
import com.hedvig.android.owldroid.graphql.type.JSONString
import com.hedvig.android.owldroid.graphql.type.LocalDate
import com.hedvig.android.owldroid.graphql.type.PaymentMethodsResponse

val CUSTOM_SCALAR_ADAPTERS: CustomScalarAdapters = CustomScalarAdapters.Builder()
    .add(JSONString.type, JSONStringAdapter)
    .add(LocalDate.type, PromiscuousLocalDateAdapter)
    .add(PaymentMethodsResponse.type, PaymentMethodsApiResponseAdapter)
    .build()
