package com.hedvig.app.feature.whatsnew

import android.content.Context
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.language.LanguageService
import com.hedvig.app.BuildConfig
import giraffe.WhatsNewQuery

class WhatsNewRepository(
  private val apolloClient: ApolloClient,
  private val context: Context,
  private val languageService: LanguageService,
) {
  suspend fun whatsNew(sinceVersion: String?): OperationResult<WhatsNewQuery.Data> {
    return apolloClient
      .query(
        WhatsNewQuery(
          locale = languageService.getGraphQLLocale(),
          sinceVersion = sinceVersion ?: latestSeenNews(),
        ),
      )
      .safeExecute()
  }

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
