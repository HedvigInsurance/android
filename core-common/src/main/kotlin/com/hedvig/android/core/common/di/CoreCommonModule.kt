package com.hedvig.android.core.common.di

import com.hedvig.android.core.common.ApplicationScope
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

@Suppress("RemoveExplicitTypeArguments")
val coreCommonModule = module {
  single<ApplicationScope> { ApplicationScope() }
  single<CoroutineContext>(ioDispatcherQualifier) { Dispatchers.IO }
}
