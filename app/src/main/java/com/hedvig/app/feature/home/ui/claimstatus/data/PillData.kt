package com.hedvig.app.feature.home.ui.claimstatus.data

import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.ClaimStatusCardPillType

data class PillData(
    val text: String,
    val type: PillType,
) {

    enum class PillType {
        OPEN,
        CLOSED,
        REOPENED,
        PAYMENT,
        UNKNOWN, // Default type to not break clients on breaking API changes. Should default to how OPEN is rendered
        ;

        companion object {
            fun fromQueryType(queryType: ClaimStatusCardPillType): PillType = when (queryType) {
                ClaimStatusCardPillType.OPEN -> OPEN
                ClaimStatusCardPillType.CLOSED -> CLOSED
                ClaimStatusCardPillType.REOPENED -> REOPENED
                ClaimStatusCardPillType.PAYMENT -> PAYMENT
                ClaimStatusCardPillType.UNKNOWN__ -> UNKNOWN
            }
        }
    }

    companion object {
        fun fromClaimStatusCardsQuery(
            claimStatusCards: HomeQuery.ClaimStatusCard,
        ): List<PillData> = claimStatusCards.pills.map { pill ->
            PillData(
                text = pill.text,
                type = PillType.fromQueryType(pill.type),
            )
        }
    }
}
