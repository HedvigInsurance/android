package com.hedvig.android.memberreminders

import arrow.core.Either
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.testing.QueueTestNetworkTransport
import com.apollographql.apollo3.testing.enqueueTestNetworkError
import com.apollographql.apollo3.testing.enqueueTestResponse
import com.hedvig.android.apollo.giraffe.test.GiraffeFakeResolver
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager2
import com.hedvig.android.logger.TestLogcatLoggingRule
import giraffe.GetPayinMethodStatusQuery
import giraffe.type.PayinMethodStatus
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class GetConnectPaymentReminderUseCaseTest {

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private lateinit var apolloClient: ApolloClient

  @Before
  fun setUp() {
    apolloClient = ApolloClient.Builder()
      .networkTransport(QueueTestNetworkTransport())
      .build()
  }

  @After
  fun tearDown() {
    apolloClient.close()
  }

  @Test
  fun `when payin method needs setup and the feature flag is on, show the reminder`() = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(
      apolloClient,
      FakeFeatureManager2(mapOf(Feature.CONNECT_PAYIN_REMINDER to true)),
    )
    apolloClient.enqueueTestResponse(
      GetPayinMethodStatusQuery(),
      GetPayinMethodStatusQuery.Data(GiraffeFakeResolver) {
        payinMethodStatus = PayinMethodStatus.NEEDS_SETUP
      },
    )

    val result = getConnectPaymentReminderUseCase.invoke()

    assertAll {
      assertThat(result).isInstanceOf<Either.Right<*>>()
      assertThat(result.getOrNull()).isEqualTo(ShowConnectPaymentReminder)
    }
  }

  @Test
  fun `when payin method is pending and the feature flag is on, don't show the reminder`() = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(
      apolloClient,
      FakeFeatureManager2(mapOf(Feature.CONNECT_PAYIN_REMINDER to true)),
    )
    apolloClient.enqueueTestResponse(
      GetPayinMethodStatusQuery(),
      GetPayinMethodStatusQuery.Data(GiraffeFakeResolver) {
        payinMethodStatus = PayinMethodStatus.PENDING
      },
    )

    val result = getConnectPaymentReminderUseCase.invoke()

    assertAll {
      assertThat(result).isInstanceOf<Either.Left<*>>()
      assertThat(result.leftOrNull()).isEqualTo(ConnectPaymentReminderError.AlreadySetup)
    }
  }

  @Test
  fun `with the feature flag off, don't get a 'ShowReminder' response`() = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(
      apolloClient,
      FakeFeatureManager2(mapOf(Feature.CONNECT_PAYIN_REMINDER to false)),
    )
    apolloClient.enqueueTestResponse(
      GetPayinMethodStatusQuery(),
      GetPayinMethodStatusQuery.Data(GiraffeFakeResolver) {
        payinMethodStatus = PayinMethodStatus.NEEDS_SETUP
      },
    )

    val result = getConnectPaymentReminderUseCase.invoke()

    assertAll {
      assertThat(result).isInstanceOf<Either.Left<*>>()
      assertThat(result.leftOrNull()).isEqualTo(ConnectPaymentReminderError.FeatureFlagNotEnabled)
    }
  }

  @Test
  fun `with the feature flag on but payment already connected, don't get a 'ShowReminder' response`() = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(
      apolloClient,
      FakeFeatureManager2(mapOf(Feature.CONNECT_PAYIN_REMINDER to true)),
    )
    apolloClient.enqueueTestResponse(
      GetPayinMethodStatusQuery(),
      GetPayinMethodStatusQuery.Data(GiraffeFakeResolver) {
        payinMethodStatus = PayinMethodStatus.ACTIVE
      },
    )

    val result = getConnectPaymentReminderUseCase.invoke()

    assertAll {
      assertThat(result).isInstanceOf<Either.Left<*>>()
      assertThat(result.leftOrNull()).isEqualTo(ConnectPaymentReminderError.AlreadySetup)
    }
  }

  @Test
  fun `with the feature flag on but network failure, don't get a 'ShowReminder' response`() = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(
      apolloClient,
      FakeFeatureManager2(mapOf(Feature.CONNECT_PAYIN_REMINDER to true)),
    )
    apolloClient.enqueueTestNetworkError()

    val result = getConnectPaymentReminderUseCase.invoke()

    assertAll {
      assertThat(result).isInstanceOf<Either.Left<*>>()
      assertThat(result.leftOrNull()).isNotNull().isInstanceOf<ConnectPaymentReminderError.NetworkError>()
      assertThat((result.leftOrNull() as ConnectPaymentReminderError.NetworkError).message)
        .isEqualTo("Network error queued in QueueTestNetworkTransport")
    }
  }
}
