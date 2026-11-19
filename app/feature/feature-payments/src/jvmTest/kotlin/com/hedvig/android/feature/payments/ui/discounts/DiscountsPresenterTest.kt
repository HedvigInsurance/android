package com.hedvig.android.feature.payments.ui.discounts

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.payments.data.Discount
import com.hedvig.android.feature.payments.data.DiscountedContract
import com.hedvig.android.feature.payments.data.DiscountsDetails
import com.hedvig.android.feature.payments.data.DiscountsOverview
import com.hedvig.android.feature.payments.data.GetDiscountsOverviewUseCase
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DiscountsPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val initialState = DiscountsUiState(foreverInformation = null, discountedContracts = emptyList())

  private val discountedContract = DiscountedContract(
    contractId = "contractId",
    contractDisplayName = "Home insurance",
    discountsDetails = DiscountsDetails(
      discountInfo = null,
      appliedDiscounts = listOf(
        Discount(
          code = "WELCOME",
          description = "First month free",
          status = Discount.DiscountStatus.ACTIVE,
          statusDescription = null,
          amount = null,
          isReferral = false,
        ),
      ),
    ),
  )

  @Test
  fun `a loaded overview shows the discounted contracts`() = runTest {
    val useCase = FakeGetDiscountsOverviewUseCase()
    DiscountsPresenter(useCase).test(initialState) {
      assertThat(awaitItem()).isEqualTo(initialState)
      useCase.responses.send(DiscountsOverview(listOf(discountedContract), foreverInformation = null).right())
      assertThat(awaitItem()).isEqualTo(
        initialState.copy(
          discountedContracts = listOf(discountedContract),
          error = null,
          isLoadingPaymentOverView = false,
        ),
      )
    }
  }

  @Test
  fun `a failed load shows an error, and retrying clears it once the load succeeds`() = runTest {
    val useCase = FakeGetDiscountsOverviewUseCase()
    DiscountsPresenter(useCase).test(initialState) {
      assertThat(awaitItem()).isEqualTo(initialState)
      useCase.responses.send(ErrorMessage().left())
      val failed = initialState.copy(error = true, isLoadingPaymentOverView = false)
      assertThat(awaitItem()).isEqualTo(failed)

      sendEvent(DiscountsEvent.Retry)
      assertThat(awaitItem()).isEqualTo(failed.copy(isRetrying = true))
      useCase.responses.send(DiscountsOverview(listOf(discountedContract), foreverInformation = null).right())
      assertThat(awaitItem()).isEqualTo(
        initialState.copy(
          discountedContracts = listOf(discountedContract),
          error = null,
          isLoadingPaymentOverView = false,
          isRetrying = false,
        ),
      )
      assertThat(useCase.calls).isEqualTo(2)
    }
  }
}

private class FakeGetDiscountsOverviewUseCase : GetDiscountsOverviewUseCase {
  val responses = Channel<Either<ErrorMessage, DiscountsOverview>>(Channel.UNLIMITED)
  var calls = 0

  override suspend fun invoke(): Either<ErrorMessage, DiscountsOverview> {
    calls++
    return responses.receive()
  }
}
