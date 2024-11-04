package com.hedvig.android.ui.claimstatus.model

import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentType.ACTIVE
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentType.INACTIVE
import octopus.fragment.ClaimFragment
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
    ACTIVE,
    INACTIVE,
    UNKNOWN,
  }

  companion object {
    fun fromClaimFragment(claim: ClaimFragment): List<ClaimProgressSegment> = when (claim.status) {
      ClaimStatus.CREATED -> buildSegments(ACTIVE, INACTIVE, INACTIVE)

      ClaimStatus.REOPENED,
      ClaimStatus.IN_PROGRESS,
      -> buildSegments(ACTIVE, ACTIVE, INACTIVE)

      ClaimStatus.CLOSED -> buildSegments(ACTIVE, ACTIVE, ACTIVE)
      ClaimStatus.UNKNOWN__,
      null,
      -> buildSegments(ACTIVE, INACTIVE, INACTIVE)
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
