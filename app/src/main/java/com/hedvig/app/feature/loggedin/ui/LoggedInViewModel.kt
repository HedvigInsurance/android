package com.hedvig.app.feature.loggedin.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import kotlinx.coroutines.launch

abstract class LoggedInViewModel : ViewModel() {
    protected val _data = MutableLiveData<LoggedInQuery.Data>()
    val data: LiveData<LoggedInQuery.Data> = _data

    private val _scroll = MutableLiveData<Int>()
    val scroll: LiveData<Int> = _scroll

    fun onScroll(scroll: Int) {
        _scroll.postValue(scroll)
    }
}

class LoggedInViewModelImpl(
    private val loggedInRepository: LoggedInRepository
) : LoggedInViewModel() {

    init {
        viewModelScope.launch {
            val response = runCatching {
                loggedInRepository
                    .loggedInData()
            }

            response.getOrNull()?.data?.let { _data.postValue(it) }
        }
    }
}
