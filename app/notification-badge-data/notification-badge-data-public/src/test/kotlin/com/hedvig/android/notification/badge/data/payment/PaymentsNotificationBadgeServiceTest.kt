package com.hedvig.android.notification.badge.data.payment

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.android.notification.badge.data.storage.DatastoreNotificationBadgeStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PaymentsNotificationBadgeServiceTest {
  @get:Rule
  val testFolder = TemporaryFolder()

  private fun TestScope.service(useCase: GetPaymentsNotificationBadgeUseCase): PaymentsNotificationBadgeService {
    val dataStore = TestPreferencesDataStore(
      coroutineScope = backgroundScope,
      datastoreTestFileDirectory = testFolder.newFolder("test_datastore_file", ".preferences_pb"),
    )
    return PaymentsNotificationBadgeServiceImpl(useCase, DatastoreNotificationBadgeStorage(dataStore))
  }

  @Test
  fun `a missed payment shows the red dot even when there is an unseen charge notice`() = runTest {
    val useCase = FakeGetPaymentsNotificationBadgeUseCase(
      PaymentsNotificationBadgeData(hasMissedPayment = true, chargeNoticeIds = setOf("pre-1")),
    )

    service(useCase).badge().test {
      assertThat(awaitItem()).isEqualTo(PaymentsNotificationBadge.MissedPayment)
    }
  }

  @Test
  fun `no charge notices shows no dot`() = runTest {
    val useCase = FakeGetPaymentsNotificationBadgeUseCase(
      PaymentsNotificationBadgeData(hasMissedPayment = false, chargeNoticeIds = emptySet()),
    )

    service(useCase).badge().test {
      assertThat(awaitItem()).isNull()
    }
  }

  @Test
  fun `a seen charge notice hides the blue dot until a new one arrives`() = runTest {
    val useCase = FakeGetPaymentsNotificationBadgeUseCase(
      PaymentsNotificationBadgeData(hasMissedPayment = false, chargeNoticeIds = setOf("pre-1")),
    )
    val service = service(useCase)

    service.badge().test {
      assertThat(awaitItem()).isEqualTo(PaymentsNotificationBadge.ChargeNotice)

      service.markChargeNoticesAsSeen()
      assertThat(awaitItem()).isNull()

      useCase.data.value = PaymentsNotificationBadgeData(
        hasMissedPayment = false,
        chargeNoticeIds = setOf("pre-1", "retry-1"),
      )
      assertThat(awaitItem()).isEqualTo(PaymentsNotificationBadge.ChargeNotice)
    }
  }
}

private class FakeGetPaymentsNotificationBadgeUseCase(
  initial: PaymentsNotificationBadgeData?,
) : GetPaymentsNotificationBadgeUseCase {
  val data = MutableStateFlow(initial)

  override fun invoke(): Flow<PaymentsNotificationBadgeData?> = data
}
