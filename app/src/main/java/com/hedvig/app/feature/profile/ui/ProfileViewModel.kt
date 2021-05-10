package com.hedvig.app.feature.profile.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.util.LiveEvent

abstract class ProfileViewModel : ViewModel() {
    abstract val data: LiveData<Result<ProfileQuery.Data>>
    abstract val dirty: MutableLiveData<Boolean>
    abstract val trustlyUrl: LiveEvent<String>

    abstract fun load()
    abstract fun selectCashback(id: String)
    abstract fun triggerFreeTextChat(done: () -> Unit)
    abstract fun saveInputs(emailInput: String, phoneNumberInput: String)
    abstract fun emailChanged(newEmail: String)
    abstract fun phoneNumberChanged(newPhoneNumber: String)
    abstract fun updateReferralsInformation(data: RedeemReferralCodeMutation.Data)
}
