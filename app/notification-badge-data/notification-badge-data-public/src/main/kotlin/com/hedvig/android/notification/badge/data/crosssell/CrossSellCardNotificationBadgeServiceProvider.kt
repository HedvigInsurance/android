package com.hedvig.android.notification.badge.data.crosssell

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.notification.badge.data.crosssell.card.CrossSellCardNotificationBadgeService

class CrossSellCardNotificationBadgeServiceProvider(
  override val demoManager: DemoManager,
  override val demoImpl: CrossSellCardNotificationBadgeService,
  override val prodImpl: CrossSellCardNotificationBadgeService,
) : ProdOrDemoProvider<CrossSellCardNotificationBadgeService>
