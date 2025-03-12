package com.hedvig.android.feature.profile.myinfo

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.raise.zipOrAccumulate
import com.hedvig.android.core.common.android.validation.validateEmail
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.core.demomode.Provider
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
    var myInfoMember by remember { mutableStateOf(lastState.safeCast<MyInfoUiState.Success>()?.member) }
    var hasError by remember { mutableStateOf(lastState is MyInfoUiState.Error) }
    var isLoading by remember { mutableStateOf(lastState is MyInfoUiState.Loading) }

    var updateInfoIteration by remember { mutableIntStateOf(0) }
    var dataLoadIteration by remember { mutableIntStateOf(0) }
    var isSubmittingNewValues by remember { mutableStateOf(false) }

    // The "real" values as saved in the backend. We should not re-send the same values to the backend.
    var originalPhoneNumber by remember { mutableStateOf(lastState.safeCast<MyInfoUiState.Success>()?.member?.email) }
    var originalEmail by remember { mutableStateOf(lastState.safeCast<MyInfoUiState.Success>()?.member?.email) }

    val canSubmit by remember {
      derivedStateOf {
        @Suppress("NAME_SHADOWING")
        val myInfoMember = myInfoMember ?: return@derivedStateOf false
        val hasChangedPhoneNumberOrEmail =
          myInfoMember.phoneNumber != originalPhoneNumber || myInfoMember.email != originalEmail
        !isSubmittingNewValues && hasChangedPhoneNumberOrEmail
      }
    }

    CollectEvents { event ->
      when (event) {
        MyInfoEvent.Reload -> dataLoadIteration++

        is MyInfoEvent.EmailChanged -> {
          val myInfoMemberValue = myInfoMember ?: return@CollectEvents
          myInfoMember = myInfoMemberValue.copy(email = event.email, emailErrorMessage = null)
        }

        is MyInfoEvent.PhoneNumberChanged -> {
          val myInfoMemberValue = myInfoMember ?: return@CollectEvents
          myInfoMember = myInfoMemberValue.copy(phoneNumber = event.phoneNumber, phoneNumberErrorMessage = null)
        }

        MyInfoEvent.UpdateEmailAndPhoneNumber -> updateInfoIteration++
      }
    }

    LaunchedEffect(dataLoadIteration) {
      Snapshot.withMutableSnapshot {
        if (dataLoadIteration > 0 || lastState !is MyInfoUiState.Success) {
          isLoading = true
        }
        hasError = false
      }
      profileRepositoryProvider.provide().profile().fold(
        ifLeft = {
          hasError = true
        },
        ifRight = { profile ->
          Snapshot.withMutableSnapshot {
            originalPhoneNumber = profile.member.phoneNumber
            originalEmail = profile.member.email
            myInfoMember = MyInfoMember(
              email = profile.member.email,
              emailErrorMessage = null,
              phoneNumber = profile.member.phoneNumber,
              phoneNumberErrorMessage = null,
            )
          }
        },
      )
      isLoading = false
    }

    LaunchedEffect(updateInfoIteration) {
      if (updateInfoIteration == 0) return@LaunchedEffect
      val myInfoMemberValue = myInfoMember ?: return@LaunchedEffect
      val validMyInfoMember = myInfoMemberValue.validate().fold(
        ifLeft = { errors ->
          myInfoMember = myInfoMemberValue.copy(
            emailErrorMessage = errors.firstOrNull { it is MyInfoMemberErrors.Email }?.errorMessage,
            phoneNumberErrorMessage = errors.firstOrNull { it is MyInfoMemberErrors.PhoneNumber }?.errorMessage,
          )
          return@LaunchedEffect
        },
        ifRight = { validMyInfoMember ->
          myInfoMember = validMyInfoMember.toMyInfoMember()
          validMyInfoMember
        },
      )
      isSubmittingNewValues = true
      profileRepositoryProvider
        .provide()
        .updatePhoneAndEmail(validMyInfoMember, originalPhoneNumber, originalEmail)
        .fold(
          { hasError = true },
          {
            Snapshot.withMutableSnapshot {
              originalPhoneNumber = it.phoneNumber
              originalEmail = it.email
              myInfoMember = it
            }
          },
        )
      isSubmittingNewValues = false
    }
    val myInfoMemberValue = myInfoMember
    return when {
      isLoading -> MyInfoUiState.Loading
      hasError -> MyInfoUiState.Error
      myInfoMemberValue != null -> MyInfoUiState.Success(
        member = myInfoMemberValue,
        isSubmitting = isSubmittingNewValues,
        canSubmit = canSubmit,
      )

      else -> MyInfoUiState.Error
    }
  }
}

private suspend fun ProfileRepository.updatePhoneAndEmail(
  validMyInfoMember: ValidMyInfoMember,
  originalPhoneNumber: String?,
  originalEmail: String?,
): Either<Unit, MyInfoMember> {
  return either {
    val phoneNumber: String? = if (originalPhoneNumber != validMyInfoMember.phoneNumber) {
      updatePhoneNumber(validMyInfoMember.phoneNumber).mapLeft { Unit }.bind().phoneNumber
    } else {
      validMyInfoMember.phoneNumber
    }
    val email = if (originalEmail != validMyInfoMember.email) {
      updateEmail(validMyInfoMember.email).mapLeft { Unit }.bind().email
    } else {
      validMyInfoMember.email
    }
    validMyInfoMember.toMyInfoMember().copy(phoneNumber = phoneNumber, email = email)
  }
}

internal sealed interface MyInfoEvent {
  data object Reload : MyInfoEvent

  data class PhoneNumberChanged(val phoneNumber: String) : MyInfoEvent

  data class EmailChanged(val email: String) : MyInfoEvent

  data object UpdateEmailAndPhoneNumber : MyInfoEvent
}

internal sealed interface MyInfoUiState {
  data object Loading : MyInfoUiState

  data object Error : MyInfoUiState

  data class Success(
    val member: MyInfoMember,
    val isSubmitting: Boolean,
    val canSubmit: Boolean,
  ) : MyInfoUiState
}

data class MyInfoMember(
  val email: String,
  @StringRes
  val emailErrorMessage: Int?,
  val phoneNumber: String?,
  @StringRes
  val phoneNumberErrorMessage: Int?,
)

private data class ValidMyInfoMember(val email: String, val phoneNumber: String) {
  fun toMyInfoMember(): MyInfoMember = MyInfoMember(email, null, phoneNumber, null)
}

private val phoneNumberRegex = Regex("([+]*[0-9]+[+. -]*)")

sealed interface MyInfoMemberErrors {
  @get:StringRes
  val errorMessage: Int

  data object Email : MyInfoMemberErrors {
    override val errorMessage: Int
      get() = R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_DESCRIPTION_EMAIL
  }

  data object PhoneNumber : MyInfoMemberErrors {
    override val errorMessage: Int
      get() = R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_DESCRIPTION_PHONE_NUMBER
  }
}

private fun MyInfoMember.validate(): Either<NonEmptyList<MyInfoMemberErrors>, ValidMyInfoMember> {
  return either {
    zipOrAccumulate(
      {
        val hasValidEmail = email.isNotBlank() && validateEmail(email).isSuccessful
        if (!hasValidEmail) raise(MyInfoMemberErrors.Email)
        email
      },
      {
        val phoneNumber = phoneNumber ?: raise(MyInfoMemberErrors.PhoneNumber)
        val hasValidPhoneNumber = phoneNumber.isNotBlank() == true && phoneNumberRegex.matches(phoneNumber)
        if (!hasValidPhoneNumber) raise(MyInfoMemberErrors.PhoneNumber)
        phoneNumber
      },
    ) { email: String, phoneNumber: String ->
      ValidMyInfoMember(email, phoneNumber)
    }
  }
}
