package com.hedvig.android.datadog.demo.tracking.di

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.datadog.core.attributestracking.DatadogAttributeProvider
import com.hedvig.android.datadog.demo.tracking.DatadogDemoModeTracking
import org.koin.dsl.bind
import org.koin.dsl.module

val datadogDemoTrackingModule = module {
  single<DatadogDemoModeTracking> {
    DatadogDemoModeTracking(get<DemoManager>())
  } bind DatadogAttributeProvider::class
}
