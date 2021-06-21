package com.hedvig.app.util

import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.OperationName
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.ScalarTypeAdapters
import com.apollographql.apollo.api.internal.ResponseFieldMapper
import okio.BufferedSource
import okio.ByteString

class MockOperation : Operation<Operation.Data, Void, Operation.Variables> {
    override fun composeRequestBody(): ByteString {
        TODO("Not yet implemented")
    }

    override fun composeRequestBody(scalarTypeAdapters: ScalarTypeAdapters): ByteString {
        TODO("Not yet implemented")
    }

    override fun composeRequestBody(
        autoPersistQueries: Boolean,
        withQueryDocument: Boolean,
        scalarTypeAdapters: ScalarTypeAdapters,
    ): ByteString {
        TODO("Not yet implemented")
    }

    override fun name(): OperationName {
        TODO("Not yet implemented")
    }

    override fun operationId(): String {
        TODO("Not yet implemented")
    }

    override fun parse(source: BufferedSource): Response<Void> {
        TODO("Not yet implemented")
    }

    override fun parse(source: BufferedSource, scalarTypeAdapters: ScalarTypeAdapters): Response<Void> {
        TODO("Not yet implemented")
    }

    override fun parse(byteString: ByteString): Response<Void> {
        TODO("Not yet implemented")
    }

    override fun parse(byteString: ByteString, scalarTypeAdapters: ScalarTypeAdapters): Response<Void> {
        TODO("Not yet implemented")
    }

    override fun queryDocument(): String {
        TODO("Not yet implemented")
    }

    override fun responseFieldMapper(): ResponseFieldMapper<Operation.Data> {
        TODO("Not yet implemented")
    }

    override fun variables(): Operation.Variables {
        TODO("Not yet implemented")
    }

    override fun wrapData(data: Operation.Data?): Void? {
        TODO("Not yet implemented")
    }
}
