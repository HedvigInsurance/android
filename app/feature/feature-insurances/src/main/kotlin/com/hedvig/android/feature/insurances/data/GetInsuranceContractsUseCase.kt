package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.ui.insurance.toProductVariant
import octopus.InsuranceContractsQuery
import octopus.fragment.ContractFragment

internal interface GetInsuranceContractsUseCase {
  suspend fun invoke(forceNetworkFetch: Boolean): Either<ErrorMessage, List<InsuranceContract>>
}

internal class GetInsuranceContractsUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetInsuranceContractsUseCase {
  override suspend fun invoke(forceNetworkFetch: Boolean): Either<ErrorMessage, List<InsuranceContract>> {
    return either {
      val insuranceQueryData = apolloClient
        .query(InsuranceContractsQuery())
        .fetchPolicy(if (forceNetworkFetch) FetchPolicy.NetworkOnly else FetchPolicy.CacheFirst)
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()

      insuranceQueryData.currentMember.terminatedContracts.map {
        it.toContract(isTerminated = true)
      } + insuranceQueryData.currentMember.activeContracts.map {
        it.toContract()
      }
    }
  }
}

private fun ContractFragment.toContract(isTerminated: Boolean = false): InsuranceContract {
  return InsuranceContract(
    id = id,
    displayName = currentAgreement.productVariant.displayName,
    exposureDisplayName = exposureDisplayName,
    inceptionDate = masterInceptionDate,
    renewalDate = upcomingRenewal.renewalDate,
    terminationDate = terminationDate,
    currentAgreement = Agreement(
      activeFrom = currentAgreement.activeFrom,
      activeTo = currentAgreement.activeTo,
      displayItems = currentAgreement.displayItems.map {
        Agreement.DisplayItem(
          it.displayTitle,
          it.displayValue,
        )
      },
      productVariant = currentAgreement.productVariant.toProductVariant(),
      certificateUrl = currentAgreement.certificateUrl
    ),
    upcomingAgreement = upcomingChangedAgreement?.let {
      Agreement(
        activeFrom = it.activeFrom,
        activeTo = it.activeTo,
        displayItems = currentAgreement.displayItems.map {
          Agreement.DisplayItem(
            it.displayTitle,
            it.displayValue,
          )
        },
        productVariant = it.productVariant.toProductVariant(),
        certificateUrl = it.certificateUrl,
      )
    },
    supportsAddressChange = true,
    isTerminated = isTerminated,
  )
}
