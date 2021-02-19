package com.hedvig.onboarding.moreoptions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.MemberIdQuery
import e
import kotlinx.coroutines.launch

abstract class MoreOptionsViewModel : ViewModel() {
    protected val _data = MutableLiveData<Result<MemberIdQuery.Data>>()
    val data: LiveData<Result<MemberIdQuery.Data>> = _data
    abstract fun load()
}

class MoreOptionsViewModelImpl(
    private val memberIdRepository: MemberIdRepository
) : MoreOptionsViewModel() {

    init {
        load()
    }

    override fun load() {
        viewModelScope.launch {
            val response = runCatching {
                memberIdRepository.memberId()
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
