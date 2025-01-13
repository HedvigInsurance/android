package ui

import android.text.TextUtils
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.Optional.Present
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.feature.movingflow.compose.ValidatedInput
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressEvent
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressPresenter
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState
import com.hedvig.android.featureflags.test.FakeFeatureManager2
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import octopus.feature.movingflow.MoveIntentV2RequestMutation
import octopus.type.MoveApartmentSubType
import octopus.type.MoveApiVersion
import octopus.type.MoveIntentRequestInput
import octopus.type.MoveToAddressInput
import octopus.type.MoveToApartmentInput
import octopus.type.buildMoveIntent
import octopus.type.buildMoveIntentMutationOutput
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class EnterNewAddressPresenterTest {
  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Before
  fun setUp() {
    mockkStatic(TextUtils::class)
    every { TextUtils.isDigitsOnly(any()) } answers { true }
  }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithGoodResponseForFilledData: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = MoveIntentV2RequestMutation(
          fakeMovingStateBeforeHomeQuotesFilled.id,
          addonsFlagOn = true,
          moveIntentRequestInput = MoveIntentRequestInput(
            apiVersion = Present(value = MoveApiVersion.V2_TIERS_AND_DEDUCTIBLES),
            moveToAddress = MoveToAddressInput(
              street = fakeMovingStateBeforeHomeQuotesFilled.addressInfo.street!!,
              postalCode = fakeMovingStateBeforeHomeQuotesFilled.addressInfo.postalCode!!,
            ),
            moveFromAddressId = fakeMovingStateBeforeHomeQuotesFilled.moveFromAddressId,
            movingDate = fakeMovingStateBeforeHomeQuotesFilled.movingDateState.selectedMovingDate!!,
            numberCoInsured = fakeMovingStateBeforeHomeQuotesFilled.propertyState.numberCoInsuredState.selectedNumberCoInsured,
            squareMeters = fakeMovingStateBeforeHomeQuotesFilled.propertyState.squareMetersState.selectedSquareMeters!!,
            apartment = com.apollographql.apollo.api.Optional.present(
              MoveToApartmentInput(
                subType = MoveApartmentSubType.RENT,
                isStudent = false,
              ),
            ),
          ),
        ),
        data = MoveIntentV2RequestMutation.Data(OctopusFakeResolver) {
          moveIntentRequest = buildMoveIntentMutationOutput {
            moveIntent = buildMoveIntent {
              homeQuotes = emptyList()
              mtaQuotes = emptyList()
            }
            userError = null
          }
        },
      )
    }

//  data = MoveIntentV2RequestMutation.Data(OctopusFakeResolver) {
//    moveIntentRequest = buildMoveIntentMutationOutput {
//      moveIntent = null
//      userError = buildUserError {
//        message = "Somehow cannot get quotes for BRF"
//      }
//    }
//  },

  @Test
  fun `movingFlowState displays correctly`() = runTest {
    val repo = FakeMovingFlowRepository()
    val featureManager = FakeFeatureManager2(true)
    val sut = EnterNewAddressPresenter(
      apolloClient = apolloClientWithGoodResponseForFilledData,
      featureManager = featureManager,
      movingFlowRepository = repo,
      moveIntentId = moveIntentIdFake,
    )
    sut.test(EnterNewAddressUiState.Loading) {
      skipItems(1)
      repo.movingFlowStateTurbine.add(fakeMovingStateBeforeHomeQuotes)
      val result = awaitItem()
      assertThat(result).isInstanceOf(EnterNewAddressUiState.Content::class)
        .prop(EnterNewAddressUiState.Content::moveFromAddressId)
        .isEqualTo("moveFromAddressId")
      assertThat(result).isInstanceOf(EnterNewAddressUiState.Content::class)
        .prop(EnterNewAddressUiState.Content::address)
        .isInstanceOf(ValidatedInput::class)
    }
  }

  @Test
  fun `navigating to chose coverage clears navigation flag`() = runTest {
    val repo = FakeMovingFlowRepository()
    val featureManager = FakeFeatureManager2(true)
    val sut = EnterNewAddressPresenter(
      apolloClient = apolloClientWithGoodResponseForFilledData,
      featureManager = featureManager,
      movingFlowRepository = repo,
      moveIntentId = moveIntentIdFake,
    )
    sut.test(EnterNewAddressUiState.Loading) {
      skipItems(1)
      repo.movingFlowStateTurbine.add(fakeMovingStateBeforeHomeQuotesFilled)
      skipItems(1)
      sendEvent(EnterNewAddressEvent.Submit)
      skipItems(1)
      assertThat(awaitItem()).isInstanceOf(EnterNewAddressUiState.Content::class)
        .prop(EnterNewAddressUiState.Content::navigateToChoseCoverage)
        .isTrue()
      sendEvent(EnterNewAddressEvent.NavigatedToChoseCoverage)
      val result = awaitItem()
      assertThat(result).isInstanceOf(EnterNewAddressUiState.Content::class)
        .prop(EnterNewAddressUiState.Content::navigateToChoseCoverage)
        .isFalse()
    }
    // Simulate navigation to chose coverage and verify that navigateToChoseCoverage is reset to false
  }

  @Test
  fun `navigating to add house information clears navigation flag`() = runTest {
    // Simulate navigation to add house information and verify that navigateToAddHouseInformation is reset to false
  }

  @Test
  fun `dismiss submission error clears error state`() = runTest {
    // Simulate dismissing a submission error and verify that submittingInfoFailure is set to null
  }

  @Test
  fun `submit with valid content triggers repository update`() = runTest {
    // Simulate a Submit event with valid content and verify that movingFlowRepository is updated with the correct data
  }

  @Test
  fun `submit with not valid content does nothing`() = runTest {
    // Simulate a Submit event with valid content and verify that movingFlowRepository is updated with the correct data
  }

  @Test
  fun `submit navigates to add house information for house property type`() = runTest {
    // Simulate a Submit event with a house property type and verify that navigateToAddHouseInformation is set to true
  }

  @Test
  fun `submit triggers move intent request for apartment property type mapped correctly`() = runTest {
    // Simulate a Submit event with an apartment property type and verify that inputForSubmission is set correctly
  }

  @Test
  fun `if move intent request gets good response navigate to choose coverage`() = runTest {
    // Simulate a scenario where inputForSubmission is set, and verify that the mutation is executed and a successful response is handled correctly
  }

  @Test
  fun `if move intent request gets user error with message show error section with this message`() = runTest {
    // Simulate a scenario where inputForSubmission is set, and verify that the mutation handles network failure correctly
  }

  @Test
  fun `if move intent request gets bad response without specific error show general error section`() = runTest {
    // Simulate a scenario where inputForSubmission is set, and verify that the mutation handles user errors correctly
  }
}
