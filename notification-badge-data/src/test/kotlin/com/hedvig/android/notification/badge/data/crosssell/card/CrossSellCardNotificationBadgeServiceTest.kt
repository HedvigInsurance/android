package com.hedvig.android.notification.badge.data.crosssell.card

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.apollo.graphql.type.TypeOfContract
import com.hedvig.android.notification.badge.data.crosssell.CrossSellNotificationBadgeService
import com.hedvig.android.notification.badge.data.crosssell.FakeNotificationBadgeStorage
import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellsContractTypesUseCase
import com.hedvig.android.notification.badge.data.storage.NotificationBadge
import com.hedvig.android.notification.badge.data.storage.NotificationBadgeStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CrossSellCardNotificationBadgeServiceTest {

  private fun crossSellCardNotificationBadgeService(
    notificationBadgeStorage: NotificationBadgeStorage,
    getCrossSellsContractTypesUseCase: GetCrossSellsContractTypesUseCase,
  ): CrossSellCardNotificationBadgeService {
    return CrossSellCardNotificationBadgeService(
      CrossSellNotificationBadgeService(
        getCrossSellsContractTypesUseCase,
        notificationBadgeStorage,
      ),
    )
  }

  @Test
  fun `When backend returns no cross sells, show no badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeStorage(this)
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellsContractTypesUseCase()
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellsContractTypesUseCase = getCrossSellsContractTypesUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(false)
  }

  @Test
  fun `When backend returns a cross sell and it's not seen, show card badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeStorage(this)
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellsContractTypesUseCase {
      setOf(TypeOfContract.SE_ACCIDENT)
    }
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellsContractTypesUseCase = getCrossSellsContractTypesUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(true)
  }

  @Test
  fun `When backend returns a cross sell but it's seen, show no badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeStorage(this).apply {
      setValue(
        NotificationBadge.CrossSellInsuranceFragmentCard,
        setOf(TypeOfContract.SE_ACCIDENT.rawValue),
      )
    }
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellsContractTypesUseCase {
      setOf(TypeOfContract.SE_ACCIDENT)
    }
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellsContractTypesUseCase = getCrossSellsContractTypesUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(false)
  }

  @Test
  fun `When backend returns two cross sells but they're both seen, show no badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeStorage(this).apply {
      setValue(
        NotificationBadge.CrossSellInsuranceFragmentCard,
        setOf(
          TypeOfContract.SE_ACCIDENT.rawValue,
          TypeOfContract.SE_CAR_FULL.rawValue,
        ),
      )
    }
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellsContractTypesUseCase {
      setOf(TypeOfContract.SE_ACCIDENT, TypeOfContract.SE_CAR_FULL)
    }
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellsContractTypesUseCase = getCrossSellsContractTypesUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(false)
  }

  @Test
  fun `When backend returns two cross sells but only one is seen, show badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeStorage(this).apply {
      setValue(
        NotificationBadge.CrossSellInsuranceFragmentCard,
        setOf(TypeOfContract.SE_ACCIDENT.rawValue),
      )
    }
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellsContractTypesUseCase {
      setOf(TypeOfContract.SE_ACCIDENT, TypeOfContract.SE_CAR_FULL)
    }
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellsContractTypesUseCase = getCrossSellsContractTypesUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(true)
  }

  @Test
  fun `When backend returns two cross sells and one is seen plus another random one is seen, show badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeStorage(this).apply {
      setValue(
        NotificationBadge.CrossSellInsuranceFragmentCard,
        setOf(
          TypeOfContract.SE_ACCIDENT.rawValue,
          TypeOfContract.SE_ACCIDENT.rawValue,
        ),
      )
    }
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellsContractTypesUseCase {
      setOf(TypeOfContract.SE_ACCIDENT, TypeOfContract.SE_CAR_FULL)
    }
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellsContractTypesUseCase = getCrossSellsContractTypesUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(true)
  }

  @Test
  fun `Storing old seen contract types shouldn't affect the shown badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeStorage(this).apply {
      setValue(
        NotificationBadge.BottomNav.CrossSellOnInsuranceScreen,
        setOf(
          TypeOfContract.SE_ACCIDENT.rawValue,
          TypeOfContract.SE_APARTMENT_BRF.rawValue,
          TypeOfContract.SE_CAR_FULL.rawValue,
          TypeOfContract.SE_HOUSE.rawValue,
        ),
      )
    }
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellsContractTypesUseCase {
      setOf(TypeOfContract.SE_QASA_SHORT_TERM_RENTAL)
    }
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellsContractTypesUseCase = getCrossSellsContractTypesUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(true)
  }

  @Test
  fun `When a notification is shown, when it is marked as seen it no longer shows`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeStorage(this)
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellsContractTypesUseCase {
      setOf(TypeOfContract.SE_ACCIDENT)
    }
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellsContractTypesUseCase = getCrossSellsContractTypesUseCase,
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
