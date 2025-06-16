package com.hedvig.android.notification.badge.data.crosssell.card

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.notification.badge.data.crosssell.CrossSellIdentifier
import com.hedvig.android.notification.badge.data.crosssell.CrossSellNotificationBadgeService
import com.hedvig.android.notification.badge.data.crosssell.FakeNotificationBadgeStorage
import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellRecommendationIdUseCase
import com.hedvig.android.notification.badge.data.storage.NotificationBadge
import com.hedvig.android.notification.badge.data.storage.NotificationBadgeStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CrossSellCardNotificationBadgeServiceTest {
  private fun crossSellCardNotificationBadgeService(
    notificationBadgeStorage: NotificationBadgeStorage,
    getCrossSellRecommendationIdUseCase: GetCrossSellRecommendationIdUseCase,
  ): CrossSellCardNotificationBadgeService {
    return CrossSellCardNotificationBadgeServiceImpl(
      CrossSellNotificationBadgeService(
        getCrossSellRecommendationIdUseCase,
        notificationBadgeStorage,
      ),
    )
  }

  @Test
  fun `When backend returns no cross sells, show no badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeStorage(backgroundScope)
    val getCrossSellRecommendationIdUseCase = FakeGetCrossSellRecommendationIdUseCase()
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellRecommendationIdUseCase = getCrossSellRecommendationIdUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(false)
  }

  @Test
  fun `When backend returns a cross sell and it's not seen, show card badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeStorage(backgroundScope)
    val getCrossSellRecommendationIdUseCase = FakeGetCrossSellRecommendationIdUseCase {
      CrossSellIdentifier("SE_ACCIDENT")
    }
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellRecommendationIdUseCase = getCrossSellRecommendationIdUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(true)
  }

  @Test
  fun `When backend returns a cross sell but it's seen, show no badge`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val notificationBadgeService = FakeNotificationBadgeStorage(backgroundScope).apply {
      setValue(
        NotificationBadge.CrossSellInsuranceFragmentCard,
        setOf(seAccident.rawValue),
      )
    }
    val getCrossSellRecommendationIdUseCase = FakeGetCrossSellRecommendationIdUseCase {
      seAccident
    }
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellRecommendationIdUseCase = getCrossSellRecommendationIdUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(false)
  }

  @Test
  fun `When a notification is shown, when it is marked as seen it no longer shows`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val notificationBadgeService = FakeNotificationBadgeStorage(backgroundScope)
    val getCrossSellRecommendationIdUseCase = FakeGetCrossSellRecommendationIdUseCase {
      seAccident
    }
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellRecommendationIdUseCase = getCrossSellRecommendationIdUseCase,
    )

    service.showNotification().test {
      assertThat(awaitItem()).isEqualTo(true)
      service.markAsSeen()
      runCurrent()
      assertThat(awaitItem()).isEqualTo(false)
      ensureAllEventsConsumed()
    }
  }
}
