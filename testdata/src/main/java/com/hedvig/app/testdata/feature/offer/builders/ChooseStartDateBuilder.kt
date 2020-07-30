package com.hedvig.app.testdata.feature.offer.builders

import com.hedvig.android.owldroid.graphql.ChooseStartDateMutation
import java.time.LocalDate

data class ChooseStartDateBuilder(
    private val id: String = "ea656f5f-40b2-4953-85d9-752b33e69e38",
    private val date: LocalDate = LocalDate.now()
) {
    fun build() = ChooseStartDateMutation.Data(
        editQuote = ChooseStartDateMutation.EditQuote(
            asCompleteQuote = ChooseStartDateMutation.AsCompleteQuote(
                id = id,
                startDate = date
            )
        )
    )
}
