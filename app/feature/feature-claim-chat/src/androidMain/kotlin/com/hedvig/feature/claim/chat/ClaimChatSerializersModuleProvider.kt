package com.hedvig.feature.claim.chat

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.navigation.common.HedvigNavKey
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@ContributesTo(AppScope::class)
interface ClaimChatSerializersModuleProvider {
  @Provides
  @IntoSet
  fun provideClaimChatSerializersModule(): SerializersModule = SerializersModule {
    polymorphic(HedvigNavKey::class) {
      subclass(ClaimChatKey::class)
      subclass(ClaimOutcomeDeflectKey::class)
      subclass(ClaimOutcomeNewClaimKey::class)
      subclass(UpdateAppKey::class)
    }
  }
}
