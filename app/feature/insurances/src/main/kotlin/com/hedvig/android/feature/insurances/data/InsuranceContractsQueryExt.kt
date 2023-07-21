package com.hedvig.android.feature.insurances.data

import giraffe.InsuranceContractsQuery

internal fun InsuranceContractsQuery.Contract.isTerminated(): Boolean {
  return this.status.fragments.contractStatusFragment.asTerminatedStatus != null
}
