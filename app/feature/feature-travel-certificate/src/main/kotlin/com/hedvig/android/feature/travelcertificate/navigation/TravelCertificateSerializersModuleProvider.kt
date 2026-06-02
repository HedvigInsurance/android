package com.hedvig.android.feature.travelcertificate.navigation

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.navigation.common.HedvigNavKey
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@ContributesTo(AppScope::class)
interface TravelCertificateSerializersModuleProvider {
  @Provides
  @IntoSet
  fun provideTravelCertificateSerializersModule(): SerializersModule = SerializersModule {
    polymorphic(HedvigNavKey::class) {
      subclass(TravelCertificateKey::class)
      subclass(TravelCertificateChooseContractKey::class)
      subclass(TravelCertificateDateInputKey::class)
      subclass(TravelCertificateTravellersInputKey::class)
      subclass(ShowCertificateKey::class)
    }
  }
}
