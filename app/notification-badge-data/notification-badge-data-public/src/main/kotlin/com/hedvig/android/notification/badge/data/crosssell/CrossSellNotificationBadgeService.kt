package com.hedvig.android.notification.badge.data.crosssell

import com.hedvig.android.notification.badge.data.storage.NotificationBadgeStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

/**
 * Notification badges for cross-sells show if there is a potential cross-sell
 * recommendation coming from the backend, and the member
 * has never seen it before.
 * The cross-sells recommendation comes from the backend as id
 * and we store the id locally.
 */
internal class CrossSellNotificationBadgeService(
  private val getCrossSellRecommendationIdUseCase: GetCrossSellRecommendationIdUseCase,
  private val notificationBadgeStorage: NotificationBadgeStorage,
) {
  fun showNotification(badgeType: CrossSellBadgeType): Flow<Boolean> {
    val notificationBadge = badgeType.associatedNotificationBadge
    return combine(
      getCrossSellRecommendationIdUseCase
        .invoke(),
      notificationBadgeStorage.getValue(notificationBadge),
    ) { potentialCrossSellRecommendationData, seenCrossSells ->
      val potentialCrossSellRecommendation = potentialCrossSellRecommendationData?.rawValue
      if (potentialCrossSellRecommendation == null) {
        false
      } else {
        !seenCrossSells.contains(potentialCrossSellRecommendation)
      }
    }
  }

  suspend fun markCurrentCrossSellsAsSeen(badgeType: CrossSellBadgeType) {
    val notificationBadge = badgeType.associatedNotificationBadge
    val potentialCrossSellRecommendation = getCrossSellRecommendationIdUseCase
      .invoke().first()
      ?.rawValue
    notificationBadgeStorage.setValue(
      notificationBadge = notificationBadge,
      newStatus = setOfNotNull(potentialCrossSellRecommendation),
    )
  }
}
