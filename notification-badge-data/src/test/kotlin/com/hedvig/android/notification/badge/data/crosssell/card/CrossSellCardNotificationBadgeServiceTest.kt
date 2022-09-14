package com.hedvig.android.notification.badge.data.crosssell.card

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.apollo.graphql.type.TypeOfContract
import com.hedvig.android.notification.badge.data.crosssell.CrossSellNotificationBadgeService
import com.hedvig.android.notification.badge.data.crosssell.FakeNotificationBadgeService
import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellsContractTypesUseCase
import com.hedvig.android.notification.badge.data.storage.NotificationBadge
import com.hedvig.android.notification.badge.data.storage.NotificationBadgeService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CrossSellCardNotificationBadgeServiceTest {

  private fun crossSellCardNotificationBadgeService(
    notificationBadgeService: NotificationBadgeService,
    getCrossSellsContractTypesUseCase: GetCrossSellsContractTypesUseCase,
  ): CrossSellCardNotificationBadgeService {
    return CrossSellCardNotificationBadgeService(
      CrossSellNotificationBadgeService(
        getCrossSellsContractTypesUseCase,
        notificationBadgeService,
      ),
    )
  }

  @Test
  fun `When backend returns no cross sells, show no badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeService(this)
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellsContractTypesUseCase()
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeService = notificationBadgeService,
      getCrossSellsContractTypesUseCase = getCrossSellsContractTypesUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(false)
  }

  @Test
  fun `When backend returns a cross sell and it's not seen, show card badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeService(this)
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellsContractTypesUseCase {
      setOf(TypeOfContract.SE_ACCIDENT)
    }
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeService = notificationBadgeService,
      getCrossSellsContractTypesUseCase = getCrossSellsContractTypesUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(true)
  }

  @Test
  fun `When backend returns a cross sell but it's seen, show no badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeService(this).apply {
      setValue(
        NotificationBadge.CrossSellInsuranceFragmentCard,
        setOf(TypeOfContract.SE_ACCIDENT.rawValue),
      )
    }
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellsContractTypesUseCase {
      setOf(TypeOfContract.SE_ACCIDENT)
    }
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeService = notificationBadgeService,
      getCrossSellsContractTypesUseCase = getCrossSellsContractTypesUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(false)
  }

  @Test
  fun `When backend returns two cross sells but they're both seen, show no badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeService(this).apply {
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
      notificationBadgeService = notificationBadgeService,
      getCrossSellsContractTypesUseCase = getCrossSellsContractTypesUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(false)
  }

  @Test
  fun `When backend returns two cross sells but only one is seen, show badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeService(this).apply {
      setValue(
        NotificationBadge.CrossSellInsuranceFragmentCard,
        setOf(TypeOfContract.SE_ACCIDENT.rawValue),
      )
    }
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellsContractTypesUseCase {
      setOf(TypeOfContract.SE_ACCIDENT, TypeOfContract.SE_CAR_FULL)
    }
    val service = crossSellCardNotificationBadgeService(
      notificationBadgeService = notificationBadgeService,
      getCrossSellsContractTypesUseCase = getCrossSellsContractTypesUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(true)
  }

  @Test
  fun `When backend returns two cross sells and one is seen plus another random one is seen, show badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeService(this).apply {
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
      notificationBadgeService = notificationBadgeService,
      getCrossSellsContractTypesUseCase = getCrossSellsContractTypesUseCase,
    )

    val showNotification = service.showNotification().first()

    assertThat(showNotification).isEqualTo(true)
  }
}
