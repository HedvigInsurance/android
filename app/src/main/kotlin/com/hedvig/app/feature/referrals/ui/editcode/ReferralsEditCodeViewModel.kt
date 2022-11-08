package com.hedvig.app.feature.referrals.ui.editcode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.apollo.graphql.UpdateReferralCampaignCodeMutation
import com.hedvig.app.feature.referrals.data.ReferralsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class ReferralsEditCodeViewModel : ViewModel() {
  sealed class ViewState {
    data class Success(val data: UpdateReferralCampaignCodeMutation.Data) : ViewState()
    object NotSubmitted : ViewState()
    object Error : ViewState()
  }

  protected val _data = MutableStateFlow<ViewState>(ViewState.NotSubmitted)
  val data = _data.asStateFlow()

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
  private val referralsRepository: ReferralsRepository,
) : ReferralsEditCodeViewModel() {
  override fun changeCode(newCode: String) {
    viewModelScope.launch {
      _isSubmitting.postValue(true)
      val response = runCatching { referralsRepository.updateCode(newCode) }
      _isSubmitting.postValue(false)

      if (response.isFailure) {
        response.exceptionOrNull()?.let { _data.value = ViewState.Error }
        return@launch
      }

      if (response.getOrNull()?.hasErrors() == true) {
        _data.value = ViewState.Error
        return@launch
      }

      val data = response.getOrNull()?.data ?: return@launch

      _data.value = ViewState.Success(data)
    }
  }
}
