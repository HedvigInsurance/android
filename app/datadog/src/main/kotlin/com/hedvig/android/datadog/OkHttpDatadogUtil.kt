package com.hedvig.android.datadog

import com.datadog.android.DatadogEventListener
import com.datadog.android.DatadogInterceptor
import okhttp3.OkHttpClient

fun OkHttpClient.Builder.addDatadogConfiguration(): OkHttpClient.Builder {
  return addInterceptor(DatadogInterceptor())
    .eventListenerFactory(DatadogEventListener.Factory())
}
