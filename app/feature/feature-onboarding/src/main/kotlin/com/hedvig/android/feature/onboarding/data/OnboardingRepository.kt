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
          // Terminating entries are excluded (their missing info is moot), matching the home-screen
          // "needs co-insured info" reminder so both surfaces agree on what still counts as missing.
          missingCoInsuredCount = if (contract.supportsCoInsured) {
            contract.coInsured.orEmpty().count { it.hasMissingInfo && it.terminatesOn == null }
          } else {
            0
          },
          missingCoOwnersCount = if (contract.supportsCoOwners) {
            contract.coOwners.orEmpty().count { it.hasMissingInfo && it.terminatesOn == null }
          } else {
            0
          },
          isMissingPetId = contract.isMissingPetId,
          coInsuredCount = if (contract.supportsCoInsured) {
            contract.coInsured.orEmpty().count { it.terminatesOn == null }
          } else {
            0
          },
          coInsuredNames = if (contract.supportsCoInsured) {
            contract.coInsured.orEmpty().filter { it.terminatesOn == null }.mapNotNull { it.firstName }
          } else {
            emptyList()
          },
          coOwnerCount = if (contract.supportsCoOwners) {
            contract.coOwners.orEmpty().count { it.terminatesOn == null }
          } else {
            0
          },
          coOwnerNames = if (contract.supportsCoOwners) {
            contract.coOwners.orEmpty().filter { it.terminatesOn == null }.mapNotNull { it.firstName }
          } else {
            emptyList()
          },
        )
      },
      referralInformation = member.referralInformation.let { referralInformation ->
        OnboardingReferralInformation(
          code = referralInformation.code,
          monthlyDiscountPerReferralAmount = referralInformation.monthlyDiscountPerReferral.amount,
          currencyCode = referralInformation.monthlyDiscountPerReferral.currencyCode.rawValue,
        )
      },
      payinStatus = member.paymentMethods.payinMethods.map { it.status.rawValue }.let { statuses ->
        when {
          statuses.any { it == "ACTIVE" } -> OnboardingPayinStatus.Active

          // A PENDING method counts as "connected enough" to skip the step (bank activation takes
          // days), but the step UI still shows it as pending rather than claiming it is connected.
          statuses.any { it == "PENDING" } -> OnboardingPayinStatus.Pending

          else -> OnboardingPayinStatus.NeedsSetup
        }
      },
      crossSells = member.crossSellV2.otherCrossSells.map { crossSell ->
        OnboardingCrossSell(
          id = crossSell.id,
          title = crossSell.title,
          description = crossSell.description,
          storeUrl = crossSell.storeUrl,
          pillowImageUrl = crossSell.pillowImageSmall.src,
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
