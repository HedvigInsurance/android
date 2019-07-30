package com.hedvig.app.feature.profile.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.net.Uri
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.feature.chat.ChatRepository
import com.hedvig.app.feature.profile.data.ProfileRepository
import com.hedvig.app.service.Referrals
import com.hedvig.app.service.RemoteConfig
import com.hedvig.app.service.RemoteConfigData
import com.hedvig.app.util.LiveEvent
import com.hedvig.app.util.Optional
import com.hedvig.app.util.extensions.default
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.zipWith
import timber.log.Timber

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val referrals: Referrals,
    private val remoteConfig: RemoteConfig,
    private val chatRepository: ChatRepository
) :
    ViewModel() {
    val data: MutableLiveData<ProfileQuery.Data> = MutableLiveData()
    val dirty: MutableLiveData<Boolean> = MutableLiveData<Boolean>().default(false)
    val trustlyUrl: LiveEvent<String> = LiveEvent()
    val firebaseLink: MutableLiveData<Uri> = MutableLiveData()
    val remoteConfigData: MutableLiveData<RemoteConfigData> = MutableLiveData()

    private val disposables = CompositeDisposable()

    init {
        loadProfile()
        loadRemoteConfig()
    }

    fun refreshProfile() =
        profileRepository.refreshProfile()

    private fun loadRemoteConfig() {
        disposables += remoteConfig
            .fetch()
            .subscribe(
                { remoteConfigData.postValue(it) },
                { error ->
                    Timber.e(error, "Failed to fetch RemoteConfig data")
                })
    }

    fun startTrustlySession() {
        disposables += profileRepository
            .startTrustlySession()
            .subscribe({ url ->
                trustlyUrl.postValue(url.startDirectDebitRegistration)
            }, { error ->
                Timber.e(error)
            })
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    fun saveInputs(emailInput: String, phoneNumberInput: String) {
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
                profileRepository.writeEmailAndPhoneNumberInCache(email.getOrNull(), phoneNumber.getOrNull())
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

    fun emailChanged(newEmail: String) {
        val currentEmail = data.value?.member?.email ?: ""
        if (currentEmail != newEmail && dirty.value != true) {
            dirty.value = true
        }
    }

    fun phoneNumberChanged(newPhoneNumber: String) {
        val currentPhoneNumber = data.value?.member?.phoneNumber ?: ""
        if (currentPhoneNumber != newPhoneNumber && dirty.value != true) {
            dirty.value = true
        }
    }

    fun selectCashback(id: String) {
        disposables += profileRepository.selectCashback(id)
            .subscribe({ response ->
                response.data()?.selectCashbackOption?.let { cashback ->
                    profileRepository.writeCashbackToCache(cashback)
                }
            }, { error ->
                Timber.e(error, "Failed to select cashback")
            })
    }

    fun refreshBankAccountInfo() {
        disposables += profileRepository.refreshBankAccountInfo()
            .subscribe({ response ->
                response.data()?.let { data ->
                    data.bankAccount?.let { bankAccount ->
                        profileRepository.writeBankAccountInfoToCache(bankAccount)
                    } ?: Timber.e("Failed to refresh bank account info")
                } ?: Timber.e("Failed to refresh bank account info")
            }, { error ->
                Timber.e(error, "Failed to refresh bank account info")
            })
    }

    fun generateReferralLink(memberId: String) {
        remoteConfigData.value?.let { data ->
            disposables += referrals.generateFirebaseLink(memberId, data)
                .subscribe({ uri ->
                    firebaseLink.postValue(uri)
                }, { error ->
                    Timber.e(error)
                })
        }
    }

    fun triggerFreeTextChat(done: () -> Unit) {
        disposables += chatRepository
            .triggerFreeTextChat()
            .subscribe({ done() }, { Timber.e(it) })
    }

    fun updateReferralsInformation(data: RedeemReferralCodeMutation.Data) {
        profileRepository.writeRedeemedCostToCache(data)
    }
}
