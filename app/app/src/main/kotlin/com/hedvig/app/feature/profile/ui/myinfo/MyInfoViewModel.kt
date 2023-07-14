package com.hedvig.app.feature.profile.ui.myinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.app.feature.profile.data.ProfileRepository
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class MyInfoViewModel(
  hAnalytics: HAnalytics,
  private val profileRepository: ProfileRepository,
) : ViewModel() {
  private val _data = MutableStateFlow(MyInfoUiState())
  val data: StateFlow<MyInfoUiState> = _data

  init {
    hAnalytics.screenView(AppScreen.CONTACT_INFO)

    viewModelScope.launch {
      profileRepository.profile().fold(
        ifLeft = {
          _data.update {
            it.copy(
              errorMessage = it.errorMessage,
              isLoading = false,
            )
          }
        },
        ifRight = { profile ->
          _data.update {
            it.copy(
              member = MyInfoMember(
                email = profile.member.email,
                phoneNumber = profile.member.phoneNumber,
              ),
              isLoading = false,
            )
          }
        },
      )
    }
  }

  fun updateEmailAndPhoneNumber() {
    viewModelScope.launch {
      _data.update { it.copy(isLoading = true) }
      Either.Companion.zipOrAccumulate(
        profileRepository.updatePhoneNumber(_data.value.member?.phoneNumber ?: ""),
        profileRepository.updateEmail(_data.value.member?.email ?: ""),
      ) { memberWithPhone, memberWithEmail ->
        memberWithPhone.copy(email = memberWithEmail.email)
      }.fold(
        ifLeft = {
          _data.update {
            it.copy(
              errorMessage = it.errorMessage,
              isLoading = false,
            )
          }
        },
        ifRight = { member ->
          _data.update {
            it.copy(
              member = MyInfoMember(
                email = member.email,
                phoneNumber = member.phoneNumber,
              ),
              isLoading = false,
            )
          }
        },
      )
    }
  }

  fun emailChanged(email: String) {
    _data.update { it.copy(member = it.member?.copy(email = email)) }
  }

  fun phoneNumberChanged(phoneNumber: String) {
    _data.update { it.copy(member = it.member?.copy(phoneNumber = phoneNumber)) }
  }
}

data class MyInfoUiState(
  val member: MyInfoMember? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = true,
)

data class MyInfoMember(
  val email: String?,
  val phoneNumber: String?,
)
