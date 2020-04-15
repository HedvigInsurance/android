package com.hedvig.app.feature.claims.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.claims.data.ClaimsRepository
import e
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ClaimsViewModel(
    private val claimsRepository: ClaimsRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    val data: MutableLiveData<CommonClaimQuery.Data> = MutableLiveData()

    private val disposables = CompositeDisposable()

    init {
        fetchCommonClaims()
    }

    fun fetchCommonClaims() {
        viewModelScope.launch {
            val response = runCatching { claimsRepository.fetchCommonClaims() }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e { "$it Failed to fetch claims data" } }
                return@launch
            }
            data.postValue(response.getOrNull())
        }
    }

    fun triggerClaimsChat(claimTypeId: String? = null, done: () -> Unit) {
        viewModelScope.launch {
            val response = runCatching { claimsRepository.triggerClaimsChat(claimTypeId) }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            done()
        }
    }

    fun triggerFreeTextChat(done: () -> Unit) {
        viewModelScope.launch {
            chatRepository
                .triggerFreeTextChat()
                .onEach { done() }
                .catch { e(it) }
                .launchIn(this)
        }
    }

    fun triggerCallMeChat(done: () -> Unit) {
        viewModelScope.launch {
            val response = runCatching { claimsRepository.triggerCallMeChat() }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            done()
        }
    }
}
