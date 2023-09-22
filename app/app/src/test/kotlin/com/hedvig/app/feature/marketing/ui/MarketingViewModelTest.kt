package com.hedvig.app.feature.marketing.ui

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.hedvig.android.core.common.test.MainCoroutineRule
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.test.FakeHAnalytics
import com.hedvig.android.market.Language
import com.hedvig.android.market.Market
import com.hedvig.app.feature.marketing.MarketingViewModel
import com.hedvig.app.feature.marketing.data.GetInitialMarketPickerValuesUseCase
import com.hedvig.app.feature.marketing.data.GetMarketingBackgroundUseCase
import com.hedvig.app.feature.marketing.data.UpdateApplicationLanguageUseCase
import com.hedvig.hanalytics.HAnalytics
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class MarketingViewModelTest {
  @get:Rule
  val mainCoroutineRule = MainCoroutineRule()

  private fun createMarketingViewModel(
    market: Market? = null,
    hAnalytics: HAnalytics = FakeHAnalytics(),
    getMarketingBackgroundUseCase: GetMarketingBackgroundUseCase = mockk(relaxed = true),
    updateApplicationLanguageUseCase: UpdateApplicationLanguageUseCase = mockk(relaxed = true),
    getInitialMarketPickerValuesUseCase: GetInitialMarketPickerValuesUseCase = GetInitialMarketPickerValuesUseCase(
      mockk(relaxed = true),
      mockk(relaxed = true),
      mockk(relaxed = true),
    ),
    featureManager: FeatureManager = mockk(relaxed = true),
  ) = MarketingViewModel(
    market = market,
    hAnalytics = hAnalytics,
    getMarketingBackgroundUseCase = getMarketingBackgroundUseCase,
    updateApplicationLanguageUseCase = updateApplicationLanguageUseCase,
    getInitialMarketPickerValuesUseCase = getInitialMarketPickerValuesUseCase,
    featureManager = featureManager,
  )

  @Test
  fun `when no market is selected, should display the market picker`() = runTest {
    val viewModel = createMarketingViewModel()

    assertThat(viewModel.state.value.selectedMarket).isEqualTo(null)
    assertThat(viewModel.state.value).prop(com.hedvig.app.feature.marketing.MarketingViewState::isLoading).isTrue()
  }

  @Test
  fun `when geo returns always enabled market, should pre-select that market`() = runTest {
    val initialValues = mockk<GetInitialMarketPickerValuesUseCase>()
    coEvery { initialValues.invoke() } returns Pair(Market.SE, Language.EN_SE)

    val viewModel = createMarketingViewModel(getInitialMarketPickerValuesUseCase = initialValues)
    advanceUntilIdle()

    assertThat(viewModel.state.value.isLoading).isFalse()
    assertThat(viewModel.state.value.market).isEqualTo(Market.SE)
  }

  @Test
  fun `when selecting a market, should update with that market showing`() = runTest {
    val viewModel = createMarketingViewModel()
    advanceUntilIdle()
    viewModel.setMarket(Market.SE)

    assertThat(viewModel.state.value.market).isEqualTo(Market.SE)
  }

  @Test
  fun `when selecting a language, should update with that language showing`() = runTest {
    val viewModel = createMarketingViewModel()
    advanceUntilIdle()
    viewModel.setLanguage(Language.EN_SE)

    assertThat(viewModel.state.value.language).isEqualTo(Language.EN_SE)
  }

  @Test
  fun `after setting both market and language, should be able to set`() = runTest {
    val viewModel = createMarketingViewModel()

    advanceUntilIdle()
    viewModel.setMarket(Market.SE)
    viewModel.setLanguage(Language.EN_SE)

    assertThat(viewModel.state.value.canSetMarketAndLanguage()).isTrue()
  }

  @Test
  fun `when setting market but not language, should set a default language for that market`() = runTest {
    val viewModel = createMarketingViewModel()

    advanceUntilIdle()
    viewModel.setMarket(Market.SE)

    assertThat(viewModel.state.value.language).isNotNull()
  }

  @Test
  fun `when switching market and old language is incompatible, should set default language for that market`() =
    runTest {
      val viewModel = createMarketingViewModel()

      advanceUntilIdle()
      viewModel.setMarket(Market.SE)
      viewModel.setMarket(Market.NO)

      assertThat(viewModel.state.value.language).all {
        isNotNull()
        isNotEqualTo(Language.EN_SE)
      }
    }

  @Test
  fun `when a market is provided to viewModel, should have a selected market`() = runTest {
    val viewModel = createMarketingViewModel(market = Market.SE)
    assertThat(viewModel.state.value.selectedMarket).isEqualTo(Market.SE)
  }

  @Test
  fun `when submitting market and language preferences, should transition to market picked`() = runTest {
    val viewModel = createMarketingViewModel(
      market = null,
    )
    advanceUntilIdle()
    viewModel.setMarket(Market.SE)
    viewModel.submitMarketAndLanguage()
    advanceUntilIdle()

    assertThat(viewModel.state.value.selectedMarket).isNotNull()
  }
}
