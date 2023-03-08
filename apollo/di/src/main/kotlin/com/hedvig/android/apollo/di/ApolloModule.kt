package com.hedvig.android.apollo.di

import com.hedvig.android.apollo.giraffe.di.giraffeModule
import com.hedvig.android.apollo.octopus.di.octopusModule
import org.koin.dsl.module

val apolloClientModule = module {
  includes(
    giraffeModule,
    octopusModule,
  )
}
