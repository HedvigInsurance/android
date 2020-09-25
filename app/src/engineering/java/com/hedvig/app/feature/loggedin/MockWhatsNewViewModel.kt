package com.hedvig.app.feature.loggedin

import com.hedvig.app.feature.whatsnew.WhatsNewViewModel
import com.hedvig.app.testdata.feature.loggedin.WHATS_NEW

class MockWhatsNewViewModel : WhatsNewViewModel() {
    override fun fetchNews(sinceVersion: String?) {
        news.postValue(whatsNewData)
    }

    override fun hasSeenNews(version: String) {
    }

    companion object {
        var whatsNewData = WHATS_NEW
    }
}
