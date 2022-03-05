package com.hedvig.app.feature.embark.passages

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice.DataCollectionResult
import com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice.RetrievePriceViewModel
import com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice.StartDataCollectionUseCase
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.tracking.MockHAnalytics
import com.hedvig.app.util.coroutines.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RetrievePriceViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val marketManager = object : MarketManager {
        override val enabledMarkets: List<Market>
            get() = listOf(Market.SE, Market.NO, Market.DK)
        override var market: Market?
            get() = Market.SE
            set(value) {}
        override var hasSelectedMarket: Boolean
            get() = true
            set(value) {}
    }

    private val startDataCollectionUseCase = mockk<StartDataCollectionUseCase>()

    private lateinit var viewModel: RetrievePriceViewModel

    @Before
    fun setup() {
        viewModel = RetrievePriceViewModel(
            marketManager = marketManager,
            startDataCollectionUseCase = startDataCollectionUseCase,
            collectionId = "testCollectionId",
            insurerName = "testInsurerName",
            hAnalytics = MockHAnalytics(),
        )
    }

    @Test
    fun testInput() = runTest {
        viewModel.onIdentityInput("1")
        assertThat(viewModel.viewState.value.input).isEqualTo("1")
        assertThat(viewModel.viewState.value.inputError).isNull()

        viewModel.onIdentityInput("Invalid input")
        assertThat(viewModel.viewState.value.input).isEqualTo("Invalid input")
        assertThat(viewModel.viewState.value.inputError).isEqualTo(
            RetrievePriceViewModel.ViewState.InputError(R.string.INVALID_NATIONAL_IDENTITY_NUMBER)
        )

        viewModel.onIdentityInput("1")
        assertThat(viewModel.viewState.value.input).isEqualTo("1")
        assertThat(viewModel.viewState.value.inputError).isNull()
    }

    @Test
    fun testErrorDataCollectionError() = runTest {
        coEvery {
            startDataCollectionUseCase.startDataCollection(
                "9101131093",
                "testCollectionId"
            )
        } coAnswers {
            delay(100)
            DataCollectionResult.Error.NoData
        }

        viewModel.onIdentityInput("9101131093")
        assertThat(viewModel.viewState.value.inputError).isEqualTo(null)

        viewModel.onRetrievePriceInfo()
        runCurrent()
        assertThat(viewModel.viewState.value.isLoading).isEqualTo(true)
        advanceUntilIdle()
        assertThat(viewModel.viewState.value.error).isEqualTo(DataCollectionResult.Error.NoData)
        assertThat(viewModel.viewState.value.isLoading).isEqualTo(false)
    }

    @Test
    fun testErrorDataCollectionSuccess() = runTest {
        coEvery {
            startDataCollectionUseCase.startDataCollection(
                "9101131093",
                "testCollectionId"
            )
        } coAnswers {
            delay(100)
            DataCollectionResult.Success("testToken")
        }

        viewModel.onIdentityInput("9101131093")
        assertThat(viewModel.viewState.value.inputError).isEqualTo(null)

        viewModel.onRetrievePriceInfo()
        runCurrent()
        assertThat(viewModel.viewState.value.isLoading).isEqualTo(true)
        advanceUntilIdle()
        assertThat(viewModel.events.first()).isEqualTo(
            RetrievePriceViewModel.Event.AuthInformation(
                "testToken"
            )
        )
        assertThat(viewModel.viewState.value.isLoading).isEqualTo(false)
    }
}
