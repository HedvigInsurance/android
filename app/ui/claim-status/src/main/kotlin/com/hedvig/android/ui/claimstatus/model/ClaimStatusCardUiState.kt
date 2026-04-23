package com.hedvig.android.ui.claimstatus.model

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import octopus.fragment.ClaimFragment
import octopus.fragment.PartnerClaimFragment

data class ClaimStatusCardUiState(
  val id: String,
  val claimType: String?,
  val insuranceDisplayName: String?,
  val submittedDate: Instant,
  val pillTypes: List<ClaimPillType>,
  val claimProgressItemsUiState: List<ClaimProgressSegment>,
) {
  companion object {
    fun fromPartnerClaim(claim: PartnerClaimFragment): ClaimStatusCardUiState {
      return ClaimStatusCardUiState(
        id = claim.id,
        claimType = claim.claimType,
        insuranceDisplayName = claim.exposureDisplayName ?: claim.productVariant?.displayName,
        submittedDate = claim.submittedAt?.atStartOfDayIn(TimeZone.UTC) ?: Clock.System.now(),
        pillTypes = ClaimPillType.fromPartnerClaim(claim.status),
        claimProgressItemsUiState = ClaimProgressSegment.fromPartnerClaim(claim.status),
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
      )
    }
  }
}
