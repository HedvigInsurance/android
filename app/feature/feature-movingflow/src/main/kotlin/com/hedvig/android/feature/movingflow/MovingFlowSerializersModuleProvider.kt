package com.hedvig.android.feature.movingflow

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.navigation.common.HedvigNavKey
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@ContributesTo(AppScope::class)
interface MovingFlowSerializersModuleProvider {
  @Provides
  @IntoSet
  fun provideMovingFlowSerializersModule(): SerializersModule = SerializersModule {
    polymorphic(HedvigNavKey::class) {
      subclass(SelectContractForMovingKey::class)
      subclass(HousingTypeKey::class)
      subclass(EnterNewAddressKey::class)
      subclass(AddHouseInformationKey::class)
      subclass(ChoseCoverageLevelAndDeductibleKey::class)
      subclass(CompareCoverageKey::class)
      subclass(SummaryKey::class)
      subclass(SuccessfulMoveKey::class)
    }
  }
}
