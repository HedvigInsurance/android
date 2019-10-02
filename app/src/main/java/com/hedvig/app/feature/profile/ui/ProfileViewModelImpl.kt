package com.hedvig.app.feature.profile.ui

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.profile.data.ProfileRepository
import com.hedvig.app.util.LiveEvent
import com.hedvig.app.util.Optional
import com.hedvig.app.util.extensions.default
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.zipWith
import timber.log.Timber

class ProfileViewModelImpl(
    private val profileRepository: ProfileRepository,
    private val chatRepository: ChatRepository
) : ProfileViewModel() {
    override val data: MutableLiveData<ProfileQuery.Data> = MutableLiveData()
    override val dirty: MutableLiveData<Boolean> = MutableLiveData<Boolean>().default(false)
    override val trustlyUrl: LiveEvent<String> = LiveEvent()

    private val disposables = CompositeDisposable()

    init {
        loadProfile()
    }

    override fun refreshProfile() =
        profileRepository.refreshProfile()

    override fun startTrustlySession() {
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

    override fun triggerFreeTextChat(done: () -> Unit) {
        disposables += chatRepository
            .triggerFreeTextChat()
            .subscribe({ done() }, { Timber.e(it) })
    }

    override fun updateReferralsInformation(data: RedeemReferralCodeMutation.Data) {
        profileRepository.writeRedeemedCostToCache(data)
    }
}
