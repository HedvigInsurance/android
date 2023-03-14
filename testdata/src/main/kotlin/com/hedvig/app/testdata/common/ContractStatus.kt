package com.hedvig.app.testdata.common

import giraffe.type.ActiveInFutureAndTerminatedInFutureStatus
import giraffe.type.ActiveInFutureStatus
import giraffe.type.ActiveStatus
import giraffe.type.PendingStatus
import giraffe.type.TerminatedInFutureStatus
import giraffe.type.TerminatedStatus
import giraffe.type.TerminatedTodayStatus

enum class ContractStatus {
  PENDING,
  ACTIVE_IN_FUTURE,
  ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE,
  ACTIVE_IN_FUTURE_INVALID,
  ACTIVE,
  TERMINATED_TODAY,
  TERMINATED,
  TERMINATED_IN_FUTURE,
  ;

  val typename: String
    get() = when (this) {
      PENDING -> PendingStatus.type.name
      ACTIVE_IN_FUTURE, ACTIVE_IN_FUTURE_INVALID -> ActiveInFutureStatus.type.name
      ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE -> ActiveInFutureAndTerminatedInFutureStatus.type.name
      ACTIVE -> ActiveStatus.type.name
      TERMINATED_TODAY -> TerminatedTodayStatus.type.name
      TERMINATED -> TerminatedStatus.type.name
      TERMINATED_IN_FUTURE -> TerminatedInFutureStatus.type.name
    }
}
