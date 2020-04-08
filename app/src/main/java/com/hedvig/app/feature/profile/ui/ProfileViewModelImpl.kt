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
import com.hedvig.app.util.Optional
import com.hedvig.app.util.extensions.default
import e
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.zipWith
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class ProfileViewModelImpl(
    private val profileRepository: ProfileRepository,
    private val chatRepository: ChatRepository,
    private val payinStatusRepository: PayinStatusRepository
) : ProfileViewModel() {
    override val data: MutableLiveData<ProfileQuery.Data> = MutableLiveData()
    override val dirty: MutableLiveData<Boolean> = MutableLiveData<Boolean>().default(false)
    override val trustlyUrl: LiveEvent<String> = LiveEvent()
    override val payinStatus = MutableLiveData<PayinStatusQuery.Data>()

    private val disposables = CompositeDisposable()

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

    override fun refreshProfile() =
        profileRepository.refreshProfile()

    override fun startTrustlySession() {
        disposables += profileRepository
            .startTrustlySession()
            .subscribe({ url ->
                url?.startDirectDebitRegistration?.let { ddUrl ->
                    trustlyUrl.postValue(ddUrl.toString())
                }
            }, { error ->
                Timber.e(error)
            })
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    override fun saveInputs(emailInput: String, phoneNumberInput: String) {
        val email = data.value?.member?.email
        val phoneNumber = data.value?.member?.phoneNumber

        val emailObservable = if (email != emailInput) {
            profileRepository
                .updateEmail(emailInput)
                .map { Optional.Some(it.data()?.updateEmail?.email) }
        } else Observable.just(Optional.None)

        val phoneNumberObservable = if (phoneNumber != phoneNumberInput) {
            profileRepository
                .updatePhoneNumber(phoneNumberInput)
                .map { Optional.Some(it.data()?.updatePhoneNumber?.phoneNumber) }
        } else Observable.just(Optional.None)

        disposables += emailObservable
            .zipWith(phoneNumberObservable) { t1, t2 -> Pair(t1, t2) }
            .subscribe({ (email, phoneNumber) ->
                profileRepository.writeEmailAndPhoneNumberInCache(
                    email.getOrNull(),
                    phoneNumber.getOrNull()
                )
                dirty.postValue(false)
            }, { error ->
                Timber.e(error, "Failed to update email and/or phone number")
            })
    }

    private fun loadProfile() {
        disposables += profileRepository.fetchProfile()
            .subscribe({ response ->
                data.postValue(response)
            }, { error ->
                Timber.e(error, "Failed to load profile data")
            })
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
        disposables += profileRepository.selectCashback(id)
            .subscribe({ response ->
                response.data()?.selectCashbackOption?.let { cashback ->
                    profileRepository.writeCashbackToCache(cashback)
                }
            }, { error ->
                Timber.e(error, "Failed to select cashback")
            })
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
        disposables += chatRepository
            .triggerFreeTextChat()
            .subscribe({ done() }, { Timber.e(it) })
    }

    override fun updateReferralsInformation(data: RedeemReferralCodeMutation.Data) {
        profileRepository.writeRedeemedCostToCache(data)
    }
}
