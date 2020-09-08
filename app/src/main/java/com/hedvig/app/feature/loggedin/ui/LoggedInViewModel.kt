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

    private val _bottomTabInset = MutableLiveData<Int>()
    val bottomTabInset: LiveData<Int>
        get() = _bottomTabInset

    private val _toolbarInset = MutableLiveData<Int>()
    val toolbarInset: LiveData<Int> = _toolbarInset

    fun updateBottomTabInset(newInset: Int) {
        _bottomTabInset.postValue(newInset)
    }

    fun updateToolbarInset(newInset: Int) {
        _toolbarInset.postValue(newInset)
    }

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
                    .loggedInDataAsync()
                    .await()
            }

            response.getOrNull()?.data?.let { _data.postValue(it) }
        }
    }
}
