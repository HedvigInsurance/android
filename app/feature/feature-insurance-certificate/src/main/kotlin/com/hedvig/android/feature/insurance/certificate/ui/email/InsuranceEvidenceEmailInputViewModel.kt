package com.hedvig.android.feature.insurance.certificate.ui.email

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.insurance.certificate.data.GenerateInsuranceEvidenceUseCase
import com.hedvig.android.feature.insurance.certificate.data.GetInsuranceEvidenceInitialEmailUseCase
import com.hedvig.android.feature.insurance.certificate.ui.email.InsuranceEvidenceEmailInputState.Loading
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.core.common.android.validation.validateEmail
import hedvig.resources.Res
import hedvig.resources.PROFILE_MY_INFO_INVALID_EMAIL
import hedvig.resources.something_went_wrong
import hedvig.resources.travel_certificate_email_empty_error
import org.jetbrains.compose.resources.StringResource

internal class InsuranceEvidenceEmailInputViewModel(
  generateInsuranceEvidenceUseCase: GenerateInsuranceEvidenceUseCase,
  getEmailUseCase: GetInsuranceEvidenceInitialEmailUseCase,
) : MoleculeViewModel<InsuranceEvidenceEmailInputEvent, InsuranceEvidenceEmailInputState>(
    initialState = Loading,
    presenter = InsuranceEvidenceEmailInputPresenter(
      generateInsuranceEvidenceUseCase,
      getEmailUseCase,
    ),
  )

internal class InsuranceEvidenceEmailInputPresenter(
  private val generateInsuranceEvidenceUseCase: GenerateInsuranceEvidenceUseCase,
  private val getEmailUseCase: GetInsuranceEvidenceInitialEmailUseCase,
) : MoleculePresenter<InsuranceEvidenceEmailInputEvent, InsuranceEvidenceEmailInputState> {
  @Composable
  override fun MoleculePresenterScope<InsuranceEvidenceEmailInputEvent>.present(
    lastState: InsuranceEvidenceEmailInputState,
  ): InsuranceEvidenceEmailInputState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var generateIteration by remember { mutableIntStateOf(0) }
    var currentState by remember { mutableStateOf(lastState) }
    var generateCertificateInputData by remember { mutableStateOf<String?>(null) }

    CollectEvents { event ->
      fun validateInputAndContinue() {
        val successScreenState = currentState as? InsuranceEvidenceEmailInputState.Success ?: return
        val email = successScreenState.email
        if (email != null &&
          validateEmail(email).isSuccessful
        ) {
          generateCertificateInputData = email
          generateIteration++
          currentState = successScreenState.copy(emailValidationErrorMessage = null)
        } else {
          val invalidEmailErrorMessage = if (successScreenState.email.isNullOrEmpty()) {
            Res.string.travel_certificate_email_empty_error
          } else {
            Res.string.PROFILE_MY_INFO_INVALID_EMAIL
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

        InsuranceEvidenceEmailInputEvent.ClearErrorMessage -> {
          val successScreenState = currentState as? InsuranceEvidenceEmailInputState.Success ?: return@CollectEvents
          currentState = successScreenState.copy(
            generatingErrorMessage = null,
          )
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

    LaunchedEffect(generateIteration, generateCertificateInputData) {
      val emailToSubmit = generateCertificateInputData ?: return@LaunchedEffect
      val successScreenState = currentState as? InsuranceEvidenceEmailInputState.Success ?: return@LaunchedEffect
      currentState = successScreenState.copy(buttonLoading = true)
      generateInsuranceEvidenceUseCase.invoke(emailToSubmit)
        .fold(
          ifLeft = {
            currentState = successScreenState.copy(
              generatingErrorMessage = Res.string.something_went_wrong,
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
    val emailValidationErrorMessage: StringResource? = null,
    val buttonLoading: Boolean = false,
    val generatingErrorMessage: StringResource? = null,
    val fetchedCertificateUrl: String? = null,
  ) : InsuranceEvidenceEmailInputState
}

internal sealed interface InsuranceEvidenceEmailInputEvent {
  data class ChangeEmailInput(val email: String) : InsuranceEvidenceEmailInputEvent

  data object Submit : InsuranceEvidenceEmailInputEvent

  data object RetryLoadData : InsuranceEvidenceEmailInputEvent

  data object ClearErrorMessage : InsuranceEvidenceEmailInputEvent

  data object ClearNavigation : InsuranceEvidenceEmailInputEvent
}
