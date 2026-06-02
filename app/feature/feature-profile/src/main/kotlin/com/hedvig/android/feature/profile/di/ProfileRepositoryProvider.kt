package com.hedvig.android.feature.profile.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.profile.data.ContactInfoRepository
import com.hedvig.android.feature.profile.data.ContactInfoRepositoryDemo
import com.hedvig.android.feature.profile.data.ContactInfoRepositoryImpl
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding<Provider<ContactInfoRepository>>())
internal class ProfileRepositoryProvider(
  override val demoManager: DemoManager,
  override val prodImpl: ContactInfoRepositoryImpl,
  override val demoImpl: ContactInfoRepositoryDemo,
) : ProdOrDemoProvider<ContactInfoRepository>
