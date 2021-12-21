package com.hedvig.app.feature.home.ui.claimstatus.data

import com.hedvig.android.owldroid.fragment.ProgressSegments
import com.hedvig.android.owldroid.type.ClaimStatusProgressType

data class ClaimProgressData(
    val text: String,
    val type: ClaimProgressType,
) {
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
        fun fromProgressSegments(
            progressSegments: List<ProgressSegments>,
        ): List<ClaimProgressData> = progressSegments
            .map { progressSegment ->
                ClaimProgressData(
                    text = progressSegment.text,
                    type = ClaimProgressType.fromQueryType(progressSegment.type),
                )
            }
    }
}
