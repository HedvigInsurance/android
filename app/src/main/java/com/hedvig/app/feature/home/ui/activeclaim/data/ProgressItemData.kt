package com.hedvig.app.feature.home.ui.activeclaim.data

import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.ClaimOutcome
import com.hedvig.android.owldroid.type.ClaimStatus
import com.hedvig.app.R

data class ProgressItemData(val text: String, val type: ProgressItemType) {

    sealed class ProgressItemType {
        abstract val color: Color
            @Composable get

        abstract val contentAlpha: Float
            @Composable get

        object Paid : ProgressItemType() {
            override val color: Color
                @Composable get() = colorResource(R.color.colorInfoCardSurface)
            override val contentAlpha: Float
                @Composable get() = ContentAlpha.high
        }

        object PastInactive : ProgressItemType() {
            override val color: Color
                @Composable get() = MaterialTheme.colors.primary
            override val contentAlpha: Float
                @Composable get() = ContentAlpha.medium
        }

        object CurrentlyActive : ProgressItemType() {
            override val color: Color
                @Composable get() = MaterialTheme.colors.primary
            override val contentAlpha: Float
                @Composable get() = ContentAlpha.high
        }

        object FutureInactive : ProgressItemType() {
            override val color: Color
                @Composable get() = MaterialTheme.colors.primary
            override val contentAlpha: Float
                @Composable get() = ContentAlpha.disabled
        }

        object Reopened : ProgressItemType() {
            override val color: Color
                @Composable get() = Color(0xFFFE9650)
            override val contentAlpha: Float
                @Composable get() = ContentAlpha.high
        }
    }

    companion object {
        fun progressItemListFromActiveClaim(activeClaim: HomeQuery.ActiveClaim): List<ProgressItemData> {
            val (first, second, third) = progressItemTypeListFromActiveClaim(activeClaim)
            return listOf(
                ProgressItemData("Submitted", first),
                ProgressItemData("Being handled", second),
                ProgressItemData("Closed", third),
            )
        }

        private fun progressItemTypeListFromActiveClaim(
            activeClaim: HomeQuery.ActiveClaim
        ): Triple<ProgressItemType, ProgressItemType, ProgressItemType> {
            return when (activeClaim.status) {
                ClaimStatus.SUBMITTED -> Triple(
                    ProgressItemType.CurrentlyActive,
                    ProgressItemType.FutureInactive,
                    ProgressItemType.FutureInactive,
                )
                ClaimStatus.BEING_HANDLED -> Triple(
                    ProgressItemType.PastInactive,
                    ProgressItemType.CurrentlyActive,
                    ProgressItemType.FutureInactive,
                )
                ClaimStatus.CLOSED -> {
                    when (activeClaim.outcome) {
                        ClaimOutcome.PAID -> Triple(
                            ProgressItemType.Paid,
                            ProgressItemType.Paid,
                            ProgressItemType.Paid,
                        )
                        else -> Triple(
                            ProgressItemType.CurrentlyActive,
                            ProgressItemType.CurrentlyActive,
                            ProgressItemType.CurrentlyActive,
                        )
                    }
                }
                ClaimStatus.REOPENED -> Triple(
                    ProgressItemType.PastInactive,
                    ProgressItemType.Reopened,
                    ProgressItemType.FutureInactive,
                )
                ClaimStatus.UNKNOWN__ -> throw IllegalArgumentException()
            }
        }
    }
}
