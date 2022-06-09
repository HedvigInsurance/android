@file:OptIn(ApolloExperimental::class)

package com.hedvig.app.apollo

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.mockserver.MockServer
import com.apollographql.apollo3.mockserver.enqueue
import com.apollographql.apollo3.testing.runTest
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.android.owldroid.graphql.type.Locale
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_ADYEN_CONNECTED
import org.junit.Test

class PaymentQueryParsingTest {
    private lateinit var mockServer: MockServer
    private lateinit var apolloClient: ApolloClient

    private suspend fun before() {
        mockServer = MockServer()
        apolloClient = ApolloClient.Builder().serverUrl(mockServer.url()).build()
    }

    private suspend fun after() {
        apolloClient.dispose()
        mockServer.stop()
    }

    @Test
    fun `apollo parses a payment with adyent connected`() = runTest(
        before = { before() },
        after = { after() }
    ) {
        val originalData = PAYMENT_DATA_ADYEN_CONNECTED
        val jsonData = originalData.toJsonStringWithData()
        mockServer.enqueue(jsonData)

        val response = apolloClient
            .query(PaymentQuery(Locale.sv_SE))
            .execute()

        assertThat(response.data).isNotNull()
        assertThat(response.data!!).isEqualTo(originalData)
    }
}
