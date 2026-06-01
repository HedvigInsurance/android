package com.hedvig.android.feature.profile.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.feature.profile.data.ContactInformation.Email
import com.hedvig.android.feature.profile.data.ContactInformation.PhoneNumber
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class ContactInfoRepositoryDemo : ContactInfoRepository {
  private var contactInformation = ContactInformation(
    PhoneNumber("072102103"),
    Email("google@gmail.com"),
  )

  override suspend fun contactInfo(): Either<ErrorMessage, ContactInformation> {
    return contactInformation.right()
  }

  override suspend fun updateInfo(phoneNumber: PhoneNumber, email: Email): Either<ErrorMessage, ContactInformation> {
    contactInformation = contactInformation.copy(
      email = email,
      phoneNumber = phoneNumber,
    )
    return contactInformation.right()
  }
}
