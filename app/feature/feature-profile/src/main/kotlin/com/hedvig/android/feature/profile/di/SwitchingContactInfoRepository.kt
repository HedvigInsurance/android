package com.hedvig.android.feature.profile.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.feature.profile.data.ContactInfoRepository
import com.hedvig.android.feature.profile.data.ContactInfoRepositoryDemo
import com.hedvig.android.feature.profile.data.ContactInfoRepositoryImpl
import com.hedvig.android.feature.profile.data.ContactInformation.Email
import com.hedvig.android.feature.profile.data.ContactInformation.PhoneNumber
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class SwitchingContactInfoRepository(
  private val demoManager: DemoManager,
  private val prodImpl: ContactInfoRepositoryImpl,
  private val demoImpl: ContactInfoRepositoryDemo,
) : ContactInfoRepository {
  override suspend fun contactInfo() = pick().contactInfo()

  override suspend fun updateInfo(phoneNumber: PhoneNumber, email: Email) = pick().updateInfo(phoneNumber, email)

  private suspend fun pick(): ContactInfoRepository = if (demoManager.isDemoMode().first()) demoImpl else prodImpl
}
