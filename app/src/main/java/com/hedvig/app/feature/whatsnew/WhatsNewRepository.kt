package com.hedvig.app.feature.whatsnew

import com.apollographql.apollo.api.Response
import com.hedvig.android.owldroid.graphql.WhatsNewQuery

interface WhatsNewRepository {
    suspend fun whatsNew(sinceVersion: String? = null): Response<WhatsNewQuery.Data>
    fun removeNewsForNewUser()
    fun hasSeenNews(version: String)
    fun latestSeenNews(): String
}
