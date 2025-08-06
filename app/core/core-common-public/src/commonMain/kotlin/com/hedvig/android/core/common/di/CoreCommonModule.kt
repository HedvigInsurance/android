package com.hedvig.android.core.common.di

import com.hedvig.android.core.common.ApplicationScope
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val coreCommonModule = module {
  single<ApplicationScope> { ApplicationScope() }
  single<CoroutineContext>(ioDispatcherQualifier) { Dispatchers.IO }
}
