package com.hedvig.android.language.di

import com.hedvig.android.language.AndroidLanguageLaunchCheckUseCase
import com.hedvig.android.language.LanguageLaunchCheckUseCase
import com.hedvig.android.language.LanguageService
import org.koin.dsl.module

val languageMigrationModule = module {
  single<LanguageLaunchCheckUseCase> {
    AndroidLanguageLaunchCheckUseCase(get<LanguageService>())
  }
}
