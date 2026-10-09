package com.hedvig.android.notification.badge.data.payment

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.QueueTestNetworkTransport
import com.apollographql.apollo.testing.enqueueTestNetworkError
import com.apollographql.apollo.testing.enqueueTestResponse
import com.apollographql.cache.normalized.memory.MemoryCacheFactory
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import octopus.MissedPaymentQuery
import octopus.builder.Data
import octopus.builder.buildMember
import octopus.cache.Cache
import org.junit.After
import org.junit.Test

@OptIn(ApolloExperimental::class)
class GetPaymentsNotificationBadgeUseCaseImplTest {
  private val apolloClient = with(Cache) {
    ApolloClient.Builder()
      .networkTransport(QueueTestNetworkTransport())
      .cache(MemoryCacheFactory())
      .build()
  }

  @After
  fun tearDown() {
    apolloClient.close()
  }

  @Test
  fun `every read reaches the network even when an earlier answer is cached`() = runTest {
    val useCase = GetPaymentsNotificationBadgeUseCaseImpl(apolloClient)
    apolloClient.enqueueTestResponse(MissedPaymentQuery(), data(preChargeNotice = "pre-1"))
    apolloClient.enqueueTestResponse(MissedPaymentQuery(), data(preChargeNotice = "pre-2"))

    assertThat(useCase.invoke().first())
      .isEqualTo(PaymentsNotificationBadgeData(hasMissedPayment = false, chargeNoticeIds = setOf("pre-1")))
    assertThat(useCase.invoke().first())
      .isEqualTo(PaymentsNotificationBadgeData(hasMissedPayment = false, chargeNoticeIds = setOf("pre-2")))
  }

  @Test
  fun `an unreachable network falls back to the cached answer`() = runTest {
    val useCase = GetPaymentsNotificationBadgeUseCaseImpl(apolloClient)
    apolloClient.enqueueTestResponse(MissedPaymentQuery(), data(preChargeNotice = "pre-1"))
    apolloClient.enqueueTestNetworkError()

    useCase.invoke().first()

    assertThat(useCase.invoke().first())
      .isEqualTo(PaymentsNotificationBadgeData(hasMissedPayment = false, chargeNoticeIds = setOf("pre-1")))
  }

  private fun data(preChargeNotice: String?) = MissedPaymentQuery.Data(OctopusFakeResolver) {
    currentMember = buildMember {
      missedChargeIdToChargeManually = null
      showPreChargeNotice = preChargeNotice
      showRetryChargeNotice = null
    }
  }
}
