package com.hedvig.app.feature.home.ui.claimstatus.data

import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.ClaimOutcome
import com.hedvig.android.owldroid.type.ClaimStatus
import com.hedvig.app.R
import com.hedvig.app.util.compose.ContentAlpha
import com.hedvig.app.util.compose.DarkAndLightColor
import com.hedvig.app.util.compose.DisplayableText

data class ClaimProgressData(
    val displayableText: DisplayableText,
    val type: ClaimProgressType
) {
    sealed class ClaimProgressType {
        abstract val color: DarkAndLightColor
        abstract val contentAlpha: ContentAlpha

        object Paid : ClaimProgressType() {
            override val color: DarkAndLightColor
                get() = ClaimStatusColors.Progress.paid
            override val contentAlpha: ContentAlpha
                get() = ContentAlpha.HIGH
        }

        object PastInactive : ClaimProgressType() {
            override val color: DarkAndLightColor
                get() = DarkAndLightColor.primary()
            override val contentAlpha: ContentAlpha
                get() = ContentAlpha.MEDIUM
        }

        object CurrentlyActive : ClaimProgressType() {
            override val color: DarkAndLightColor
                get() = DarkAndLightColor.primary()
            override val contentAlpha: ContentAlpha
                get() = ContentAlpha.HIGH
        }

        object FutureInactive : ClaimProgressType() {
            override val color: DarkAndLightColor
                get() = DarkAndLightColor.primary()
            override val contentAlpha: ContentAlpha
                get() = ContentAlpha.DISABLED
        }

        object Reopened : ClaimProgressType() {
            override val color: DarkAndLightColor
                get() = ClaimStatusColors.Progress.reopened
            override val contentAlpha: ContentAlpha
                get() = ContentAlpha.HIGH
        }
    }

    companion object {
        fun claimProgressDataListFromHomeQueryClaim(
            homeQueryClaim: HomeQuery.Claim
        ): List<ClaimProgressData> {
            val (first, second, third) = claimProgressDataTripleFromHomeQueryClaim(homeQueryClaim)
            return listOf(
                ClaimProgressData(DisplayableText(R.string.claim_status_bar_submitted), first),
                ClaimProgressData(DisplayableText(R.string.claim_status_bar_being_handled), second),
                ClaimProgressData(DisplayableText(R.string.claim_status_bar_closed), third),
            )
        }

        private fun claimProgressDataTripleFromHomeQueryClaim(
            homeQueryClaim: HomeQuery.Claim
        ): Triple<ClaimProgressType, ClaimProgressType, ClaimProgressType> {
            return when (homeQueryClaim.status) {
                ClaimStatus.SUBMITTED -> Triple(
                    ClaimProgressType.CurrentlyActive,
                    ClaimProgressType.FutureInactive,
                    ClaimProgressType.FutureInactive,
                )
                ClaimStatus.BEING_HANDLED -> Triple(
                    ClaimProgressType.PastInactive,
                    ClaimProgressType.CurrentlyActive,
                    ClaimProgressType.FutureInactive,
                )
                ClaimStatus.CLOSED -> {
                    when (homeQueryClaim.outcome) {
                        ClaimOutcome.PAID -> Triple(
                            ClaimProgressType.Paid,
                            ClaimProgressType.Paid,
                            ClaimProgressType.Paid,
                        )
                        else -> Triple(
                            ClaimProgressType.CurrentlyActive,
                            ClaimProgressType.CurrentlyActive,
                            ClaimProgressType.CurrentlyActive,
                        )
                    }
                }
                ClaimStatus.REOPENED -> Triple(
                    ClaimProgressType.PastInactive,
                    ClaimProgressType.Reopened,
                    ClaimProgressType.FutureInactive,
                )
                else -> Triple(
                    ClaimProgressType.CurrentlyActive,
                    ClaimProgressType.CurrentlyActive,
                    ClaimProgressType.CurrentlyActive,
                )
            }
        }
    }
}
