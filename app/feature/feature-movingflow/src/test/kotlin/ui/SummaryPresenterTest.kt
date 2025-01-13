package ui

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.movingflow.MovingFlowDestinations.Summary
import com.hedvig.android.feature.movingflow.data.HousingType
import com.hedvig.android.feature.movingflow.data.MovingFlowState
import com.hedvig.android.feature.movingflow.data.MovingFlowState.AddressInfo
import com.hedvig.android.feature.movingflow.ui.summary.SummaryEvent
import com.hedvig.android.feature.movingflow.ui.summary.SummaryInfo
import com.hedvig.android.feature.movingflow.ui.summary.SummaryPresenter
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Loading
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.feature.movingflow.MoveIntentV2CommitMutation
import octopus.type.buildMoveIntent
import octopus.type.buildMoveIntentMutationOutput
import octopus.type.buildUserError
import org.junit.Rule
import org.junit.Test

internal class SummaryPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  private val summaryRoute = Summary(moveIntentIdFake, homeQuoteIdFake)

  val repo = FakeMovingFlowRepository()

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithGoodResponse: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = MoveIntentV2CommitMutation(
          intentId = moveIntentIdFake,
          homeQuoteId = homeQuoteIdFake,
        ),
        data = MoveIntentV2CommitMutation.Data(OctopusFakeResolver) {
          moveIntentCommit = buildMoveIntentMutationOutput {
            moveIntent = buildMoveIntent {
              id = moveIntentIdFake
            }
            userError = null
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithBadResponse: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = MoveIntentV2CommitMutation(
          intentId = moveIntentIdFake,
          homeQuoteId = homeQuoteIdFake,
        ),
        errors = listOf(com.apollographql.apollo.api.Error.Builder(message = "Bad error message").build()),
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithUserError: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = MoveIntentV2CommitMutation(
          intentId = moveIntentIdFake,
          homeQuoteId = homeQuoteIdFake,
        ),
        data = MoveIntentV2CommitMutation.Data(OctopusFakeResolver) {
          moveIntentCommit = buildMoveIntentMutationOutput {
            userError = buildUserError {
              message = "Bad user error message"
            }
          }
        },
      )
    }

  @Test
  fun `the uiState total is correctly calculated with only home quote`() = runTest {
    val presenter = SummaryPresenter(
      summaryRoute = summaryRoute,
      movingFlowRepository = repo,
      apolloClient = apolloClientWithGoodResponse,
    )

    val totalPremiumOnlyHomeQuote = fakeMovingStateWithOnlyHomeQuote.movingFlowQuotes?.homeQuotes[0]?.premium
    presenter.test(Loading) {
      repo.movingFlowStateTurbine.add(fakeMovingStateWithOnlyHomeQuote)
      assertThat(awaitItem()).isInstanceOf(SummaryUiState.Loading::class)
      val state = awaitItem()
      assertThat(state)
        .isInstanceOf(SummaryUiState.Content::class)
        .prop(SummaryUiState.Content::summaryInfo)
        .prop(SummaryInfo::totalPremium)
        .isEqualTo(totalPremiumOnlyHomeQuote)
    }
  }

  @Test
  fun `the uiState total is correctly calculated with no addons`() = runTest {
    val presenter = SummaryPresenter(
      summaryRoute = summaryRoute,
      movingFlowRepository = repo,
      apolloClient = apolloClientWithGoodResponse,
    )
    val noAddonsAmount = listOf(
      fakeMovingStateNoAddons.movingFlowQuotes!!.homeQuotes[0].premium.amount,
      fakeMovingStateNoAddons.movingFlowQuotes.mtaQuotes[0].premium.amount,
      fakeMovingStateNoAddons.movingFlowQuotes.mtaQuotes[1].premium.amount,
    ).sum()
    val totalPremiumNoAddons = UiMoney(noAddonsAmount, UiCurrencyCode.SEK)
    presenter.test(Loading) {
      repo.movingFlowStateTurbine.add(fakeMovingStateNoAddons)
      assertThat(awaitItem()).isInstanceOf(SummaryUiState.Loading::class)
      val state = awaitItem()
      assertThat(state)
        .isInstanceOf(SummaryUiState.Content::class)
        .prop(SummaryUiState.Content::summaryInfo)
        .prop(SummaryInfo::totalPremium)
        .isEqualTo(totalPremiumNoAddons)
    }
  }

  @Test
  fun `the uiState total is correctly calculated with 2 addons`() = runTest {
    val presenter = SummaryPresenter(
      summaryRoute = summaryRoute,
      movingFlowRepository = repo,
      apolloClient = apolloClientWithGoodResponse,
    )

    val twoAddonsAmount = listOf(
      fakeMovingStateWithTwoAddons.movingFlowQuotes!!.homeQuotes[0].premium.amount,
      fakeMovingStateWithTwoAddons.movingFlowQuotes.homeQuotes[0].relatedAddonQuotes[0].premium.amount,
      fakeMovingStateWithTwoAddons.movingFlowQuotes.mtaQuotes[0].premium.amount,
      fakeMovingStateWithTwoAddons.movingFlowQuotes.mtaQuotes[1].premium.amount,
      fakeMovingStateWithTwoAddons.movingFlowQuotes.mtaQuotes[1].relatedAddonQuotes[0].premium.amount,
    ).sum()
    val totalPremiumTwoAddons = UiMoney(twoAddonsAmount, UiCurrencyCode.SEK)
    presenter.test(Loading) {
      repo.movingFlowStateTurbine.add(fakeMovingStateWithTwoAddons)
      assertThat(awaitItem()).isInstanceOf(SummaryUiState.Loading::class)
      val state = awaitItem()
      assertThat(state)
        .isInstanceOf(SummaryUiState.Content::class)
        .prop(SummaryUiState.Content::summaryInfo)
        .prop(SummaryInfo::totalPremium)
        .isEqualTo(totalPremiumTwoAddons)
    }
  }

  @Test
  fun `if submit ends with error show error dialog`() = runTest {
    val presenter = SummaryPresenter(
      summaryRoute = summaryRoute,
      movingFlowRepository = repo,
      apolloClient = apolloClientWithBadResponse,
    )
    presenter.test(Loading) {
      repo.movingFlowStateTurbine.add(fakeMovingStateWithTwoAddons)
      sendEvent(SummaryEvent.ConfirmChanges)
      skipItems(3)
      val state = awaitItem()
      assertThat(state)
        .isInstanceOf(SummaryUiState.Content::class)
        .prop(SummaryUiState.Content::submitError)
        .isNotNull()
      assertThat(state)
        .isInstanceOf(SummaryUiState.Content::class)
        .prop(SummaryUiState.Content::navigateToFinishedScreenWithDate)
        .isNull()
    }
  }

  @Test
  fun `if submit ends with user error show error dialog`() = runTest {
    val presenter = SummaryPresenter(
      summaryRoute = summaryRoute,
      movingFlowRepository = repo,
      apolloClient = apolloClientWithUserError,
    )
    presenter.test(Loading) {
      repo.movingFlowStateTurbine.add(fakeMovingStateWithTwoAddons)
      sendEvent(SummaryEvent.ConfirmChanges)
      skipItems(3)
      val state = awaitItem()
      assertThat(state)
        .isInstanceOf(SummaryUiState.Content::class)
        .prop(SummaryUiState.Content::submitError)
        .isNotNull()
      assertThat(state)
        .isInstanceOf(SummaryUiState.Content::class)
        .prop(SummaryUiState.Content::navigateToFinishedScreenWithDate)
        .isNull()
    }
  }

  @Test
  fun `if submit ends with success navigate further`() = runTest {
    val presenter = SummaryPresenter(
      summaryRoute = summaryRoute,
      movingFlowRepository = repo,
      apolloClient = apolloClientWithGoodResponse,
    )
    presenter.test(Loading) {
      repo.movingFlowStateTurbine.add(fakeMovingStateWithTwoAddons)
      sendEvent(SummaryEvent.ConfirmChanges)
      skipItems(3)
      val state = awaitItem()
      assertThat(state)
        .isInstanceOf(SummaryUiState.Content::class)
        .prop(SummaryUiState.Content::submitError)
        .isNull()
      assertThat(state)
        .isInstanceOf(SummaryUiState.Content::class)
        .prop(SummaryUiState.Content::navigateToFinishedScreenWithDate)
        .isNotNull()
    }
  }

  @Test
  fun `if the matching quote is found in the repo show correct content`() = runTest {
    val presenter = SummaryPresenter(
      summaryRoute = summaryRoute,
      movingFlowRepository = repo,
      apolloClient = apolloClientWithGoodResponse,
    )
    presenter.test(Loading) {
      repo.movingFlowStateTurbine.add(fakeMovingStateWithTwoAddons)
      skipItems(1)
      val state = awaitItem()
      assertThat(state)
        .isInstanceOf(SummaryUiState.Content::class)
        .prop(SummaryUiState.Content::summaryInfo)
        .isEqualTo(fakeSummaryInfoWithTwoAddons)
    }
  }

  @Test
  fun `if the quote with this id is not in the repo show error screen`() = runTest {
    val presenter = SummaryPresenter(
      summaryRoute = Summary("bad_id", "even_worse_id"),
      movingFlowRepository = repo,
      apolloClient = apolloClientWithGoodResponse,
    )
    presenter.test(Loading) {
      repo.movingFlowStateTurbine.add(fakeMovingStateWithTwoAddons)
      skipItems(1)
      val state = awaitItem()
      assertThat(state)
        .isInstanceOf(SummaryUiState.Error::class)
    }
  }

  @Test
  fun `if there are no quotes in the repo show error screen`() = runTest {
    val presenter = SummaryPresenter(
      summaryRoute = summaryRoute,
      movingFlowRepository = repo,
      apolloClient = apolloClientWithGoodResponse,
    )
    presenter.test(Loading) {
      repo.movingFlowStateTurbine.add(
        MovingFlowState(
          id = moveIntentIdFake,
          moveFromAddressId = "id",
          housingType = HousingType.ApartmentOwn,
          addressInfo = AddressInfo("street", "18888"),
          movingDateState = MovingFlowState.MovingDateState(
            selectedMovingDate = null,
            allowedMovingDateRange = LocalDate(2025, 1, 1)..LocalDate(2025, 3, 1),
          ),
          propertyState = fakePropertyStateBRF,
          movingFlowQuotes = null,
          lastSelectedHomeQuoteId = null,
          oldAddressCoverageDurationDays = 30,
        ),
      )
      skipItems(1)
      val state = awaitItem()
      assertThat(state)
        .isInstanceOf(SummaryUiState.Error::class)
    }
  }

  @Test
  fun `when click on confirm button launch submitting quotes and show loading button`() = runTest {
    val presenter = SummaryPresenter(
      summaryRoute = summaryRoute,
      movingFlowRepository = repo,
      apolloClient = apolloClientWithGoodResponse,
    )
    presenter.test(Loading) {
      repo.movingFlowStateTurbine.add(fakeMovingStateWithTwoAddons)
      skipItems(1)
      val state1 = awaitItem()
      assertThat(state1)
        .isInstanceOf(SummaryUiState.Content::class)
        .prop(SummaryUiState.Content::isSubmitting).isFalse()
      sendEvent(SummaryEvent.ConfirmChanges)
      val state2 = awaitItem()
      assertThat(state2)
        .isInstanceOf(SummaryUiState.Content::class)
        .prop(SummaryUiState.Content::isSubmitting).isTrue()
      val state3 = awaitItem()
      assertThat(state3)
        .isInstanceOf(SummaryUiState.Content::class)
        .prop(SummaryUiState.Content::isSubmitting).isFalse()
    }
  }

  @Test
  fun `when click on close error dialog it does not show`() = runTest {
    val presenter = SummaryPresenter(
      summaryRoute = summaryRoute,
      movingFlowRepository = repo,
      apolloClient = apolloClientWithBadResponse,
    )
    presenter.test(Loading) {
      repo.movingFlowStateTurbine.add(fakeMovingStateWithTwoAddons)
      sendEvent(SummaryEvent.ConfirmChanges)
      skipItems(3)
      assertThat(awaitItem())
        .isInstanceOf(SummaryUiState.Content::class)
        .prop(SummaryUiState.Content::submitError).isNotNull()
      sendEvent(SummaryEvent.DismissSubmissionError)
      assertThat(awaitItem())
        .isInstanceOf(SummaryUiState.Content::class)
        .prop(SummaryUiState.Content::submitError).isNull()
    }
  }
}
