package com.hedvig.android.notification.badge.data.crosssell.card

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.android.notification.badge.data.crosssell.CrossSellIdentifier
import com.hedvig.android.notification.badge.data.crosssell.CrossSellNotificationBadgeService
import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellRecommendationIdUseCase
import com.hedvig.android.notification.badge.data.crosssell.home.CrossSellHomeNotificationService
import com.hedvig.android.notification.badge.data.crosssell.home.CrossSellHomeNotificationServiceImpl
import com.hedvig.android.notification.badge.data.storage.DatastoreNotificationBadgeStorage
import com.hedvig.android.notification.badge.data.storage.NotificationBadge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CrossSellHomeNotificationBadgeServiceTest {
  @get:Rule
  val testFolder = TemporaryFolder()

  private fun TestScope.fakeDataStore(): TestPreferencesDataStore {
    return TestPreferencesDataStore(
      coroutineScope = backgroundScope,
      datastoreTestFileDirectory = testFolder.newFolder("test_datastore_file", ".preferences_pb"),
    )
  }

  private fun crossSellCardNotificationBadgeService(
    dataStore: DataStore<Preferences>,
    getCrossSellRecommendationIdUseCase: GetCrossSellRecommendationIdUseCase,
    notificationBadgeStorage: DatastoreNotificationBadgeStorage = DatastoreNotificationBadgeStorage(dataStore),
  ): CrossSellHomeNotificationService {
    return CrossSellHomeNotificationServiceImpl(
      CrossSellNotificationBadgeService(
        getCrossSellRecommendationIdUseCase,
        notificationBadgeStorage,
      ),
      dataStore,
    )
  }

  @Test
  fun `When backend returns no cross sells, show no badge`() = runTest {
    val getCrossSellRecommendationIdUseCase = FakeGetCrossSellRecommendationIdUseCase()
    val dataStore = fakeDataStore()
    val service = crossSellCardNotificationBadgeService(
      dataStore = dataStore,
      getCrossSellRecommendationIdUseCase = getCrossSellRecommendationIdUseCase,
    )

    val showNotification = service.showRedDotNotification().first()

    assertThat(showNotification).isEqualTo(false)
  }

  @Test
  fun `When backend returns a cross sell and it's not seen, show card badge`() = runTest {
    val getCrossSellRecommendationIdUseCase = FakeGetCrossSellRecommendationIdUseCase {
      CrossSellIdentifier("SE_ACCIDENT")
    }
    val dataStore = fakeDataStore()
    val service = crossSellCardNotificationBadgeService(
      dataStore = dataStore,
      getCrossSellRecommendationIdUseCase = getCrossSellRecommendationIdUseCase,
    )

    val showNotification = service.showRedDotNotification().first()

    assertThat(showNotification).isEqualTo(true)
  }

  @Test
  fun `When backend returns a cross sell but it's seen, show no badge`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val dataStore = fakeDataStore()
    val getCrossSellRecommendationIdUseCase = FakeGetCrossSellRecommendationIdUseCase {
      seAccident
    }
    val notificationBadgeStorage = DatastoreNotificationBadgeStorage(dataStore).apply {
      setValue(
        NotificationBadge.CrossSellInsuranceFragmentCard,
        setOf(seAccident.rawValue),
      )
    }
    val service = crossSellCardNotificationBadgeService(
      dataStore = dataStore,
      getCrossSellRecommendationIdUseCase = getCrossSellRecommendationIdUseCase,
      notificationBadgeStorage = notificationBadgeStorage,
    )

    val showNotification = service.showRedDotNotification().first()

    assertThat(showNotification).isEqualTo(false)
  }

  @Test
  fun `When a notification is shown, when it is marked as seen it no longer shows`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val dataStore = fakeDataStore()
    val getCrossSellRecommendationIdUseCase = FakeGetCrossSellRecommendationIdUseCase {
      seAccident
    }

    val service = crossSellCardNotificationBadgeService(
      dataStore = dataStore,
      getCrossSellRecommendationIdUseCase = getCrossSellRecommendationIdUseCase,
    )

    service.showRedDotNotification().test {
      assertThat(awaitItem()).isEqualTo(true)
      service.markAsSeen()
      runCurrent()
      assertThat(awaitItem()).isEqualTo(false)
      ensureAllEventsConsumed()
    }
  }
}

private class FakeGetCrossSellRecommendationIdUseCase(
  private val recommendationId: (() -> CrossSellIdentifier?) = { null },
) : GetCrossSellRecommendationIdUseCase {
  override fun invoke(): Flow<CrossSellIdentifier?> {
    return flowOf(recommendationId())
  }
}
