package com.hedvig.feature.remove.addons

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.navigation.common.HedvigNavKey
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@ContributesTo(AppScope::class)
interface RemoveAddonsSerializersModuleProvider {
  @Provides
  @IntoSet
  fun provideRemoveAddonsSerializersModule(): SerializersModule = SerializersModule {
    polymorphic(HedvigNavKey::class) {
      subclass(RemoveAddonsKey::class)
      subclass(ChooseAddonToRemoveKey::class)
      subclass(RemoveAddonSummaryKey::class)
      subclass(RemoveAddonSubmitSuccessKey::class)
      subclass(RemoveAddonSubmitFailureKey::class)
    }
  }
}
