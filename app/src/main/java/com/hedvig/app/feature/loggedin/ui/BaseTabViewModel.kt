package com.hedvig.app.feature.loggedin.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.app.feature.chat.ChatRepository
import com.hedvig.app.feature.loggedin.service.TabNotificationService
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber

class BaseTabViewModel constructor(
    private val chatRepository: ChatRepository,
    private val tabNotificationService: TabNotificationService
) : ViewModel() {

    val tabNotification = MutableLiveData<TabNotification?>()

    val disposables = CompositeDisposable()

    init {
        tabNotificationService.getTabNotification()?.let { tabNotification.value = it }
    }

    fun triggerFreeTextChat(done: () -> Unit) {
        disposables += chatRepository
            .triggerFreeTextChat()
            .subscribe({ done() }, { Timber.e(it) })
    }

    fun removeReferralNotification() {
        tabNotificationService.hasBeenNotifiedAboutReferrals()
        tabNotification.value = null
    }
}
