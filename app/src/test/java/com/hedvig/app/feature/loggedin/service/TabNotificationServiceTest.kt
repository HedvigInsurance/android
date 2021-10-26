package com.hedvig.app.feature.loggedin.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.service.badge.CrossSellNotificationBadgeService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class TabNotificationServiceTest {

    @Test
    fun `when all cross-sells have been seen, should not show notification for insurance tab`() {
        val mockService = mockedCrossSellNotificationBadgeService(
            returnValue = emptySet()
        )

        val sut = TabNotificationService(mockService)

        runBlockingTest {
            assertThat(sut.unseenTabNotifications().first()).isEqualTo(emptySet())
        }
    }

    @Test
    fun `when there is an unseen cross-sell, should show notification for insurance tab`() {
        val mockService = mockedCrossSellNotificationBadgeService(
            returnValue = setOf(TypeOfContract.SE_ACCIDENT)
        )

        val sut = TabNotificationService(mockService)

        runBlockingTest {
            assertThat(sut.unseenTabNotifications().first()).isEqualTo(setOf(LoggedInTabs.INSURANCE))
        }
    }

    private fun mockedCrossSellNotificationBadgeService(
        returnValue: Set<TypeOfContract>
    ): CrossSellNotificationBadgeService {
        val mock = mockk<CrossSellNotificationBadgeService>()
        coEvery { mock.getUnseenCrossSells(any()) } returns flowOf(returnValue)
        return mock
    }
}
