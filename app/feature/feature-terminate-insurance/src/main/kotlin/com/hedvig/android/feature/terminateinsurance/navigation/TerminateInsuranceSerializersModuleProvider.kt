package com.hedvig.android.feature.terminateinsurance.navigation

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.navigation.common.HedvigNavKey
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@ContributesTo(AppScope::class)
interface TerminateInsuranceSerializersModuleProvider {
  @Provides
  @IntoSet
  fun provideTerminateInsuranceSerializersModule(): SerializersModule = SerializersModule {
    polymorphic(HedvigNavKey::class) {
      subclass(TerminateInsuranceKey::class)
      subclass(TerminationSurveyFirstStepKey::class)
      subclass(TerminationSurveySecondStepKey::class)
      subclass(TerminationDateKey::class)
      subclass(TerminationConfirmationKey::class)
      subclass(InsuranceDeletionKey::class)
      subclass(TerminationSuccessKey::class)
      subclass(TerminationFailureKey::class)
      subclass(UnknownScreenKey::class)
      subclass(DeflectSuggestionKey::class)
    }
  }
}
