package com.hedvig.app

import com.apollographql.apollo3.api.CustomScalarAdapters
import com.hedvig.android.owldroid.graphql.type.JSONString
import com.hedvig.android.owldroid.graphql.type.PaymentMethodsResponse
import com.hedvig.app.util.apollo.JSONStringAdapter
import com.hedvig.app.util.apollo.PaymentMethodsApiResponseAdapter

val CUSTOM_SCALAR_ADAPTERS: CustomScalarAdapters = CustomScalarAdapters.Builder()
    .add(JSONString.type, JSONStringAdapter())
    .add(PaymentMethodsResponse.type, PaymentMethodsApiResponseAdapter())
    .build()
