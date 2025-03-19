package com.hedvig.android.feature.profile.data

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.fx.coroutines.parZip
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.profile.data.ContactInfoRepository.UpdateFailure
import com.hedvig.android.feature.profile.data.ContactInfoRepository.UpdateFailure.NoChanges
import com.hedvig.android.feature.profile.data.ContactInformation.Email
import com.hedvig.android.feature.profile.data.ContactInformation.PhoneNumber
import octopus.ContactInformationQuery
import octopus.MemberUpdateEmailMutation
import octopus.MemberUpdatePhoneNumberMutation

internal class ContactInfoRepositoryImpl(
  private val apolloClient: ApolloClient,
  private val networkCacheManager: NetworkCacheManager,
) : ContactInfoRepository {
  override suspend fun contactInfo(): Either<ErrorMessage, ContactInformation> {
    return either {
      val member = apolloClient
        .query(ContactInformationQuery())
        .safeExecute(::ErrorMessage)
        .bind()
        .currentMember
      val phoneNumber = PhoneNumber.fromStringAfterTrimmingWhitespaces(member.phoneNumber).bind()
      val email = Email.fromString(member.email).bind()
      ContactInformation(phoneNumber, email)
    }
  }

  override suspend fun updateInfo(
    phoneNumber: PhoneNumber?,
    email: Email?,
    originalNumber: PhoneNumber,
    originalEmail: Email,
  ): Either<UpdateFailure, ContactInformation> {
    return parZip(
      {
        if (email != null && email != originalEmail) {
          updateEmail(email)
        } else {
          NoChanges.left()
        }
      },
      {
        if (phoneNumber != null && phoneNumber != originalNumber) {
          updatePhoneNumber(phoneNumber)
        } else {
          NoChanges.left()
        }
      },
    ) { emailResult, phoneNumberResult ->
      either {
        val emailResultContactInformation = emailResult.getOrNull() ?: phoneNumberResult.getOrNull()
        val phoneNumberResultContactInformation = phoneNumberResult.getOrNull() ?: emailResult.getOrNull()
        if (emailResultContactInformation != null && phoneNumberResultContactInformation != null) {
          return@either ContactInformation(
            phoneNumber = phoneNumberResultContactInformation.phoneNumber,
            email = emailResultContactInformation.email,
          )
        }
        raise(UpdateFailure.merge(emailResult.leftOrNull(), phoneNumberResult.leftOrNull()))
      }
    }
  }

  private suspend fun updateEmail(email: Email): Either<UpdateFailure, ContactInformation> {
    return either {
      val response = apolloClient
        .mutation(MemberUpdateEmailMutation(email.value))
        .safeExecute(::ErrorMessage)
        .bind()
        .memberUpdateEmail
      ensure(response.userError == null) {
        ErrorMessage("UpdateEmail error: ${response.userError!!.message}")
      }
      val member = response.member
      ensureNotNull(member) {
        ErrorMessage("UpdateEmail no member data")
      }

      networkCacheManager.clearCache()

      ContactInformation(
        PhoneNumber.fromStringAfterTrimmingWhitespaces(member.phoneNumber).bind(),
        Email.fromString(member.email).bind(),
      )
    }.mapLeft(UpdateFailure::Error)
  }

  private suspend fun updatePhoneNumber(phoneNumber: PhoneNumber): Either<UpdateFailure, ContactInformation> {
    return either {
      val response = apolloClient
        .mutation(MemberUpdatePhoneNumberMutation(phoneNumber.value))
        .safeExecute(::ErrorMessage)
        .bind()
        .memberUpdatePhoneNumber

      ensure(response.userError == null) {
        ErrorMessage("UpdatePhoneNumber error: ${response.userError!!.message}")
      }
      val member = response.member
      ensureNotNull(member) {
        ErrorMessage("UpdatePhoneNumber no member data")
      }

      networkCacheManager.clearCache()

      ContactInformation(
        PhoneNumber.fromStringAfterTrimmingWhitespaces(member.phoneNumber).bind(),
        Email.fromString(member.email).bind(),
      )
    }.mapLeft(UpdateFailure::Error)
  }
}
