package com.hedvig.android.feature.profile.myinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.Either.Companion.zipOrAccumulate
import com.hedvig.android.core.common.android.validation.ValidationResult
import com.hedvig.android.core.common.android.validation.validateEmail
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.feature.profile.data.ProfileRepository
import hedvig.resources.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class MyInfoViewModel(
  private val profileRepositoryProvider: Provider<ProfileRepository>,
) : ViewModel() {
  private val _uiState = MutableStateFlow(MyInfoUiState())
  val uiState: StateFlow<MyInfoUiState> = _uiState

  init {
    viewModelScope.launch {
      profileRepositoryProvider.provide().profile().fold(
        ifLeft = {
          _uiState.update {
            it.copy(
              errorMessage = it.errorMessage,
              isLoading = false,
            )
          }
        },
        ifRight = { profile ->
          _uiState.update {
            it.copy(
              member = MyInfoMember(
                email = ValidatedInput(profile.member.email),
                phoneNumber = ValidatedInput(profile.member.phoneNumber),
              ),
              isLoading = false,
            )
          }
        },
      )
    }
  }

  fun updateEmailAndPhoneNumber() {
    _uiState.update { it.validateInput() }
    if (_uiState.value.isInputValid) {
      viewModelScope.launch {
        _uiState.update { it.copy(isSubmitting = true) }
        Either.zipOrAccumulate(
          profileRepositoryProvider.provide().updatePhoneNumber(_uiState.value.member?.phoneNumber?.input ?: ""),
          profileRepositoryProvider.provide().updateEmail(_uiState.value.member?.email?.input ?: ""),
        ) { memberWithPhone, memberWithEmail ->
          memberWithPhone.copy(email = memberWithEmail.email)
        }.fold(
          ifLeft = {
            _uiState.update {
              it.copy(
                errorMessage = it.errorMessage,
                isSubmitting = false,
                canSubmit = false,
              )
            }
          },
          ifRight = { member ->
            _uiState.update {
              it.copy(
                member = MyInfoMember(
                  email = ValidatedInput(member.email),
                  phoneNumber = ValidatedInput(member.phoneNumber),
                ),
                isSubmitting = false,
                canSubmit = false,
              )
            }
          },
        )
      }
    }
  }

  fun emailChanged(email: String) {
    _uiState.update {
      it.copy(
        member = it.member?.copy(email = ValidatedInput(email)),
        canSubmit = true,
        errorMessage = null,
      )
    }
  }

  fun phoneNumberChanged(phoneNumber: String) {
    _uiState.update {
      it.copy(
        member = it.member?.copy(phoneNumber = ValidatedInput(phoneNumber)),
        canSubmit = true,
        errorMessage = null,
      )
    }
  }

  fun dismissError() {
    _uiState.update { it.copy(errorMessage = null) }
  }
}

data class MyInfoUiState(
  val member: MyInfoMember? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = true,
  val isSubmitting: Boolean = false,
  val canSubmit: Boolean = false,
) {
  val isInputValid = member?.email?.errorMessageRes == null &&
    member?.phoneNumber?.errorMessageRes == null

  fun validateInput(): MyInfoUiState {
    return copy(
      member = member?.copy(
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

data class MyInfoMember(
  val email: ValidatedInput<String?>,
  val phoneNumber: ValidatedInput<String?>,
)

private val phoneNumberRegex = Regex("([+]*[0-9]+[+. -]*)")

fun validatePhoneNumber(phoneNumber: CharSequence): ValidationResult = if (!phoneNumberRegex.matches(phoneNumber)) {
  ValidationResult(false, R.string.PROFILE_MY_INFO_INVALID_PHONE_NUMBER)
} else {
  ValidationResult(true, null)
}
