package com.hedvig.app.feature.referrals.ui.tab

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.feature.referrals.data.ReferralsRepository
import com.hedvig.app.util.apollo.QueryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class ReferralsViewModel : ViewModel() {
  sealed class ViewState {
    data class Success(
      val data: ReferralsQuery.Data,
    ) : ViewState()

    object Loading : ViewState()
    object Error : ViewState()
  }

  protected val _data = MutableStateFlow<ViewState>(ViewState.Loading)
  val data = _data.asStateFlow()

  protected val _isRefreshing = MutableLiveData<Boolean>()

  val isRefreshing: LiveData<Boolean> = _isRefreshing

  fun setRefreshing(refreshing: Boolean) {
    _isRefreshing.postValue(refreshing)
  }

  abstract fun load()

  fun retry() {
    load()
  }
}

class ReferralsViewModelImpl(
  private val referralsRepository: ReferralsRepository,
) : ReferralsViewModel() {
  init {
    viewModelScope.launch {
      referralsRepository
        .referrals()
        .collect { response ->
          when (response) {
            is QueryResult.Error -> {
              _data.value = ViewState.Error
            }
            is QueryResult.Success -> {
              _data.value = ViewState.Success(
                data = response.data,
              )
            }
          }
        }
    }
  }

  override fun load() {
    viewModelScope.launch {
      runCatching { referralsRepository.reloadReferrals() }
      _isRefreshing.postValue(false)
    }
  }
}
