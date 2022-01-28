package com.hedvig.app.service.badge

import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.feature.crossselling.usecase.GetCrossSellsContractTypesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class CrossSellNotificationBadgeService(
    private val getCrossSellsContractTypesUseCase: GetCrossSellsContractTypesUseCase,
    private val notificationBadgeService: NotificationBadgeService,
) {
    suspend fun getUnseenCrossSells(badgeType: CrossSellBadgeType): Flow<Set<TypeOfContract>> {
        val potentialCrossSells = getCrossSellsContractTypesUseCase.invoke()
        val tabNotifications = badgeType.associatedBadge

        return notificationBadgeService.getValue(tabNotifications)
            .map { unseenCrossSellStrings ->
                unseenCrossSellStrings.map(TypeOfContract::safeValueOf).toSet()
            }
            .map { seenCrossSells ->
                potentialCrossSells subtract seenCrossSells
            }
    }

    suspend fun markCurrentCrossSellsAsSeen(badgeType: CrossSellBadgeType) {
        val associatedBadge = badgeType.associatedBadge
        val potentialCrossSells = getCrossSellsContractTypesUseCase.invoke().map(TypeOfContract::rawValue).toSet()
        val alreadySeenCrossSells = notificationBadgeService
            .getValue(associatedBadge)
            .first()
        notificationBadgeService.setValue(
            associatedBadge,
            potentialCrossSells + alreadySeenCrossSells
        )
    }

    suspend fun shouldShowTabNotification(): Flow<Boolean> {
        return getUnseenCrossSells(CrossSellBadgeType.BottomNav).map { contracts ->
            contracts.isNotEmpty()
        }
    }

    sealed class CrossSellBadgeType {
        abstract val associatedBadge: NotificationBadge<Set<String>>

        object BottomNav : CrossSellBadgeType() {
            override val associatedBadge: NotificationBadge<Set<String>>
                get() = NotificationBadge.BottomNav.CrossSellOnInsuranceFragment
        }

        object InsuranceFragmentCard : CrossSellBadgeType() {
            override val associatedBadge: NotificationBadge<Set<String>>
                get() = NotificationBadge.CrossSellInsuranceFragmentCard
        }
    }
}
