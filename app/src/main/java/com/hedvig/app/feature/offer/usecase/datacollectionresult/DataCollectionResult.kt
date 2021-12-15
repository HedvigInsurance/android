package com.hedvig.app.feature.offer.usecase.datacollectionresult

import com.hedvig.android.owldroid.graphql.DataCollectionResultQuery
import com.hedvig.app.util.apollo.toMonetaryAmount
import javax.money.MonetaryAmount

sealed class DataCollectionResult {

    object Empty : DataCollectionResult()
    data class Content(
        val collectedList: List<CollectedInsuranceData>,
    ) : DataCollectionResult()

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

    companion object {
        fun fromDto(dto: DataCollectionResultQuery.Data): DataCollectionResult {
            val collectedInsuranceDataList = dto.externalInsuranceProvider
                ?.dataCollectionV2
                ?.mapNotNull(CollectedInsuranceData::fromDto)
            return if (collectedInsuranceDataList == null || collectedInsuranceDataList.isEmpty()) {
                Empty
            } else {
                Content(collectedInsuranceDataList)
            }
        }
    }
}
