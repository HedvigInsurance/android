package com.hedvig.app.feature.marketing.ui

import arrow.core.Either
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.hedvig.app.feature.marketing.MarketingViewModel
import com.hedvig.app.feature.marketing.data.GetInitialMarketPickerValuesUseCase
import com.hedvig.app.feature.marketing.data.GetMarketingBackgroundUseCase
import com.hedvig.app.feature.marketing.data.SubmitMarketAndLanguagePreferencesUseCase
import com.hedvig.app.feature.marketing.data.UpdateApplicationLanguageUseCase
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.coroutines.StandardTestDispatcherAsMainDispatcherRule
import com.hedvig.app.util.featureflags.FeatureManager
import com.hedvig.hanalytics.HAnalytics
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class MarketingViewModelTest {
    @get:Rule
    val mainCoroutineRule = StandardTestDispatcherAsMainDispatcherRule()

    private fun sut(
        marketManager: MarketManager = mockk(relaxed = true),
        hAnalytics: HAnalytics = mockk(relaxed = true),
        submitMarketAndLanguagePreferencesUseCase: SubmitMarketAndLanguagePreferencesUseCase = mockk(
            relaxed = true
        ),
        getMarketingBackgroundUseCase: GetMarketingBackgroundUseCase = mockk(relaxed = true),
        updateApplicationLanguageUseCase: UpdateApplicationLanguageUseCase = mockk(relaxed = true),
        getInitialMarketPickerValuesUseCase: GetInitialMarketPickerValuesUseCase = mockk(relaxed = true),
        featureManager: FeatureManager = mockk(relaxed = true)
    ) = MarketingViewModel(
        marketManager = marketManager,
        hAnalytics = hAnalytics,
        submitMarketAndLanguagePreferencesUseCase = submitMarketAndLanguagePreferencesUseCase,
        getMarketingBackgroundUseCase = getMarketingBackgroundUseCase,
        updateApplicationLanguageUseCase = updateApplicationLanguageUseCase,
        getInitialMarketPickerValuesUseCase = getInitialMarketPickerValuesUseCase,
        featureManager = featureManager,
    )

    @Test
    fun `when no market is selected, should display the market picker`() = runTest {
        val marketManager = mockk<MarketManager>()
        every { marketManager.hasSelectedMarket } returns false

        val model = sut(
            marketManager = marketManager,
        )

        assertThat(model.state.value)
            .isInstanceOf(com.hedvig.app.feature.marketing.PickMarket::class)
            .prop(com.hedvig.app.feature.marketing.PickMarket::isLoading).isTrue()
    }

    @Test
    fun `when geo returns always enabled market, should pre-select that market`() = runTest {
        val marketManager = mockk<MarketManager>()
        every { marketManager.hasSelectedMarket } returns false
        val initialValues = mockk<GetInitialMarketPickerValuesUseCase>()
        coEvery { initialValues.invoke() } returns Either.Right(Market.SE to null)

        val model = sut(
            marketManager = marketManager,
            getInitialMarketPickerValuesUseCase = initialValues,
        )
        advanceUntilIdle()

        assertThat(model.state.value)
            .isInstanceOf(com.hedvig.app.feature.marketing.PickMarket::class)
            .all {
                prop(com.hedvig.app.feature.marketing.PickMarket::isLoading).isFalse()
                prop(com.hedvig.app.feature.marketing.PickMarket::market).isEqualTo(Market.SE)
            }
    }

    @Test
    fun `when geo fails, should not preselect a market`() = runTest {
        val marketManager = mockk<MarketManager>()
        every { marketManager.hasSelectedMarket } returns false
        val initialValues = mockk<GetInitialMarketPickerValuesUseCase>()
        coEvery { initialValues.invoke() } returns Either.Left(QueryResult.Error.NetworkError(""))

        val model = sut(
            marketManager = marketManager,
            getInitialMarketPickerValuesUseCase = initialValues,
        )
        advanceUntilIdle()

        assertThat(model.state.value)
            .isInstanceOf(com.hedvig.app.feature.marketing.PickMarket::class)
            .all {
                prop(com.hedvig.app.feature.marketing.PickMarket::isLoading).isFalse()
                prop(com.hedvig.app.feature.marketing.PickMarket::market).isNull()
            }
    }

    @Test
    fun `when selecting a market, should update with that market showing`() = runTest {
        val marketManager = mockk<MarketManager>()
        every { marketManager.hasSelectedMarket } returns false

        val model = sut(
            marketManager = marketManager,
        )
        advanceUntilIdle()
        model.setMarket(Market.SE)

        assertThat(model.state.value)
            .isInstanceOf(com.hedvig.app.feature.marketing.PickMarket::class)
            .prop(com.hedvig.app.feature.marketing.PickMarket::market).isEqualTo(Market.SE)
    }

    @Test
    fun `when selecting a language, should update with that language showing`() = runTest {
        val marketManager = mockk<MarketManager>()
        every { marketManager.hasSelectedMarket } returns false

        val model = sut(
            marketManager = marketManager,
        )
        advanceUntilIdle()
        model.setLanguage(Language.EN_SE)

        assertThat(model.state.value)
            .isInstanceOf(com.hedvig.app.feature.marketing.PickMarket::class)
            .prop(com.hedvig.app.feature.marketing.PickMarket::language).isEqualTo(Language.EN_SE)
    }

    @Test
    fun `after setting both market and language, should be valid`() = runTest {
        val marketManager = mockk<MarketManager>()
        every { marketManager.hasSelectedMarket } returns false

        val model = sut(
            marketManager = marketManager,
        )
        advanceUntilIdle()
        model.setMarket(Market.SE)
        model.setLanguage(Language.EN_SE)

        assertThat(model.state.value)
            .isInstanceOf(com.hedvig.app.feature.marketing.PickMarket::class)
            .prop(com.hedvig.app.feature.marketing.PickMarket::isValid).isTrue()
    }

    @Test
    fun `when setting market but not language, should set a default language for that market`() =
        runTest {
            val marketManager = mockk<MarketManager>()
            every { marketManager.hasSelectedMarket } returns false

            val model = sut(
                marketManager = marketManager,
            )
            advanceUntilIdle()
            model.setMarket(Market.SE)

            assertThat(model.state.value)
                .isInstanceOf(com.hedvig.app.feature.marketing.PickMarket::class)
                .prop(com.hedvig.app.feature.marketing.PickMarket::language).isNotNull()
        }

    @Test
    fun `when switching market and old language is incompatible, should set default language for that market`() =
        runTest {
            val marketManager = mockk<MarketManager>()
            every { marketManager.hasSelectedMarket } returns false

            val model = sut(
                marketManager = marketManager,
            )
            advanceUntilIdle()
            model.setMarket(Market.SE)
            model.setMarket(Market.NO)

            assertThat(model.state.value)
                .isInstanceOf(com.hedvig.app.feature.marketing.PickMarket::class)
                .prop(com.hedvig.app.feature.marketing.PickMarket::language).all {
                    isNotEqualTo(Language.EN_SE)
                    isNotNull()
                }
        }

    @Test
    fun `when a market is selected, should display market picked`() = runTest {
        val marketManager = mockk<MarketManager>()
        every { marketManager.hasSelectedMarket } returns true

        val model = sut(
            marketManager = marketManager,
        )

        assertThat(model.state.value)
            .isInstanceOf(com.hedvig.app.feature.marketing.MarketPicked.Loading::class)
    }

    @Test
    fun `when submitting market and language preferences, should transition to market picked`() =
        runTest {
            val marketManager = mockk<MarketManager>()
            every { marketManager.hasSelectedMarket } returns false

            val model = sut(
                marketManager = marketManager,
            )
            advanceUntilIdle()
            model.setMarket(Market.SE)
            model.submitMarketAndLanguage()
            advanceUntilIdle()

            assertThat(model.state.value)
                .isInstanceOf(com.hedvig.app.feature.marketing.MarketPicked::class)
        }
}
