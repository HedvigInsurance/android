package com.hedvig.app.feature.embark

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.HedvigApplication
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.jsonObjectOfNotNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import ru.gildor.coroutines.okhttp.await

class EmbarkRepository(
    private val apolloClient: ApolloClient,
    private val okHttpClient: OkHttpClient,
    private val application: HedvigApplication,
    private val localeManager: LocaleManager
) {
    suspend fun embarkStory(name: String) = apolloClient
        .query(EmbarkStoryQuery(name, localeManager.defaultLocale().rawValue))
        .await()

    suspend fun graphQLQuery(query: String, variables: JSONObject? = null) = okHttpClient
        .newCall(
            Request.Builder()
                .url(application.graphqlUrl)
                .header("Content-Type", "application/json")
                .post(
                    jsonObjectOfNotNull(
                        "query" to query,
                        variables?.let { "variables" to variables }
                    ).toString().toRequestBody()
                )
                .build()
        ).await()
}
