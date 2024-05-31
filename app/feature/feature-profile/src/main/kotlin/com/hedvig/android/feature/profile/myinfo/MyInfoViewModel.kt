package com.hedvig.android.feature.profile.myinfo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.Either
import com.hedvig.android.core.common.android.validation.ValidationResult
import com.hedvig.android.core.common.android.validation.validateEmail
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.feature.profile.data.ProfileRepository
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import hedvig.resources.R

internal class MyInfoViewModel(
  profileRepositoryProvider: Provider<ProfileRepository>,
) : MoleculeViewModel<MyInfoEvent, MyInfoUiState>(
    presenter = MyInfoPresenter(profileRepositoryProvider),
    initialState = MyInfoUiState.Loading,
  )

internal class MyInfoPresenter(
  private val profileRepositoryProvider: Provider<ProfileRepository>,
) : MoleculePresenter<MyInfoEvent, MyInfoUiState> {
  @Composable
  override fun MoleculePresenterScope<MyInfoEvent>.present(lastState: MyInfoUiState): MyInfoUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var updateLoadIteration by remember { mutableIntStateOf(0) }
    var dataLoadIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        MyInfoEvent.Reload -> dataLoadIteration++

        is MyInfoEvent.EmailChanged -> {
          val state = currentState as? MyInfoUiState.Success ?: return@CollectEvents
          currentState = state.copy(
            member = state.member.copy(email = ValidatedInput(event.email)),
            canSubmit = true,
          )
        }

        is MyInfoEvent.PhoneNumberChanged -> {
          val state = currentState as? MyInfoUiState.Success ?: return@CollectEvents
          currentState = state.copy(
            member = state.member.copy(phoneNumber = ValidatedInput(event.phoneNumber)),
            canSubmit = true,
          )
        }

        MyInfoEvent.UpdateEmailAndPhoneNumber -> updateLoadIteration++
      }
    }

    LaunchedEffect(dataLoadIteration) {
      profileRepositoryProvider.provide().profile().fold(
        ifLeft = {
          currentState = MyInfoUiState.Error
        },
        ifRight = { profile ->
          currentState = MyInfoUiState.Success(
            member = MyInfoMember(
              email = ValidatedInput(profile.member.email),
              phoneNumber = ValidatedInput(profile.member.phoneNumber),
            ),
          )
        },
      )
    }

    LaunchedEffect(updateLoadIteration) {
      if (updateLoadIteration == 0) return@LaunchedEffect
      val successState = currentState as? MyInfoUiState.Success ?: return@LaunchedEffect
      val validatedState = successState.validateInput()
      currentState = validatedState
      if (validatedState.isInputValid) {
        currentState = validatedState.copy(isSubmitting = true)
        Either.zipOrAccumulate(
          profileRepositoryProvider.provide().updatePhoneNumber(validatedState.member.phoneNumber.input ?: ""),
          profileRepositoryProvider.provide().updateEmail(validatedState.member.email.input ?: ""),
        ) { memberWithPhone, memberWithEmail ->
          memberWithPhone.copy(email = memberWithEmail.email)
        }.fold(
          ifLeft = {
            currentState = MyInfoUiState.Error
          },
          ifRight = { member ->
            currentState = MyInfoUiState.Success(
              member = MyInfoMember(
                email = ValidatedInput(member.email),
                phoneNumber = ValidatedInput(member.phoneNumber),
              ),
              isSubmitting = false,
              canSubmit = false,
            )
          },
        )
      }
    }
    return currentState
  }
}

internal sealed interface MyInfoUiState {
  data object Loading : MyInfoUiState

  data object Error : MyInfoUiState

  data class Success(
    val member: MyInfoMember,
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
  ) : MyInfoUiState {
    val isInputValid = member.email.errorMessageRes == null &&
      member.phoneNumber.errorMessageRes == null

    fun validateInput(): Success {
      return copy(
        member = member.copy(
          email = member.email.copy(
            errorMessageRes = if (!member.hasValidEmail()) {
              R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_DESCRIPTION_EMAIL
            } else {
              null
            },
          ),
          phoneNumber = member.phoneNumber.copy(
            errorMessageRes = if (!member.hasValidPhoneNumber()) {
              R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_DESCRIPTION_PHONE_NUMBER
            } else {
              null
            },
          ),
        ),
      )
    }

    private fun MyInfoMember.hasValidEmail() = email.isPresent &&
      email.input?.isBlank() == false &&
      validateEmail(email.input!!).isSuccessful

    private fun MyInfoMember.hasValidPhoneNumber() = phoneNumber.isPresent &&
      phoneNumber.input?.isBlank() == false &&
      validatePhoneNumber(phoneNumber.input!!).isSuccessful
  }
}

data class MyInfoMember(
  val email: ValidatedInput<String?>,
  val phoneNumber: ValidatedInput<String?>,
)

private val phoneNumberRegex = Regex("([+]*[0-9]+[+. -]*)")

private fun validatePhoneNumber(phoneNumber: CharSequence): ValidationResult =
  if (!phoneNumberRegex.matches(phoneNumber)) {
    ValidationResult(false, R.string.PROFILE_MY_INFO_INVALID_PHONE_NUMBER)
  } else {
    ValidationResult(true, null)
  }

internal sealed interface MyInfoEvent {
  data object Reload : MyInfoEvent

  data class PhoneNumberChanged(val phoneNumber: String) : MyInfoEvent

  data class EmailChanged(val email: String) : MyInfoEvent

  data object UpdateEmailAndPhoneNumber : MyInfoEvent
}
