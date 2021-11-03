package com.hedvig.app.feature.home.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.app.data.debit.PayinStatusRepository
import com.hedvig.app.feature.home.data.HomeRepository
import com.zhuinden.livedatacombinetuplekt.combineTuple
import e
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class HomeViewModel : ViewModel() {
    sealed class ViewState {
        data class Success(
            val homeData: HomeQuery.Data,
        ) : ViewState()

        object Loading : ViewState()
        object Error : ViewState()
    }

    protected val _homeData = MutableLiveData<ViewState>()
    protected val _payinStatusData = MutableLiveData<PayinStatusQuery.Data>()

    // TODO Fetch address change in progress
    protected val _addressChangeInProgress = MutableLiveData("")

    val data: LiveData<Triple<ViewState?, PayinStatusQuery.Data?, String?>> = combineTuple(
        _homeData,
        _payinStatusData,
        _addressChangeInProgress
    ).distinctUntilChanged()

    abstract fun load()
}

class HomeViewModelImpl(
    private val homeRepository: HomeRepository,
    private val payinStatusRepository: PayinStatusRepository,
) : HomeViewModel() {
    init {
        payinStatusRepository
            .payinStatus()
            .onEach { response ->
                response.data?.let { _payinStatusData.postValue(it) }
            }
            .catch { err ->
                e(err)
            }.launchIn(viewModelScope)
    }

    override fun load() {
        viewModelScope.launch {
            _homeData.value = ViewState.Loading
            runCatching {
                val response = homeRepository.reloadHome()
                response.errors?.let {
                    _homeData.postValue(ViewState.Error)
                    return@runCatching
                }
                response.data?.let { data ->
                    _homeData.postValue(ViewState.Success(data))
                }
            }
            runCatching { payinStatusRepository.refreshPayinStatus() }
        }
    }
}
