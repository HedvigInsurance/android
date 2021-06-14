package com.hedvig.app.di

import com.apollographql.apollo.api.Response
import com.hedvig.android.owldroid.graphql.WhatsNewQuery
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.feature.whatsnew.WhatsNewRepository
import com.hedvig.app.testdata.feature.loggedin.WHATS_NEW
import javax.inject.Inject

class WhatsNewRepositoryMock @Inject constructor() : WhatsNewRepository {

    var whatsNewData: WhatsNewQuery.Data = WHATS_NEW

    override suspend fun whatsNew(sinceVersion: String?): Response<WhatsNewQuery.Data> {
        return Response.builder<WhatsNewQuery.Data>(
            WhatsNewQuery(Locale.EN_SE, sinceVersion ?: "3.0.0")
        )
            .data(whatsNewData)
            .build()
    }

    override fun removeNewsForNewUser() {
        // NO-OP
    }

    override fun hasSeenNews(version: String) {
        // NO-OP
    }

    override fun latestSeenNews(): String {
        return "test"
    }
}
