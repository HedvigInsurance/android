package com.hedvig.app.feature.onbarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.MoreOptionsQuery
import e
import kotlinx.coroutines.launch

abstract class MoreOptionsViewModel : ViewModel() {
    protected val _data = MutableLiveData<Result<MoreOptionsQuery.Data>>()
    val data: LiveData<Result<MoreOptionsQuery.Data>> = _data
    abstract fun load()
}

class MoreOptionsViewModelImpl(
    private val moreOptionsRepository: MoreOptionsRepository
) : MoreOptionsViewModel() {

    init {
        load()
    }

    override fun load() {
        viewModelScope.launch {
            val response = runCatching {
                moreOptionsRepository.memberId()
            }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { exception ->
                    e(exception)
                    _data.postValue(Result.failure(exception))
                }
                return@launch
            }

            if (response.getOrNull()?.hasErrors() == true) {
                _data.postValue(Result.failure(Error()))
                return@launch
            }

            response.getOrNull()?.data?.let { _data.postValue(Result.success(it)) }
        }
    }
}
