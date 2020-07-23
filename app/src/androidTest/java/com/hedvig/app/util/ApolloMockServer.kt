package com.hedvig.app.util

import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.OperationName
import com.apollographql.apollo.api.toJson
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONObject

fun apolloMockServer(vararg mocks: Pair<OperationName, () -> Operation.Data>) =
    MockWebServer().apply {
        dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val body = request.body.peek().readUtf8()
                val bodyAsJson = JSONObject(body)
                val operationName = bodyAsJson.getString("operationName")

                val dataProvider =
                    mocks.firstOrNull { it.first.name() == operationName }?.second
                        ?: return super.peek()

                return MockResponse().setBody(dataProvider().toJson())
            }
        }
    }

