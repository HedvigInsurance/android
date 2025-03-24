package com.hedvig.android.feature.profile.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.profile.data.ContactInfoRepository.UpdateFailure
import com.hedvig.android.feature.profile.data.ContactInformation.Email
import com.hedvig.android.feature.profile.data.ContactInformation.PhoneNumber

internal class ContactInfoRepositoryDemo : ContactInfoRepository {
  private var contactInformation = ContactInformation(
    PhoneNumber("072102103"),
    Email("google@gmail.com"),
  )

  override suspend fun contactInfo(): Either<ErrorMessage, ContactInformation> {
    return contactInformation.right()
  }

  override suspend fun updateInfo(
    phoneNumber: PhoneNumber?,
    email: Email?,
    originalNumber: PhoneNumber?,
    originalEmail: Email?,
  ): Either<UpdateFailure, ContactInformation> {
    contactInformation = contactInformation.copy(
      email = email,
      phoneNumber = phoneNumber,
    )
    return contactInformation.right()
  }
}
