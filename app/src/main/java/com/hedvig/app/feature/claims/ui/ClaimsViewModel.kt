package com.hedvig.app.feature.claims.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.claims.data.ClaimsRepository
import e
import kotlinx.coroutines.launch

class ClaimsViewModel(
    private val claimsRepository: ClaimsRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    val data: MutableLiveData<CommonClaimQuery.Data> = MutableLiveData()

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
            data.postValue(response.getOrNull()?.data)
        }
    }

    suspend fun triggerFreeTextChat() {
        val response = runCatching {
            chatRepository
                .triggerFreeTextChat()
        }
        if (response.isFailure) {
            response.exceptionOrNull()?.let { e(it) }
            return
        }
    }
}
