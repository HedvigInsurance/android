package com.hedvig.app.service.badge

import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.feature.loggedin.service.GetCrossSellsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class CrossSellNotificationBadgeService(
    private val getCrossSellsUseCase: GetCrossSellsUseCase,
    private val notificationBadgeService: NotificationBadgeService,
) {
    suspend fun getUnseenCrossSells(badgeType: CrossSellBadgeType): Flow<Set<TypeOfContract>> {
        val potentialCrossSells = getCrossSellsUseCase.invoke()
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
        val potentialCrossSells = getCrossSellsUseCase.invoke().map(TypeOfContract::toString).toSet()
        val alreadySeenCrossSells = notificationBadgeService
            .getValue(associatedBadge)
            .first()
        notificationBadgeService.setValue(
            associatedBadge,
            potentialCrossSells + alreadySeenCrossSells
        )
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
