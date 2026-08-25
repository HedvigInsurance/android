package ui.stepsummary

import FakeChangeTierRepository
import TestBackstack
import arrow.core.Either
import arrow.core.raise.either
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.feature.change.tier.data.CurrentContractData
import com.hedvig.android.feature.change.tier.data.GetCurrentContractDataUseCase
import com.hedvig.android.feature.change.tier.navigation.SummaryParameters
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryPresenter
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import currentQuote
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import testQuote

class SummaryPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `when payment protection amount is increased show the qualification period info`() = runTest {
    val tierRepo = FakeChangeTierRepository()
    val presenter = SummaryPresenter(
      params = params,
      tierRepository = tierRepo,
      getCurrentContractDataUseCase = FakeGetCurrentContractDataUseCase(),
      backstack = TestBackstack(),
    )
    presenter.test(SummaryState.Loading) {
      tierRepo.quoteTurbine.add(either { higherTierPaymentProtectionQuote })
      tierRepo.quoteTurbine.add(either { currentPaymentProtectionQuote })
      skipItems(1)
      assertThat(awaitItem())
        .isInstanceOf(SummaryState.Success::class)
        .prop(SummaryState.Success::showQualificationPeriodInfo)
        .isEqualTo(true)
    }
  }

  @Test
  fun `when the payment protection amount is not increased do not show the qualification period info`() = runTest {
    val tierRepo = FakeChangeTierRepository()
    val presenter = SummaryPresenter(
      params = params,
      tierRepository = tierRepo,
      getCurrentContractDataUseCase = FakeGetCurrentContractDataUseCase(),
      backstack = TestBackstack(),
    )
    presenter.test(SummaryState.Loading) {
      tierRepo.quoteTurbine.add(either { currentPaymentProtectionQuote })
      tierRepo.quoteTurbine.add(either { currentPaymentProtectionQuote })
      skipItems(1)
      assertThat(awaitItem())
        .isInstanceOf(SummaryState.Success::class)
        .prop(SummaryState.Success::showQualificationPeriodInfo)
        .isEqualTo(false)
    }
  }

  @Test
  fun `when the contract is not payment protection do not show the qualification period info`() = runTest {
    val tierRepo = FakeChangeTierRepository()
    val presenter = SummaryPresenter(
      params = params,
      tierRepository = tierRepo,
      getCurrentContractDataUseCase = FakeGetCurrentContractDataUseCase(),
      backstack = TestBackstack(),
    )
    presenter.test(SummaryState.Loading) {
      tierRepo.quoteTurbine.add(either { testQuote })
      tierRepo.quoteTurbine.add(either { currentQuote })
      skipItems(1)
      assertThat(awaitItem())
        .isInstanceOf(SummaryState.Success::class)
        .prop(SummaryState.Success::showQualificationPeriodInfo)
        .isEqualTo(false)
    }
  }
}

private fun TierDeductibleQuote.asPaymentProtection(): TierDeductibleQuote =
  copy(productVariant = productVariant.copy(contractGroup = ContractGroup.PAYMENT_PROTECTION))

private val currentPaymentProtectionQuote = currentQuote.asPaymentProtection()

private val higherTierPaymentProtectionQuote = testQuote.asPaymentProtection()

private class FakeGetCurrentContractDataUseCase : GetCurrentContractDataUseCase {
  override suspend fun invoke(insuranceId: String): Either<ErrorMessage, CurrentContractData> {
    return either { CurrentContractData("exposure name") }
  }
}

private val params = SummaryParameters(
  quoteIdToSubmit = "id0",
  insuranceId = "testId",
  activationDate = LocalDate(2025, 9, 11),
)
