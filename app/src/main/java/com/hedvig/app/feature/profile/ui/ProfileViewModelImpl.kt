package com.hedvig.app.feature.profile.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.profile.data.ProfileRepository
import com.hedvig.app.util.LiveEvent
import com.hedvig.app.util.extensions.default
import com.hedvig.app.util.extensions.setAuthenticationToken
import com.hedvig.app.util.extensions.triggerRestartActivity
import e
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModelImpl(
    private val profileRepository: ProfileRepository,
    private val chatRepository: ChatRepository,
) : ProfileViewModel() {
    override val data = MutableLiveData<Result<ProfileQuery.Data>>()
    override val dirty: MutableLiveData<Boolean> = MutableLiveData<Boolean>().default(false)
    override val trustlyUrl: LiveEvent<String> = LiveEvent()

    init {
        load()
    }

    override fun saveInputs(emailInput: String, phoneNumberInput: String) {
        var email = data.value?.getOrNull()?.member?.email
        var phoneNumber = data.value?.getOrNull()?.member?.phoneNumber
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
                        data.postValue(Result.failure(Error()))
                        return@onEach
                    }
                    response.data?.let { data.postValue(Result.success(it)) }
                }
                .catch { e ->
                    data.postValue(Result.failure(e))
                }
                .launchIn(this)
        }
    }

    override fun emailChanged(newEmail: String) {
        val currentEmail = data.value?.getOrNull()?.member?.email ?: ""
        if (currentEmail != newEmail && dirty.value != true) {
            dirty.value = true
        }
    }

    override fun phoneNumberChanged(newPhoneNumber: String) {
        val currentPhoneNumber = data.value?.getOrNull()?.member?.phoneNumber ?: ""
        if (currentPhoneNumber != newPhoneNumber && dirty.value != true) {
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
}
