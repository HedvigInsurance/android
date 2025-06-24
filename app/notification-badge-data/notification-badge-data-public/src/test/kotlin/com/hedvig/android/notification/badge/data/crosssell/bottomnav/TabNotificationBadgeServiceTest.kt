package com.hedvig.android.notification.badge.data.crosssell.bottomnav

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.notification.badge.data.crosssell.FakeNotificationBadgeStorage
import com.hedvig.android.notification.badge.data.referrals.ReferralsNotificationBadgeService
import com.hedvig.android.notification.badge.data.storage.NotificationBadgeStorage
import com.hedvig.android.notification.badge.data.tab.BottomNavTab
import com.hedvig.android.notification.badge.data.tab.TabNotificationBadgeService
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TabNotificationBadgeServiceTest {
  private fun tabNotificationBadgeService(
    notificationBadgeStorage: NotificationBadgeStorage,
  ): TabNotificationBadgeService {
    return TabNotificationBadgeService(
      ReferralsNotificationBadgeService(
        notificationBadgeStorage,
      ),
    )
  }

  @Test
  fun `When a notification is shown, when it is marked as seen it no longer shows`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeStorage(backgroundScope)
    val service = tabNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
    )
    service.unseenTabNotificationBadges().test {
      assertThat(awaitItem()).isEqualTo(setOf(BottomNavTab.FOREVER))
      service.visitTab(BottomNavTab.FOREVER)
      assertThat(awaitItem()).isEqualTo(emptySet())
      ensureAllEventsConsumed()
    }
  }
}
