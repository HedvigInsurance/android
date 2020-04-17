package com.hedvig.app.feature.profile.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.data.debit.PayinStatusRepository
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.profile.data.ProfileRepository
import com.hedvig.app.util.LiveEvent
import com.hedvig.app.util.extensions.default
import e
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModelImpl(
    private val profileRepository: ProfileRepository,
    private val chatRepository: ChatRepository,
    private val payinStatusRepository: PayinStatusRepository
) : ProfileViewModel() {
    override val data: MutableLiveData<ProfileQuery.Data> = MutableLiveData()
    override val dirty: MutableLiveData<Boolean> = MutableLiveData<Boolean>().default(false)
    override val trustlyUrl: LiveEvent<String> = LiveEvent()
    override val payinStatus = MutableLiveData<PayinStatusQuery.Data>()

    init {
        loadProfile()

        viewModelScope.launch {
            payinStatusRepository
                .payinStatus()
                .onEach { response ->
                    response.data()?.let { payinStatus.postValue(it) }
                }
                .catch { e(it) }
                .collect()
        }
    }

    override fun refreshProfile() {
        viewModelScope.launch {
            profileRepository.refreshProfile()
        }
    }

    override fun startTrustlySession() {
        viewModelScope.launch {
            val response =
                runCatching { profileRepository.startTrustlySessionAsync().await().data() }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.let { data ->
                data.startDirectDebitRegistration?.let { ddUrl ->
                    trustlyUrl.postValue(ddUrl)
                }
            }
        }
    }

    override fun saveInputs(emailInput: String, phoneNumberInput: String) {
        var email = data.value?.member?.email
        var phoneNumber = data.value?.member?.phoneNumber
        viewModelScope.launch {
            if (email != emailInput) {
                val response =
                    runCatching { profileRepository.updateEmailAsync(emailInput).await() }
                if (response.isFailure) {
                    response.exceptionOrNull()?.let { e { "$it error updating email" } }
                    return@launch
                }
                response.getOrNull()?.let {
                    email = it.data()?.updateEmail?.email
                }
            }

            if (phoneNumber != phoneNumberInput) {
                val response = runCatching {
                    profileRepository.updatePhoneNumberAsync(phoneNumberInput).await()
                }
                if (response.isFailure) {
                    response.exceptionOrNull()?.let { e { "$it error updating phone number" } }
                    return@launch
                }
                response.getOrNull()?.let {
                    phoneNumber = it.data()?.updatePhoneNumber?.phoneNumber
                }
            }

            profileRepository.writeEmailAndPhoneNumberInCache(email, phoneNumber)
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val response = runCatching { profileRepository.fetchProfileAsync().await().data() }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e { "$it Failed to load profile data" } }
                return@launch
            }
            data.postValue(response.getOrNull())
        }
    }

    override fun emailChanged(newEmail: String) {
        val currentEmail = data.value?.member?.email ?: ""
        if (currentEmail != newEmail && dirty.value != true) {
            dirty.value = true
        }
    }

    override fun phoneNumberChanged(newPhoneNumber: String) {
        val currentPhoneNumber = data.value?.member?.phoneNumber ?: ""
        if (currentPhoneNumber != newPhoneNumber && dirty.value != true) {
            dirty.value = true
        }
    }

    override fun selectCashback(id: String) {
        viewModelScope.launch {
            profileRepository.selectCashback(id)
                .onEach { response ->
                    response.data()?.selectCashbackOption?.let { cashback ->
                        profileRepository.writeCashbackToCache(cashback)
                    }
                }
                .catch { e { "$it Failed to select cashback" } }
                .launchIn(this)
        }
    }

    override fun refreshBankAccountInfo() {
        viewModelScope.launch {
            withContext(NonCancellable) {
                val result = runCatching {
                    payinStatusRepository.refreshPayinStatus()
                }

                result.exceptionOrNull()?.let { e(it) }

                val payinMethodResult = runCatching {
                    profileRepository.refreshPayinMethod()
                }

                payinMethodResult.exceptionOrNull()?.let { e(it) }
            }
        }
    }

    override fun triggerFreeTextChat(done: () -> Unit) {
        viewModelScope.launch {
            chatRepository
                .triggerFreeTextChat()
                .onEach { done() }
                .catch { e(it) }
                .launchIn(this)
        }
    }

    override fun updateReferralsInformation(data: RedeemReferralCodeMutation.Data) {
        profileRepository.writeRedeemedCostToCache(data)
    }
}
