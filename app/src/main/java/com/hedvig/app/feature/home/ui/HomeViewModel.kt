package com.hedvig.app.feature.home.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.feature.home.data.HomeRepository
import kotlinx.coroutines.launch

abstract class HomeViewModel : ViewModel() {
    protected val _data = MutableLiveData<HomeQuery.Data>()
    val data: LiveData<HomeQuery.Data> = _data
}

class HomeViewModelImpl(
    private val homeRepository: HomeRepository
) : HomeViewModel() {
    init {
        viewModelScope.launch {
            val result = runCatching { homeRepository.homeAsync().await() }
            if (result.isFailure) {
                TODO("Present error to user")
            }

            result.getOrNull()?.data?.let { _data.postValue(it) }
        }
    }
}

