package ui.stepcustomize

import CURRENT_ID
import FakeChangeTierRepository
import arrow.core.Either
import arrow.core.raise.either
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.prop
import basTier
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.change.tier.data.CurrentContractData
import com.hedvig.android.feature.change.tier.data.GetCurrentContractDataUseCase
import com.hedvig.android.feature.change.tier.navigation.InsuranceCustomizationParameters
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoveragePresenter
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageState
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageSuccessUiState
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import currentQuote
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import standardTier
import testQuote
import testQuote2
import testQuote3

class SelectCoveragePresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `when sending ids for quotes that are not stored show Failure`() = runTest {
    val getCurrentContractDataUseCase = FakeGetCurrentContractDataUseCase()
    val tierRepo = FakeChangeTierRepository()
    val presenter = SelectCoveragePresenter(
      params = params,
      tierRepository = tierRepo,
      getCurrentContractDataUseCase = getCurrentContractDataUseCase,
    )
    presenter.test(SelectCoverageState.Loading) {
      tierRepo.quoteListTurbine.add(listOf())
      skipItems(1)
      assertThat(awaitItem()).isInstanceOf<SelectCoverageState.Failure>()
    }
  }

  @Test
  fun `when sending quoteIds that are stored show Success`() = runTest {
    val getCurrentContractDataUseCase = FakeGetCurrentContractDataUseCase()
    val tierRepo = FakeChangeTierRepository()
    val presenter = SelectCoveragePresenter(
      params = params,
      tierRepository = tierRepo,
      getCurrentContractDataUseCase = getCurrentContractDataUseCase,
    )
    presenter.test(SelectCoverageState.Loading) {
      tierRepo.quoteListTurbine.add(listOf(testQuote, testQuote2, currentQuote))
      skipItems(1)
      assertThat(awaitItem()).isInstanceOf<SelectCoverageState.Success>()
    }
  }

  @Test
  fun `in the beginning the current quote is pre-select`() = runTest {
    val getCurrentContractDataUseCase = FakeGetCurrentContractDataUseCase()
    val tierRepo = FakeChangeTierRepository()
    val presenter = SelectCoveragePresenter(
      params = params,
      tierRepository = tierRepo,
      getCurrentContractDataUseCase = getCurrentContractDataUseCase,
    )
    presenter.test(SelectCoverageState.Loading) {
      tierRepo.quoteListTurbine.add(listOf(testQuote, testQuote2, currentQuote))
      skipItems(1)
      val state = awaitItem()
      assertThat(state).isInstanceOf(SelectCoverageState.Success::class)
        .prop(SelectCoverageState.Success::uiState)
        .isInstanceOf(SelectCoverageSuccessUiState::class.java)
        .prop(SelectCoverageSuccessUiState::chosenQuote)
        .isEqualTo(currentQuote)
      assertThat(state).isInstanceOf(SelectCoverageState.Success::class)
        .prop(SelectCoverageState.Success::uiState)
        .isInstanceOf(SelectCoverageSuccessUiState::class.java)
        .prop(SelectCoverageSuccessUiState::chosenTier)
        .isEqualTo(currentQuote.tier)
    }
  }

  @Test
  fun `if it is current quote that is selected continue button should be disabled`() = runTest {
    val getCurrentContractDataUseCase = FakeGetCurrentContractDataUseCase()
    val tierRepo = FakeChangeTierRepository()
    val presenter = SelectCoveragePresenter(
      params = params,
      tierRepository = tierRepo,
      getCurrentContractDataUseCase = getCurrentContractDataUseCase,
    )
    presenter.test(SelectCoverageState.Loading) {
      tierRepo.quoteListTurbine.add(listOf(testQuote, testQuote2, testQuote3, currentQuote))
      skipItems(1)
      val state = awaitItem()
      assertThat(state).isInstanceOf(SelectCoverageState.Success::class)
        .prop(SelectCoverageState.Success::uiState)
        .isInstanceOf(SelectCoverageSuccessUiState::class.java)
        .prop(SelectCoverageSuccessUiState::isCurrentChosen)
        .isEqualTo(true)
      sendEvent(
        SelectCoverageEvent.ChangeTierInDialog(
          standardTier,
        ),
      )
      sendEvent(SelectCoverageEvent.ChangeTier)
      sendEvent(
        SelectCoverageEvent.ChangeTierInDialog(basTier),
      )
      sendEvent(SelectCoverageEvent.ChangeTier)
      skipItems(3)
      val state2 = awaitItem()
      val isCurrentChosen = (state2 as SelectCoverageState.Success).uiState.isCurrentChosen
      val chosenQuoteIsNotNull = state2.uiState.chosenQuote != null
      assertThat(chosenQuoteIsNotNull && !isCurrentChosen).isEqualTo(false)
    }
  }

  @Test
  fun `if the selected quote is not null and not current continue button should be enabled`() = runTest {
    val getCurrentContractDataUseCase = FakeGetCurrentContractDataUseCase()
    val tierRepo = FakeChangeTierRepository()
    val presenter = SelectCoveragePresenter(
      params = params,
      tierRepository = tierRepo,
      getCurrentContractDataUseCase = getCurrentContractDataUseCase,
    )
    presenter.test(SelectCoverageState.Loading) {
      tierRepo.quoteListTurbine.add(listOf(testQuote, testQuote2, testQuote3, currentQuote))
      skipItems(1)
      val state = awaitItem()
      assertThat(state).isInstanceOf(SelectCoverageState.Success::class)
        .prop(SelectCoverageState.Success::uiState)
        .isInstanceOf(SelectCoverageSuccessUiState::class.java)
        .prop(SelectCoverageSuccessUiState::isCurrentChosen)
        .isEqualTo(true)
      sendEvent(
        SelectCoverageEvent.ChangeTierInDialog(standardTier),
      )
      skipItems(1)
      sendEvent(SelectCoverageEvent.ChangeTier)
      val state2 = awaitItem()
      assertThat(state2).isInstanceOf(SelectCoverageState.Success::class)
        .prop(SelectCoverageState.Success::uiState)
        .isInstanceOf(SelectCoverageSuccessUiState::class.java)
        .prop(SelectCoverageSuccessUiState::isCurrentChosen)
        .isEqualTo(false)
    }
  }

  @Test
  fun `when going to comparison one quote of each Tier is sent as parameter`() = runTest {
    val getCurrentContractDataUseCase = FakeGetCurrentContractDataUseCase()
    val tierRepo = FakeChangeTierRepository()
    val presenter = SelectCoveragePresenter(
      params = params,
      tierRepository = tierRepo,
      getCurrentContractDataUseCase = getCurrentContractDataUseCase,
    )
    presenter.test(SelectCoverageState.Loading) {
      tierRepo.quoteListTurbine.add(listOf(testQuote, testQuote2, testQuote3, currentQuote))
      skipItems(2)
      sendEvent(
        SelectCoverageEvent.LaunchComparison,
      )
      val state = awaitItem()
      assertThat(state).isInstanceOf(SelectCoverageState.Success::class)
        .prop(SelectCoverageState.Success::uiState)
        .prop(SelectCoverageSuccessUiState::quotesToCompare)
        .transform {
          it?.map { quote ->
            quote.tier.tierName
          }
        }
        .isEqualTo(listOf("STANDARD", "BAS"))
    }
  }

  @Test
  fun `when confirming tier change in dialog show correct selection of quotes in the deductible dropdown`() = runTest {
    val getCurrentContractDataUseCase = FakeGetCurrentContractDataUseCase()
    val tierRepo = FakeChangeTierRepository()
    val presenter = SelectCoveragePresenter(
      params = params,
      tierRepository = tierRepo,
      getCurrentContractDataUseCase = getCurrentContractDataUseCase,
    )
    presenter.test(SelectCoverageState.Loading) {
      tierRepo.quoteListTurbine.add(listOf(testQuote, testQuote2, currentQuote))
      skipItems(2)

      // if you change Tier in dialog but don't press confirm, the selection of
      // quotes for deductible dropdown don't change:
      sendEvent(
        SelectCoverageEvent.ChangeTierInDialog(
          standardTier,
        ),
      )
      val state = awaitItem()
      assertThat(state).isInstanceOf(SelectCoverageState.Success::class)
        .prop(SelectCoverageState.Success::uiState)
        .isInstanceOf(SelectCoverageSuccessUiState::class.java)
        .prop(SelectCoverageSuccessUiState::quotesForChosenTier)
        .isEqualTo(listOf(currentQuote))

      // pressing Confirm button should successfully change
      // the selection of quotes for deductible dropdown:
      sendEvent(SelectCoverageEvent.ChangeTier)
      val state2 = awaitItem()
      assertThat(state2).isInstanceOf(SelectCoverageState.Success::class)
        .prop(SelectCoverageState.Success::uiState)
        .isInstanceOf(SelectCoverageSuccessUiState::class.java)
        .prop(SelectCoverageSuccessUiState::quotesForChosenTier)
        .isEqualTo(listOf(testQuote, testQuote2))
    }
  }

  @Test
  fun `if there is no such deductible after you changed the tier the should be no chosen deductible`() = runTest {
    val getCurrentContractDataUseCase = FakeGetCurrentContractDataUseCase()
    val tierRepo = FakeChangeTierRepository()
    val presenter = SelectCoveragePresenter(
      params = params,
      tierRepository = tierRepo,
      getCurrentContractDataUseCase = getCurrentContractDataUseCase,
    )
    presenter.test(SelectCoverageState.Loading) {
      tierRepo.quoteListTurbine.add(listOf(testQuote, testQuote2, currentQuote))
      sendEvent(
        SelectCoverageEvent.ChangeTierInDialog(
          standardTier,
        ),
      )
      skipItems(3)
      sendEvent(SelectCoverageEvent.ChangeTier)
      val state2 = awaitItem()
      assertThat(state2).isInstanceOf(SelectCoverageState.Success::class)
        .prop(SelectCoverageState.Success::uiState)
        .isInstanceOf(SelectCoverageSuccessUiState::class.java)
        .prop(SelectCoverageSuccessUiState::chosenQuote)
        .isNull()
    }
  }
}

private class FakeGetCurrentContractDataUseCase() : GetCurrentContractDataUseCase {
  override suspend fun invoke(insuranceId: String): Either<ErrorMessage, CurrentContractData> {
    return either { CurrentContractData("exposure name") }
  }
}

private val params = InsuranceCustomizationParameters(
  activationDate = LocalDate(2025, 9, 11),
  insuranceId = "testId",
  quoteIds = listOf("id0", "id1", CURRENT_ID),
)
