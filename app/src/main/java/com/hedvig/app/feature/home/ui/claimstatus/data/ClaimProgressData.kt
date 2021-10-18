package com.hedvig.app.feature.home.ui.claimstatus.data

import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.ClaimOutcome
import com.hedvig.android.owldroid.type.ClaimStatus
import com.hedvig.app.ui.compose.theme.ClaimStatusColors

data class ClaimProgressData(val text: String, val type: ClaimProgressType) {

    sealed class ClaimProgressType {
        abstract val color: Color
            @Composable get

        abstract val contentAlpha: Float
            @Composable get

        object Paid : ClaimProgressType() {
            override val color: Color
                @Composable get() = ClaimStatusColors.Progress.paid
            override val contentAlpha: Float
                @Composable get() = ContentAlpha.high
        }

        object PastInactive : ClaimProgressType() {
            override val color: Color
                @Composable get() = MaterialTheme.colors.primary
            override val contentAlpha: Float
                @Composable get() = ContentAlpha.medium
        }

        object CurrentlyActive : ClaimProgressType() {
            override val color: Color
                @Composable get() = MaterialTheme.colors.primary
            override val contentAlpha: Float
                @Composable get() = ContentAlpha.high
        }

        object FutureInactive : ClaimProgressType() {
            override val color: Color
                @Composable get() = MaterialTheme.colors.primary
            override val contentAlpha: Float
                @Composable get() = ContentAlpha.disabled
        }

        object Reopened : ClaimProgressType() {
            override val color: Color
                @Composable get() = ClaimStatusColors.Progress.reopened
            override val contentAlpha: Float
                @Composable get() = ContentAlpha.high
        }
    }

    companion object {
        fun claimProgressDataListFromHomeQueryClaim(
            homeQueryClaim: HomeQuery.Claim
        ): List<ClaimProgressData> {
            val (first, second, third) = claimProgressDataTripleFromHomeQueryClaim(homeQueryClaim)
            return listOf(
                ClaimProgressData("Submitted", first),
                ClaimProgressData("Being handled", second),
                ClaimProgressData("Closed", third),
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
                ClaimStatus.UNKNOWN__ -> throw IllegalArgumentException()
            }
        }
    }
}
