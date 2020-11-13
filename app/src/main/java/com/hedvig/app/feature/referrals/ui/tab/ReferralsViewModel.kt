package com.hedvig.app.feature.referrals.ui.tab

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.feature.referrals.data.ReferralsRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class ReferralsViewModel : ViewModel() {
    protected val _data = MutableLiveData<Result<ReferralsQuery.Data>>()
    val data: LiveData<Result<ReferralsQuery.Data>> = _data

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
    private val referralsRepository: ReferralsRepository
) : ReferralsViewModel() {
    init {
        viewModelScope.launch {
            referralsRepository
                .referrals()
                .onEach { response ->
                    if (response.errors?.isNotEmpty() == true) {
                        _data.postValue(Result.failure(Error()))
                        return@onEach
                    }
                    response.data?.let { _data.postValue(Result.success(it)) }
                }
                .catch { e ->
                    _data.postValue(Result.failure(e))
                }
                .launchIn(this)
        }
    }

    override fun load() {
        viewModelScope.launch {
            runCatching { referralsRepository.reloadReferrals() }
            _isRefreshing.postValue(false)
        }
    }
}
