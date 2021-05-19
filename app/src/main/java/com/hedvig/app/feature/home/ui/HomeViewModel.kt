package com.hedvig.app.feature.home.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
    protected val _homeData = MutableLiveData<Result<HomeQuery.Data>>()
    protected val _payinStatusData = MutableLiveData<PayinStatusQuery.Data>()
    // TODO Fetch address change in progress
    protected val _addressChangeInProgress = MutableLiveData("")
    val data: LiveData<Triple<Result<HomeQuery.Data>?, PayinStatusQuery.Data?, String?>> = combineTuple(
        _homeData,
        _payinStatusData,
        _addressChangeInProgress
    )

    abstract fun load()
}

class HomeViewModelImpl(
    private val homeRepository: HomeRepository,
    private val payinStatusRepository: PayinStatusRepository
) : HomeViewModel() {
    init {
        viewModelScope.launch {
            homeRepository
                .home()
                .onEach { response ->
                    response.errors?.let {
                        _homeData.postValue(Result.failure(Error()))
                        return@onEach
                    }
                    response.data?.let { _homeData.postValue(Result.success(it)) }
                }
                .catch { e ->
                    _homeData.postValue(Result.failure(e))
                }
                .launchIn(this)

            payinStatusRepository
                .payinStatus()
                .onEach { response ->
                    response.data?.let { _payinStatusData.postValue(it) }
                }
                .catch { err ->
                    e(err)
                }.launchIn(this)
        }
    }

    override fun load() {
        viewModelScope.launch {
            runCatching { homeRepository.reloadHome() }
            runCatching { payinStatusRepository.refreshPayinStatus() }
        }
    }
}
