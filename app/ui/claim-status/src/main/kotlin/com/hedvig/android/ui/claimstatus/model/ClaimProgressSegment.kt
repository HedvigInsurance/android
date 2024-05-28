package com.hedvig.android.ui.claimstatus.model

import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentType.CLOSED
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentType.CURRENTLY_ACTIVE
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentType.FUTURE_INACTIVE
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentType.PAID
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentType.PAST_INACTIVE
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentType.REOPENED
import octopus.fragment.ClaimFragment
import octopus.type.ClaimOutcome
import octopus.type.ClaimStatus

data class ClaimProgressSegment(
  val text: SegmentText,
  val type: SegmentType,
) {
  enum class SegmentText {
    Submitted,
    BeingHandled,
    Closed,
  }

  enum class SegmentType {
    CURRENTLY_ACTIVE,
    PAST_INACTIVE,
    FUTURE_INACTIVE,
    PAID,
    REOPENED,
    CLOSED,
    UNKNOWN,
  }

  companion object {
    fun fromClaimFragment(claim: ClaimFragment): List<ClaimProgressSegment> = when (claim.status) {
      ClaimStatus.CREATED -> buildSegments(CURRENTLY_ACTIVE, FUTURE_INACTIVE, FUTURE_INACTIVE)
      ClaimStatus.IN_PROGRESS -> buildSegments(PAST_INACTIVE, CURRENTLY_ACTIVE, FUTURE_INACTIVE)
      ClaimStatus.CLOSED -> {
        when (claim.outcome) {
          ClaimOutcome.PAID -> buildSegments(PAID, PAID, PAID)
          ClaimOutcome.NOT_COMPENSATED,
          ClaimOutcome.NOT_COVERED,
          ClaimOutcome.UNKNOWN__,
          null,
          -> buildSegments(CLOSED, CLOSED, CLOSED)
        }
      }
      ClaimStatus.REOPENED -> buildSegments(PAST_INACTIVE, REOPENED, FUTURE_INACTIVE)
      ClaimStatus.UNKNOWN__,
      null,
      -> buildSegments(CURRENTLY_ACTIVE, FUTURE_INACTIVE, FUTURE_INACTIVE)
    }

    private fun buildSegments(type1: SegmentType, type2: SegmentType, type3: SegmentType): List<ClaimProgressSegment> {
      return listOf(
        ClaimProgressSegment(SegmentText.Submitted, type1),
        ClaimProgressSegment(SegmentText.BeingHandled, type2),
        ClaimProgressSegment(SegmentText.Closed, type3),
      )
    }
  }
}
