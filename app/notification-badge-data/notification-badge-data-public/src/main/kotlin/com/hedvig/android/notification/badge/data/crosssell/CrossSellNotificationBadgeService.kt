package com.hedvig.android.notification.badge.data.crosssell

import com.hedvig.android.notification.badge.data.storage.NotificationBadgeStorage
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
  private val getCrossSellIdentifiersUseCase: GetCrossSellIdentifiersUseCase,
  private val notificationBadgeStorage: NotificationBadgeStorage,
) {
  fun showNotification(badgeType: CrossSellBadgeType): Flow<Boolean> {
    return flow {
      val notificationBadge = badgeType.associatedNotificationBadge
      val potentialCrossSells = getCrossSellIdentifiersUseCase
        .invoke()
        .filter(CrossSellIdentifier::isKnownCrossSell)
        .map(CrossSellIdentifier::rawValue)

      emitAll(
        notificationBadgeStorage.getValue(notificationBadge)
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
    val potentialCrossSells = getCrossSellIdentifiersUseCase
      .invoke()
      .filter(CrossSellIdentifier::isKnownCrossSell)
      .map(CrossSellIdentifier::rawValue)
      .toSet()

    val alreadySeenCrossSells = notificationBadgeStorage.getValue(notificationBadge).first()
    notificationBadgeStorage.setValue(
      notificationBadge = notificationBadge,
      newStatus = potentialCrossSells + alreadySeenCrossSells,
    )
  }
}
