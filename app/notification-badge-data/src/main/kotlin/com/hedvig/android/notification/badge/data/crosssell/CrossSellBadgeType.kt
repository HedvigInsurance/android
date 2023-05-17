package com.hedvig.android.notification.badge.data.crosssell

import com.hedvig.android.notification.badge.data.storage.NotificationBadge

internal sealed interface CrossSellBadgeType {
  val associatedNotificationBadge: NotificationBadge<Set<String>>

  object BottomNav : CrossSellBadgeType {
    override val associatedNotificationBadge: NotificationBadge<Set<String>> =
      NotificationBadge.BottomNav.CrossSellOnInsuranceScreen
  }

  object InsuranceFragmentCard : CrossSellBadgeType {
    override val associatedNotificationBadge: NotificationBadge<Set<String>> =
      NotificationBadge.CrossSellInsuranceFragmentCard
  }
}
