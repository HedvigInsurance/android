package com.hedvig.android.feature.terminateinsurance.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager2
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import octopus.FlowTerminationStartMutation
import octopus.type.FlowTerminationStartInput
import octopus.type.FlowTerminationSurveyRedirectAction
import octopus.type.buildFlow
import octopus.type.buildFlowTerminationSurveyOption
import octopus.type.buildFlowTerminationSurveyOptionFeedback
import octopus.type.buildFlowTerminationSurveyOptionSuggestionAction
import octopus.type.buildFlowTerminationSurveyStep
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@ExperimentalCoroutinesApi
class TerminateInsuranceRepositoryImplTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  private val testCoroutineDispatcher = UnconfinedTestDispatcher()
  private val testCoroutineScope = TestScope(testCoroutineDispatcher + Job())
  private val testDataStore: DataStore<Preferences> =
    PreferenceDataStoreFactory.create(
      scope = testCoroutineScope,
      produceFile = { tmpFolder.newFile(TEST_DATASTORE_NAME) },
    )
  private val testId = "testId"
  private val TEST_DATASTORE_NAME: String = "user.preferences_pb"

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithGoodResponse: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = FlowTerminationStartMutation(FlowTerminationStartInput(testId), false),
        data = FlowTerminationStartMutation.Data(OctopusFakeResolver) {
          flowTerminationStart = buildFlow {
            id = "flowId"
            context = "flowContext"
            currentStep = buildFlowTerminationSurveyStep {
              id = "surveyStepId"
              options = buildList {
                add(
                  buildFlowTerminationSurveyOption {
                    id = "000"
                    title = "Don't need insurance anymore"
                    suggestion = null
                    subOptions = null
                    feedBack = buildFlowTerminationSurveyOptionFeedback {
                      id = "fb00"
                      isRequired = true
                    }
                  },
                )
                add(
                  buildFlowTerminationSurveyOption {
                    id = "001"
                    title = "Found better price"
                    suggestion = null
                    subOptions = buildList {
                      add(
                        buildFlowTerminationSurveyOption {
                          id = "0010"
                          title = "Change coverage level"
                          suggestion = buildFlowTerminationSurveyOptionSuggestionAction {
                            id = "actionid"
                            buttonTitle = "Get new quote"
                            description = "Check if you can get a better price with another coverage"
                            action = FlowTerminationSurveyRedirectAction.CHANGE_TIER_FOUND_BETTER_PRICE
                          }
                          feedBack = null
                        },
                      )
                      add(
                        buildFlowTerminationSurveyOption {
                          id = "0011"
                          title = "Quit without new quote"
                          suggestion = null
                          feedBack = buildFlowTerminationSurveyOptionFeedback {
                            id = "fb01"
                            isRequired = true
                          }
                        },
                      )
                    }
                    feedBack = null
                  },
                )
                add(
                  buildFlowTerminationSurveyOption {
                    id = "0013"
                    title = "Change coverage level"
                    suggestion = buildFlowTerminationSurveyOptionSuggestionAction {
                      id = "actionid"
                      buttonTitle = "Get new quote"
                      description = "Check if you can get a better coverage"
                      action = FlowTerminationSurveyRedirectAction.CHANGE_TIER_MISSING_COVERAGE_AND_TERMS
                    }
                    feedBack = null
                  },
                )
              }
            }
          }
        },
      )
    }

  @Test
  fun `when response is ok, options with tier-related subOptions should have subOptions and no required feedback`() =
    runTest {
      val featureManager = FakeFeatureManager2(
        fixedMap = mapOf(
          Feature.TRAVEL_ADDON to false,
        ),
      )

      val repo = TerminateInsuranceRepositoryImpl(
        apolloClient = apolloClientWithGoodResponse,
        featureManager = featureManager,
        terminationFlowContextStorage = TerminationFlowContextStorage(testDataStore),
      )
      val result = repo.startTerminationFlow(InsuranceId(testId))

      assertk.assertThat(result)
        .isRight()
        .isInstanceOf(TerminateInsuranceStep.Survey::class)
        .prop(TerminateInsuranceStep.Survey::options)
        .transform { list ->
          list.any { option ->
            option.subOptions.any { subOption ->
              subOption.suggestion is SurveyOptionSuggestion.Action.DowngradePriceByChangingTier ||
                subOption.suggestion is SurveyOptionSuggestion.Action.UpgradeCoverageByChangingTier
            }
          }
        }
        .isTrue()

      assertk.assertThat(result)
        .isRight()
        .isInstanceOf(TerminateInsuranceStep.Survey::class)
        .prop(TerminateInsuranceStep.Survey::options)
        .transform { list ->
          list.filter { option ->
            option.id == "001"
          }.all {
            !it.feedBackRequired && it.subOptions.isNotEmpty()
          }
        }
        .isTrue()
    }

  @Test
  fun `when response is ok, options with tier actions should have corresponding action and have no feedback`() =
    runTest {
      val featureManager = FakeFeatureManager2(
        fixedMap = mapOf(
          Feature.TRAVEL_ADDON to false,
        ),
      )

      val repo = TerminateInsuranceRepositoryImpl(
        apolloClient = apolloClientWithGoodResponse,
        featureManager = featureManager,
        terminationFlowContextStorage = TerminationFlowContextStorage(testDataStore),
      )
      val result = repo.startTerminationFlow(InsuranceId(testId))

      assertk.assertThat(result)
        .isRight()
        .isInstanceOf(TerminateInsuranceStep.Survey::class)
        .prop(TerminateInsuranceStep.Survey::options)
        .transform { list ->
          list.filter { option ->
            option.id == "0013"
          }.all {
            !it.feedBackRequired && it.suggestion is SurveyOptionSuggestion.Action.UpgradeCoverageByChangingTier
          }
        }
        .isTrue()
    }
}
