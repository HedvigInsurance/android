package com.hedvig.android.memberreminders

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsSubList
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
import com.hedvig.android.test.clock.TestClock
import giraffe.GetUpcomingRenewalReminderQuery
import giraffe.type.buildContract
import giraffe.type.buildUpcomingRenewal
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toLocalDateTime
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

@OptIn(ApolloExperimental::class)
class GetUpcomingRenewalRemindersUseCaseTest {

  @get:Rule
  val testApolloClientRule = TestApolloClientRule()
  val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  @Test
  fun `one upcoming renewal should show the reminder`() = runTest {
    val clock = TestClock()
    val getUpcomingRenewalRemindersUseCase = GetUpcomingRenewalRemindersUseCaseImpl(
      apolloClient,
      clock,
    )
    val upcomingRenewalLocalDate = clock.now().plus(1.days).toLocalDateTime(TimeZone.UTC).date.toJavaLocalDate()
    apolloClient.enqueueTestResponse(
      GetUpcomingRenewalReminderQuery(),
      GetUpcomingRenewalReminderQuery.Data(GiraffeFakeResolver) {
        contracts = listOf(
          buildContract {
            displayName = "display name"
            upcomingRenewal = buildUpcomingRenewal {
              renewalDate = upcomingRenewalLocalDate
              draftCertificateUrl = "draftUrl"
            }
          },
        )
      },
    )

    val result = getUpcomingRenewalRemindersUseCase.invoke()

    assertThat(result).isRight().containsExactly(
      UpcomingRenewal("display name", upcomingRenewalLocalDate.toKotlinLocalDate(), "draftUrl"),
    )
  }

  @Test
  fun `many upcoming renewals should show the many reminders`() = runTest {
    val clock = TestClock()
    val getUpcomingRenewalRemindersUseCase = GetUpcomingRenewalRemindersUseCaseImpl(
      apolloClient,
      clock,
    )
    apolloClient.enqueueTestResponse(
      GetUpcomingRenewalReminderQuery(),
      GetUpcomingRenewalReminderQuery.Data(GiraffeFakeResolver) {
        contracts = List(30) { index ->
          buildContract {
            displayName = "#$index"
            upcomingRenewal = buildUpcomingRenewal {
              renewalDate = clock.now().plus((index + 1).days).toLocalDateTime(TimeZone.UTC).date.toJavaLocalDate()
              draftCertificateUrl = "url#$index"
            }
          }
        }
      },
    )

    val result = getUpcomingRenewalRemindersUseCase.invoke()

    assertThat(result).isRight().containsSubList(
      List(30) { index ->
        UpcomingRenewal(
          contractDisplayName = "#$index",
          renewalDate = clock.now().plus((index + 1).days).toLocalDateTime(TimeZone.UTC).date,
          draftCertificateUrl = "url#$index",
        )
      },
    )
  }

  @Test
  fun `having a renewal yesterday show no reminders`() = runTest {
    val clock = TestClock()
    val getUpcomingRenewalRemindersUseCase = GetUpcomingRenewalRemindersUseCaseImpl(
      apolloClient,
      clock,
    )
    val upcomingRenewalLocalDate = clock.now().minus(1.days).toLocalDateTime(TimeZone.UTC).date.toJavaLocalDate()
    apolloClient.enqueueTestResponse(
      GetUpcomingRenewalReminderQuery(),
      GetUpcomingRenewalReminderQuery.Data(GiraffeFakeResolver) {
        contracts = listOf(
          buildContract {
            displayName = "display name"
            upcomingRenewal = buildUpcomingRenewal {
              renewalDate = upcomingRenewalLocalDate
              draftCertificateUrl = "draftUrl"
            }
          },
        )
      },
    )

    val result = getUpcomingRenewalRemindersUseCase.invoke()

    assertThat(result).isLeft().isEqualTo(UpcomingRenewalReminderError.NoUpcomingRenewals)
  }

  @Test
  fun `many renewals in the past, show no reminders`() = runTest {
    val clock = TestClock()
    val getUpcomingRenewalRemindersUseCase = GetUpcomingRenewalRemindersUseCaseImpl(
      apolloClient,
      clock,
    )
    apolloClient.enqueueTestResponse(
      GetUpcomingRenewalReminderQuery(),
      GetUpcomingRenewalReminderQuery.Data(GiraffeFakeResolver) {
        contracts = List(10) { index ->
          buildContract {
            displayName = "#$index"
            upcomingRenewal = buildUpcomingRenewal {
              renewalDate = clock.now().minus(index.days).toLocalDateTime(TimeZone.UTC).date.toJavaLocalDate()
              draftCertificateUrl = "url#$index"
            }
          }
        }
      },
    )

    val result = getUpcomingRenewalRemindersUseCase.invoke()

    assertThat(result).isLeft().isEqualTo(UpcomingRenewalReminderError.NoUpcomingRenewals)
  }

  @Test
  fun `some renewals being before today and some being after, returns only the 'after' reminders`() = runTest {
    val clock = TestClock()
    val getUpcomingRenewalRemindersUseCase = GetUpcomingRenewalRemindersUseCaseImpl(
      apolloClient,
      clock,
    )
    val renewalOffsets = mapOf<Int, Duration>(
      0 to -1.days,
      1 to 1.days,
      2 to -2.days,
      3 to 15.days,
      4 to 28.days,
    )
    apolloClient.enqueueTestResponse(
      GetUpcomingRenewalReminderQuery(),
      GetUpcomingRenewalReminderQuery.Data(GiraffeFakeResolver) {
        contracts = List(5) { index ->
          buildContract {
            displayName = "#$index"
            upcomingRenewal = buildUpcomingRenewal {
              renewalDate = (clock.now() + renewalOffsets[index]!!).toLocalDateTime(TimeZone.UTC).date.toJavaLocalDate()
              draftCertificateUrl = "url#$index"
            }
          }
        }
      },
    )

    val result = getUpcomingRenewalRemindersUseCase.invoke()

    assertThat(result).isRight().containsExactly(
      UpcomingRenewal("#1", clock.now().plus(renewalOffsets[1]!!).toLocalDateTime(TimeZone.UTC).date, "url#1"),
      UpcomingRenewal("#3", clock.now().plus(renewalOffsets[3]!!).toLocalDateTime(TimeZone.UTC).date, "url#3"),
      UpcomingRenewal("#4", clock.now().plus(renewalOffsets[4]!!).toLocalDateTime(TimeZone.UTC).date, "url#4"),
    )
  }

  @Test
  fun `network failure returns no reminders`() = runTest {
    val getUpcomingRenewalRemindersUseCase = GetUpcomingRenewalRemindersUseCaseImpl(
      apolloClient,
      TestClock(),
    )

    apolloClient.enqueueTestNetworkError()

    val result = getUpcomingRenewalRemindersUseCase.invoke()

    assertThat(result)
      .isLeft()
      .isInstanceOf<UpcomingRenewalReminderError.NetworkError>()
      .prop(UpcomingRenewalReminderError.NetworkError::message)
      .isEqualTo("Network error queued in QueueTestNetworkTransport")
  }
}
