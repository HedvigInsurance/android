package com.hedvig.android.language.di

import org.koin.core.module.Module
import org.koin.dsl.module

val languageModule = module {
  includes(platformLanguageModule)
}

internal expect val platformLanguageModule: Module
