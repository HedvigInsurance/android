package com.hedvig.android.feature.profile.eurobonus

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.feature.profile.data.GetEurobonusDataUseCase
import com.hedvig.android.feature.profile.data.UpdateEurobonusNumberUseCase
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import octopus.EurobonusDataQuery
import octopus.UpdateEurobonusNumberMutation
import org.junit.Test

class EurobonusPresenterTest {
  private val currentMember = EurobonusDataQuery.Data.CurrentMember(
    "name",
    id = "id",
    partnerData = EurobonusDataQuery.Data.CurrentMember.PartnerData(
      EurobonusDataQuery.Data.CurrentMember.PartnerData.Sas("BA1234556", true),
    ),
  )

  @Test
  fun `initially received eurobonus number is shown and save button is inactive`() = runTest {
    val getEurobonusDataUseCase = FakeGetEurobonusDataUseCase()
    val updateEurobonusNumberUseCase = FakeUpdateEurobonusNumberUseCase()
    val presenter = EurobonusPresenter(getEurobonusDataUseCase, updateEurobonusNumberUseCase)
    presenter.test(
      EurobonusUiState(
        canSubmit = false,
        isLoading = true,
        hasError = false,
      ),
    ) {
      assertThat(awaitItem()).isEqualTo(
        EurobonusUiState(
          canSubmit = false,
          isLoading = true,
          hasError = false,
          isEligibleForEurobonus = true,
        ),
      )
      getEurobonusDataUseCase.responseTurbine.add(
        EurobonusDataQuery.Data(currentMember = currentMember).right(),
      )
      assertThat(awaitItem()).isEqualTo(
        EurobonusUiState(
          canSubmit = false,
          isLoading = false,
          hasError = false,
          isEligibleForEurobonus = true,
          isSubmitting = false,
          eurobonusNumber = currentMember.partnerData!!.sas!!.eurobonusNumber!!,
        ),
      )
    }
  }

  @Test
  fun `if state has error can edit, get error cleared and submit new value`() = runTest {
    val getEurobonusDataUseCase = FakeGetEurobonusDataUseCase()
    val updateEurobonusNumberUseCase = FakeUpdateEurobonusNumberUseCase()
    val presenter = EurobonusPresenter(getEurobonusDataUseCase, updateEurobonusNumberUseCase)
    presenter.test(
      EurobonusUiState(
        canSubmit = false,
        isLoading = true,
        hasError = false,
      ),
    ) {
      awaitItem()
      getEurobonusDataUseCase.responseTurbine.add(ApolloOperationError.OperationError.Other("msg").left())
      assertThat(awaitItem()).isEqualTo(
        EurobonusUiState(
          canSubmit = false,
          isLoading = false,
          hasError = true,
          isEligibleForEurobonus = true,
          isSubmitting = false,
        ),
      )
      sendEvent(EurobonusEvent.UpdateEurobonusValue("new number"))
      assertThat(awaitItem()).isEqualTo(
        EurobonusUiState(
          canSubmit = true,
          isLoading = false,
          hasError = false,
          isEligibleForEurobonus = true,
          isSubmitting = false,
          eurobonusNumber = "new number",
        ),
      )
    }
  }

  @Test
  fun `if new value is submitted, save button shows loading`() = runTest {
    val getEurobonusDataUseCase = FakeGetEurobonusDataUseCase()
    val updateEurobonusNumberUseCase = FakeUpdateEurobonusNumberUseCase()
    val presenter = EurobonusPresenter(getEurobonusDataUseCase, updateEurobonusNumberUseCase)
    presenter.test(
      EurobonusUiState(
        canSubmit = false,
        isLoading = true,
        hasError = false,
      ),
    ) {
      awaitItem()
      getEurobonusDataUseCase.responseTurbine.add(
        EurobonusDataQuery.Data(currentMember = currentMember).right(),
      )
      awaitItem()
      sendEvent(EurobonusEvent.UpdateEurobonusValue("new number"))
      assertThat(awaitItem()).isEqualTo(
        EurobonusUiState(
          canSubmit = true,
          isLoading = false,
          hasError = false,
          isEligibleForEurobonus = true,
          isSubmitting = false,
          eurobonusNumber = "new number",
        ),
      )
      sendEvent(EurobonusEvent.SubmitEditedEurobonus)
      assertThat(awaitItem().isSubmitting).isEqualTo(true)
    }
  }

  @Test
  fun `if receive error while submitting, show it`() = runTest {
    val getEurobonusDataUseCase = FakeGetEurobonusDataUseCase()
    val updateEurobonusNumberUseCase = FakeUpdateEurobonusNumberUseCase()
    val presenter = EurobonusPresenter(getEurobonusDataUseCase, updateEurobonusNumberUseCase)
    presenter.test(
      EurobonusUiState(
        canSubmit = false,
        isLoading = true,
        hasError = false,
      ),
    ) {
      awaitItem()
      getEurobonusDataUseCase.responseTurbine.add(
        EurobonusDataQuery.Data(currentMember = currentMember).right(),
      )
      assertThat(awaitItem().hasError).isEqualTo(false)
      sendEvent(EurobonusEvent.UpdateEurobonusValue("new number"))
      awaitItem()
      sendEvent(EurobonusEvent.SubmitEditedEurobonus)
      awaitItem()
      updateEurobonusNumberUseCase.responseTurbine.add(ApolloOperationError.OperationError.Other("msg").left())
      assertThat(awaitItem().hasError).isEqualTo(true)
    }
  }

  @Test
  fun `only submit if newly edited value is not identical to the one received from the backend`() = runTest {
    val getEurobonusDataUseCase = FakeGetEurobonusDataUseCase()
    val updateEurobonusNumberUseCase = FakeUpdateEurobonusNumberUseCase()
    val presenter = EurobonusPresenter(getEurobonusDataUseCase, updateEurobonusNumberUseCase)
    presenter.test(
      EurobonusUiState(
        canSubmit = false,
        isLoading = true,
        hasError = false,
      ),
    ) {
      awaitItem()
      getEurobonusDataUseCase.responseTurbine.add(
        EurobonusDataQuery.Data(currentMember = currentMember).right(),
      )
      awaitItem()
      sendEvent(EurobonusEvent.UpdateEurobonusValue("new new"))
      assertThat(awaitItem().canSubmit).isEqualTo(true)
      sendEvent(EurobonusEvent.UpdateEurobonusValue(currentMember.partnerData!!.sas!!.eurobonusNumber!!))
      assertThat(awaitItem().canSubmit).isEqualTo(false)
    }
  }
}

private class FakeUpdateEurobonusNumberUseCase : UpdateEurobonusNumberUseCase {
  val responseTurbine = Turbine<Either<ApolloOperationError, UpdateEurobonusNumberMutation.Data>>()

  override suspend fun invoke(
    newValueToSubmit: String,
  ): Either<ApolloOperationError, UpdateEurobonusNumberMutation.Data> {
    return responseTurbine.awaitItem()
  }
}

private class FakeGetEurobonusDataUseCase : GetEurobonusDataUseCase {
  val responseTurbine = Turbine<Either<ApolloOperationError, EurobonusDataQuery.Data>>()

  override suspend fun invoke(): Either<ApolloOperationError, EurobonusDataQuery.Data> {
    return responseTurbine.awaitItem()
  }
}
