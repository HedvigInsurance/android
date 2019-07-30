package com.hedvig.app.feature.whatsnew

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.WhatsNewQuery
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.BuildConfig

class WhatsNewRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
) {
    fun fetchWhatsNew(sinceVersion: String? = null) =
        Rx2Apollo.from(
            apolloClientWrapper.apolloClient.query(
                WhatsNewQuery
                    .builder()
                    .locale(Locale.SV_SE)
                    .sinceVersion(sinceVersion ?: latestSeenNews())
                    .build()
            )
        )

    fun removeNewsForNewUser() {
        if (latestSeenNews() == VERSION_BEFORE_NEWS_WERE_RELEASED) {
            hasSeenNews(BuildConfig.VERSION_NAME)
        }
    }

    fun hasSeenNews(version: String) {
        context
            .getSharedPreferences(WHATS_NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putString(LAST_NEWS_SEEN, version)
            .apply()
    }

    private fun latestSeenNews() = context
        .getSharedPreferences(WHATS_NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        .getString(LAST_NEWS_SEEN, VERSION_BEFORE_NEWS_WERE_RELEASED)

    companion object {
        private const val WHATS_NEW_SHARED_PREFERENCES = "whats_new"
        private const val LAST_NEWS_SEEN = "last_news_seen"

        private const val VERSION_BEFORE_NEWS_WERE_RELEASED = "2.7.3"
    }
}
