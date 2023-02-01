package com.hedvig.android.datadog.di

import io.opentracing.Tracer
import io.opentracing.util.GlobalTracer
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val datadogModule = module {
  single<Tracer> { GlobalTracer.get() }
}
