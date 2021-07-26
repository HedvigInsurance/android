package com.hedvig.app.util

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.cache.CacheHeaders

class MockedApolloCall<T>(
    private val response: T,
) : ApolloCall<T> {
    override fun cancel() {
        TODO("Not yet implemented")
    }

    override fun isCanceled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun enqueue(callback: ApolloCall.Callback<T>?) {
        callback?.onResponse(
            Response<T>(
                MockOperation(),
                response,
            )
        )
    }

    override fun cacheHeaders(cacheHeaders: CacheHeaders): ApolloCall<T> {
        TODO("Not yet implemented")
    }

    override fun clone(): ApolloCall<T> {
        TODO("Not yet implemented")
    }

    override fun operation(): Operation<*, *, *> {
        TODO("Not yet implemented")
    }

    override fun toBuilder(): ApolloCall.Builder<T> {
        TODO("Not yet implemented")
    }
}
