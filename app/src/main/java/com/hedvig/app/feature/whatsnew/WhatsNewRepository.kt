package com.hedvig.app.feature.whatsnew

import android.content.Context
import com.hedvig.android.owldroid.graphql.WhatsNewQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.BuildConfig
import com.hedvig.app.util.apollo.defaultLocale
import com.hedvig.app.util.apollo.toDeferred

class WhatsNewRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
) {
    suspend fun fetchWhatsNew(sinceVersion: String? = null) =
        apolloClientWrapper.apolloClient.query(
            WhatsNewQuery(
                locale = defaultLocale(context),
                sinceVersion = sinceVersion ?: latestSeenNews()
            )
        ).toDeferred().await()

    fun removeNewsForNewUser() {
        if (latestSeenNews() == NEWS_BASELINE_VERSION) {
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
        .getString(LAST_NEWS_SEEN, NEWS_BASELINE_VERSION) as String

    companion object {
        private const val WHATS_NEW_SHARED_PREFERENCES = "whats_new"
        private const val LAST_NEWS_SEEN = "last_news_seen"

        private const val NEWS_BASELINE_VERSION = "3.0.0"
    }
}
