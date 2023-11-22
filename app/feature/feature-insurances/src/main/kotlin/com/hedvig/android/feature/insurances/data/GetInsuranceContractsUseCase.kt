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
import com.hedvig.android.data.productVariant.android.toProductVariant
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import octopus.InsuranceContractsQuery
import octopus.fragment.AgreementFragment
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
        val contractHolderDisplayName = insuranceQueryData.getContractHolderDisplayName()
        val contractHolderSSN = insuranceQueryData.currentMember.ssn

        val terminatedContracts = insuranceQueryData.currentMember.terminatedContracts.map {
          it.toContract(isTerminated = true, supportsAddressChange, contractHolderDisplayName, contractHolderSSN)
        }
        val activeContracts = insuranceQueryData.currentMember.activeContracts.map {
          it.toContract(isTerminated = false, supportsAddressChange, contractHolderDisplayName, contractHolderSSN)
        }
        terminatedContracts + activeContracts
      }
    }
  }
}

private fun InsuranceContractsQuery.Data.getContractHolderDisplayName(): String =
  "${currentMember.firstName} ${currentMember.lastName}"

private fun ContractFragment.toContract(
  isTerminated: Boolean,
  supportsAddressChange: Boolean,
  contractHolderDisplayName: String,
  contractHolderSSN: String?,
): InsuranceContract {
  return InsuranceContract(
    id = id,
    displayName = currentAgreement.productVariant.displayName,
    contractHolderDisplayName = contractHolderDisplayName,
    contractHolderSSN = contractHolderSSN,
    exposureDisplayName = exposureDisplayName,
    inceptionDate = masterInceptionDate,
    renewalDate = upcomingChangedAgreement?.activeFrom,
    terminationDate = terminationDate,
    currentInsuranceAgreement = InsuranceAgreement(
      activeFrom = currentAgreement.activeFrom,
      activeTo = currentAgreement.activeTo,
      displayItems = currentAgreement.displayItems.map {
        InsuranceAgreement.DisplayItem(
          it.displayTitle,
          it.displayValue,
        )
      },
      productVariant = currentAgreement.productVariant.toProductVariant(),
      certificateUrl = currentAgreement.certificateUrl,
      coInsured = buildList {
        val currentCoInsured = currentAgreement.coInsured
          ?.map { it.toCoInsured() }
          ?: emptyList()

        addAll(currentCoInsured)

        // Get the co insured only present in upcoming agreement and set active from.
        val upcomingCoInsured = upcomingChangedAgreement?.coInsured
          ?.map { it.toCoInsured() }
          ?.filter { it.activeFrom != null }
          ?: emptyList()

        val newCoInsured = upcomingCoInsured.subtract(currentCoInsured.toSet())
          .map { it.copy(activeFrom = upcomingChangedAgreement?.activeFrom) }

        addAll(newCoInsured)
      }.toPersistentList(),
    ),
    upcomingInsuranceAgreement = upcomingChangedAgreement?.let {
      InsuranceAgreement(
        activeFrom = it.activeFrom,
        activeTo = it.activeTo,
        displayItems = it.displayItems.map {
          InsuranceAgreement.DisplayItem(
            it.displayTitle,
            it.displayValue,
          )
        },
        productVariant = it.productVariant.toProductVariant(),
        certificateUrl = it.certificateUrl,
        coInsured = it.coInsured?.map { it.toCoInsured() }?.toPersistentList() ?: persistentListOf(),
      )
    },
    supportsAddressChange = supportsAddressChange,
    isTerminated = isTerminated,
  )
}

private fun AgreementFragment.CoInsured.toCoInsured(): InsuranceAgreement.CoInsured = InsuranceAgreement.CoInsured(
  firstName = firstName,
  lastName = lastName,
  ssn = ssn,
  birthDate = birthdate,
  activeFrom = null,
  hasMissingInfo = needsMissingInfo,
)
