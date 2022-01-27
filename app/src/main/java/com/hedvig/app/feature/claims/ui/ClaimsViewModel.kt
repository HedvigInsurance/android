package com.hedvig.app.feature.claims.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.claims.data.ClaimsRepository
import e
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ClaimsViewModel(
    private val claimsRepository: ClaimsRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    val data: MutableLiveData<CommonClaimQuery.Data> = MutableLiveData()

    sealed class Event {
        object StartChat : Event()
        object Error : Event()
    }

    private val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    init {
        fetchCommonClaims()
    }

    private fun fetchCommonClaims() {
        viewModelScope.launch {
            val response = runCatching { claimsRepository.fetchCommonClaims() }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e { "$it Failed to fetch claims data" } }
                return@launch
            }
            response.getOrNull()?.data?.let(data::postValue)
        }
    }

    suspend fun triggerFreeTextChat() {
        viewModelScope.launch {
            val event = when (val result = chatRepository.triggerFreeTextChat()) {
                is Either.Left -> Event.Error
                is Either.Right -> Event.StartChat
            }
            _events.trySend(event)
        }
    }
}
