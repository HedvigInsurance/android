package com.hedvig.app.feature.profile

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.testdata.feature.profile.PROFILE_DATA
import com.hedvig.app.util.LiveEvent

class MockProfileViewModel : ProfileViewModel() {
    override val data = MutableLiveData<Result<ProfileQuery.Data>>()
    override val dirty = MutableLiveData<Boolean>()
    override val trustlyUrl = LiveEvent<String>()

    init {
        load()
    }

    override fun load() {
        if (!shouldError) {
            data.postValue(Result.success(profileData))
        } else {
            data.postValue(Result.failure(Error()))
            shouldError = false
        }
    }

    override fun selectCashback(id: String) = Unit
    override fun triggerFreeTextChat(done: () -> Unit) = Unit
    override fun saveInputs(emailInput: String, phoneNumberInput: String) = Unit
    override fun emailChanged(newEmail: String) = Unit
    override fun phoneNumberChanged(newPhoneNumber: String) = Unit
    override fun updateReferralsInformation(data: RedeemReferralCodeMutation.Data) = Unit
    override fun refreshProfile() = Unit

    companion object {
        var profileData = PROFILE_DATA
        var shouldError = false
    }
}
