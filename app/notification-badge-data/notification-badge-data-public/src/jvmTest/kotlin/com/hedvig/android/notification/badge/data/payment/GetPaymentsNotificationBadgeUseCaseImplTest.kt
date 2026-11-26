package com.hedvig.android.notification.badge.data.payment

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.enqueueTestNetworkError
import com.apollographql.apollo.testing.enqueueTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.auth.test.TestAuthTokenService
import com.hedvig.android.authlib.AccessToken
import com.hedvig.android.authlib.RefreshToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import octopus.MissedPaymentQuery
import octopus.builder.Data
import octopus.builder.buildMember
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class GetPaymentsNotificationBadgeUseCaseImplTest {
  @get:Rule
  val testApolloClientRule = TestApolloClientRule()
  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  @Test
  fun `every read reaches the network even when an earlier answer is cached`() = runTest {
    val useCase = GetPaymentsNotificationBadgeUseCaseImpl(apolloClient, loggedInAuthTokenService())
    apolloClient.enqueueTestResponse(MissedPaymentQuery(), data(preChargeNotice = "pre-1"))
    apolloClient.enqueueTestResponse(MissedPaymentQuery(), data(preChargeNotice = "pre-2"))

    assertThat(useCase.invoke().first())
      .isEqualTo(PaymentsNotificationBadgeData(hasMissedPayment = false, chargeNoticeIds = setOf("pre-1")))
    assertThat(useCase.invoke().first())
      .isEqualTo(PaymentsNotificationBadgeData(hasMissedPayment = false, chargeNoticeIds = setOf("pre-2")))
  }

  @Test
  fun `an unreachable network falls back to the cached answer`() = runTest {
    val useCase = GetPaymentsNotificationBadgeUseCaseImpl(apolloClient, loggedInAuthTokenService())
    apolloClient.enqueueTestResponse(MissedPaymentQuery(), data(preChargeNotice = "pre-1"))
    apolloClient.enqueueTestNetworkError()

    useCase.invoke().first()

    assertThat(useCase.invoke().first())
      .isEqualTo(PaymentsNotificationBadgeData(hasMissedPayment = false, chargeNoticeIds = setOf("pre-1")))
  }

  @Test
  fun `no query is sent while logged out, and logging in sends it`() = runTest {
    val authTokenService = TestAuthTokenService().apply { logoutAndInvalidateTokens() }
    val useCase = GetPaymentsNotificationBadgeUseCaseImpl(apolloClient, authTokenService)
    // A query sent while logged out would consume this response, and the one after login would then fail.
    apolloClient.enqueueTestResponse(MissedPaymentQuery(), data(preChargeNotice = "pre-1"))

    useCase.invoke().test {
      assertThat(awaitItem()).isNull()
      expectNoEvents()

      authTokenService.login()
      assertThat(awaitItem())
        .isEqualTo(PaymentsNotificationBadgeData(hasMissedPayment = false, chargeNoticeIds = setOf("pre-1")))
      expectNoEvents()
    }
  }

  @Test
  fun `a missed payment is checked again until it is paid`() = runTest {
    val useCase = GetPaymentsNotificationBadgeUseCaseImpl(apolloClient, loggedInAuthTokenService())
    apolloClient.enqueueTestResponse(MissedPaymentQuery(), data(preChargeNotice = null, missedChargeId = "missed-1"))
    apolloClient.enqueueTestResponse(MissedPaymentQuery(), data(preChargeNotice = null))

    useCase.invoke().test {
      assertThat(awaitItem())
        .isEqualTo(PaymentsNotificationBadgeData(hasMissedPayment = true, chargeNoticeIds = emptySet()))
      assertThat(awaitItem())
        .isEqualTo(PaymentsNotificationBadgeData(hasMissedPayment = false, chargeNoticeIds = emptySet()))
      expectNoEvents()
    }
  }

  private suspend fun loggedInAuthTokenService() = TestAuthTokenService().apply { login() }

  private suspend fun TestAuthTokenService.login() {
    loginWithTokens(AccessToken("access", 60), RefreshToken("refresh", 60))
  }

  private fun data(preChargeNotice: String?, missedChargeId: String? = null) =
    MissedPaymentQuery.Data(OctopusFakeResolver) {
      currentMember = buildMember {
        missedChargeIdToChargeManually = missedChargeId
        showPreChargeNotice = preChargeNotice
        showRetryChargeNotice = null
      }
    }
}
