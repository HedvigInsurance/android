package com.hedvig.android.ui.claimstatus.model

import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import octopus.fragment.ClaimFragment
import octopus.fragment.PartnerClaimFragment

data class ClaimStatusCardUiState(
  val id: String,
  val claimType: String?,
  val insuranceDisplayName: String?,
  // Null for partner claims with no submission date returned by the BE.
  val submittedDate: Instant?,
  val pillTypes: List<ClaimPillType>,
  val claimProgressItemsUiState: List<ClaimProgressSegment>,
  val isPartnerClaim: Boolean,
) {
  companion object {
    fun fromPartnerClaim(claim: PartnerClaimFragment): ClaimStatusCardUiState {
      return ClaimStatusCardUiState(
        id = claim.id,
        claimType = claim.claimType,
        insuranceDisplayName = null,
        submittedDate = claim.submittedAt?.atStartOfDayIn(TimeZone.UTC),
        pillTypes = ClaimPillType.fromPartnerClaim(claim.status),
        claimProgressItemsUiState = ClaimProgressSegment.fromPartnerClaim(claim.status),
        isPartnerClaim = true,
      )
    }

    fun fromClaimStatusCardsQuery(claim: ClaimFragment): ClaimStatusCardUiState {
      return ClaimStatusCardUiState(
        id = claim.id,
        claimType = claim.claimType,
        insuranceDisplayName = claim.productVariant?.displayName,
        submittedDate = claim.submittedAt,
        pillTypes = ClaimPillType.fromClaimFragment(claim),
        claimProgressItemsUiState = ClaimProgressSegment.fromClaimFragment(claim),
        isPartnerClaim = false,
      )
    }
  }
}
