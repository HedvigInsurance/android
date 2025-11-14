package com.hedvig.android.shareddi

import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module = module {
  single<ExtraApolloClientConfiguration> {
    NoopExtraApolloClientConfiguration()
  }
}
