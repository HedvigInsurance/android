package com.hedvig.app.util.apollo

import com.apollographql.apollo3.api.json.BufferedSinkJsonWriter
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeGraphqlCall
import com.hedvig.android.core.common.android.jsonObjectOfNotNull
import com.hedvig.app.service.FileService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class GraphQLQueryHandler(
  private val okHttpClient: OkHttpClient,
  private val fileService: FileService,
  private val giraffeUrl: String,
) {

  suspend fun graphQLQuery(
    query: String,
    variables: JSONObject? = null,
    files: List<FileVariable>,
  ): OperationResult<JSONObject> {
    var requestBody = createVariableRequestBody(query, variables, files)
    requestBody = if (files.isNotEmpty()) {
      createFileUploadRequestBody(requestBody, files)
    } else {
      requestBody
    }

    val modifiedOkHttpClient = if (files.isNotEmpty()) {
      okHttpClient.newBuilder()
        .readTimeout(0, TimeUnit.SECONDS)
        .build()
    } else {
      okHttpClient
    }

    return modifiedOkHttpClient
      .newCall(
        Request.Builder()
          .url(giraffeUrl)
          .header("Content-Type", "application/json")
          .post(requestBody)
          .build(),
      ).safeGraphqlCall()
  }

  private fun createVariableRequestBody(
    query: String,
    variables: JSONObject?,
    files: List<FileVariable>,
  ): RequestBody {
    val jsonObject = variables ?: JSONObject()

    files.map { it.key }
      .forEach { jsonObject.put(it, JSONObject.NULL) }

    return jsonObjectOfNotNull(
      "query" to query,
      "variables" to jsonObject,
    )
      .toString()
      .toRequestBody()
  }

  private fun createFileUploadRequestBody(request: RequestBody, variables: List<FileVariable>): RequestBody {
    val buffer = createFileMap(variables)

    val multipartBodyBuilder = MultipartBody.Builder()
      .setType(MultipartBody.FORM)
      .addFormDataPart(
        name = "operations",
        filename = null,
        body = request,
      )
      .addFormDataPart(
        name = "map",
        filename = null,
        body = buffer.readByteString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()),
      )

    variables.forEachIndexed { i, variable ->
      val file = File(variable.path)
      val mediaType = fileService.getMimeType(variable.path).toMediaType()
      multipartBodyBuilder.addFormDataPart(
        i.toString(),
        file.name,
        file.asRequestBody(mediaType),
      )
    }

    return multipartBodyBuilder.build()
  }

  private fun createFileMap(fileVariables: List<FileVariable>): Buffer {
    val buffer = Buffer()
    val jsonWriter = BufferedSinkJsonWriter(buffer)
    jsonWriter.beginObject()
    fileVariables.forEachIndexed { i, variable ->
      jsonWriter.name(i.toString()).beginArray()
      jsonWriter.value("variables." + variable.key)
      jsonWriter.endArray()
    }

    jsonWriter.endObject()
    jsonWriter.close()
    return buffer
  }
}
