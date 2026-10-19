package com.hedvig.android.feature.payoutaccount.ui.editbankaccount

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
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
import com.hedvig.android.feature.payoutaccount.FakeNetworkCacheManager
import com.hedvig.android.feature.payoutaccount.TestBackstack
import com.hedvig.android.feature.payoutaccount.TestRootKey
import com.hedvig.android.feature.payoutaccount.data.SetupNordeaPayoutUseCase
import com.hedvig.android.feature.payoutaccount.navigation.SelectPayoutMethodKey
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import octopus.SetupNordeaPayoutMutation
import octopus.builder.Data
import octopus.builder.buildPaymentMethodSetupOutput
import octopus.builder.buildUserError
import octopus.type.PaymentMethodSetupStatus
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class EditBankAccountPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.TURBINE_MAP)
  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  private fun initialState(accountNumber: String = "") = EditBankAccountUiState(
    accountNumberState = TextFieldState(accountNumber),
    bankName = null,
    isLoading = false,
    errorMessage = null,
    isConnected = false,
  )

  @Test
  fun `the bank is named from the clearing number as the member types`() = runTest {
    presenter().test(initialState()) {
      val state = awaitItem()
      assertThat(state.bankName).isNull()
      state.accountNumberState.setTextAndPlaceCursorAtEnd("8327")
      assertThat(awaitItem().bankName).isEqualTo("Swedbank")
    }
  }

  @Test
  fun `an account number can only be saved with 10 to 17 digits`() {
    assertThat(initialState("123456789").canSave).isFalse()
    assertThat(initialState("1234567890").canSave).isTrue()
    assertThat(initialState("12345678901234567").canSave).isTrue()
    assertThat(initialState("123456789012345678").canSave).isFalse()
    assertThat(initialState("1234567890").copy(isLoading = true).canSave).isFalse()
  }

  @Test
  fun `a saved account is shown connected`() = runTest {
    val cacheManager = FakeNetworkCacheManager()
    presenter(cacheManager).test(initialState("83271234567")) {
      awaitItem()
      sendEvent(EditBankAccountEvent.Save)
      assertThat(awaitItem().isLoading).isTrue()
      registerSetup("83271234567", PaymentMethodSetupStatus.ACTIVE, errorMessage = null)
      val connected = awaitItem()
      assertThat(connected.isLoading).isFalse()
      assertThat(connected.isConnected).isTrue()
      assertThat(cacheManager.clears).isEqualTo(1)
    }
  }

  @Test
  fun `a failed save shows the error until the member edits the number`() = runTest {
    presenter().test(initialState("83271234567")) {
      awaitItem()
      sendEvent(EditBankAccountEvent.Save)
      assertThat(awaitItem().isLoading).isTrue()
      registerSetup("83271234567", PaymentMethodSetupStatus.FAILED, errorMessage = "Unknown account")
      val failed = awaitItem()
      assertThat(failed.errorMessage).isEqualTo("Unknown account")
      assertThat(failed.isConnected).isFalse()

      failed.accountNumberState.setTextAndPlaceCursorAtEnd("83271234568")
      assertThat(awaitItem().errorMessage).isNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a failure without a message falls back to a generic one`() = runTest {
    presenter().test(initialState("83271234567")) {
      awaitItem()
      sendEvent(EditBankAccountEvent.Save)
      assertThat(awaitItem().isLoading).isTrue()
      registerSetup("83271234567", PaymentMethodSetupStatus.FAILED, errorMessage = null)
      assertThat(awaitItem().errorMessage).isEqualTo("Failed to set up payout method")
    }
  }

  @Test
  fun `finishing leaves the whole payout setup flow`() = runTest {
    val backstack = TestBackstack(TestRootKey, SelectPayoutMethodKey(listOf("NORDEA")))
    presenter(backstack = backstack).test(initialState()) {
      awaitItem()
      sendEvent(EditBankAccountEvent.FinishSetup)
      this@runTest.testScheduler.advanceUntilIdle()
      assertThat(backstack.entries).containsExactly(TestRootKey)
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun presenter(
    cacheManager: FakeNetworkCacheManager = FakeNetworkCacheManager(),
    backstack: TestBackstack = TestBackstack(TestRootKey),
  ) = EditBankAccountPresenter(SetupNordeaPayoutUseCase(apolloClient, cacheManager), backstack)

  private fun registerSetup(accountNumber: String, status: PaymentMethodSetupStatus, errorMessage: String?) {
    apolloClient.registerSuspendingTestResponse(
      SetupNordeaPayoutMutation(accountNumber = accountNumber),
      SetupNordeaPayoutMutation.Data(OctopusFakeResolver) {
        paymentMethodSetupNordeaPayout = buildPaymentMethodSetupOutput {
          this.status = status
          error = errorMessage?.let { buildUserError { message = it } }
        }
      },
    )
  }
}
