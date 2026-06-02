package com.hedvig.android.feature.profile.navigation

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.navigation.common.HedvigNavKey
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@ContributesTo(AppScope::class)
interface ProfileSerializersModuleProvider {
  @Provides
  @IntoSet
  fun provideProfileSerializersModule(): SerializersModule = SerializersModule {
    polymorphic(HedvigNavKey::class) {
      subclass(ProfileKey::class)
      subclass(ContactInfoKey::class)
      subclass(EurobonusKey::class)
      subclass(CertificatesKey::class)
      subclass(InformationKey::class)
      subclass(LicensesKey::class)
      subclass(SettingsGraphKey::class)
      subclass(SettingsKey::class)
    }
  }
}
