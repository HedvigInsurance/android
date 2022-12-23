package com.hedvig.android.core.common.di

import com.hedvig.android.core.common.ApplicationScope
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val coreCommonModule = module {
  single<ApplicationScope> { ApplicationScope() }
}
