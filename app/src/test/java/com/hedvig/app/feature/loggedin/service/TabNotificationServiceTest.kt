package com.hedvig.app.feature.loggedin.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.service.badge.NotificationBadge
import com.hedvig.app.service.badge.NotificationBadgeService
import com.hedvig.app.service.badge.Seen
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class TabNotificationServiceTest {

    @Test
    fun `when all cross-sells have been seen, should not show notification for insurance tab`() {
        val mockUseCase = mockk<GetCrossSellsUseCase>()
        coEvery { mockUseCase.invoke() } returns setOf(TypeOfContract.SE_ACCIDENT)
        val mockDataStore = mockedNotificationBadgeService(Seen.seen())

        val sut = TabNotificationService(
            mockUseCase,
            mockDataStore,
        )

        runBlockingTest {
            assertThat(sut.unseenTabNotifications().first()).isEqualTo(emptySet())
        }
    }

    @Test
    fun `when there is an unseen cross-sell, should show notification for insurance tab`() {
        val mockUseCase = mockk<GetCrossSellsUseCase>()
        coEvery { mockUseCase.invoke() } returns setOf(TypeOfContract.SE_ACCIDENT)
        val mockNotificationBadgeService = mockedNotificationBadgeService(Seen.notSeen())

        val sut = TabNotificationService(
            mockUseCase,
            mockNotificationBadgeService,
        )

        runBlockingTest {
            assertThat(sut.unseenTabNotifications().first()).isEqualTo(setOf(LoggedInTabs.INSURANCE))
        }
    }

    private fun mockedNotificationBadgeService(seen: Seen): NotificationBadgeService {
        val mockNotificationBadgeService = mockk<NotificationBadgeService>()
        every {
            mockNotificationBadgeService.seenStatus(any<List<NotificationBadge>>())
        } returns flowOf<List<Pair<NotificationBadge, Seen>>>(
            listOf(
                NotificationBadge.BottomNav.CrossSellOnInsuranceFragment to seen
            )
        )
        return mockNotificationBadgeService
    }
}
