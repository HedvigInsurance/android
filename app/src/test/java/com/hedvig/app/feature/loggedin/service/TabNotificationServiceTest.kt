package com.hedvig.app.feature.loggedin.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.service.badge.CrossSellNotificationBadgeService
import com.hedvig.app.service.badge.ReferralsNotificationBadgeService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class TabNotificationServiceTest {

    @Test
    fun `when all notifications have been seen, should not show notification for any tab`() {
        val mockedCrossSellNotificationBadgeService = mockedCrossSellNotificationBadgeService(
            showNotification = false
        )

        val mockedReferralsNotificationBadgeService = mockedReferralsNotificationBadgeService(
            showNotification = false
        )

        val sut = TabNotificationService(
            mockedCrossSellNotificationBadgeService,
            mockedReferralsNotificationBadgeService
        )

        runBlockingTest {
            assertThat(sut.unseenTabNotifications().first()).isEqualTo(emptySet())
        }
    }

    @Test
    fun `when there is an unseen cross-sell notification, should show notification for insurance tab`() {
        val mockedCrossSellNotificationBadgeService = mockedCrossSellNotificationBadgeService(
            showNotification = true
        )

        val mockedReferralsNotificationBadgeService = mockedReferralsNotificationBadgeService(
            showNotification = false
        )

        val sut = TabNotificationService(
            mockedCrossSellNotificationBadgeService,
            mockedReferralsNotificationBadgeService
        )

        runBlockingTest {
            assertThat(sut.unseenTabNotifications().first()).isEqualTo(setOf(LoggedInTabs.INSURANCE))
        }
    }

    @Test
    fun `when there is an unseen referral notification, should show notification for referral tab`() {
        val mockedCrossSellNotificationBadgeService = mockedCrossSellNotificationBadgeService(
            showNotification = false
        )

        val mockedReferralsNotificationBadgeService = mockedReferralsNotificationBadgeService(
            showNotification = true
        )

        val sut = TabNotificationService(
            mockedCrossSellNotificationBadgeService,
            mockedReferralsNotificationBadgeService
        )

        runBlockingTest {
            assertThat(sut.unseenTabNotifications().first()).isEqualTo(setOf(LoggedInTabs.REFERRALS))
        }
    }

    private fun mockedCrossSellNotificationBadgeService(
        showNotification: Boolean
    ): CrossSellNotificationBadgeService {
        val mock = mockk<CrossSellNotificationBadgeService>()
        coEvery { mock.shouldShowTabNotification() } returns flowOf(showNotification)
        return mock
    }

    private fun mockedReferralsNotificationBadgeService(
        showNotification: Boolean
    ): ReferralsNotificationBadgeService {
        val mock = mockk<ReferralsNotificationBadgeService>()
        coEvery { mock.shouldShowNotification() } returns flowOf(showNotification)
        return mock
    }
}
