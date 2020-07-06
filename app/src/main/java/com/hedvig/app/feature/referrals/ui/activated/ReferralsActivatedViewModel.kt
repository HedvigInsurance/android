package com.hedvig.app.feature.referrals.ui.activated

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.feature.loggedin.ui.LoggedInRepository
import e
import kotlinx.coroutines.launch

abstract class ReferralsActivatedViewModel : ViewModel() {
    abstract val data: LiveData<LoggedInQuery.Data>
}

class ReferralsActivatedViewModelImpl(
    private val loggedInRepository: LoggedInRepository
) : ReferralsActivatedViewModel() {
    override val data = MutableLiveData<LoggedInQuery.Data>()

    init {
        viewModelScope.launch {
            val response = runCatching {
                loggedInRepository
                    .loggedInDataAsync()
                    .await()
            }

            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }

            data.postValue(response.getOrNull()?.data())
        }
    }
}
