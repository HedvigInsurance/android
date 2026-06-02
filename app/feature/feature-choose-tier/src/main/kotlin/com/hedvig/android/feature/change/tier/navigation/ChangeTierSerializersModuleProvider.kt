package com.hedvig.android.feature.change.tier.navigation

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.navigation.common.HedvigNavKey
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@ContributesTo(AppScope::class)
interface ChangeTierSerializersModuleProvider {
  @Provides
  @IntoSet
  fun provideChangeTierSerializersModule(): SerializersModule = SerializersModule {
    polymorphic(HedvigNavKey::class) {
      subclass(StartTierFlowKey::class)
      subclass(StartTierFlowChooseInsuranceKey::class)
      subclass(ChooseTierKey::class)
      subclass(ComparisonKey::class)
      subclass(SummaryKey::class)
      subclass(SubmitSuccessKey::class)
      subclass(SubmitFailureKey::class)
    }
  }
}
