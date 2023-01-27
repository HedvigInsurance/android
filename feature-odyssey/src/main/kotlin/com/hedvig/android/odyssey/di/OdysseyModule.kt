package com.hedvig.android.odyssey.di

import com.hedvig.android.odyssey.AndroidDatadogLogger
import com.hedvig.android.odyssey.AndroidDatadogProvider
import com.hedvig.odyssey.datadog.DatadogLogger
import com.hedvig.odyssey.datadog.DatadogProvider
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val odysseyModule = module {
  single<DatadogLogger> { AndroidDatadogLogger() }
  single<DatadogProvider> { AndroidDatadogProvider(get(), get()) }
}
