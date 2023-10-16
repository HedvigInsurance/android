package com.hedvig.android.datadog.core

import com.datadog.android.DatadogEventListener
import com.datadog.android.DatadogInterceptor
import com.hedvig.android.code.buildoconstants.HedvigBuildConstants
import okhttp3.OkHttpClient

fun OkHttpClient.Builder.addDatadogConfiguration(
  hedvigBuildConstants: HedvigBuildConstants,
): OkHttpClient.Builder {
  return this
    .eventListenerFactory(DatadogEventListener.Factory())
    .addInterceptor(
      DatadogInterceptor(
        firstPartyHosts = listOf(
          hedvigBuildConstants.urlGiraffeGraphql,
          hedvigBuildConstants.urlGraphqlOctopus,
        ),
      ),
    )
}
