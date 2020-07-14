package com.hedvig.app.feature.referrals.ui.editcode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.UpdateReferralCampaignCodeMutation
import com.hedvig.app.feature.referrals.ReferralsRepository
import kotlinx.coroutines.launch

abstract class ReferralsEditCodeViewModel : ViewModel() {
    protected val _data = MutableLiveData<UpdateReferralCampaignCodeMutation.Data>()

    val data: LiveData<UpdateReferralCampaignCodeMutation.Data>
        get() = _data

    abstract fun changeCode(newCode: String)
}

class ReferralsEditCodeViewModelImpl(
    private val referralsRepository: ReferralsRepository
) : ReferralsEditCodeViewModel() {
    override fun changeCode(newCode: String) {
        viewModelScope.launch {
            val response = runCatching { referralsRepository.updateCode(newCode) }

            if (response.isFailure) {
                // TODO In another PR: Implement the generic error state
            }

            _data.postValue(response.getOrNull()?.data)
        }
    }
}
