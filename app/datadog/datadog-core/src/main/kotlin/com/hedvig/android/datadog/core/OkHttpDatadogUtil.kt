package com.hedvig.android.datadog.core

import com.datadog.android.core.sampling.RateBasedSampler
import com.datadog.android.okhttp.DatadogEventListener
import com.datadog.android.okhttp.DatadogInterceptor
import com.datadog.android.okhttp.trace.TracingInterceptor
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import okhttp3.OkHttpClient

fun OkHttpClient.Builder.addDatadogConfiguration(hedvigBuildConstants: HedvigBuildConstants): OkHttpClient.Builder {
  val tracedHosts = listOf(hedvigBuildConstants.urlGraphqlOctopus.removePrefix("https://"))
  return this
    .eventListenerFactory(DatadogEventListener.Factory())
    .addInterceptor(
      DatadogInterceptor(
        sdkInstanceName = null,
        firstPartyHosts = tracedHosts,
        traceSampler = RateBasedSampler(sampleRate = 100f),
      ),
    )
    .addNetworkInterceptor(
      TracingInterceptor(
        sdkInstanceName = null,
        tracedHosts = tracedHosts,
        traceSampler = RateBasedSampler(sampleRate = 100f),
      ),
    )
}
