package com.hedvig.android.feature.onboarding.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import octopus.OnboardingQuery
import octopus.OnboardingUpdateContactInfoMutation

internal interface OnboardingRepository {
  suspend fun getOnboardingData(): Either<ErrorMessage, OnboardingData>

  suspend fun updateContactInfo(email: String, phoneNumber: String): Either<ErrorMessage, Unit>
}

@ContributesBinding(AppScope::class)
@Inject
internal class OnboardingRepositoryImpl(
  private val apolloClient: ApolloClient,
) : OnboardingRepository {
  override suspend fun getOnboardingData(): Either<ErrorMessage, OnboardingData> = either {
    val member = apolloClient
      .query(OnboardingQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute(::ErrorMessage)
      .bind()
      .currentMember
    OnboardingData(
      email = member.email,
      phoneNumber = member.phoneNumber,
      contracts = member.activeContracts.map { contract ->
        OnboardingContract(
          id = contract.id,
          displayName = contract.currentAgreement.productVariant.displayName,
          exposureName = contract.exposureDisplayNameShort,
          typeOfContract = contract.currentAgreement.productVariant.typeOfContract,
          missingCoInsuredCount = contract.coInsured.orEmpty().count { it.hasMissingInfo },
          isMissingPetId = contract.isMissingPetId,
        )
      },
      referralInformation = member.referralInformation.let { referralInformation ->
        OnboardingReferralInformation(
          code = referralInformation.code,
          monthlyDiscountPerReferralAmount = referralInformation.monthlyDiscountPerReferral.amount,
          currencyCode = referralInformation.monthlyDiscountPerReferral.currencyCode.rawValue,
        )
      },
      hasConnectedPayinMethod = member.paymentMethods.payinMethods.isNotEmpty(),
      crossSells = member.crossSellV2.otherCrossSells.map { crossSell ->
        OnboardingCrossSell(
          id = crossSell.id,
          title = crossSell.title,
          description = crossSell.description,
          storeUrl = crossSell.storeUrl,
        )
      },
    )
  }

  override suspend fun updateContactInfo(email: String, phoneNumber: String): Either<ErrorMessage, Unit> = either {
    val result = apolloClient
      .mutation(OnboardingUpdateContactInfoMutation(email = email, phoneNumber = phoneNumber))
      .safeExecute(::ErrorMessage)
      .bind()
    val userError = result.memberUpdateContactInfo.userError
    ensure(userError == null) { ErrorMessage(userError?.message) }
  }
}
