package com.hedvig.app.feature.profile.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.authenticate.LogoutUseCase
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.profile.data.ProfileRepository
import com.hedvig.app.util.LiveEvent
import com.hedvig.app.util.extensions.default
import e
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ProfileViewModelImpl(
    private val profileRepository: ProfileRepository,
    private val chatRepository: ChatRepository,
    private val logoutUseCase: LogoutUseCase
) : ProfileViewModel() {
    override val dirty: MutableLiveData<Boolean> = MutableLiveData<Boolean>().default(false)
    override val trustlyUrl: LiveEvent<String> = LiveEvent()

    init {
        load()
    }

    override fun saveInputs(emailInput: String, phoneNumberInput: String) {
        var (email, phoneNumber) =
            (data.value as? ViewState.Success)?.data?.member?.let { Pair(it.email, it.phoneNumber) }
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

    override fun load() {
        viewModelScope.launch {
            profileRepository
                .profile()
                .onEach { response ->
                    response.errors?.let {
                        _data.value = ViewState.Error
                        return@onEach
                    }
                    response.data?.let { _data.value = ViewState.Success(it) }
                }
                .catch { exception ->
                    e(exception)
                    _data.value = ViewState.Error
                }
                .launchIn(this)
        }
    }

    override fun emailChanged(newEmail: String) {
        if (currentEmailOrEmpty() != newEmail && dirty.value != true) {
            dirty.value = true
        }
    }

    private fun currentEmailOrEmpty() = (data.value as? ViewState.Success)?.data?.member?.email ?: ""
    private fun currentPhoneNumberOrEmpty() = (data.value as? ViewState.Success)?.data?.member?.phoneNumber ?: ""

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

    override fun triggerFreeTextChat(done: () -> Unit) {
        viewModelScope.launch {
            val response = runCatching {
                chatRepository
                    .triggerFreeTextChat()
            }

            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
            }
            done()
        }
    }

    override fun updateReferralsInformation(data: RedeemReferralCodeMutation.Data) {
        profileRepository.writeRedeemedCostToCache(data)
    }

    override fun onLogout() {
        viewModelScope.launch {
            when (val result = logoutUseCase.logout()) {
                is LogoutUseCase.LogoutResult.Error -> _events.tryEmit(Event.Error(result.message))
                LogoutUseCase.LogoutResult.Success -> _events.tryEmit(Event.Logout)
            }
        }
    }
}
