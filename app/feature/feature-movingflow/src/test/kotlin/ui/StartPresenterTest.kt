package ui

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.feature.movingflow.data.HousingType
import com.hedvig.android.feature.movingflow.ui.start.StartEvent
import com.hedvig.android.feature.movingflow.ui.start.StartPresenter
import com.hedvig.android.feature.movingflow.ui.start.StartUiState
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.feature.movingflow.MoveIntentV2CreateMutation
import octopus.feature.movingflow.fragment.MoveIntentFragment
import octopus.type.MoveExtraBuildingType
import octopus.type.buildMoveAddress
import octopus.type.buildMoveIntent
import octopus.type.buildMoveIntentMutationOutput
import octopus.type.buildUserError
import org.junit.Rule
import org.junit.Test

internal class StartPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  val repo = FakeMovingFlowRepository()

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithGoodResponse: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = MoveIntentV2CreateMutation(),
        data = MoveIntentV2CreateMutation.Data(OctopusFakeResolver) {
          moveIntentCreate = buildMoveIntentMutationOutput {
            moveIntent = buildMoveIntent {
              id = "ididid"
              minMovingDate = LocalDate(2026, 1, 1)
              maxMovingDate = LocalDate(2025, 1, 1)
              maxHouseNumberCoInsured = 6
              maxHouseSquareMeters = 200
              maxApartmentNumberCoInsured = 6
              maxApartmentSquareMeters = 200
              isApartmentAvailableforStudent = false
              extraBuildingTypes = listOf(MoveExtraBuildingType.GREENHOUSE)
              suggestedNumberCoInsured = 2
              currentHomeAddresses = buildList {
                add(
                  0,
                  buildMoveAddress {
                    id = "adsressid"
                    oldAddressCoverageDurationDays = 30
                  },
                )
              }
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
        operation = MoveIntentV2CreateMutation(),
        data = MoveIntentV2CreateMutation.Data(OctopusFakeResolver) {
          moveIntentCreate = buildMoveIntentMutationOutput {
            moveIntent = null
            userError = buildUserError {
              message = "Bad but readable error"
            }
          }
        },
      )
    }

  @Test
  fun `test housing type selection updates state`() = runTest {
    val presenter = StartPresenter(
      apolloClient = apolloClientWithGoodResponse,
      movingFlowRepository = repo,
    )
    presenter.test(StartUiState.Loading) {
      skipItems(1)
      assertThat(awaitItem()).isInstanceOf(StartUiState.Content::class)
        .prop(StartUiState.Content::selectedHousingType)
        .isEqualTo(HousingType.entries.first())
      sendEvent(StartEvent.SelectHousingType(HousingType.Villa))
      val result = awaitItem()
      assertThat(result).isInstanceOf(StartUiState.Content::class)
        .prop(StartUiState.Content::selectedHousingType)
        .isEqualTo(HousingType.Villa)
    }
  }

  @Test
  fun `test submit housing type triggers loading state of button and initiates moving flow `() = runTest {
    val presenter = StartPresenter(
      apolloClient = apolloClientWithGoodResponse,
      movingFlowRepository = repo,
    )
    presenter.test(StartUiState.Loading) {
      skipItems(2)
      sendEvent(StartEvent.SelectHousingType(HousingType.Villa))
      skipItems(1)
      sendEvent(StartEvent.SubmitHousingType)
      assertThat(awaitItem()).isInstanceOf(StartUiState.Content::class)
        .prop(StartUiState.Content::buttonLoading)
        .isTrue()
      assertThat(repo.movingFlowInitiatedTurbine.awaitItem()).isTrue()
      assertThat(awaitItem()).isInstanceOf(StartUiState.Content::class)
        .prop(StartUiState.Content::navigateToNextStep)
        .isTrue()
    }
  }

  @Test
  fun `after navigating clear navigateToNextStep`() = runTest {
    val presenter = StartPresenter(
      apolloClient = apolloClientWithGoodResponse,
      movingFlowRepository = repo,
    )
    presenter.test(StartUiState.Loading) {
      skipItems(2)
      sendEvent(StartEvent.SelectHousingType(HousingType.Villa))
      skipItems(1)
      sendEvent(StartEvent.SubmitHousingType)
      skipItems(2)
      sendEvent(StartEvent.NavigatedToNextStep)
      assertThat(awaitItem()).isInstanceOf(StartUiState.Content::class)
        .prop(StartUiState.Content::navigateToNextStep)
        .isFalse()
    }
  }

  @Test
  fun `when apollo returns error with message show error section and on dismissing error reload data`() = runTest {
    val presenter = StartPresenter(
      apolloClient = apolloClientWithBadResponse,
      movingFlowRepository = repo,
    )
    presenter.test(StartUiState.Loading) {
      skipItems(1)
      assertThat(awaitItem()).isInstanceOf(StartUiState.StartError.UserPresentable::class)
      sendEvent(StartEvent.DismissStartError)
      assertThat(awaitItem()).isInstanceOf(StartUiState.Loading::class)
      skipItems(1)
    }
  }

  @Test
  fun `when apollo return good response show correct data`() = runTest {
    val presenter = StartPresenter(
      apolloClient = apolloClientWithGoodResponse,
      movingFlowRepository = repo,
    )
    presenter.test(StartUiState.Loading) {
      skipItems(1)
      assertThat(awaitItem()).isInstanceOf(StartUiState.Content::class)
        .prop(StartUiState.Content::initiatedMovingIntent)
        .prop(MoveIntentFragment::id)
        .isEqualTo("ididid")
    }
  }
}
