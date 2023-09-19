package com.hedvig.app.testdata.common.builders

import com.hedvig.app.testdata.common.ContractStatus
import giraffe.fragment.ContractStatusFragment
import giraffe.type.SwedishApartmentAgreement
import java.time.LocalDate

data class ContractStatusFragmentBuilder(
  private val status: ContractStatus,
) {
  fun build() = ContractStatusFragment(
    __typename = status.typename,
    asPendingStatus = if (status == ContractStatus.PENDING) {
      ContractStatusFragment.AsPendingStatus(
        __typename = status.typename,
        pendingSince = null,
      )
    } else {
      null
    },
    asActiveInFutureStatus = when (status) {
      ContractStatus.ACTIVE_IN_FUTURE -> ContractStatusFragment.AsActiveInFutureStatus(
        __typename = status.typename,
        futureInception = LocalDate.of(2025, 1, 1),
      )
      ContractStatus.ACTIVE_IN_FUTURE_INVALID -> ContractStatusFragment.AsActiveInFutureStatus(
        __typename = status.typename,
        futureInception = null,
      )
      else -> null
    },
    asActiveStatus = if (status == ContractStatus.ACTIVE) {
      ContractStatusFragment.AsActiveStatus(
        __typename = status.typename,
        pastInception = LocalDate.now(),
        upcomingAgreementChange = ContractStatusFragment.UpcomingAgreementChange(
          newAgreement = ContractStatusFragment.NewAgreement(
            __typename = SwedishApartmentAgreement.type.name,
            asSwedishApartmentAgreement = ContractStatusFragment.AsSwedishApartmentAgreement(
              __typename = SwedishApartmentAgreement.type.name,
              activeFrom = LocalDate.of(2021, 4, 6),
            ),
          ),
        ),
      )
    } else {
      null
    },
    asActiveInFutureAndTerminatedInFutureStatus = if (
      status == ContractStatus.ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE
    ) {
      ContractStatusFragment.AsActiveInFutureAndTerminatedInFutureStatus(
        __typename = status.typename,
        futureInception = LocalDate.of(2024, 1, 1),
        futureTermination = LocalDate.of(2034, 1, 1),
      )
    } else {
      null
    },
    asTerminatedInFutureStatus = null,
    asTerminatedTodayStatus = if (status == ContractStatus.TERMINATED_TODAY) {
      ContractStatusFragment.AsTerminatedTodayStatus(
        __typename = status.typename,
        today = LocalDate.now(),
      )
    } else {
      null
    },
    asTerminatedStatus = if (status == ContractStatus.TERMINATED) {
      ContractStatusFragment.AsTerminatedStatus(
        __typename = status.typename,
        termination = null,
      )
    } else {
      null
    },
    asDeletedStatus = null,
  )
}
