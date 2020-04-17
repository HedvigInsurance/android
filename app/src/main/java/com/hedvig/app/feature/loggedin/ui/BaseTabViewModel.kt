package com.hedvig.app.feature.loggedin.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.loggedin.service.TabNotificationService
import e
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
            chatRepository
                .triggerFreeTextChat()
                .onEach { done() }
                .catch { e(it) }
                .launchIn(this)
        }
    }

    fun removeReferralNotification() {
        tabNotificationService.hasBeenNotifiedAboutReferrals()
        tabNotification.value = null
    }
}
