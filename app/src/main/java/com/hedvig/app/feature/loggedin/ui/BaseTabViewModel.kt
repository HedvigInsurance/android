package com.hedvig.app.feature.loggedin.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.loggedin.service.TabNotificationService
import e
import kotlinx.coroutines.launch

class BaseTabViewModel constructor(
    private val chatRepository: ChatRepository,
    private val tabNotificationService: TabNotificationService
) : ViewModel() {

    val tabNotification = MutableLiveData<TabNotification?>()

    init {
        tabNotificationService.getTabNotification()?.let { tabNotification.value = it }
    }

    fun triggerFreeTextChat(done: () -> Unit) {
        viewModelScope.launch {
            val response = runCatching {
                chatRepository
                    .triggerFreeTextChatAsync()
                    .await()
            }

            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
            }
            done()
        }
    }

    fun removeReferralNotification() {
        tabNotificationService.hasBeenNotifiedAboutReferrals()
        tabNotification.value = null
    }
}
