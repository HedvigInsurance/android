@file:OptIn(ApolloExperimental::class)

package com.hedvig.app.apollo

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.mockserver.enqueue
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_ADYEN_CONNECTED
import giraffe.PaymentQuery
import giraffe.type.Locale
import org.junit.Test

class PaymentQueryParsingTest {

  @Test
  fun `apollo parses a payment with adyent connected`() = runApolloTest { mockServer, apolloClient ->
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
