package com.hedvig.android.feature.movingflow.ui.selectcontract

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.android.feature.movingflow.HousingTypeKey
import com.hedvig.android.feature.movingflow.MovingSource
import com.hedvig.android.feature.movingflow.SelectContractForMovingKey
import com.hedvig.android.feature.movingflow.TEST_MOVE_INTENT_ID
import com.hedvig.android.feature.movingflow.TestBackstack
import com.hedvig.android.feature.movingflow.awaitCondition
import com.hedvig.android.feature.movingflow.data.MovingFlowState
import com.hedvig.android.feature.movingflow.moveIntentCreateData
import com.hedvig.android.feature.movingflow.storage.MovingFlowRepository
import com.hedvig.android.feature.movingflow.storage.MovingFlowStorage
import com.hedvig.android.feature.movingflow.testMoveAddress
import com.hedvig.android.feature.movingflow.ui.selectcontract.SelectContractState.NotEmpty.Content
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import octopus.builder.Data
import octopus.builder.buildMoveIntentMutationOutput
import octopus.builder.buildUserError
import octopus.feature.movingflow.MoveIntentV2CreateMutation
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ApolloExperimental::class)
class SelectContractPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  @get:Rule
  val testFolder = TemporaryFolder()

  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  private fun TestScope.repository() = MovingFlowRepository(
    MovingFlowStorage(
      TestPreferencesDataStore(
        datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
        coroutineScope = CoroutineScope(backgroundScope.coroutineContext),
      ),
    ),
  )

  private fun presenter(
    repository: MovingFlowRepository,
    backstack: TestBackstack,
    movingSource: MovingSource = MovingSource.INSURANCE,
  ) = SelectContractPresenter(apolloClient, repository, backstack, movingSource)

  @Test
  fun `a network error shows a generic error`() = runTest {
    apolloClient.registerTestResponse(
      MoveIntentV2CreateMutation(),
      errors = listOf(Error.Builder("error").build()),
    )

    presenter(repository(), TestBackstack()).test(SelectContractState.Loading) {
      assertThat(awaitItem()).isEqualTo(SelectContractState.Loading)
      assertThat(awaitItem()).isInstanceOf<SelectContractState.Error.GenericError>()
    }
  }

  @Test
  fun `a user error is shown with its message`() = runTest {
    apolloClient.registerTestResponse(
      MoveIntentV2CreateMutation(),
      MoveIntentV2CreateMutation.Data(OctopusFakeResolver) {
        moveIntentCreate = buildMoveIntentMutationOutput {
          moveIntent = null
          userError = buildUserError { message = "You can not move right now" }
        }
      },
    )

    presenter(repository(), TestBackstack()).test(SelectContractState.Loading) {
      skipItems(1)
      assertThat(awaitItem()).isEqualTo(SelectContractState.Error.UserPresentable("You can not move right now"))
    }
  }

  @Test
  fun `no current addresses shows a generic error`() = runTest {
    apolloClient.registerTestResponse(MoveIntentV2CreateMutation(), moveIntentCreateData(addresses = emptyList()))

    presenter(repository(), TestBackstack()).test(SelectContractState.Loading) {
      skipItems(1)
      assertThat(awaitItem()).isInstanceOf<SelectContractState.Error.GenericError>()
    }
  }

  @Test
  fun `a single address starts the flow and replaces the select screen`() = runTest {
    apolloClient.registerTestResponse(MoveIntentV2CreateMutation(), moveIntentCreateData())
    val repository = repository()
    val backstack = TestBackstack(mutableListOf(SelectContractForMovingKey(MovingSource.TERMINATION)))

    presenter(repository, backstack, MovingSource.TERMINATION).test(SelectContractState.Loading) {
      skipItems(1)
      assertThat(awaitItem()).isInstanceOf<SelectContractState.NotEmpty.Redirecting>()
        .transform { it.selectedAddress.id }
        .isEqualTo("addressId")
      awaitCondition { backstack.entries.lastOrNull() is HousingTypeKey }
      cancelAndIgnoreRemainingEvents()
    }

    assertThat(backstack.entries).containsExactly(HousingTypeKey(TEST_MOVE_INTENT_ID))
    val state = repository.movingFlowState().first()
    assertThat(state).isNotNull().prop(MovingFlowState::moveFromAddressId).isEqualTo("addressId")
    assertThat(state!!.movingSource).isEqualTo(MovingSource.TERMINATION)
  }

  @Test
  fun `with several addresses the user picks one before continuing`() = runTest {
    apolloClient.registerTestResponse(
      MoveIntentV2CreateMutation(),
      moveIntentCreateData(addresses = listOf(testMoveAddress("first"), testMoveAddress("second"))),
    )
    val repository = repository()
    val backstack = TestBackstack(mutableListOf(SelectContractForMovingKey()))

    presenter(repository, backstack).test(SelectContractState.Loading) {
      skipItems(1)
      val content = awaitItem() as Content
      assertThat(content.selectedAddress).isNull()
      assertThat(content.buttonLoading).isEqualTo(false)

      sendEvent(SelectContractEvent.SubmitContract)
      expectNoEvents()

      val second = content.intent.currentHomeAddresses.single { it.id == "second" }
      sendEvent(SelectContractEvent.SelectContract(second))
      assertThat(awaitItem()).isInstanceOf<Content>().prop(Content::selectedAddress).isEqualTo(second)

      sendEvent(SelectContractEvent.SubmitContract)
      awaitCondition { backstack.entries.lastOrNull() is HousingTypeKey }
      cancelAndIgnoreRemainingEvents()
    }

    assertThat(backstack.entries).containsExactly(SelectContractForMovingKey(), HousingTypeKey(TEST_MOVE_INTENT_ID))
    assertThat(repository.movingFlowState().first()?.moveFromAddressId).isEqualTo("second")
  }
}
