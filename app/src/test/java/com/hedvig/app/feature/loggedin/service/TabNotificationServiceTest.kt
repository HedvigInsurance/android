package com.hedvig.app.feature.loggedin.service

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
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
        coEvery { mockUseCase.invoke() } returns setOf(TypeOfContract.SE_ACCIDENT.toString())
        val mockDataStore = mockedDataStore(setOf(TypeOfContract.SE_ACCIDENT.toString()))

        val sut = TabNotificationService(
            mockUseCase,
            mockDataStore,
        )

        runBlockingTest {
            assertThat(sut.load().first()).isEqualTo(emptySet())
        }
    }

    @Test
    fun `when there is an unseen cross-sell, should show notification for insurance tab`() {
        val mockUseCase = mockk<GetCrossSellsUseCase>()
        coEvery { mockUseCase.invoke() } returns setOf(TypeOfContract.SE_ACCIDENT.toString())
        val mockDataStore = mockedDataStore(emptySet())

        val sut = TabNotificationService(
            mockUseCase,
            mockDataStore,
        )

        runBlockingTest {
            assertThat(sut.load().first()).isEqualTo(setOf(LoggedInTabs.INSURANCE))
        }
    }

    private fun mockedDataStore(data: Set<String>): DataStore<Preferences> {
        val mockPreferences = mockk<Preferences>()
        every { mockPreferences[TabNotificationService.SEEN_CROSS_SELLS_KEY] } returns data
        val mockDataStore = mockk<DataStore<Preferences>>()
        every { mockDataStore.data } returns flowOf(mockPreferences)
        return mockDataStore
    }
}
