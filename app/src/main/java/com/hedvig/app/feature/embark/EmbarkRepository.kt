package com.hedvig.app.feature.embark

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.internal.json.JsonWriter
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.internal.interceptor.ApolloServerInterceptor
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.HedvigApplication
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.jsonObjectOfNotNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Buffer
import org.json.JSONObject
import ru.gildor.coroutines.okhttp.await
import java.io.File

class EmbarkRepository(
    private val apolloClient: ApolloClient,
    private val okHttpClient: OkHttpClient,
    private val application: HedvigApplication,
    private val localeManager: LocaleManager
) {
    suspend fun embarkStory(name: String) = apolloClient
        .query(EmbarkStoryQuery(name, localeManager.defaultLocale().rawValue))
        .await()

    suspend fun graphQLQuery(
        query: String,
        variables: JSONObject? = null,
        files: List<FileVariable>
    ): Response {

        val requestBody = if (files.isEmpty()) {
            createVariableRequestBody(query, variables)
        } else {
            createMultiPartRequestBody(query, variables, files)
        }

        return okHttpClient
            .newCall(
                Request.Builder()
                    .url(application.graphqlUrl)
                    .header("Content-Type", "application/json")
                    .post(requestBody)
                    .build()
            ).await()
    }

    private fun createMultiPartRequestBody(
        query: String,
        variables: JSONObject?,
        files: List<FileVariable>
    ): MultipartBody {
        val builder = MultipartBody.Builder()

        val variableRequestBody = createVariableRequestBody(query, variables)
        builder.addPart(variableRequestBody)

        val fileUploadRequestBody = createFileUploadRequestBody(query, files)
        builder.addPart(fileUploadRequestBody)

        return builder.build()
    }

    private fun createVariableRequestBody(query: String, variables: JSONObject?): RequestBody {
        return jsonObjectOfNotNull(
            "query" to query,
            variables?.let { "variables" to variables }
        )
            .toString()
            .toRequestBody()
    }

    private fun createFileUploadRequestBody(query: String, variables: List<FileVariable>): RequestBody {
        val buffer = createFileMap(variables)

        val multipartBodyBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                name = "operations",
                filename = null,
                body = jsonObjectOfNotNull("query" to query)
                    .toString()
                    .toRequestBody()
            )
            .addFormDataPart(
                name = "map",
                filename = null,
                body = RequestBody.create(ApolloServerInterceptor.MEDIA_TYPE, buffer.readByteString())
            )

        variables.forEachIndexed { i, variable ->
            val file = File(variable.path)
            multipartBodyBuilder.addFormDataPart(
                i.toString(),
                file.name,
                file.asRequestBody("audio".toMediaType())
            )
        }

        return multipartBodyBuilder.build()
    }

    private fun createFileMap(fileVariables: List<FileVariable>): Buffer {
        val buffer = Buffer()
        val jsonWriter = JsonWriter.of(buffer)
        jsonWriter.beginObject()
        fileVariables.forEachIndexed { i, variable ->
            jsonWriter.name(i.toString()).beginArray()
            jsonWriter.value(variable.key)
            jsonWriter.endArray()
        }

        jsonWriter.endObject()
        jsonWriter.close()
        return buffer
    }
}

data class FileVariable(
    val key: String,
    val path: String
)
