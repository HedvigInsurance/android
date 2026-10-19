package com.hedvig.android.feature.payoutaccount.ui.setupswish

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.apollo.test.registerSuspendingTestResponse
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.paying.member.GetMemberPhoneNumberUseCase
import com.hedvig.android.feature.payoutaccount.FakeNetworkCacheManager
import com.hedvig.android.feature.payoutaccount.TestBackstack
import com.hedvig.android.feature.payoutaccount.TestRootKey
import com.hedvig.android.feature.payoutaccount.data.SetupSwishPayoutUseCase
import com.hedvig.android.feature.payoutaccount.navigation.SelectPayoutMethodKey
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import octopus.SetupSwishPayoutMutation
import octopus.builder.Data
import octopus.builder.buildPaymentMethodSetupOutput
import octopus.builder.buildUserError
import octopus.type.PaymentMethodSetupStatus
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class SetupSwishPayoutPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.TURBINE_MAP)
  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  private fun initialState() = SetupSwishPayoutUiState(TextFieldState(), false, false, null, false)

  @Test
  fun `the field is seeded with the number the backend already holds`() = runTest {
    presenter(storedNumber = "070-123 45 67").test(initialState()) {
      val state = awaitItem()
      this@runTest.testScheduler.advanceUntilIdle()
      assertThat(state.phoneNumberState.text.toString()).isEqualTo("0701234567")
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a number the member already typed is not overwritten by the stored one`() = runTest {
    val typed = initialState().copy(phoneNumberState = TextFieldState("0709999999"))
    presenter(storedNumber = "0701234567").test(typed) {
      val state = awaitItem()
      this@runTest.testScheduler.advanceUntilIdle()
      assertThat(state.phoneNumberState.text.toString()).isEqualTo("0709999999")
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `saving is ignored until the member confirms the number is their own`() = runTest {
    presenter(storedNumber = null).test(initialState()) {
      val state = awaitItem()
      state.phoneNumberState.setTextAndPlaceCursorAtEnd("0701234567")
      sendEvent(SetupSwishPayoutEvent.Save)
      this@runTest.testScheduler.advanceUntilIdle()
      expectNoEvents()
    }
  }

  @Test
  fun `a confirmed number is set up and the screen shows it connected`() = runTest {
    val cacheManager = FakeNetworkCacheManager()
    presenter(storedNumber = null, cacheManager = cacheManager).test(initialState()) {
      awaitItem().phoneNumberState.setTextAndPlaceCursorAtEnd("0701234567")
      sendEvent(SetupSwishPayoutEvent.ToggleOwnNumberConfirmed)
      assertThat(awaitItem().isOwnNumberConfirmed).isTrue()

      sendEvent(SetupSwishPayoutEvent.Save)
      assertThat(awaitItem().isLoading).isTrue()
      registerSetup("0701234567", PaymentMethodSetupStatus.ACTIVE, errorMessage = null)
      val connected = awaitItem()
      assertThat(connected.isLoading).isFalse()
      assertThat(connected.isConnected).isTrue()
      assertThat(connected.errorMessage).isNull()
      assertThat(cacheManager.clears).isEqualTo(1)
    }
  }

  @Test
  fun `a failed setup shows the backend's message and can be saved again`() = runTest {
    presenter(storedNumber = null).test(initialState()) {
      awaitItem().phoneNumberState.setTextAndPlaceCursorAtEnd("0701234567")
      sendEvent(SetupSwishPayoutEvent.ToggleOwnNumberConfirmed)
      assertThat(awaitItem().isOwnNumberConfirmed).isTrue()

      sendEvent(SetupSwishPayoutEvent.Save)
      assertThat(awaitItem().isLoading).isTrue()
      registerSetup("0701234567", PaymentMethodSetupStatus.FAILED, errorMessage = "Number is not enrolled in Swish")
      val failed = awaitItem()
      assertThat(failed.isLoading).isFalse()
      assertThat(failed.isConnected).isFalse()
      assertThat(failed.errorMessage?.message).isEqualTo("Number is not enrolled in Swish")

      sendEvent(SetupSwishPayoutEvent.Save)
      assertThat(awaitItem().isLoading).isTrue()
    }
  }

  @Test
  fun `finishing leaves the whole payout setup flow`() = runTest {
    val backstack = TestBackstack(TestRootKey, SelectPayoutMethodKey(listOf("SWISH")))
    presenter(storedNumber = null, backstack = backstack).test(initialState()) {
      awaitItem()
      sendEvent(SetupSwishPayoutEvent.FinishSetup)
      this@runTest.testScheduler.advanceUntilIdle()
      assertThat(backstack.entries).containsExactly(TestRootKey)
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun presenter(
    storedNumber: String?,
    cacheManager: FakeNetworkCacheManager = FakeNetworkCacheManager(),
    backstack: TestBackstack = TestBackstack(TestRootKey),
  ) = SetupSwishPayoutPresenter(
    setupSwishPayoutUseCase = SetupSwishPayoutUseCase(apolloClient, cacheManager),
    getMemberPhoneNumberUseCase = object : GetMemberPhoneNumberUseCase {
      override suspend fun invoke(): Either<ErrorMessage, String?> = storedNumber.right()
    },
    backstack = backstack,
  )

  private fun registerSetup(phoneNumber: String, status: PaymentMethodSetupStatus, errorMessage: String?) {
    apolloClient.registerSuspendingTestResponse(
      SetupSwishPayoutMutation(phoneNumber = phoneNumber),
      SetupSwishPayoutMutation.Data(OctopusFakeResolver) {
        paymentMethodSetupSwishPayout = buildPaymentMethodSetupOutput {
          this.status = status
          error = errorMessage?.let { buildUserError { message = it } }
        }
      },
    )
  }
}
