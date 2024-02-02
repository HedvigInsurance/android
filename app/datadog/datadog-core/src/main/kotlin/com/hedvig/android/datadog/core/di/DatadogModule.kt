package com.hedvig.android.datadog.core.di

import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.datadog.core.attributestracking.DatadogAttributesManager
import com.hedvig.android.datadog.core.attributestracking.DatadogAttributesManagerImpl
import com.hedvig.android.datadog.core.memberid.DatadogMemberIdUpdatingAuthEventListener
import io.opentracing.Tracer
import io.opentracing.util.GlobalTracer
import org.koin.dsl.bind
import org.koin.dsl.module

val datadogModule = module {
  single<DatadogMemberIdUpdatingAuthEventListener> {
    DatadogMemberIdUpdatingAuthEventListener(get<DatadogAttributesManager>())
  } bind AuthEventListener::class
  single<Tracer> { GlobalTracer.get() }
  single<DatadogAttributesManager> { DatadogAttributesManagerImpl() }
}
