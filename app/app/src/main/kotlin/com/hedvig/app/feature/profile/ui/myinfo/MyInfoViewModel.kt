package com.hedvig.app.feature.profile.ui.myinfo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.app.feature.profile.data.ProfileRepository
import com.hedvig.app.feature.profile.ui.tab.Member
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import slimber.log.e
import kotlin.time.Duration.Companion.seconds

class MyInfoViewModel(
  private val hAnalytics: HAnalytics,
  private val profileRepository: ProfileRepository,
) : ViewModel() {
  val data: StateFlow<MyInfoUiState> = profileRepository.profile()
    .mapLatest { profileQueryDataResult ->
      profileQueryDataResult.map { profileQueryData ->
        Member.fromDto(profileQueryData.member)
      }
    }
    .mapLatest { memberResult ->
      when (memberResult) {
        is Either.Left -> {
          MyInfoUiState.Error
        }
        is Either.Right -> {
          MyInfoUiState.Success(memberResult.value)
        }
      }
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5.seconds),
      initialValue = MyInfoUiState.Loading,
    )
  val dirty: MutableLiveData<Boolean> = MutableLiveData(false)

  init {
    hAnalytics.screenView(AppScreen.CONTACT_INFO)
  }

  fun saveInputs(
    emailInput: String,
    phoneNumberInput: String,
  ) {
    var (email, phoneNumber) = (data.value as? MyInfoUiState.Success)

      ?.member
      ?.let { Pair(it.email, it.phoneNumber) } ?: Pair(null, null)
    viewModelScope.launch {
      if (email != emailInput) {
        val response = runCatching { profileRepository.updateEmail(emailInput) }
        if (response.isFailure) {
          response.exceptionOrNull()?.let { e(it) { "$it error updating email" } }
          return@launch
        }
        response.getOrNull()?.let {
          email = it.data?.updateEmail?.email
        }
      }

      if (phoneNumber != phoneNumberInput) {
        val response = runCatching {
          profileRepository.updatePhoneNumber(phoneNumberInput)
        }
        if (response.isFailure) {
          response.exceptionOrNull()?.let { e(it) { "$it error updating phone number" } }
          return@launch
        }
        response.getOrNull()?.let {
          phoneNumber = it.data?.updatePhoneNumber?.phoneNumber
        }
      }

      profileRepository.writeEmailAndPhoneNumberInCache(email, phoneNumber)
      dirty.value = false
    }
  }

  private fun currentEmailOrEmpty(): String {
    return (data.value as? MyInfoUiState.Success)?.member?.email ?: ""
  }

  fun emailChanged(email: String) {
    if (currentEmailOrEmpty() != email && dirty.value != true) {
      dirty.value = true
    }
  }

  private fun currentPhoneNumberOrEmpty(): String {
    return (data.value as? MyInfoUiState.Success)?.member?.phoneNumber ?: ""
  }

  fun phoneNumberChanged(newPhoneNumber: String) {
    if (currentPhoneNumberOrEmpty() != newPhoneNumber && dirty.value != true) {
      dirty.value = true
    }
  }
}

sealed interface MyInfoUiState {
  data class Success(val member: Member) : MyInfoUiState
  object Error : MyInfoUiState
  object Loading : MyInfoUiState
}
