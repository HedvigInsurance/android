package com.hedvig.android.datadog.demo.tracking.di

import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.datadog.core.attributestracking.DatadogAttributesManager
import com.hedvig.android.datadog.demo.tracking.DatadogDemoModeTracking
import com.hedvig.android.initializable.Initializable
import org.koin.dsl.bind
import org.koin.dsl.module

val datadogDemoTrackingModule = module {
  single<DatadogDemoModeTracking> {
    DatadogDemoModeTracking(get<ApplicationScope>(), get<DemoManager>(), get<DatadogAttributesManager>())
  } bind Initializable::class
}
