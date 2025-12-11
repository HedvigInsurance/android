package com.hedvig.android.language.di

import com.hedvig.android.language.AndroidLanguageService
import com.hedvig.android.language.LanguageService
import org.koin.dsl.module

internal actual val platformLanguageModule = module {
  single<LanguageService> { AndroidLanguageService() }
}
