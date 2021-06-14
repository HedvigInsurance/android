package com.hedvig.app.feature.whatsnew

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.WhatsNewQuery
import com.hedvig.app.BuildConfig
import com.hedvig.app.util.LocaleManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WhatsNewRepositoryImpl @Inject constructor(
    private val apolloClient: ApolloClient,
    @ApplicationContext private val context: Context,
    private val localeManager: LocaleManager
) : WhatsNewRepository {
    override suspend fun whatsNew(sinceVersion: String?) =
        apolloClient.query(
            WhatsNewQuery(
                locale = localeManager.defaultLocale(),
                sinceVersion = sinceVersion ?: latestSeenNews()
            )
        ).await()

    override fun removeNewsForNewUser() {
        if (latestSeenNews() == NEWS_BASELINE_VERSION) {
            hasSeenNews(BuildConfig.VERSION_NAME)
        }
    }

    override fun hasSeenNews(version: String) {
        context
            .getSharedPreferences(WHATS_NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putString(LAST_NEWS_SEEN, version)
            .apply()
    }

    override fun latestSeenNews() = context
        .getSharedPreferences(WHATS_NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        .getString(LAST_NEWS_SEEN, NEWS_BASELINE_VERSION) as String

    companion object {
        private const val WHATS_NEW_SHARED_PREFERENCES = "whats_new"
        private const val LAST_NEWS_SEEN = "last_news_seen"

        private const val NEWS_BASELINE_VERSION = "3.0.0"
    }
}
