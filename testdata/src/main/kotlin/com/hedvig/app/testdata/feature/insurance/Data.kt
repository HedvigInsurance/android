package com.hedvig.app.testdata.feature.insurance

import com.hedvig.app.testdata.feature.insurance.builders.InsuranceContractBuilder
import giraffe.InsuranceQuery
import giraffe.type.AgreementStatus

val INSURANCE_DATA_SWEDISH_APARTMENT =
  InsuranceQuery.Data(contracts = listOf(InsuranceContractBuilder().build()))
val INSURANCE_DATA_SWEDISH_APARTMENT_NO_RENEWAL =
  InsuranceQuery.Data(
    contracts = listOf(InsuranceContractBuilder(renewal = null).build()),
  )
val INSURANCE_DATA_SWEDISH_HOUSE =
  InsuranceQuery.Data(
    contracts = listOf(InsuranceContractBuilder().build()),
  )
val INSURANCE_DATA_NORWEGIAN_TRAVEL =
  InsuranceQuery.Data(
    contracts = listOf(InsuranceContractBuilder().build()),
  )
val INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS =
  InsuranceQuery.Data(
    contracts = listOf(
      InsuranceContractBuilder().build(),
    ),
  )
val INSURANCE_DATA_DANISH_TRAVEL =
  InsuranceQuery.Data(
    contracts = listOf(InsuranceContractBuilder().build()),
  )
val INSURANCE_DATA_MULTIPLE_DANISH_CONTRACTS = InsuranceQuery.Data(
  contracts = listOf(
    InsuranceContractBuilder().build(),
    InsuranceContractBuilder().build(),
    InsuranceContractBuilder().build(),
  ),
)
val INSURANCE_DATA_PENDING_CONTRACT = InsuranceQuery.Data(
  contracts = listOf(
    InsuranceContractBuilder(
      agreementStatus = AgreementStatus.PENDING,
    ).build(),
    InsuranceContractBuilder(
      agreementStatus = AgreementStatus.ACTIVE,
    ).build(),
  ),
)
