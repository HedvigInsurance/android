package com.hedvig.android.feature.profile.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.logger.logcat
import octopus.MemberUpdateEmailMutation
import octopus.MemberUpdatePhoneNumberMutation
import octopus.ProfileQuery
import octopus.type.MemberUpdateEmailInput
import octopus.type.MemberUpdatePhoneNumberInput

internal class ProfileRepositoryImpl(
  private val apolloClient: ApolloClient,
  private val networkCacheManager: NetworkCacheManager,
) : ProfileRepository {
  override suspend fun profile(): Either<Unit, ProfileData> = either {
    val member = apolloClient
      .query(ProfileQuery())
      .safeExecute { Unit }
      .bind()
      .toMember()

    ProfileData(member = member)
  }

  override suspend fun updateEmail(input: String): Either<Unit, ProfileData.Member> = either {
    val result = apolloClient.mutation(MemberUpdateEmailMutation(MemberUpdateEmailInput(input)))
      .safeExecute { Unit }
      .bind()

    networkCacheManager.clearCache()

    val error = result.memberUpdateEmail.userError
    val member = result.memberUpdateEmail.member

    if (error != null) {
      logcat { "UpdateEmail error: ${error.message}" }
      raise(Unit)
    }
    ensureNotNull(member) {
      logcat { "UpdateEmail no member data" }
      Unit
    }
    member.toMember()
  }

  override suspend fun updatePhoneNumber(input: String): Either<Unit, ProfileData.Member> = either {
    val result = apolloClient.mutation(MemberUpdatePhoneNumberMutation(MemberUpdatePhoneNumberInput(input)))
      .safeExecute { Unit }
      .bind()

    networkCacheManager.clearCache()

    val error = result.memberUpdatePhoneNumber.userError
    val member = result.memberUpdatePhoneNumber.member

    if (error != null) {
      logcat { "UpdatePhoneNumber error: ${error.message}" }
      raise(Unit)
    }
    ensureNotNull(member) {
      logcat { "UpdatePhoneNumber no member data" }
      Unit
    }
    member.toMember()
  }
}

private fun MemberUpdateEmailMutation.Data.MemberUpdateEmail.Member.toMember() = ProfileData.Member(
  id = id,
  firstName = firstName,
  lastName = lastName,
  email = email,
  phoneNumber = phoneNumber,
)

private fun MemberUpdatePhoneNumberMutation.Data.MemberUpdatePhoneNumber.Member.toMember() = ProfileData.Member(
  id = id,
  firstName = firstName,
  lastName = lastName,
  email = email,
  phoneNumber = phoneNumber,
)

private fun ProfileQuery.Data.toMember() = ProfileData.Member(
  id = currentMember.id,
  firstName = currentMember.firstName,
  lastName = currentMember.lastName,
  email = currentMember.email,
  phoneNumber = currentMember.phoneNumber,
)
