package com.hedvig.android.language.di

import com.hedvig.android.language.AndroidLanguageService
import com.hedvig.android.language.LanguageService
import org.koin.dsl.module

val languageModule = module {
  single<LanguageService> { AndroidLanguageService() }
}
