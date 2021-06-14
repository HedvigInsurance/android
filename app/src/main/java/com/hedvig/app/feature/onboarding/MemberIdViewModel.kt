package com.hedvig.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import e
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class MemberIdViewModel : ViewModel() {
    sealed class State {
        data class Success(
            val id: String
        ) : State()

        object Error : State()
        object Loading : State()
    }

    protected val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    abstract fun load()
}

@HiltViewModel
class MemberIdViewModelImpl @Inject constructor(
    private val memberIdRepository: MemberIdRepository
) : MemberIdViewModel() {

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
                    _state.value = State.Error
                }
                return@launch
            }
            if (response.getOrNull()?.hasErrors() == true) {
                _state.value = State.Error
                return@launch
            }

            response.getOrNull()?.data?.member?.id?.let { _state.value = State.Success(it) }
        }
    }
}
