package ui.chooseinsurance

import FakeChangeTierRepository
import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleIntent
import com.hedvig.android.data.contract.ContractGroup.HOMEOWNER
import com.hedvig.android.data.contract.ContractGroup.RENTAL
import com.hedvig.android.feature.change.tier.data.CustomisableInsurance
import com.hedvig.android.feature.change.tier.data.GetCustomizableInsurancesUseCase
import com.hedvig.android.feature.change.tier.ui.chooseinsurance.ChooseInsurancePresenter
import com.hedvig.android.feature.change.tier.ui.chooseinsurance.ChooseInsuranceToCustomizeEvent
import com.hedvig.android.feature.change.tier.ui.chooseinsurance.ChooseInsuranceUiState
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import testQuote

class ChooseInsurancePresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `if receive error instead of list of insurances show error screen`() = runTest {
    val tierRepo = FakeChangeTierRepository()
    val useCase = FakeGetCustomizableInsurancesUseCase()
    val presenter = ChooseInsurancePresenter(
      tierRepository = tierRepo,
      getCustomizableInsurancesUseCase = useCase,
    )
    presenter.test(ChooseInsuranceUiState.Loading()) {
      skipItems(1)
      useCase.turbine.add(flowOf(ErrorMessage().left()))
      val state = awaitItem()
      assertThat(state).isInstanceOf(ChooseInsuranceUiState.Failure::class)
    }
  }

  @Test
  fun `if receive null instead of list of insurances show error screen`() = runTest {
    val tierRepo = FakeChangeTierRepository()
    val useCase = FakeGetCustomizableInsurancesUseCase()
    val presenter = ChooseInsurancePresenter(
      tierRepository = tierRepo,
      getCustomizableInsurancesUseCase = useCase,
    )
    presenter.test(ChooseInsuranceUiState.Loading()) {
      skipItems(1)
      useCase.turbine.add(flowOf(null.right()))
      val state = awaitItem()
      assertThat(state).isInstanceOf(ChooseInsuranceUiState.Failure::class)
    }
  }

  @Test
  fun `if receive only one customisable insurance try to fetch intent for it directly and if success navigate further`() =
    runTest {
      val tierRepo = FakeChangeTierRepository()
      val useCase = FakeGetCustomizableInsurancesUseCase()
      val presenter = ChooseInsurancePresenter(
        tierRepository = tierRepo,
        getCustomizableInsurancesUseCase = useCase,
      )
      presenter.test(ChooseInsuranceUiState.Loading()) {
        assertThat(awaitItem()).isInstanceOf(ChooseInsuranceUiState.Loading::class)
          .prop(ChooseInsuranceUiState.Loading::paramsToNavigateToNextStep)
          .isNull()
        useCase.turbine.add(
          flowOf(
            nonEmptyListOf(
              CustomisableInsurance(
                "id",
                "displayName",
                "ExposureName",
                RENTAL,
              ),
            ).right(),
          ),
        )
        tierRepo.changeTierIntentTurbine.add(
          ChangeTierDeductibleIntent(
            LocalDate(2024, 11, 15),
            listOf(testQuote),
          ).right(),
        )
        assertThat(awaitItem()).isInstanceOf(ChooseInsuranceUiState.Loading::class)
          .prop(ChooseInsuranceUiState.Loading::paramsToNavigateToNextStep)
          .isNotNull()
      }
    }

  @Test
  fun `if receive only one customisable insurance try to fetch intent for it directly and if it has empty quotes show empty quotes screen `() =
    runTest {
      val tierRepo = FakeChangeTierRepository()
      val useCase = FakeGetCustomizableInsurancesUseCase()
      val presenter = ChooseInsurancePresenter(
        tierRepository = tierRepo,
        getCustomizableInsurancesUseCase = useCase,
      )
      presenter.test(ChooseInsuranceUiState.Loading()) {
        assertThat(awaitItem()).isInstanceOf(ChooseInsuranceUiState.Loading::class)
          .prop(ChooseInsuranceUiState.Loading::paramsToNavigateToNextStep)
          .isNull()
        useCase.turbine.add(
          flowOf(
            nonEmptyListOf(
              CustomisableInsurance(
                "id",
                "displayName",
                "ExposureName",
                RENTAL,
              ),
            ).right(),
          ),
        )
        tierRepo.changeTierIntentTurbine.add(ChangeTierDeductibleIntent(LocalDate(2024, 11, 15), listOf()).right())
        assertThat(awaitItem()).isInstanceOf(ChooseInsuranceUiState.NotAllowed::class)
      }
    }

  @Test
  fun `if receive only one customisable insurance try to fetch intent for it directly and if it fails show failure`() =
    runTest {
      val tierRepo = FakeChangeTierRepository()
      val useCase = FakeGetCustomizableInsurancesUseCase()
      val presenter = ChooseInsurancePresenter(
        tierRepository = tierRepo,
        getCustomizableInsurancesUseCase = useCase,
      )
      presenter.test(ChooseInsuranceUiState.Loading()) {
        assertThat(awaitItem()).isInstanceOf(ChooseInsuranceUiState.Loading::class)
          .prop(ChooseInsuranceUiState.Loading::paramsToNavigateToNextStep)
          .isNull()
        useCase.turbine.add(
          flowOf(
            nonEmptyListOf(
              CustomisableInsurance(
                "id",
                "displayName",
                "ExposureName",
                RENTAL,
              ),
            ).right(),
          ),
        )
        tierRepo.changeTierIntentTurbine.add(ErrorMessage().left())
        assertThat(awaitItem()).isInstanceOf(ChooseInsuranceUiState.Failure::class)
      }
    }

  @Test
  fun `if receive more than one customisable insurance show list of insurances to choose from`() = runTest {
    val tierRepo = FakeChangeTierRepository()
    val useCase = FakeGetCustomizableInsurancesUseCase()
    val presenter = ChooseInsurancePresenter(
      tierRepository = tierRepo,
      getCustomizableInsurancesUseCase = useCase,
    )
    presenter.test(ChooseInsuranceUiState.Loading()) {
      assertThat(awaitItem()).isInstanceOf(ChooseInsuranceUiState.Loading::class)
        .prop(ChooseInsuranceUiState.Loading::paramsToNavigateToNextStep)
        .isNull()
      useCase.turbine.add(flowOf(listOfInsurances.right()))
      assertThat(awaitItem()).isInstanceOf(ChooseInsuranceUiState.Success::class)
        .prop(ChooseInsuranceUiState.Success::insuranceList)
        .isEqualTo(listOfInsurances)
    }
  }

  @Test
  fun `if receive more than one customisable insurance none is pre-chosen and continue button is disabled`() = runTest {
    val tierRepo = FakeChangeTierRepository()
    val useCase = FakeGetCustomizableInsurancesUseCase()
    val presenter = ChooseInsurancePresenter(
      tierRepository = tierRepo,
      getCustomizableInsurancesUseCase = useCase,
    )
    presenter.test(ChooseInsuranceUiState.Loading()) {
      assertThat(awaitItem()).isInstanceOf(ChooseInsuranceUiState.Loading::class)
        .prop(ChooseInsuranceUiState.Loading::paramsToNavigateToNextStep)
        .isNull()
      useCase.turbine.add(flowOf(listOfInsurances.right()))
      assertThat(awaitItem()).isInstanceOf(ChooseInsuranceUiState.Success::class)
        .prop(ChooseInsuranceUiState.Success::selectedInsurance)
        .isNull()
    }
  }

  @Test
  fun `when insurance is chosen enable continue button`() = runTest {
    val tierRepo = FakeChangeTierRepository()
    val useCase = FakeGetCustomizableInsurancesUseCase()
    val presenter = ChooseInsurancePresenter(
      tierRepository = tierRepo,
      getCustomizableInsurancesUseCase = useCase,
    )
    presenter.test(ChooseInsuranceUiState.Loading()) {
      assertThat(awaitItem()).isInstanceOf(ChooseInsuranceUiState.Loading::class)
        .prop(ChooseInsuranceUiState.Loading::paramsToNavigateToNextStep)
        .isNull()
      useCase.turbine.add(flowOf(listOfInsurances.right()))
      skipItems(1)
      sendEvent(ChooseInsuranceToCustomizeEvent.SelectInsurance(listOfInsurances[0].id))
      val state = awaitItem()
      assertThat(state).isInstanceOf(ChooseInsuranceUiState.Success::class)
        .prop(ChooseInsuranceUiState.Success::selectedInsurance)
        .isEqualTo(listOfInsurances[0])
    }
  }

  @Test
  fun `when insurance is chosen on continue try to fetch intent and then navigate further if success`() = runTest {
    val tierRepo = FakeChangeTierRepository()
    val useCase = FakeGetCustomizableInsurancesUseCase()
    val presenter = ChooseInsurancePresenter(
      tierRepository = tierRepo,
      getCustomizableInsurancesUseCase = useCase,
    )
    presenter.test(ChooseInsuranceUiState.Loading()) {
      assertThat(awaitItem()).isInstanceOf(ChooseInsuranceUiState.Loading::class)
        .prop(ChooseInsuranceUiState.Loading::paramsToNavigateToNextStep)
        .isNull()
      useCase.turbine.add(flowOf(listOfInsurances.right()))
      sendEvent(ChooseInsuranceToCustomizeEvent.SelectInsurance(listOfInsurances[0].id))
      sendEvent(ChooseInsuranceToCustomizeEvent.SubmitSelectedInsuranceToCustomize(listOfInsurances[0]))
      skipItems(3)
      tierRepo.changeTierIntentTurbine.add(
        ChangeTierDeductibleIntent(
          LocalDate(2024, 11, 15),
          listOf(testQuote),
        ).right(),
      )
      assertThat(awaitItem()).isInstanceOf(ChooseInsuranceUiState.Loading::class)
        .prop(ChooseInsuranceUiState.Loading::paramsToNavigateToNextStep)
        .isNotNull()
    }
  }
}

private class FakeGetCustomizableInsurancesUseCase() : GetCustomizableInsurancesUseCase {
  val turbine = Turbine<Flow<Either<ErrorMessage, NonEmptyList<CustomisableInsurance>?>>>()

  override suspend fun invoke(): Flow<Either<ErrorMessage, NonEmptyList<CustomisableInsurance>?>> {
    return turbine.awaitItem()
  }
}

private val listOfInsurances = nonEmptyListOf(
  CustomisableInsurance(
    "id",
    "displayName",
    "ExposureName",
    RENTAL,
  ),
  CustomisableInsurance(
    "id2",
    "displayName2",
    "ExposureName2",
    HOMEOWNER,
  ),
)
