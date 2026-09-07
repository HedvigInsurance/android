package com.hedvig.android.datadog.core.di

import com.datadog.android.core.sampling.RateBasedSampler
import com.datadog.android.okhttp.DatadogEventListener
import com.datadog.android.okhttp.DatadogInterceptor
import com.datadog.android.okhttp.trace.TracingInterceptor
import com.hedvig.android.authlib.AuthEnvironment
import com.hedvig.android.authlib.baseUrl
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.AuthHttpClientEngine
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

/**
 * The auth client is built inside `:authlib`, which is KMP and carries no Datadog dependency, so its
 * observability is attached here by handing it an engine that is already instrumented.
 *
 * Instrumenting at the OkHttp layer rather than through the Ktor plugin is what makes the per-request
 * timing breakdown land: [DatadogInterceptor] opens the RUM resource and [DatadogEventListener]
 * reports DNS, connect, SSL, first-byte and download against the same OkHttp-owned key.
 */
@ContributesTo(AppScope::class)
interface AuthNetworkMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  @AuthHttpClientEngine
  fun provideAuthHttpClientEngine(hedvigBuildConstants: HedvigBuildConstants): HttpClientEngine {
    val tracedHosts = listOf(authHost(hedvigBuildConstants))
    return OkHttp.create {
      config {
        eventListenerFactory(DatadogEventListener.Factory())
        addInterceptor(
          DatadogInterceptor.Builder(tracedHosts)
            .setTraceSampler(RateBasedSampler(sampleRate = TRACE_SAMPLE_RATE))
            .build(),
        )
        addNetworkInterceptor(
          TracingInterceptor.Builder(tracedHosts)
            .setTraceSampler(RateBasedSampler(sampleRate = TRACE_SAMPLE_RATE))
            .build(),
        )
      }
    }
  }
}

private const val TRACE_SAMPLE_RATE = 100f

internal fun authHost(hedvigBuildConstants: HedvigBuildConstants): String {
  val environment = if (hedvigBuildConstants.isProduction) {
    AuthEnvironment.PRODUCTION
  } else {
    AuthEnvironment.STAGING
  }
  return environment.baseUrl.removePrefix("https://")
}
