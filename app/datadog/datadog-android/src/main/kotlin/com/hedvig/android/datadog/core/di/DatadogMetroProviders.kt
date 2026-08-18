package com.hedvig.android.datadog.core.di

import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.Tracer

@ContributesTo(AppScope::class)
interface DatadogMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideTracer(): Tracer = GlobalOpenTelemetry.get().getTracer("android")
}
