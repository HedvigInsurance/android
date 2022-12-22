package com.hedvig.app.testdata.common

import com.hedvig.android.apollo.graphql.type.ActiveInFutureAndTerminatedInFutureStatus
import com.hedvig.android.apollo.graphql.type.ActiveInFutureStatus
import com.hedvig.android.apollo.graphql.type.ActiveStatus
import com.hedvig.android.apollo.graphql.type.PendingStatus
import com.hedvig.android.apollo.graphql.type.TerminatedInFutureStatus
import com.hedvig.android.apollo.graphql.type.TerminatedStatus
import com.hedvig.android.apollo.graphql.type.TerminatedTodayStatus

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
