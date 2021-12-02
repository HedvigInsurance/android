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
                    // todo check what running actually means?
                    com.hedvig.android.owldroid.type.DataCollectionStatus.RUNNING -> IN_PROGRESS
                    com.hedvig.android.owldroid.type.DataCollectionStatus.COLLECTING -> IN_PROGRESS
                    // todo should show loading while not knowing? 🤔
                    com.hedvig.android.owldroid.type.DataCollectionStatus.UNKNOWN__ -> IN_PROGRESS

                    com.hedvig.android.owldroid.type.DataCollectionStatus.LOGIN,
                    com.hedvig.android.owldroid.type.DataCollectionStatus.USER_INPUT,
                    com.hedvig.android.owldroid.type.DataCollectionStatus.WAITING_FOR_AUTHENTICATION,
                    -> IN_PROGRESS // todo should never reach these options while waiting for auth?

                    // todo what is partial? Should be considered complete?
                    com.hedvig.android.owldroid.type.DataCollectionStatus.COMPLETED_PARTIAL,
                    com.hedvig.android.owldroid.type.DataCollectionStatus.COMPLETED,
                    com.hedvig.android.owldroid.type.DataCollectionStatus.COMPLETED_EMPTY,
                    -> COMPLETE

                    com.hedvig.android.owldroid.type.DataCollectionStatus.FAILED -> FAILED
                }
            }
        }
    }
}
