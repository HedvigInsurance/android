package com.hedvig.android.feature.profile.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.profile.data.ContactInfoRepository
import com.hedvig.android.feature.profile.data.ContactInfoRepositoryDemo
import com.hedvig.android.feature.profile.data.ContactInfoRepositoryImpl
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
internal interface ProfileMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideProfileRepositoryProvider(
    demoManager: DemoManager,
    prodImpl: ContactInfoRepositoryImpl,
    demoImpl: ContactInfoRepositoryDemo,
  ): Provider<ContactInfoRepository> = ProfileRepositoryProvider(
    demoManager = demoManager,
    demoImpl = demoImpl,
    prodImpl = prodImpl,
  )
}
