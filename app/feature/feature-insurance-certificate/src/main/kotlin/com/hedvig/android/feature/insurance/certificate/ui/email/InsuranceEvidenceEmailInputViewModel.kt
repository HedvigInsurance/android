package com.hedvig.android.feature.insurance.certificate.ui.email

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.android.validation.validateEmail
import com.hedvig.android.feature.insurance.certificate.data.GenerateInsuranceEvidenceUseCase
import com.hedvig.android.feature.insurance.certificate.data.GetInsuranceEvidenceInitialDataUseCase
import com.hedvig.android.feature.insurance.certificate.ui.email.InsuranceEvidenceEmailInputState.Loading
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import hedvig.resources.R

internal class InsuranceEvidenceEmailInputViewModel(
  generateInsuranceEvidenceUseCase: GenerateInsuranceEvidenceUseCase,
  getEmailUseCase: GetInsuranceEvidenceInitialDataUseCase,
) : MoleculeViewModel<InsuranceEvidenceEmailInputEvent, InsuranceEvidenceEmailInputState>(
    initialState = Loading,
    presenter = InsuranceEvidenceEmailInputPresenter(
      generateInsuranceEvidenceUseCase,
      getEmailUseCase,
    ),
  )

internal class InsuranceEvidenceEmailInputPresenter(
  private val generateInsuranceEvidenceUseCase: GenerateInsuranceEvidenceUseCase,
  private val getEmailUseCase: GetInsuranceEvidenceInitialDataUseCase,
) : MoleculePresenter<InsuranceEvidenceEmailInputEvent, InsuranceEvidenceEmailInputState> {
  @Composable
  override fun MoleculePresenterScope<InsuranceEvidenceEmailInputEvent>.present(
    lastState: InsuranceEvidenceEmailInputState,
  ): InsuranceEvidenceEmailInputState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var currentState by remember { mutableStateOf(lastState) }
    var generalErrorMessage by remember { mutableStateOf<Int?>(null) }
    var generateCertificateInputData by remember { mutableStateOf<String?>(null) }

    CollectEvents { event ->
      fun validateInputAndContinue() {
        val successScreenState = currentState as? InsuranceEvidenceEmailInputState.Success ?: return
        val email = successScreenState.email
        if (email != null &&
          validateEmail(email).isSuccessful
        ) {
          generateCertificateInputData = email
          currentState = successScreenState.copy(emailValidationErrorMessage = null)
        } else {
          val invalidEmailErrorMessage = if (successScreenState.email.isNullOrEmpty()) {
            R.string.travel_certificate_email_empty_error
          } else {
            R.string.PROFILE_MY_INFO_INVALID_EMAIL
          }
          currentState = successScreenState.copy(emailValidationErrorMessage = invalidEmailErrorMessage)
        }
      }

      when (event) {
        is InsuranceEvidenceEmailInputEvent.ChangeEmailInput -> {
          val successScreenState = currentState as? InsuranceEvidenceEmailInputState.Success ?: return@CollectEvents
          currentState = successScreenState.copy(
            email = event.email,
            emailValidationErrorMessage = null,
          )
        }
        InsuranceEvidenceEmailInputEvent.ClearNavigation -> {
          val successScreenState = currentState as? InsuranceEvidenceEmailInputState.Success ?: return@CollectEvents
          currentState = successScreenState.copy(fetchedCertificateUrl = null)
        }
        InsuranceEvidenceEmailInputEvent.RetryLoadData -> loadIteration++
        InsuranceEvidenceEmailInputEvent.Submit -> {
          validateInputAndContinue()
        }
      }
    }

    LaunchedEffect(loadIteration) {
      getEmailUseCase.invoke().fold(
        ifLeft = {
          currentState = InsuranceEvidenceEmailInputState.Failure
        },
        ifRight = { email ->
          currentState = InsuranceEvidenceEmailInputState.Success(email)
        },
      )
    }

    LaunchedEffect(generateCertificateInputData) {
      val emailToSubmit = generateCertificateInputData
      if (emailToSubmit == null) return@LaunchedEffect
      val successScreenState = currentState as? InsuranceEvidenceEmailInputState.Success ?: return@LaunchedEffect
      currentState = successScreenState.copy(buttonLoading = true)
      generateInsuranceEvidenceUseCase.invoke(emailToSubmit)
        .fold(
          ifLeft = {
            currentState = successScreenState.copy(
              generatingErrorMessage = R.string.something_went_wrong,
              buttonLoading = false,
            )
          },
          ifRight = { url ->
            currentState = successScreenState.copy(
              generatingErrorMessage = null,
              fetchedCertificateUrl = url,
              buttonLoading = false,
            )
          },
        )
    }

    return currentState
  }
}

internal sealed interface InsuranceEvidenceEmailInputState {
  data object Loading : InsuranceEvidenceEmailInputState

  data object Failure : InsuranceEvidenceEmailInputState

  data class Success(
    val email: String?,
    val emailValidationErrorMessage: Int? = null,
    val buttonLoading: Boolean = false,
    val generatingErrorMessage: Int? = null,
    val fetchedCertificateUrl: String? = null,
  ) : InsuranceEvidenceEmailInputState
}

internal sealed interface InsuranceEvidenceEmailInputEvent {
  data class ChangeEmailInput(val email: String) : InsuranceEvidenceEmailInputEvent

  data object Submit : InsuranceEvidenceEmailInputEvent

  data object RetryLoadData : InsuranceEvidenceEmailInputEvent

  data object ClearNavigation : InsuranceEvidenceEmailInputEvent
}
