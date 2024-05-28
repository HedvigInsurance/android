package com.hedvig.android.notification.badge.data.crosssell.card

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.notification.badge.data.crosssell.CrossSellIdentifier
import com.hedvig.android.notification.badge.data.crosssell.CrossSellNotificationBadgeService
import com.hedvig.android.notification.badge.data.crosssell.FakeNotificationBadgeStorage
import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellIdentifiersUseCase
import com.hedvig.android.notification.badge.data.storage.NotificationBadge
import com.hedvig.android.notification.badge.data.storage.NotificationBadgeStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CrossSellCardNotificationBadgeServiceTest {
  private fun crossSellCardNotificationBadgeService(
    notificationBadgeStorage: NotificationBadgeStorage,
    getCrossSellIdentifiersUseCase: GetCrossSellIdentifiersUseCase,
  ): CrossSellCardNotificationBadgeService {
    return CrossSellCardNotificationBadgeServiceImpl(
      CrossSellNotificationBadgeService(
        getCrossSellIdentifiersUseCase,
        notificationBadgeStorage,
      ),
    )
  }

  @Test
  fun `When backend returns no cross sells, show no badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeStorage(backgroundScope)
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellIdentifiersUseCase()
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellIdentifiersUseCase = getCrossSellsContractTypesUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(false)
  }

  @Test
  fun `When backend returns a cross sell and it's not seen, show card badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeStorage(backgroundScope)
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellIdentifiersUseCase {
      setOf(CrossSellIdentifier("SE_ACCIDENT"))
    }
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellIdentifiersUseCase = getCrossSellsContractTypesUseCase,
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
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellIdentifiersUseCase {
      setOf(seAccident)
    }
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellIdentifiersUseCase = getCrossSellsContractTypesUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(false)
  }

  @Test
  fun `When backend returns two cross sells but they're both seen, show no badge`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val seCarFull = CrossSellIdentifier("SE_CAR_FULL")
    val notificationBadgeService = FakeNotificationBadgeStorage(backgroundScope).apply {
      setValue(
        NotificationBadge.CrossSellInsuranceFragmentCard,
        setOf(
          seAccident.rawValue,
          seCarFull.rawValue,
        ),
      )
    }
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellIdentifiersUseCase {
      setOf(seAccident, seCarFull)
    }
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellIdentifiersUseCase = getCrossSellsContractTypesUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(false)
  }

  @Test
  fun `When backend returns two cross sells but only one is seen, show badge`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val seCarFull = CrossSellIdentifier("SE_CAR_FULL")
    val notificationBadgeService = FakeNotificationBadgeStorage(backgroundScope).apply {
      setValue(
        NotificationBadge.CrossSellInsuranceFragmentCard,
        setOf(seAccident.rawValue),
      )
    }
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellIdentifiersUseCase {
      setOf(seAccident, seCarFull)
    }
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellIdentifiersUseCase = getCrossSellsContractTypesUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(true)
  }

  @Test
  fun `When backend returns two cross sells and one is seen plus another random one is seen, show badge`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val seCarFull = CrossSellIdentifier("SE_CAR_FULL")
    val seApartmentRent = CrossSellIdentifier("SE_APARTMENT_RENT")
    val notificationBadgeService = FakeNotificationBadgeStorage(backgroundScope).apply {
      setValue(
        NotificationBadge.CrossSellInsuranceFragmentCard,
        setOf(
          seAccident.rawValue,
          seApartmentRent.rawValue,
        ),
      )
    }
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellIdentifiersUseCase {
      setOf(seAccident, seCarFull)
    }
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellIdentifiersUseCase = getCrossSellsContractTypesUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(true)
  }

  @Test
  fun `Storing old seen contract types shouldn't affect the shown badge`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val seApartmentBrf = CrossSellIdentifier("SE_APARTMENT_BRF")
    val seCarFull = CrossSellIdentifier("SE_CAR_FULL")
    val seHouse = CrossSellIdentifier("SE_HOUSE")
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
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellIdentifiersUseCase {
      setOf(seQasaShortTermRental)
    }
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellIdentifiersUseCase = getCrossSellsContractTypesUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(true)
  }

  @Test
  fun `When a notification is shown, when it is marked as seen it no longer shows`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val notificationBadgeService = FakeNotificationBadgeStorage(backgroundScope)
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellIdentifiersUseCase {
      setOf(seAccident)
    }
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellIdentifiersUseCase = getCrossSellsContractTypesUseCase,
    )

    service.showNotification().test {
      assertThat(awaitItem()).isEqualTo(true)
      service.markAsSeen()
      runCurrent()
      assertThat(awaitItem()).isEqualTo(false)
      ensureAllEventsConsumed()
    }
  }

  @Test
  fun `When an unknown cross sell exists, the notification still does not show`() = runTest {
    val unknownCrossSellIdentifier = CrossSellIdentifier("UNKNOWN__")
    val notificationBadgeService = FakeNotificationBadgeStorage(backgroundScope)
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellIdentifiersUseCase {
      setOf(unknownCrossSellIdentifier)
    }
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellIdentifiersUseCase = getCrossSellsContractTypesUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(false)
  }
}
