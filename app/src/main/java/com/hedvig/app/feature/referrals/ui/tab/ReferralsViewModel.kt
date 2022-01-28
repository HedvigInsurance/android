package com.hedvig.app.feature.referrals.ui.tab

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.feature.referrals.data.ReferralsRepository
import com.hedvig.app.service.RemoteConfig
import e
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class ReferralsViewModel : ViewModel() {
    sealed class ViewState {
        data class Success(
            val showCampaignBar: Boolean,
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
    private val remoteConfig: RemoteConfig,
) : ReferralsViewModel() {
    init {
        viewModelScope.launch {
            val showCampaignBar = shouldShowCampaignBar()

            referralsRepository
                .referrals()
                .onEach { response ->
                    if (response.errors?.isNotEmpty() == true) {
                        _data.value = ViewState.Error
                        return@onEach
                    }
                    response.data?.let {
                        _data.value = ViewState.Success(
                            data = it,
                            showCampaignBar = showCampaignBar
                        )
                    }
                }
                .catch { e ->
                    e(e)
                    _data.value = ViewState.Error
                }
                .launchIn(this)
        }
    }

    private suspend fun shouldShowCampaignBar(): Boolean {
        val remoteConfig = RemoteConfig()
        val data = remoteConfig.fetch()
        return data.campaignVisible
    }

    override fun load() {
        viewModelScope.launch {
            runCatching { referralsRepository.reloadReferrals() }
            _isRefreshing.postValue(false)
        }
    }
}
