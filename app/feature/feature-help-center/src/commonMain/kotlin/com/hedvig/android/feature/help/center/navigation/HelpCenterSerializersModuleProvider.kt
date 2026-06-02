package com.hedvig.android.feature.help.center.navigation

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.navigation.common.HedvigNavKey
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@ContributesTo(AppScope::class)
interface HelpCenterSerializersModuleProvider {
  @Provides
  @IntoSet
  fun provideHelpCenterSerializersModule(): SerializersModule = SerializersModule {
    polymorphic(HedvigNavKey::class) {
      subclass(HelpCenterKey::class)
      subclass(HelpCenterHomeKey::class)
      subclass(HelpCenterTopicKey::class)
      subclass(HelpCenterQuestionKey::class)
      subclass(EmergencyKey::class)
      subclass(FirstVetKey::class)
      subclass(PuppyGuideKey::class)
      subclass(PuppyGuideArticleKey::class)
    }
  }
}
