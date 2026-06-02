package com.hedvig.android.feature.claim.details.navigation

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.navigation.common.HedvigNavKey
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@ContributesTo(AppScope::class)
interface ClaimDetailsSerializersModuleProvider {
  @Provides
  @IntoSet
  fun provideClaimDetailsSerializersModule(): SerializersModule = SerializersModule {
    polymorphic(HedvigNavKey::class) {
      subclass(ClaimDetailsKey::class)
      subclass(AddFilesKey::class)
    }
  }
}
