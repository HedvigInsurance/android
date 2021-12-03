package com.hedvig.app.feature.home.ui.claimstatus.data

import android.os.Parcelable
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.ClaimStatusProgressType
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClaimProgressData(
    val text: String,
    val type: ClaimProgressType,
) : Parcelable {
    enum class ClaimProgressType {
        PAST_INACTIVE,
        CURRENTLY_ACTIVE,
        FUTURE_INACTIVE,
        PAID,
        REOPENED,
        UNKNOWN,
        ;

        companion object {
            fun fromQueryType(queryType: ClaimStatusProgressType): ClaimProgressType = when (queryType) {
                ClaimStatusProgressType.PAST_INACTIVE -> PAST_INACTIVE
                ClaimStatusProgressType.CURRENTLY_ACTIVE -> CURRENTLY_ACTIVE
                ClaimStatusProgressType.FUTURE_INACTIVE -> FUTURE_INACTIVE
                ClaimStatusProgressType.PAID -> PAID
                ClaimStatusProgressType.REOPENED -> REOPENED
                ClaimStatusProgressType.UNKNOWN__ -> UNKNOWN
            }
        }
    }

    companion object {
        fun fromClaimStatusCardsQuery(
            claimStatusCards: HomeQuery.ClaimStatusCard,
        ): List<ClaimProgressData> = claimStatusCards
            .progressSegments
            .map { progressSegment ->
                ClaimProgressData(
                    text = progressSegment.text,
                    type = ClaimProgressType.fromQueryType(progressSegment.type),
                )
            }
    }
}
