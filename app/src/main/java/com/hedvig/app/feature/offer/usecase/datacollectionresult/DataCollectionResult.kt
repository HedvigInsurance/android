package com.hedvig.app.feature.offer.usecase.datacollectionresult

import com.hedvig.android.owldroid.graphql.DataCollectionResultQuery
import com.hedvig.app.util.apollo.toMonetaryAmount
import javax.money.MonetaryAmount

data class DataCollectionResult(
    val collectedList: List<CollectedInsuranceData>,
) {
    companion object {
        fun fromDto(dto: DataCollectionResultQuery.Data): DataCollectionResult {
            return DataCollectionResult(
                dto.externalInsuranceProvider
                    ?.dataCollectionV2
                    ?.mapNotNull(CollectedInsuranceData::fromDto) ?: emptyList()
            )
        }
    }

    data class CollectedInsuranceData(
        val name: String?,
        val netPremium: MonetaryAmount?,
    ) {
        companion object {
            fun fromDto(dto: DataCollectionResultQuery.DataCollectionV2): CollectedInsuranceData? {
                val house = dto.asHouseInsuranceCollection
                val person = dto.asPersonTravelInsuranceCollection
                return when {
                    house != null -> CollectedInsuranceData(
                        house.insuranceName,
                        house.monthlyNetPremium?.fragments?.monetaryAmountFragment?.toMonetaryAmount(),
                    )
                    person != null -> CollectedInsuranceData(
                        person.insuranceName,
                        person.monthlyNetPremium?.fragments?.monetaryAmountFragment?.toMonetaryAmount(),
                    )
                    else -> null
                }
            }
        }
    }
}
