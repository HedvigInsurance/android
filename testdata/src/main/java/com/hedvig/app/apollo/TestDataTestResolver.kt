package com.hedvig.app.apollo

import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.api.test.DefaultTestResolver

@OptIn(ApolloExperimental::class)
object TestDataTestResolver : DefaultTestResolver() {
    override fun resolveListSize(path: List<Any>): Int {
        return 0
    }
}
