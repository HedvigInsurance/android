package com.hedvig.app.feature.profile.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.app.util.apollo.toMonetaryAmount
import giraffe.ProfileQuery
import giraffe.type.DirectDebitStatus
import octopus.MemberUpdateEmailMutation
import octopus.MemberUpdatePhoneNumberMutation
import octopus.type.MemberUpdateEmailInput
import octopus.type.MemberUpdatePhoneNumberInput

internal interface ProfileRepository {
  suspend fun profile(): Either<OperationResult.Error, ProfileData>
  suspend fun updateEmail(input: String): Either<OperationResult.Error, Member>
  suspend fun updatePhoneNumber(input: String): Either<OperationResult.Error, Member>
}

internal class ProfileRepositoryImpl(
  private val giraffeApolloClient: ApolloClient,
  private val octopusApolloClient: ApolloClient,
) : ProfileRepository {
  private val oldProfileQuery = ProfileQuery()
  private val newProfileQuery = octopus.ProfileQuery()

  override suspend fun profile(): Either<OperationResult.Error, ProfileData> = either {
    val profileData = giraffeApolloClient
      .query(oldProfileQuery)
      .safeExecute()
      .toEither()
      .bind()

    val member = octopusApolloClient
      .query(newProfileQuery)
      .safeExecute()
      .toEither()
      .bind()
      .toMember()

    ProfileData(
      member = member,
      chargeEstimation = ChargeEstimation(
        subscription = profileData.chargeEstimation.subscription.fragments.monetaryAmountFragment.toMonetaryAmount(),
        discount = profileData.chargeEstimation.discount.fragments.monetaryAmountFragment.toMonetaryAmount(),
        charge = profileData.chargeEstimation.charge.fragments.monetaryAmountFragment.toMonetaryAmount(),
      ),
      directDebitStatus = when (profileData.bankAccount?.directDebitStatus) {
        DirectDebitStatus.ACTIVE -> com.hedvig.app.feature.profile.data.DirectDebitStatus.ACTIVE
        DirectDebitStatus.PENDING -> com.hedvig.app.feature.profile.data.DirectDebitStatus.PENDING
        DirectDebitStatus.NEEDS_SETUP -> com.hedvig.app.feature.profile.data.DirectDebitStatus.NEEDS_SETUP
        DirectDebitStatus.UNKNOWN__ -> com.hedvig.app.feature.profile.data.DirectDebitStatus.UNKNOWN
        null -> com.hedvig.app.feature.profile.data.DirectDebitStatus.NONE
      },
      activePaymentMethods = profileData.activePaymentMethodsV2?.fragments?.activePaymentMethodsFragment?.asStoredCardDetails?.let {
        PaymentMethod.CardPaymentMethod(
          brand = it.brand,
          lastFourDigits = it.lastFourDigits,
          expiryMonth = it.expiryMonth,
          expiryYear = it.expiryYear,
        )
      } ?: profileData.activePaymentMethodsV2?.fragments?.activePaymentMethodsFragment?.asStoredThirdPartyDetails?.let {
        PaymentMethod.ThirdPartyPaymentMethd(
          name = it.name,
          type = it.type,
        )
      },
    )
  }

  override suspend fun updateEmail(input: String): Either<OperationResult.Error, Member> = either {
    val result = octopusApolloClient.mutation(MemberUpdateEmailMutation(MemberUpdateEmailInput(input)))
      .safeExecute()
      .toEither()
      .bind()

    octopusApolloClient.apolloStore.clearAll()

    val error = result.memberUpdateEmail.userError
    val member = result.memberUpdateEmail.member

    if (error != null) {
      raise(OperationResult.Error.GeneralError(error.message))
    } else {
      member?.toMember() ?: raise(OperationResult.Error.NoDataError("No member data"))
    }
  }

  override suspend fun updatePhoneNumber(input: String): Either<OperationResult.Error, Member> = either {
    val result = octopusApolloClient.mutation(MemberUpdatePhoneNumberMutation(MemberUpdatePhoneNumberInput(input)))
      .safeExecute()
      .toEither()
      .bind()

    octopusApolloClient.apolloStore.clearAll()

    val error = result.memberUpdatePhoneNumber.userError
    val member = result.memberUpdatePhoneNumber.member

    if (error != null) {
      raise(OperationResult.Error.GeneralError(error.message))
    } else {
      member?.toMember() ?: raise(OperationResult.Error.NoDataError("No member data"))
    }
  }
}

private fun MemberUpdateEmailMutation.Data.MemberUpdateEmail.Member.toMember() = Member(
  id = id,
  firstName = firstName,
  lastName = lastName,
  email = email,
  phoneNumber = phoneNumber,
)

private fun MemberUpdatePhoneNumberMutation.Data.MemberUpdatePhoneNumber.Member.toMember() = Member(
  id = id,
  firstName = firstName,
  lastName = lastName,
  email = email,
  phoneNumber = phoneNumber,
)

private fun octopus.ProfileQuery.Data.toMember() = Member(
  id = currentMember.id,
  firstName = currentMember.firstName,
  lastName = currentMember.lastName,
  email = currentMember.email,
  phoneNumber = currentMember.phoneNumber,
)


