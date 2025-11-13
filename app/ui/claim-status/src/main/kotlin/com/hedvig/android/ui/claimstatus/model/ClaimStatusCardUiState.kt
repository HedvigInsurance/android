package com.hedvig.android.ui.claimstatus.model

import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaLocalDate
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
    fun fromClaimStatusCardsQuery(claim: ClaimFragment): ClaimStatusCardUiState {
      return ClaimStatusCardUiState(
        id = claim.id,
        claimType = claim.claimType,
        insuranceDisplayName = claim.productVariant?.displayName,
        submittedDate = claim.submittedAt,
        pillTypes = ClaimPillType.fromClaimFragment(claim.status, claim.outcome, claim.payoutAmount),
        claimProgressItemsUiState = ClaimProgressSegment.fromClaimFragment(claim.status),
      )
    }

    fun fromPartnerClaim(partnerClaim: PartnerClaimFragment): ClaimStatusCardUiState {
      return ClaimStatusCardUiState(
        id = partnerClaim.id,
        claimType = partnerClaim.claimType,
        insuranceDisplayName = partnerClaim.productVariant?.displayName,
        submittedDate = partnerClaim.submittedAt.atStartOfDayIn(TimeZone.UTC), //todo: which TimeZone???
        pillTypes = ClaimPillType.fromClaimFragment(partnerClaim.status, null, partnerClaim.payoutAmount),
        claimProgressItemsUiState = ClaimProgressSegment.fromClaimFragment(partnerClaim.status),
      )
    }
  }
}
