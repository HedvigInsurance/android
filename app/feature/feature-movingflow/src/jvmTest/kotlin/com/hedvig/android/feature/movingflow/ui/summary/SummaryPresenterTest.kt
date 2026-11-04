package com.hedvig.android.feature.movingflow.ui.summary

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.data.cross.sell.after.flow.CrossSellInfoType
import com.hedvig.android.feature.movingflow.HousingTypeKey
import com.hedvig.android.feature.movingflow.SelectContractForMovingKey
import com.hedvig.android.feature.movingflow.SuccessfulMoveKey
import com.hedvig.android.feature.movingflow.SummaryKey
import com.hedvig.android.feature.movingflow.TEST_MOVE_INTENT_ID
import com.hedvig.android.feature.movingflow.TestBackstack
import com.hedvig.android.feature.movingflow.awaitCondition
import com.hedvig.android.feature.movingflow.data.AddonId
import com.hedvig.android.feature.movingflow.data.HousingType
import com.hedvig.android.feature.movingflow.data.MovingFlowState
import com.hedvig.android.feature.movingflow.data.toMovingFlowQuotes
import com.hedvig.android.feature.movingflow.storage.MovingFlowRepository
import com.hedvig.android.feature.movingflow.storage.MovingFlowStorage
import com.hedvig.android.feature.movingflow.testMoveAddonQuote
import com.hedvig.android.feature.movingflow.testMoveHomeQuote
import com.hedvig.android.feature.movingflow.testMoveIntentQuotesFragment
import com.hedvig.android.feature.movingflow.testMoveMtaQuote
import com.hedvig.android.feature.movingflow.testMovingFlowStateWithPropertyInput
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Content
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Content.SubmitError
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.MoleculePresenterTestContext
import com.hedvig.android.molecule.test.test
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.builder.Data
import octopus.builder.buildMoveIntentMutationOutput
import octopus.builder.buildUserError
import octopus.feature.movingflow.MoveIntentCostQuery
import octopus.feature.movingflow.MoveIntentV2CommitMutation
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ApolloExperimental::class)
class SummaryPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  @get:Rule
  val testFolder = TemporaryFolder()

  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  private val summaryKey = SummaryKey(moveIntentId = TEST_MOVE_INTENT_ID, homeQuoteId = "quote")

  private val stateWithQuotes: MovingFlowState = testMovingFlowStateWithPropertyInput(HousingType.Villa).copy(
    movingFlowQuotes = testMoveIntentQuotesFragment(
      homeQuotes = listOf(
        testMoveHomeQuote(
          id = "quote",
          addons = listOf(testMoveAddonQuote("kept"), testMoveAddonQuote("excluded")),
        ),
        testMoveHomeQuote(id = "other quote"),
      ),
      mtaQuotes = listOf(testMoveMtaQuote(exposureName = "Car ABC123")),
    ).toMovingFlowQuotes(),
  )

  private val commitMutation = MoveIntentV2CommitMutation(
    intentId = TEST_MOVE_INTENT_ID,
    homeQuoteId = "quote",
    excludedAddons = listOf("excluded"),
  )

  private suspend fun TestScope.repositoryWith(state: MovingFlowState?): MovingFlowRepository {
    val storage = MovingFlowStorage(
      TestPreferencesDataStore(
        datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
        coroutineScope = CoroutineScope(backgroundScope.coroutineContext),
      ),
    )
    if (state != null) storage.setMovingFlowState(state)
    return MovingFlowRepository(storage).apply {
      changeHomeAddonExclusion(AddonId("excluded"), exclude = true)
    }
  }

  private fun presenter(
    repository: MovingFlowRepository,
    backstack: TestBackstack = TestBackstack(),
    crossSellAfterFlowRepository: CrossSellAfterFlowRepository = FakeCrossSellAfterFlowRepository(),
    key: SummaryKey = summaryKey,
  ) = SummaryPresenter(
    summaryRoute = key,
    movingFlowRepository = repository,
    apolloClient = apolloClient,
    crossSellAfterFlowRepository = crossSellAfterFlowRepository,
    getMoveIntentCostUseCase = GetMoveIntentCostUseCase(apolloClient),
    backstack = backstack,
  )

  private fun registerCost() {
    apolloClient.registerTestResponse(
      MoveIntentCostQuery(TEST_MOVE_INTENT_ID, "quote", listOf("kept")),
      testMoveIntentCostData(quoteId = "quote"),
    )
  }

  private suspend fun MoleculePresenterTestContext<*, SummaryUiState>.awaitContentWithCost(): Content {
    while (true) {
      val state = awaitItem()
      if (state is Content && state.totalPremium != null) return state
    }
  }

  @Test
  fun `without an ongoing flow an error is shown`() = runTest {
    presenter(repositoryWith(null)).test(SummaryUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(SummaryUiState.Loading)
      assertThat(awaitItem()).isEqualTo(SummaryUiState.Error)
    }
  }

  @Test
  fun `an unknown home quote shows an error`() = runTest {
    presenter(
      repository = repositoryWith(stateWithQuotes),
      key = SummaryKey(moveIntentId = TEST_MOVE_INTENT_ID, homeQuoteId = "unknown"),
    ).test(SummaryUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(SummaryUiState.Loading)
      assertThat(awaitItem()).isEqualTo(SummaryUiState.Error)
    }
  }

  @Test
  fun `the selected home quote and the mta quotes are shown with the fetched cost`() = runTest {
    registerCost()

    presenter(repositoryWith(stateWithQuotes)).test(SummaryUiState.Loading) {
      val content = awaitContentWithCost()

      assertThat(content.totalPremium).isEqualTo(UiMoney(250.0, UiCurrencyCode.SEK))
      assertThat(content.grossPremium).isEqualTo(UiMoney(300.0, UiCurrencyCode.SEK))
      assertThat(content.movingStartDate).isEqualTo(LocalDate(2026, 2, 1))
      assertThat(content.hasMtaQuotes).isEqualTo(true)
      assertThat(content.shouldDisableInput).isEqualTo(false)
      assertThat(content.cards.map { it.subtitle }).containsExactly("New street 1", "Car ABC123")
      val homeCard = content.cards.first()
      assertThat(homeCard.premium).isEqualTo(UiMoney(170.0, UiCurrencyCode.SEK))
      assertThat(homeCard.previousPremium).isEqualTo(UiMoney(200.0, UiCurrencyCode.SEK))
      // The quote itself, the one addon still included, and the discount from the fetched cost.
      assertThat(homeCard.costBreakdown.size).isEqualTo(3)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `confirming commits the move without the excluded addons and finishes the flow`() = runTest {
    registerCost()
    apolloClient.registerTestResponse(
      commitMutation,
      MoveIntentV2CommitMutation.Data(OctopusFakeResolver) {
        moveIntentCommit = buildMoveIntentMutationOutput {
          userError = null
          newContractId = "newContractId"
        }
      },
    )
    val crossSellAfterFlowRepository = FakeCrossSellAfterFlowRepository()
    val backstack = TestBackstack(
      mutableListOf(PreviousScreenKey, SelectContractForMovingKey(), HousingTypeKey(TEST_MOVE_INTENT_ID), summaryKey),
    )

    presenter(repositoryWith(stateWithQuotes), backstack, crossSellAfterFlowRepository).test(SummaryUiState.Loading) {
      awaitContentWithCost()

      sendEvent(SummaryEvent.ConfirmChanges)
      awaitCondition { backstack.entries.lastOrNull() is SuccessfulMoveKey }
      cancelAndIgnoreRemainingEvents()
    }

    assertThat(backstack.entries).containsExactly(PreviousScreenKey, SuccessfulMoveKey(LocalDate(2026, 2, 1)))
    assertThat(crossSellAfterFlowRepository.completedTypes)
      .containsExactly(CrossSellInfoType.MovingFlow("newContractId"))
  }

  @Test
  fun `finishing a flow that skipped contract selection still pops the whole flow`() = runTest {
    registerCost()
    apolloClient.registerTestResponse(
      commitMutation,
      MoveIntentV2CommitMutation.Data(OctopusFakeResolver) {
        moveIntentCommit = buildMoveIntentMutationOutput {
          userError = null
          newContractId = "newContractId"
        }
      },
    )
    val backstack = TestBackstack(mutableListOf(PreviousScreenKey, HousingTypeKey(TEST_MOVE_INTENT_ID), summaryKey))

    presenter(repositoryWith(stateWithQuotes), backstack).test(SummaryUiState.Loading) {
      awaitContentWithCost()

      sendEvent(SummaryEvent.ConfirmChanges)
      awaitCondition { backstack.entries.lastOrNull() is SuccessfulMoveKey }
      cancelAndIgnoreRemainingEvents()
    }

    assertThat(backstack.entries).containsExactly(PreviousScreenKey, SuccessfulMoveKey(LocalDate(2026, 2, 1)))
  }

  @Test
  fun `a failed commit shows a generic error that can be dismissed`() = runTest {
    registerCost()
    apolloClient.registerTestResponse(commitMutation, errors = listOf(Error.Builder("error").build()))
    val crossSellAfterFlowRepository = FakeCrossSellAfterFlowRepository()
    val backstack = TestBackstack(mutableListOf(summaryKey))

    presenter(repositoryWith(stateWithQuotes), backstack, crossSellAfterFlowRepository).test(SummaryUiState.Loading) {
      awaitContentWithCost()

      sendEvent(SummaryEvent.ConfirmChanges)
      val failed = awaitSubmitError()
      assertThat(failed.submitError).isEqualTo(SubmitError.Generic)
      assertThat(failed.shouldDisableInput).isEqualTo(true)

      sendEvent(SummaryEvent.DismissSubmissionError)
      assertThat((awaitItem() as Content).submitError).isNull()
    }

    assertThat(backstack.entries).containsExactly(summaryKey)
    assertThat(crossSellAfterFlowRepository.completedTypes).isEqualTo(emptyList())
  }

  @Test
  fun `a commit user error is shown with its message`() = runTest {
    registerCost()
    apolloClient.registerTestResponse(
      commitMutation,
      MoveIntentV2CommitMutation.Data(OctopusFakeResolver) {
        moveIntentCommit = buildMoveIntentMutationOutput {
          userError = buildUserError { message = "Move could not be completed" }
          newContractId = null
        }
      },
    )

    presenter(repositoryWith(stateWithQuotes)).test(SummaryUiState.Loading) {
      awaitContentWithCost()

      sendEvent(SummaryEvent.ConfirmChanges)

      assertThat(awaitSubmitError().submitError).isEqualTo(SubmitError.WithMessage("Move could not be completed"))
    }
  }

  private suspend fun MoleculePresenterTestContext<*, SummaryUiState>.awaitSubmitError(): Content {
    while (true) {
      val state = awaitItem()
      if (state is Content && state.submitError != null && !state.isSubmitting) return state
    }
  }
}

private data object PreviousScreenKey : HedvigNavKey

private class FakeCrossSellAfterFlowRepository : CrossSellAfterFlowRepository {
  val completedTypes = mutableListOf<CrossSellInfoType>()

  override fun shouldShowCrossSellSheetWithInfo(): Flow<CrossSellInfoType?> = emptyFlow()

  override fun completedCrossSellTriggeringSelfServiceSuccessfully(type: CrossSellInfoType) {
    completedTypes.add(type)
  }

  override fun showedCrossSellSheet(type: CrossSellInfoType?) {}
}
