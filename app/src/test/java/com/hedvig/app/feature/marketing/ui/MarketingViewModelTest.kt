package com.hedvig.app.feature.marketing.ui

import arrow.core.Either
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.hedvig.app.feature.marketing.MarketingViewModel
import com.hedvig.app.feature.marketing.data.GetInitialMarketPickerValuesUseCase
import com.hedvig.app.feature.marketing.data.GetMarketingBackgroundUseCase
import com.hedvig.app.feature.marketing.data.SubmitMarketAndLanguagePreferencesUseCase
import com.hedvig.app.feature.marketing.data.UpdateApplicationLanguageUseCase
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.util.coroutines.MainCoroutineRule
import com.hedvig.app.util.featureflags.FeatureManager
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
    hAnalytics: HAnalytics = mockk(relaxed = true),
    submitMarketAndLanguagePreferencesUseCase: SubmitMarketAndLanguagePreferencesUseCase = mockk(
      relaxed = true,
    ),
    getMarketingBackgroundUseCase: GetMarketingBackgroundUseCase = mockk(relaxed = true),
    updateApplicationLanguageUseCase: UpdateApplicationLanguageUseCase = mockk(relaxed = true),
    getInitialMarketPickerValuesUseCase: GetInitialMarketPickerValuesUseCase = mockk(relaxed = true),
    featureManager: FeatureManager = mockk(relaxed = true),
  ) = MarketingViewModel(
    market = market,
    hAnalytics = hAnalytics,
    submitMarketAndLanguagePreferencesUseCase = submitMarketAndLanguagePreferencesUseCase,
    getMarketingBackgroundUseCase = getMarketingBackgroundUseCase,
    updateApplicationLanguageUseCase = updateApplicationLanguageUseCase,
    getInitialMarketPickerValuesUseCase = getInitialMarketPickerValuesUseCase,
    featureManager = featureManager,
  )

  @Test
  fun `when no market is selected, should display the market picker`() = runTest {
    val model = createMarketingViewModel()

    assertThat(model.state.value.selectedMarket).isEqualTo(null)
    assertThat(model.state.value).prop(com.hedvig.app.feature.marketing.ViewState::isLoading).isTrue()
  }

  @Test
  fun `when geo returns always enabled market, should pre-select that market`() = runTest {
    val initialValues = mockk<GetInitialMarketPickerValuesUseCase>()
    coEvery { initialValues.invoke() } returns Pair(Market.SE, null)

    val model = createMarketingViewModel(getInitialMarketPickerValuesUseCase = initialValues)
    advanceUntilIdle()

    assertThat(model.state.value.isLoading).isFalse()
    assertThat(model.state.value.market).isEqualTo(Market.SE)
  }

  @Test
  fun `when selecting a market, should update with that market showing`() = runTest {
    val model = createMarketingViewModel()
    advanceUntilIdle()
    model.setMarket(Market.SE)

    assertThat(model.state.value.market).isEqualTo(Market.SE)
  }

  @Test
  fun `when selecting a language, should update with that language showing`() = runTest {
    val model = createMarketingViewModel()
    advanceUntilIdle()
    model.setLanguage(Language.EN_SE)

    assertThat(model.state.value.language).isEqualTo(Language.EN_SE)
  }

  @Test
  fun `after setting both market and language, should be able to set`() = runTest {
    val model = createMarketingViewModel()

    advanceUntilIdle()
    model.setMarket(Market.SE)
    model.setLanguage(Language.EN_SE)

    assertThat(model.state.value.canSetMarketAndLanguage()).isTrue()
  }

  @Test
  fun `when setting market but not language, should set a default language for that market`() = runTest {
    val model = createMarketingViewModel()

    advanceUntilIdle()
    model.setMarket(Market.SE)

    assertThat(model.state.value.language).isNotNull()
  }

  @Test
  fun `when switching market and old language is incompatible, should set default language for that market`() =
    runTest {
      val model = createMarketingViewModel()

      advanceUntilIdle()
      model.setMarket(Market.SE)
      model.setMarket(Market.NO)

      assertThat(model.state.value.language).all {
        isNotNull()
        isNotEqualTo(Language.EN_SE)
      }
    }

  @Test
  fun `when a market is provided to view model, should have a selected market`() = runTest {
    val model = createMarketingViewModel(market = Market.SE)
    assertThat(model.state.value.selectedMarket).isEqualTo(Market.SE)
  }

  @Test
  fun `when submitting market and language preferences, should transition to market picked`() = runTest {
    val submitMarketAndLanguagePreferencesUseCase = mockk<SubmitMarketAndLanguagePreferencesUseCase>()
    coEvery { submitMarketAndLanguagePreferencesUseCase.invoke(any(), any()) } returns Either.Right(Unit)

    val model = createMarketingViewModel(
      market = null,
      submitMarketAndLanguagePreferencesUseCase = submitMarketAndLanguagePreferencesUseCase,
    )
    advanceUntilIdle()
    model.setMarket(Market.SE)
    model.submitMarketAndLanguage()
    advanceUntilIdle()

    assertThat(model.state.value.selectedMarket).isNotNull()
  }
}
