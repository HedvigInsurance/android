package com.hedvig.app.feature.offer.usecase.datacollectionstatus

import com.hedvig.android.owldroid.graphql.DataCollectionStatusSubscription

data class DataCollectionStatus(
    val insuranceCompanyName: String?,
    val subscriptionStatus: DataCollectionSubscriptionStatus,
) {
    companion object {
        fun fromDto(dto: DataCollectionStatusSubscription.Data): DataCollectionStatus {
            return DataCollectionStatus(
                dto.dataCollectionStatusV2.insuranceCompany,
                DataCollectionSubscriptionStatus.fromDto(dto.dataCollectionStatusV2.status)
            )
        }
    }

    enum class DataCollectionSubscriptionStatus {
        IN_PROGRESS,
        COMPLETE,
        FAILED,
        ;

        companion object {
            fun fromDto(dto: com.hedvig.android.owldroid.type.DataCollectionStatus): DataCollectionSubscriptionStatus {
                return when (dto) {
                    com.hedvig.android.owldroid.type.DataCollectionStatus.COLLECTING,
                    com.hedvig.android.owldroid.type.DataCollectionStatus.RUNNING,
                    com.hedvig.android.owldroid.type.DataCollectionStatus.LOGIN,
                    com.hedvig.android.owldroid.type.DataCollectionStatus.UNKNOWN__,
                    -> IN_PROGRESS

                    com.hedvig.android.owldroid.type.DataCollectionStatus.COMPLETED_PARTIAL,
                    com.hedvig.android.owldroid.type.DataCollectionStatus.COMPLETED,
                    com.hedvig.android.owldroid.type.DataCollectionStatus.COMPLETED_EMPTY,
                    -> COMPLETE

                    com.hedvig.android.owldroid.type.DataCollectionStatus.WAITING_FOR_AUTHENTICATION,
                    com.hedvig.android.owldroid.type.DataCollectionStatus.USER_INPUT,
                    com.hedvig.android.owldroid.type.DataCollectionStatus.FAILED,
                    -> FAILED
                }
            }
        }
    }
}
