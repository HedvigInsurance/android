package com.hedvig.android.feature.connect.payment.trustly.ui

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.navigation.common.HedvigNavKey
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@ContributesTo(AppScope::class)
interface ConnectPaymentTrustlySerializersModuleProvider {
  @Provides
  @IntoSet
  fun provideConnectPaymentTrustlySerializersModule(): SerializersModule = SerializersModule {
    polymorphic(HedvigNavKey::class) {
      subclass(TrustlyKey::class)
    }
  }
}
