package com.hedvig.app.feature.referrals.ui.editcode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.UpdateReferralCampaignCodeMutation
import com.hedvig.app.feature.referrals.data.ReferralsRepository
import kotlinx.coroutines.launch

abstract class ReferralsEditCodeViewModel : ViewModel() {
    protected val _data = MutableLiveData<Result<UpdateReferralCampaignCodeMutation.Data>>()
    val data: LiveData<Result<UpdateReferralCampaignCodeMutation.Data>> = _data

    protected val _isSubmitting = MutableLiveData<Boolean>()
    val isSubmitting: LiveData<Boolean> = _isSubmitting

    protected val _dirty = MutableLiveData<Boolean>()
    val dirty: LiveData<Boolean> = _dirty

    abstract fun changeCode(newCode: String)
    fun setIsDirty() {
        if (dirty.value != true) {
            _dirty.value = true
        }
    }
}

class ReferralsEditCodeViewModelImpl(
    private val referralsRepository: ReferralsRepository
) : ReferralsEditCodeViewModel() {
    override fun changeCode(newCode: String) {
        viewModelScope.launch {
            _isSubmitting.postValue(true)
            val response = runCatching { referralsRepository.updateCode(newCode) }
            _isSubmitting.postValue(false)

            if (response.isFailure) {
                response.exceptionOrNull()?.let { _data.postValue(Result.failure(it)) }
                return@launch
            }

            if (response.getOrNull()?.hasErrors() == true) {
                _data.postValue(Result.failure(Error()))
                return@launch
            }

            val data = response.getOrNull()?.data ?: return@launch

            _data.postValue(Result.success(data))
        }
    }
}
