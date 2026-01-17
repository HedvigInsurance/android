package com.hedvig.android.language.di

import com.hedvig.android.language.JvmLanguageService
import com.hedvig.android.language.LanguageService
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformLanguageModule: Module = module {
  single<LanguageService> { JvmLanguageService() }
}
