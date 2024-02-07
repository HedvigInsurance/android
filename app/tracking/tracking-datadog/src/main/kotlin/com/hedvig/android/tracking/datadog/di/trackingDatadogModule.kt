package com.hedvig.android.tracking.datadog.di

import com.hedvig.android.initializable.Initializable
import com.hedvig.android.tracking.datadog.ActionLoggerInitializer
import org.koin.dsl.bind
import org.koin.dsl.module

val trackingDatadogModule = module {
  single<ActionLoggerInitializer> { ActionLoggerInitializer() } bind Initializable::class
}
