package com.hedvig.android.feature.profile.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.profile.data.ContactInformation.Email
import com.hedvig.android.feature.profile.data.ContactInformation.PhoneNumber
import com.hedvig.android.logger.LogPriority.ERROR
import com.hedvig.android.logger.logcat
import octopus.ContactInformationQuery
import octopus.MemberUpdateContactInfoMutation

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

  override suspend fun updateInfo(phoneNumber: PhoneNumber, email: Email): Either<ErrorMessage, ContactInformation> {
    return either {
      val response = apolloClient
        .mutation(MemberUpdateContactInfoMutation(email.value, phoneNumber.value))
        .safeExecute()
        .mapLeft { ErrorMessage() }
        .onLeft { left ->
          logcat(ERROR) { "Tried to update contact Info but got error: $left" }
        }
        .bind()
        .memberUpdateContactInfo
      ensure(response.userError == null) {
        ErrorMessage(response.userError!!.message)
      }
      val member = response.member
      ensureNotNull(member) {
        ErrorMessage("UpdateInfo no member data")
      }
      networkCacheManager.clearCache()
      ContactInformation(
        PhoneNumber.fromStringAfterTrimmingWhitespaces(member.phoneNumber).bind(),
        Email.fromString(member.email).bind(),
      )
    }
  }
}
