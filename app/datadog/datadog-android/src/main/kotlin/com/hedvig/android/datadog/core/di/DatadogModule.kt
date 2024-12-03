package com.hedvig.android.datadog.core.di

import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.datadog.core.attributestracking.DatadogAttributeProvider
import com.hedvig.android.datadog.core.attributestracking.DatadogAttributesManager
import com.hedvig.android.datadog.core.attributestracking.DatadogMemberIdProvider
import com.hedvig.android.datadog.core.attributestracking.DatadogMemberIdProviderImpl
import com.hedvig.android.datadog.core.attributestracking.DeviceIdProvider
import com.hedvig.android.initializable.Initializable
import io.opentracing.Tracer
import io.opentracing.util.GlobalTracer
import org.koin.dsl.bind
import org.koin.dsl.module

val datadogModule = module {
  single<Tracer> { GlobalTracer.get() }

  single<DatadogMemberIdProvider> {
    DatadogMemberIdProviderImpl(get<MemberIdService>())
  }
  single<DeviceIdProvider> {
    DeviceIdProvider(get())
  } bind DatadogAttributeProvider::class

  single<DatadogAttributesManager> {
    DatadogAttributesManager(
      get<ApplicationScope>(),
      get<DatadogMemberIdProvider>(),
      getAll<DatadogAttributeProvider>().toSet(),
    )
  } bind Initializable::class
}
