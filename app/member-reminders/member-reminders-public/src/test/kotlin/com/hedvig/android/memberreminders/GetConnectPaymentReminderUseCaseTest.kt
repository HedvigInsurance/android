package com.hedvig.android.memberreminders

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.testing.enqueueTestNetworkError
import com.apollographql.apollo3.testing.enqueueTestResponse
import com.hedvig.android.apollo.giraffe.test.GiraffeFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager2
import com.hedvig.android.logger.TestLogcatLoggingRule
import giraffe.GetPayinMethodStatusQuery
import giraffe.type.PayinMethodStatus
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class GetConnectPaymentReminderUseCaseTest {

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule()
  val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

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

    assertThat(result).isRight().isEqualTo(ShowConnectPaymentReminder)
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

    assertThat(result).isLeft().isEqualTo(ConnectPaymentReminderError.AlreadySetup)
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

    assertThat(result).isLeft().isEqualTo(ConnectPaymentReminderError.FeatureFlagNotEnabled)
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

    assertThat(result).isLeft().isEqualTo(ConnectPaymentReminderError.AlreadySetup)
  }

  @Test
  fun `with the feature flag on but network failure, don't get a 'ShowReminder' response`() = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(
      apolloClient,
      FakeFeatureManager2(mapOf(Feature.CONNECT_PAYIN_REMINDER to true)),
    )
    apolloClient.enqueueTestNetworkError()

    val result = getConnectPaymentReminderUseCase.invoke()

    assertThat(result)
      .isLeft()
      .isInstanceOf<ConnectPaymentReminderError.NetworkError>()
      .prop(ConnectPaymentReminderError.NetworkError::message)
      .isEqualTo("Network error queued in QueueTestNetworkTransport")
  }
}
