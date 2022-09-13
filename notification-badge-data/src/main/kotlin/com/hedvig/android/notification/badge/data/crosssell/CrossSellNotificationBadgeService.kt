package com.hedvig.android.notification.badge.data.crosssell

import com.hedvig.android.apollo.graphql.type.TypeOfContract
import com.hedvig.android.notification.badge.data.storage.NotificationBadgeService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Notification badges for cross-sells show if there is a potential cross-sell coming from the backend, and the member
 * has never seen it before.
 * The cross-sells come from the backend as [TypeOfContract] and we store the string representation of them locally.
 */
internal class CrossSellNotificationBadgeService(
  private val getCrossSellsContractTypesUseCase: GetCrossSellsContractTypesUseCase,
  private val notificationBadgeService: NotificationBadgeService,
) {
  fun showNotification(badgeType: CrossSellBadgeType): Flow<Boolean> {
    return flow {
      val notificationBadge = badgeType.associatedNotificationBadge
      val potentialCrossSells = getCrossSellsContractTypesUseCase.invoke().map(TypeOfContract::rawValue).toSet()

      emitAll(
        notificationBadgeService.getValue(notificationBadge)
          .map { seenCrossSells: Set<String> ->
            potentialCrossSells subtract seenCrossSells
          }
          .map { unseenCrossSells: Set<String> ->
            unseenCrossSells.isNotEmpty()
          },
      )
    }
  }

  suspend fun markCurrentCrossSellsAsSeen(badgeType: CrossSellBadgeType) {
    val notificationBadge = badgeType.associatedNotificationBadge
    val potentialCrossSells = getCrossSellsContractTypesUseCase.invoke().map(TypeOfContract::rawValue).toSet()
    val alreadySeenCrossSells = notificationBadgeService.getValue(notificationBadge).first()
    notificationBadgeService.setValue(
      notificationBadge,
      potentialCrossSells + alreadySeenCrossSells,
    )
  }
}
