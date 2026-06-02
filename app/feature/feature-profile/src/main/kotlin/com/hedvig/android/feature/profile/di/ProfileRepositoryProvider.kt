package com.hedvig.android.feature.profile.di

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.feature.profile.data.ContactInfoRepository

internal class ProfileRepositoryProvider(
  override val demoManager: DemoManager,
  override val demoImpl: ContactInfoRepository,
  override val prodImpl: ContactInfoRepository,
) : ProdOrDemoProvider<ContactInfoRepository>
