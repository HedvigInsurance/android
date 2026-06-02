package com.hedvig.android.feature.addon.purchase.navigation

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.navigation.common.HedvigNavKey
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@ContributesTo(AppScope::class)
interface AddonPurchaseSerializersModuleProvider {
  @Provides
  @IntoSet
  fun provideAddonPurchaseSerializersModule(): SerializersModule = SerializersModule {
    polymorphic(HedvigNavKey::class) {
      subclass(AddonPurchaseKey::class)
      subclass(TravelAddonTriageKey::class)
      subclass(CustomizeAddonKey::class)
      subclass(TravelInsurancePlusExplanationKey::class)
      subclass(SummaryKey::class)
      subclass(SubmitSuccessKey::class)
      subclass(SubmitFailureKey::class)
    }
  }
}
