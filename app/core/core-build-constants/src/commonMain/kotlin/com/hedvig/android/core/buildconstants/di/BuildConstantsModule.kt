package com.hedvig.android.core.buildconstants.di

import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.buildconstants.CommonHedvigBuildConstants
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.language.LanguageService
import org.koin.dsl.module

val buildConstantsModule = module {
  single<HedvigBuildConstants> {
    CommonHedvigBuildConstants(get<AppBuildConfig>(), get<LanguageService>())
  }
}
