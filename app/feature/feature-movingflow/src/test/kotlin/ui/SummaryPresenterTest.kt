package ui

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
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
import com.hedvig.android.feature.movingflow.ui.summary.SummaryInfo
import com.hedvig.android.feature.movingflow.ui.summary.SummaryPresenter
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Loading
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
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
    val repo = FakeMovingFlowRepository()
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
    val repo = FakeMovingFlowRepository()
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
    val repo = FakeMovingFlowRepository()
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
}
