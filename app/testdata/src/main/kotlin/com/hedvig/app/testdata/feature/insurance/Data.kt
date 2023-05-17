package com.hedvig.app.testdata.feature.insurance

import com.hedvig.app.testdata.feature.insurance.builders.InsuranceContractBuilder
import giraffe.InsuranceQuery

val INSURANCE_DATA_SWEDISH_HOUSE =
  InsuranceQuery.Data(
    contracts = listOf(InsuranceContractBuilder().build()),
  )
val INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS =
  InsuranceQuery.Data(
    contracts = listOf(
      InsuranceContractBuilder().build(),
    ),
  )
