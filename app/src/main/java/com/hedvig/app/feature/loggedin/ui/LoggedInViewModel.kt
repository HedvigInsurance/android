package com.hedvig.app.feature.loggedin.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import kotlinx.coroutines.launch

abstract class LoggedInViewModel : ViewModel() {
    abstract val data: LiveData<LoggedInQuery.Data>
    abstract val scroll: MutableLiveData<Float>
}

class LoggedInViewModelImpl(
    private val loggedInRepository: LoggedInRepository
) : LoggedInViewModel() {
    override val data = MutableLiveData<LoggedInQuery.Data>()
    override val scroll = MutableLiveData<Float>()

    init {
        viewModelScope.launch {
            val response = runCatching {
                loggedInRepository
                    .loggedInDataAsync()
                    .await()
            }

            data.postValue(response.getOrNull()?.data())
        }
    }
}
