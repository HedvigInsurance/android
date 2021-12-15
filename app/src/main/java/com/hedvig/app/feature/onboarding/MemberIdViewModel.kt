package com.hedvig.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.tracking.TrackingFacade
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

class MemberIdViewModelImpl(
    private val memberIdRepository: GetMemberIdUseCase,
    private val trackingFacade: TrackingFacade
) : MemberIdViewModel() {

    init {
        load()
    }

    override fun load() {
        viewModelScope.launch {
            val state = when (val result = memberIdRepository.memberId()) {
                is GetMemberIdUseCase.MemberIdResult.Error -> State.Error
                is GetMemberIdUseCase.MemberIdResult.Success -> {
                    trackingFacade.setProperty("user_id", result.id)
                    State.Success(result.id)
                }
            }
            _state.value = state
        }
    }
}
