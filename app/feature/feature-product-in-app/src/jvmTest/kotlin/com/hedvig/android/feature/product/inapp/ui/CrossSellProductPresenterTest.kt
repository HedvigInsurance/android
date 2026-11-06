package com.hedvig.android.feature.product.inapp.ui

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.feature.product.inapp.data.CrossSellProductInfo
import com.hedvig.android.feature.product.inapp.data.GetCrossSellProductInfoUseCase
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CrossSellProductPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `a successful load shows the product info`() = runTest {
    val useCase = FakeGetCrossSellProductInfoUseCase()
    val presenter = CrossSellProductPresenter(crossSellId = "id", getCrossSellProductInfoUseCase = useCase)

    presenter.test(CrossSellProductUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(CrossSellProductUiState.Loading)
      useCase.results.add(productInfo.right())
      assertThat(awaitItem()).isEqualTo(CrossSellProductUiState.Content(productInfo))
      assertThat(useCase.requestedIds).isEqualTo(listOf("id"))
    }
  }

  @Test
  fun `a failed load shows the failure state and retry loads again`() = runTest {
    val useCase = FakeGetCrossSellProductInfoUseCase()
    val presenter = CrossSellProductPresenter(crossSellId = "id", getCrossSellProductInfoUseCase = useCase)

    presenter.test(CrossSellProductUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(CrossSellProductUiState.Loading)
      useCase.results.add(ApolloOperationError.OperationError.Other("error").left())
      assertThat(awaitItem()).isEqualTo(CrossSellProductUiState.Failure)

      sendEvent(CrossSellProductEvent.Retry)
      assertThat(awaitItem()).isEqualTo(CrossSellProductUiState.Loading)
      useCase.results.add(productInfo.right())
      assertThat(awaitItem()).isEqualTo(CrossSellProductUiState.Content(productInfo))
    }
  }
}

private val productInfo = CrossSellProductInfo(
  heroImage = ImageAsset(id = "hero", src = "https://hero", description = null),
  heroIntro = CrossSellProductInfo.HeroIntro(title = "Intro", description = "Intro description"),
  usps = listOf(CrossSellProductInfo.Usp(title = "Usp", description = "Usp description")),
  carousel = listOf(
    CrossSellProductInfo.CarouselItem(
      title = "Slide",
      subtitle = null,
      image = ImageAsset(id = "slide", src = "https://slide", description = null),
      videoUrl = null,
      loop = false,
    ),
  ),
)

private class FakeGetCrossSellProductInfoUseCase : GetCrossSellProductInfoUseCase {
  val results = Turbine<Either<ApolloOperationError, CrossSellProductInfo>>()
  val requestedIds = mutableListOf<String>()

  override suspend fun invoke(crossSellId: String): Either<ApolloOperationError, CrossSellProductInfo> {
    requestedIds.add(crossSellId)
    return results.awaitItem()
  }
}
