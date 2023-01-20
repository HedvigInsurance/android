package com.hedvig.android.odyssey.di

import com.hedvig.android.odyssey.AndroidDatadogLogger
import com.hedvig.android.odyssey.AndroidDatadogProvider
import com.hedvig.common.datadog.DatadogLogger
import com.hedvig.common.datadog.DatadogProvider
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val odysseyModule = module {
  single<DatadogLogger> { AndroidDatadogLogger() }
  single<DatadogProvider> { AndroidDatadogProvider(get(), get()) }
}
