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
import dagger.hilt.android.lifecycle.HiltViewModel
import e
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class HomeViewModel : ViewModel() {
    sealed class ViewState {
        data class Success(
            val homeData: HomeQuery.Data
        ) : ViewState()

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
    )

    abstract fun load()
}

@HiltViewModel
class HomeViewModelImpl @Inject constructor(
    private val homeRepository: HomeRepository,
    private val payinStatusRepository: PayinStatusRepository
) : HomeViewModel() {
    init {
        viewModelScope.launch {
            homeRepository
                .home()
                .onEach { response ->
                    response.errors?.let {
                        _homeData.postValue(ViewState.Error)
                        return@onEach
                    }
                    response.data?.let { _homeData.postValue(ViewState.Success(it)) }
                }
                .catch { err ->
                    e(err)
                    _homeData.postValue(ViewState.Error)
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
