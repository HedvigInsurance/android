package com.hedvig.app.feature.embark

import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toDeferred
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.HedvigApplication
import com.hedvig.app.util.jsonObjectOfNotNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import ru.gildor.coroutines.okhttp.await

class EmbarkRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val okHttpClient: OkHttpClient,
    private val application: HedvigApplication
) {
    suspend fun embarkStory(name: String) = apolloClientWrapper
        .apolloClient
        .query(EmbarkStoryQuery(name))
        .await()

    suspend fun graphQLQuery(query: String, variables: JSONObject? = null) = okHttpClient
        .newCall(
            Request.Builder()
                .url(application.graphqlUrl)
                .post(jsonObjectOfNotNull(
                    "query" to query,
                    variables?.let { "variables" to variables }
                ).toString().toRequestBody())
                .build()
        ).await()
}
