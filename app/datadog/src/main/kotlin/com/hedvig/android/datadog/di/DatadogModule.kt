package com.hedvig.android.datadog.di

import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.datadog.memberid.DatadogMemberIdUpdatingAuthEventListener
import io.opentracing.Tracer
import io.opentracing.util.GlobalTracer
import org.koin.dsl.bind
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val datadogModule = module {
  single<DatadogMemberIdUpdatingAuthEventListener> {
    DatadogMemberIdUpdatingAuthEventListener()
  } bind AuthEventListener::class
  single<Tracer> { GlobalTracer.get() }
}
