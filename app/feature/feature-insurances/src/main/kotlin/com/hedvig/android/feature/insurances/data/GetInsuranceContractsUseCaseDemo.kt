package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.ProductVariant
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

internal class GetInsuranceContractsUseCaseDemo : GetInsuranceContractsUseCase {
  override suspend fun invoke(forceNetworkFetch: Boolean): Either<ErrorMessage, List<InsuranceContract>> {
    return either {
      listOf(
        InsuranceContract(
          "1",
          "Test123",
          exposureDisplayName = "Test exposure",
          inceptionDate = LocalDate.fromEpochDays(200),
          terminationDate = LocalDate.fromEpochDays(400),
          currentInsuranceAgreement = InsuranceAgreement(
            activeFrom = LocalDate.fromEpochDays(240),
            activeTo = LocalDate.fromEpochDays(340),
            displayItems = persistentListOf(),
            productVariant = ProductVariant(
              displayName = "Variant",
              contractType = ContractType.RENTAL,
              partner = null,
              perils = persistentListOf(),
              insurableLimits = persistentListOf(),
              documents = persistentListOf(),
            ),
            certificateUrl = null,
          ),
          upcomingInsuranceAgreement = null,
          renewalDate = LocalDate.fromEpochDays(500),
          supportsAddressChange = false,
          isTerminated = false,
        ),
      )
    }
  }
}
