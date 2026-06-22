package com.hedvig.android.feature.profile.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.DemoSwitcher
import com.hedvig.android.feature.profile.data.ContactInfoRepository
import com.hedvig.android.feature.profile.data.ContactInfoRepositoryDemo
import com.hedvig.android.feature.profile.data.ContactInfoRepositoryImpl
import com.hedvig.android.feature.profile.data.ContactInformation.Email
import com.hedvig.android.feature.profile.data.ContactInformation.PhoneNumber
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<ContactInfoRepository>())
internal class SwitchingContactInfoRepository(
  override val demoManager: DemoManager,
  override val prodImpl: ContactInfoRepositoryImpl,
  override val demoImpl: ContactInfoRepositoryDemo,
) : ContactInfoRepository, DemoSwitcher<ContactInfoRepository> {
  override suspend fun contactInfo() = pick().contactInfo()

  override suspend fun updateInfo(phoneNumber: PhoneNumber, email: Email) = pick().updateInfo(phoneNumber, email)
}
