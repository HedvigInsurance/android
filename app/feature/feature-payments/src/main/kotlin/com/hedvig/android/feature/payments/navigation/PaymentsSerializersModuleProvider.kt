package com.hedvig.android.feature.payments.navigation

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.navigation.common.HedvigNavKey
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@ContributesTo(AppScope::class)
interface PaymentsSerializersModuleProvider {
  @Provides
  @IntoSet
  fun providePaymentsSerializersModule(): SerializersModule = SerializersModule {
    polymorphic(HedvigNavKey::class) {
      subclass(PaymentsKey::class)
      subclass(PaymentDetailsKey::class)
      subclass(PaymentHistoryKey::class)
      subclass(DiscountsKey::class)
      subclass(ForeverKey::class)
      subclass(MemberPaymentDetailsKey::class)
      subclass(ManualChargeKey::class)
      subclass(ManualChargeSuccessKey::class)
    }
  }
}
