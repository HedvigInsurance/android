package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.ProductVariant
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

internal class GetInsuranceContractsUseCaseDemo : GetInsuranceContractsUseCase {
  override fun invoke(forceNetworkFetch: Boolean): Flow<Either<ErrorMessage, List<InsuranceContract>>> {
    return flowOf(
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
              contractGroup = ContractGroup.RENTAL,
              contractType = ContractType.SE_APARTMENT_RENT,
              partner = null,
              perils = persistentListOf(),
              insurableLimits = persistentListOf(),
              documents = persistentListOf(),
            ),
            certificateUrl = null,
            creationCause = InsuranceAgreement.CreationCause.NEW_CONTRACT,
            coInsured = persistentListOf(
              InsuranceAgreement.CoInsured(
                ssn = "123",
                birthDate = LocalDate.fromEpochDays(300),
                firstName = "Test",
                lastName = "Testersson",
                activatesOn = null,
                terminatesOn = null,
                hasMissingInfo = false,
              ),
              InsuranceAgreement.CoInsured(
                ssn = "123",
                birthDate = LocalDate.fromEpochDays(600),
                firstName = "Test 1",
                lastName = "Testersson 2",
                activatesOn = null,
                terminatesOn = null,
                hasMissingInfo = false,
              ),
              InsuranceAgreement.CoInsured(
                ssn = null,
                birthDate = null,
                firstName = null,
                lastName = null,
                activatesOn = null,
                terminatesOn = null,
                hasMissingInfo = true,
              ),
            ),
          ),
          upcomingInsuranceAgreement = null,
          renewalDate = LocalDate.fromEpochDays(500),
          supportsAddressChange = false,
          supportsEditCoInsured = true,
          isTerminated = false,
          contractHolderDisplayName = "Test Member",
          contractHolderSSN = "1111111111-33322",
        ),
      ).right(),
    )
  }
}
