package com.hedvig.android.feature.payoutaccount.navigation

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.navigation.common.HedvigNavKey
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@ContributesTo(AppScope::class)
interface PayoutAccountSerializersModuleProvider {
  @Provides
  @IntoSet
  fun providePayoutAccountSerializersModule(): SerializersModule = SerializersModule {
    polymorphic(HedvigNavKey::class) {
      subclass(PayoutAccountKey::class)
      subclass(SelectPayoutMethodKey::class)
      subclass(EditBankAccountKey::class)
      subclass(SetupSwishPayoutKey::class)
      subclass(SetupInvoicePayoutKey::class)
    }
  }
}
