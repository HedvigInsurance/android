package com.hedvig.android.datadog

import com.datadog.android.DatadogEventListener
import com.datadog.android.DatadogInterceptor
import com.datadog.android.tracing.TracingInterceptor
import okhttp3.OkHttpClient

fun OkHttpClient.Builder.addDatadogConfiguration(): OkHttpClient.Builder {
  return addInterceptor(DatadogInterceptor())
    .addInterceptor(TracingInterceptor())
    .eventListenerFactory(DatadogEventListener.Factory())
}
