package com.hedvig.android.feature.profile.di

import com.hedvig.android.feature.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.feature.profile.data.ProfileRepository

internal class ProfileRepositoryProvider(
  demoManager: DemoManager,
  demoImpl: ProfileRepository,
  prodImpl: ProfileRepository,
) : ProdOrDemoProvider<ProfileRepository>(
  demoManager,
  demoImpl,
  prodImpl,
)
