package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.fx.coroutines.parZip
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.ui.insurance.toProductVariant
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import octopus.InsuranceContractsQuery
import octopus.fragment.ContractFragment

internal interface GetInsuranceContractsUseCase {
  suspend fun invoke(forceNetworkFetch: Boolean): Either<ErrorMessage, List<InsuranceContract>>
}

internal class GetInsuranceContractsUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetInsuranceContractsUseCase {
  override suspend fun invoke(forceNetworkFetch: Boolean): Either<ErrorMessage, List<InsuranceContract>> {
    return either {
      parZip(
        {
          apolloClient
            .query(InsuranceContractsQuery())
            .fetchPolicy(if (forceNetworkFetch) FetchPolicy.NetworkOnly else FetchPolicy.CacheFirst)
            .safeExecute()
            .toEither(::ErrorMessage)
            .bind()
        },
        { featureManager.isFeatureEnabled(Feature.NEW_MOVING_FLOW) },
      ) { insuranceQueryData, supportsAddressChange ->
        val terminatedContracts = insuranceQueryData.currentMember.terminatedContracts.map {
          it.toContract(isTerminated = true, supportsAddressChange)
        }
        val activeContracts = insuranceQueryData.currentMember.activeContracts.map {
          it.toContract(isTerminated = false, supportsAddressChange)
        }
        terminatedContracts + activeContracts
      }
    }
  }
}

private fun ContractFragment.toContract(isTerminated: Boolean, supportsAddressChange: Boolean): InsuranceContract {
  return InsuranceContract(
    id = id,
    displayName = currentAgreement.productVariant.displayName,
    exposureDisplayName = exposureDisplayName,
    inceptionDate = masterInceptionDate,
    renewalDate = upcomingChangedAgreement?.activeFrom,
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
      certificateUrl = currentAgreement.certificateUrl,
    ),
    upcomingAgreement = upcomingChangedAgreement?.let {
      Agreement(
        activeFrom = it.activeFrom,
        activeTo = it.activeTo,
        displayItems = it.displayItems.map {
          Agreement.DisplayItem(
            it.displayTitle,
            it.displayValue,
          )
        },
        productVariant = it.productVariant.toProductVariant(),
        certificateUrl = it.certificateUrl,
      )
    },
    supportsAddressChange = supportsAddressChange,
    isTerminated = isTerminated,
  )
}
