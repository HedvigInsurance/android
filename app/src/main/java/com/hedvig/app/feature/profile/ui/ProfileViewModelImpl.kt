package com.hedvig.app.feature.profile.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.app.authenticate.LogoutUseCase
import com.hedvig.app.feature.profile.data.ObserveProfileUiStateUseCase
import com.hedvig.app.feature.profile.data.ProfileRepository
import com.hedvig.app.util.LiveEvent
import com.hedvig.app.util.coroutines.RetryChannel
import com.hedvig.app.util.extensions.default
import e
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class ProfileViewModelImpl(
    private val profileRepository: ProfileRepository,
    private val observeProfileUiStateUseCase: ObserveProfileUiStateUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ProfileViewModel() {
    override val dirty: MutableLiveData<Boolean> = MutableLiveData<Boolean>().default(false)
    override val trustlyUrl: LiveEvent<String> = LiveEvent()

    private val observeProfileRetryChannel = RetryChannel()
    override val data: StateFlow<ViewState> = observeProfileRetryChannel
        .flatMapLatest {
            observeProfileUiStateUseCase
                .invoke()
                .mapLatest { profileUiStateResult ->
                    when (profileUiStateResult) {
                        is Either.Left -> {
                            ViewState.Error
                        }
                        is Either.Right -> {
                            ViewState.Success(profileUiStateResult.value)
                        }
                    }
                }
                .catch { exception ->
                    e(exception)
                    ViewState.Error
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds),
            initialValue = ViewState.Loading
        )

    override fun saveInputs(emailInput: String, phoneNumberInput: String) {
        var (email, phoneNumber) =
            (data.value as? ViewState.Success)?.profileUiState?.member?.let { Pair(it.email, it.phoneNumber) }
                ?: Pair(null, null)
        viewModelScope.launch {
            if (email != emailInput) {
                val response =
                    runCatching { profileRepository.updateEmail(emailInput) }
                if (response.isFailure) {
                    response.exceptionOrNull()?.let { e { "$it error updating email" } }
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
                    response.exceptionOrNull()?.let { e { "$it error updating phone number" } }
                    return@launch
                }
                response.getOrNull()?.let {
                    phoneNumber = it.data?.updatePhoneNumber?.phoneNumber
                }
            }

            profileRepository.writeEmailAndPhoneNumberInCache(email, phoneNumber)
        }
    }

    override fun reload() {
        observeProfileRetryChannel.retry()
    }

    override fun emailChanged(newEmail: String) {
        if (currentEmailOrEmpty() != newEmail && dirty.value != true) {
            dirty.value = true
        }
    }

    private fun currentEmailOrEmpty() = (data.value as? ViewState.Success)?.profileUiState?.member?.email ?: ""

    private fun currentPhoneNumberOrEmpty(): String {
        return (data.value as? ViewState.Success)?.profileUiState?.member?.phoneNumber ?: ""
    }

    override fun phoneNumberChanged(newPhoneNumber: String) {
        if (currentPhoneNumberOrEmpty() != newPhoneNumber && dirty.value != true) {
            dirty.value = true
        }
    }

    override fun selectCashback(id: String) {
        viewModelScope.launch {
            val response = runCatching { profileRepository.selectCashback(id) }
            response.getOrNull()?.data?.selectCashbackOption?.let { cashback ->
                profileRepository.writeCashbackToCache(cashback)
            }
        }
    }

    override fun onLogout() {
        viewModelScope.launch {
            when (val result = logoutUseCase.logout()) {
                is LogoutUseCase.LogoutResult.Error -> _events.trySend(Event.Error(result.message))
                LogoutUseCase.LogoutResult.Success -> _events.trySend(Event.Logout)
            }
        }
    }
}
