package com.hedvig.android.datadog.core.di

import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.datadog.core.attributestracking.DatadogAttributesManager
import com.hedvig.android.datadog.core.attributestracking.DatadogAttributesManagerImpl
import com.hedvig.android.datadog.core.memberid.DatadogMemberIdUpdater
import com.hedvig.android.initializable.Initializable
import io.opentracing.Tracer
import io.opentracing.util.GlobalTracer
import org.koin.dsl.bind
import org.koin.dsl.module

val datadogModule = module {
  single<DatadogMemberIdUpdater> {
    DatadogMemberIdUpdater(
      get<DatadogAttributesManager>(),
      get<MemberIdService>(),
      get<ApplicationScope>(),
    )
  } bind Initializable::class
  single<Tracer> { GlobalTracer.get() }
  single<DatadogAttributesManager> { DatadogAttributesManagerImpl() }
}
