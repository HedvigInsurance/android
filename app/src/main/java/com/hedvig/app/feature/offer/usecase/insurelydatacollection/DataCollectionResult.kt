package com.hedvig.app.feature.offer.usecase.insurelydatacollection

import com.hedvig.android.owldroid.graphql.DataCollectionStatusSubscription

data class DataCollectionResult(
    val insuranceCompany: String?,
    val status: DataCollectionStatus,
) {
    companion object {
        fun fromDto(dto: DataCollectionStatusSubscription.DataCollectionStatusV2): DataCollectionResult {
            return DataCollectionResult(
                dto.insuranceCompany,
                DataCollectionStatus.fromDto(dto.status)
            )
        }
    }

    enum class DataCollectionStatus {
        IN_PROGRESS,
        COMPLETE,
        FAILED,
        ;

        companion object {
            fun fromDto(dto: com.hedvig.android.owldroid.type.DataCollectionStatus): DataCollectionStatus {
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
