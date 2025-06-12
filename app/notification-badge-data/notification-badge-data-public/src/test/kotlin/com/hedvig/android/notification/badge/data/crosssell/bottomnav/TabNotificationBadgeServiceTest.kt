package com.hedvig.android.notification.badge.data.crosssell.bottomnav

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.notification.badge.data.crosssell.CrossSellIdentifier
import com.hedvig.android.notification.badge.data.crosssell.CrossSellNotificationBadgeService
import com.hedvig.android.notification.badge.data.crosssell.FakeNotificationBadgeStorage
import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellRecommendationIdUseCase
import com.hedvig.android.notification.badge.data.crosssell.card.FakeGetCrossSellRecommendationIdUseCase
import com.hedvig.android.notification.badge.data.referrals.ReferralsNotificationBadgeService
import com.hedvig.android.notification.badge.data.storage.NotificationBadge
import com.hedvig.android.notification.badge.data.storage.NotificationBadgeStorage
import com.hedvig.android.notification.badge.data.tab.BottomNavTab
import com.hedvig.android.notification.badge.data.tab.TabNotificationBadgeService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TabNotificationBadgeServiceTest {
  private fun tabNotificationBadgeService(
    notificationBadgeStorage: NotificationBadgeStorage,
    getCrossSellRecommendationIdUseCase: GetCrossSellRecommendationIdUseCase,
  ): TabNotificationBadgeService {
    return TabNotificationBadgeService(
      CrossSellBottomNavNotificationBadgeService(
        CrossSellNotificationBadgeService(
          getCrossSellRecommendationIdUseCase,
          notificationBadgeStorage,
        ),
      ),
      ReferralsNotificationBadgeService(
        notificationBadgeStorage,
      ),
    )
  }

  @Test
  fun `When backend returns no cross sells, show no badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeStorage(backgroundScope)
    val getCrossSellsContractTypeIdentifiersUseCase = FakeGetCrossSellRecommendationIdUseCase()
    val service = tabNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellRecommendationIdUseCase = getCrossSellsContractTypeIdentifiersUseCase,
    )

    val unseenBadges = service.unseenTabNotificationBadges().first()

    assertThat(unseenBadges).isEqualTo(setOf(BottomNavTab.FOREVER))
  }

  @Test
  fun `When backend returns a cross sell and it's not seen, show insurance badge`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val notificationBadgeService = FakeNotificationBadgeStorage(backgroundScope)
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellRecommendationIdUseCase {
      setOf(seAccident)
    }
    val service = tabNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellRecommendationIdUseCase = getCrossSellsContractTypesUseCase,
    )

    val unseenBadges = service.unseenTabNotificationBadges().first()

    assertThat(unseenBadges).isEqualTo(setOf(BottomNavTab.INSURANCE, BottomNavTab.FOREVER))
  }

  @Test
  fun `When backend returns a cross sell but it's seen, show no badge`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val notificationBadgeService = FakeNotificationBadgeStorage(backgroundScope).apply {
      setValue(
        NotificationBadge.BottomNav.CrossSellOnInsuranceScreen,
        setOf(seAccident.rawValue),
      )
    }
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellRecommendationIdUseCase {
      setOf(seAccident)
    }
    val service = tabNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellRecommendationIdUseCase = getCrossSellsContractTypesUseCase,
    )

    val unseenBadges = service.unseenTabNotificationBadges().first()

    assertThat(unseenBadges).isEqualTo(setOf(BottomNavTab.FOREVER))
  }

  @Test
  fun `When backend returns two cross sells but they're both seen, show no badge`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val seCarFull = CrossSellIdentifier("SE_CAR_FULL")
    val notificationBadgeService = FakeNotificationBadgeStorage(backgroundScope).apply {
      setValue(
        NotificationBadge.BottomNav.CrossSellOnInsuranceScreen,
        setOf(
          seAccident.rawValue,
          seCarFull.rawValue,
        ),
      )
    }
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellRecommendationIdUseCase {
      setOf(seAccident, seCarFull)
    }
    val service = tabNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellRecommendationIdUseCase = getCrossSellsContractTypesUseCase,
    )

    val unseenBadges = service.unseenTabNotificationBadges().first()

    assertThat(unseenBadges).isEqualTo(setOf(BottomNavTab.FOREVER))
  }

  @Test
  fun `When backend returns two cross sells but only one is seen, still show insurance badge`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val seCarFull = CrossSellIdentifier("SE_CAR_FULL")
    val notificationBadgeService = FakeNotificationBadgeStorage(backgroundScope).apply {
      setValue(
        NotificationBadge.BottomNav.CrossSellOnInsuranceScreen,
        setOf(seAccident.rawValue),
      )
    }
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellRecommendationIdUseCase {
      setOf(seAccident, seCarFull)
    }
    val service = tabNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellRecommendationIdUseCase = getCrossSellsContractTypesUseCase,
    )

    val unseenBadges = service.unseenTabNotificationBadges().first()

    assertThat(unseenBadges).isEqualTo(setOf(BottomNavTab.INSURANCE, BottomNavTab.FOREVER))
  }

  @Test
  fun `Storing old seen contract types shouldn't affect the shown badge`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val seApartmentBrf = CrossSellIdentifier("SE_APARTMENT_BRF")
    val seHouse = CrossSellIdentifier("SE_HOUSE")
    val seCarFull = CrossSellIdentifier("SE_CAR_FULL")
    val seQasaShortTermRental = CrossSellIdentifier("SE_QASA_SHORT_TERM_RENTAL")
    val notificationBadgeService = FakeNotificationBadgeStorage(backgroundScope).apply {
      setValue(
        NotificationBadge.BottomNav.CrossSellOnInsuranceScreen,
        setOf(
          seAccident.rawValue,
          seApartmentBrf.rawValue,
          seCarFull.rawValue,
          seHouse.rawValue,
        ),
      )
    }
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellRecommendationIdUseCase {
      setOf(seQasaShortTermRental)
    }
    val service = tabNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellRecommendationIdUseCase = getCrossSellsContractTypesUseCase,
    )

    val unseenBadges = service.unseenTabNotificationBadges().first()

    assertThat(unseenBadges).isEqualTo(setOf(BottomNavTab.INSURANCE, BottomNavTab.FOREVER))
  }

  @Test
  fun `When a notification is shown, when it is marked as seen it no longer shows`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val notificationBadgeService = FakeNotificationBadgeStorage(backgroundScope)
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellRecommendationIdUseCase {
      setOf(seAccident)
    }
    val service = tabNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellRecommendationIdUseCase = getCrossSellsContractTypesUseCase,
    )
    service.unseenTabNotificationBadges().test {
      assertThat(awaitItem()).isEqualTo(setOf(BottomNavTab.INSURANCE, BottomNavTab.FOREVER))
      service.visitTab(BottomNavTab.INSURANCE)
      assertThat(awaitItem()).isEqualTo(setOf(BottomNavTab.FOREVER))
      ensureAllEventsConsumed()
    }
  }

  @Test
  fun `When two notifications are shown, they get cleared one by one when visiting the tabs`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val notificationBadgeService = FakeNotificationBadgeStorage(backgroundScope)
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellRecommendationIdUseCase {
      setOf(seAccident)
    }
    val service = tabNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellRecommendationIdUseCase = getCrossSellsContractTypesUseCase,
    )

    service.unseenTabNotificationBadges().test {
      assertThat(awaitItem()).isEqualTo(setOf(BottomNavTab.INSURANCE, BottomNavTab.FOREVER))
      service.visitTab(BottomNavTab.INSURANCE)
      assertThat(awaitItem()).isEqualTo(setOf(BottomNavTab.FOREVER))
      service.visitTab(BottomNavTab.FOREVER)
      assertThat(awaitItem()).isEqualTo(emptySet())
      ensureAllEventsConsumed()
    }
  }
}
