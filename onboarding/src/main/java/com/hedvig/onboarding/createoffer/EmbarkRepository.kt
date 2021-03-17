package com.hedvig.onboarding.createoffer

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.HedvigApplication
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.apollo.defaultLocale
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
    private val marketManager: MarketManager,
) {
    suspend fun embarkStory(name: String) = apolloClient
        .query(EmbarkStoryQuery(name, defaultLocale(application, marketManager).rawValue))
        .await()

    suspend fun graphQLQuery(query: String, variables: JSONObject? = null) = okHttpClient
        .newCall(
            Request.Builder()
                .header("Content-Type", "application/json")
                .post(jsonObjectOfNotNull(
                    "query" to query,
                    variables?.let { "variables" to variables }
                ).toString().toRequestBody())
                .build()
        ).await()
}
