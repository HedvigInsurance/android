package com.hedvig.app.feature.home.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.feature.home.data.HomeRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class HomeViewModel : ViewModel() {
    protected val _data = MutableLiveData<HomeQuery.Data>()
    val data: LiveData<HomeQuery.Data> = _data

    abstract fun load()
}

class HomeViewModelImpl(
    private val homeRepository: HomeRepository
) : HomeViewModel() {
    init {
        viewModelScope.launch {
            homeRepository
                .home()
                .onEach { response ->
                    response.data?.let { _data.postValue(it) }
                }
                .catch {
                    TODO("Present error to user")
                }
                .launchIn(this)
        }
    }

    override fun load() {
        viewModelScope.launch {
            runCatching { homeRepository.reloadHomeAsync().await() }
        }
    }
}

