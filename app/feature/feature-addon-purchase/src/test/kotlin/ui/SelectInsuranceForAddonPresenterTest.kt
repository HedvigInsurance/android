package ui

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.feature.addon.purchase.data.GetInsuranceForTravelAddonUseCase
import com.hedvig.android.feature.addon.purchase.data.InsuranceForAddon
import com.hedvig.android.feature.addon.purchase.ui.selectinsurance.SelectInsuranceForAddonEvent
import com.hedvig.android.feature.addon.purchase.ui.selectinsurance.SelectInsuranceForAddonPresenter
import com.hedvig.android.feature.addon.purchase.ui.selectinsurance.SelectInsuranceForAddonState
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SelectInsuranceForAddonPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  val testId = "test1"
  val testIds = listOf(testId, "test2")
  val emptyIds = listOf<String>()
  val listWithLonelyId = listOf(testId)

  @Test
  fun `if receive error instead of list of insurances show error screen`() = runTest {
    val useCase = FakeGetInsuranceForTravelAddonUseCase()
    val presenter = SelectInsuranceForAddonPresenter(
      getInsuranceForTravelAddonUseCase = useCase,
      ids = testIds,
    )
    presenter.test(SelectInsuranceForAddonState.Loading) {
      skipItems(1)
      useCase.turbine.add(flowOf(ErrorMessage().left()))
      val state = awaitItem()
      assertThat(state).isInstanceOf(SelectInsuranceForAddonState.Failure::class)
    }
  }

  @Test
  fun `if ids are empty show error screen without loading anything`() = runTest {
    val useCase = FakeGetInsuranceForTravelAddonUseCase()
    val presenter = SelectInsuranceForAddonPresenter(
      getInsuranceForTravelAddonUseCase = useCase,
      ids = emptyIds,
    )
    presenter.test(SelectInsuranceForAddonState.Loading) {
      skipItems(1)
      val state = awaitItem()
      assertThat(state).isInstanceOf(SelectInsuranceForAddonState.Failure::class)
    }
  }

  @Test
  fun `if id list have only 1 item navigate further without loading anything`() = runTest {
    val useCase = FakeGetInsuranceForTravelAddonUseCase()
    val presenter = SelectInsuranceForAddonPresenter(
      getInsuranceForTravelAddonUseCase = useCase,
      ids = listWithLonelyId,
    )
    presenter.test(SelectInsuranceForAddonState.Loading) {
      skipItems(1)
      val state = awaitItem()
      assertThat(state).isEqualTo(
        SelectInsuranceForAddonState.Success(
          listOfInsurances = emptyList(),
          insuranceIdToContinue = listWithLonelyId[0],
          currentlySelected = null,
        ),
      )
    }
  }

  @Test
  fun `if receive more than one customisable insurance show list of insurances to choose from`() = runTest {
    val useCase = FakeGetInsuranceForTravelAddonUseCase()
    val presenter = SelectInsuranceForAddonPresenter(
      getInsuranceForTravelAddonUseCase = useCase,
      ids = testIds,
    )
    presenter.test(SelectInsuranceForAddonState.Loading) {
      skipItems(1)
      useCase.turbine.add(flowOf(listOfInsurances.right()))
      val state = awaitItem()
      assertThat(state).isInstanceOf(SelectInsuranceForAddonState.Success::class)
        .prop(SelectInsuranceForAddonState.Success::listOfInsurances).isEqualTo(listOfInsurances)
    }
  }

  @Test
  fun `if receive more than one customisable insurance none is pre-chosen and continue button is disabled`() = runTest {
    val useCase = FakeGetInsuranceForTravelAddonUseCase()
    val presenter = SelectInsuranceForAddonPresenter(
      getInsuranceForTravelAddonUseCase = useCase,
      ids = testIds,
    )
    presenter.test(SelectInsuranceForAddonState.Loading) {
      skipItems(1)
      useCase.turbine.add(flowOf(listOfInsurances.right()))
      val state = awaitItem()
      assertThat(state).isInstanceOf(SelectInsuranceForAddonState.Success::class)
        .prop(SelectInsuranceForAddonState.Success::currentlySelected).isEqualTo(null)
    }
  }

  @Test
  fun `when insurance is chosen enable continue button`() = runTest {
    val useCase = FakeGetInsuranceForTravelAddonUseCase()
    val presenter = SelectInsuranceForAddonPresenter(
      getInsuranceForTravelAddonUseCase = useCase,
      ids = testIds,
    )
    presenter.test(SelectInsuranceForAddonState.Loading) {
      useCase.turbine.add(flowOf(listOfInsurances.right()))
      skipItems(2)
      sendEvent(SelectInsuranceForAddonEvent.SelectInsurance(listOfInsurances[0]))
      assertThat(awaitItem()).isInstanceOf(SelectInsuranceForAddonState.Success::class)
        .prop(SelectInsuranceForAddonState.Success::currentlySelected).isEqualTo(listOfInsurances[0])
    }
  }

  @Test
  fun `on continue navigate further with chosen insurance id`() = runTest {
    val useCase = FakeGetInsuranceForTravelAddonUseCase()
    val presenter = SelectInsuranceForAddonPresenter(
      getInsuranceForTravelAddonUseCase = useCase,
      ids = testIds,
    )
    presenter.test(SelectInsuranceForAddonState.Loading) {
      useCase.turbine.add(flowOf(listOfInsurances.right()))
      sendEvent(SelectInsuranceForAddonEvent.SelectInsurance(listOfInsurances[0]))
      skipItems(3)
      sendEvent(SelectInsuranceForAddonEvent.SubmitSelected(listOfInsurances[0]))
      assertThat(awaitItem()).isInstanceOf(SelectInsuranceForAddonState.Success::class)
        .prop(SelectInsuranceForAddonState.Success::insuranceIdToContinue).isEqualTo(listOfInsurances[0].id)
    }
  }
}

private class FakeGetInsuranceForTravelAddonUseCase() : GetInsuranceForTravelAddonUseCase {
  val turbine = Turbine<Flow<Either<ErrorMessage, List<InsuranceForAddon>>>>()

  override suspend fun invoke(ids: List<String>): Flow<Either<ErrorMessage, List<InsuranceForAddon>>> {
    return turbine.awaitItem()
  }
}

private val listOfInsurances = listOf(
  InsuranceForAddon(
    "id",
    "displayName",
    "ExposureName",
    ContractGroup.RENTAL,
  ),
  InsuranceForAddon(
    "id2",
    "displayName2",
    "ExposureName2",
    ContractGroup.HOMEOWNER,
  ),
)
