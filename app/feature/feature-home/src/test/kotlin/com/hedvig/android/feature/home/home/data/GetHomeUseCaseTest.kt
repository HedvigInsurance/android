package com.hedvig.android.feature.home.home.data

import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.right
import assertk.Assert
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.testing.enqueueTestResponse
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolverWithFilledLists
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.data.travelcertificate.TestGetTravelCertificateSpecificationsUseCase
import com.hedvig.android.data.travelcertificate.TravelCertificateData
import com.hedvig.android.data.travelcertificate.TravelCertificateError
import com.hedvig.android.feature.home.emergency.EmergencyData
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager2
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.memberreminders.MemberReminder
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.memberreminders.test.TestGetMemberRemindersUseCase
import com.hedvig.android.test.clock.TestClock
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octopus.HomeQuery
import octopus.type.HedvigColor
import octopus.type.buildAgreement
import octopus.type.buildClaim
import octopus.type.buildCommonClaimDescription
import octopus.type.buildCommonClaimLayoutEmergency
import octopus.type.buildCommonClaimLayoutTitleAndBulletPoints
import octopus.type.buildContract
import octopus.type.buildMember
import octopus.type.buildMemberImportantMessage
import octopus.type.buildPendingContract
import octopus.type.buildProductVariant
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ApolloExperimental::class)
@RunWith(TestParameterInjector::class)
internal class GetHomeUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule()
  val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  @Test
  fun `when we get no travel certificate data, return not allowed to generate travel certificate`() = runTest {
    val getTravelCertificateSpecificationsUseCase = TestGetTravelCertificateSpecificationsUseCase()
    val getHomeDataUseCase = GetHomeDataUseCaseImpl(
      apolloClient.apply {
        enqueueTestResponse(
          HomeQuery(),
          HomeQuery.Data(OctopusFakeResolver),
        )
      },
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      getTravelCertificateSpecificationsUseCase,
      FakeFeatureManager2(true),
      TestClock(),
      TimeZone.UTC,
    )

    getTravelCertificateSpecificationsUseCase.turbine.add(TravelCertificateError.NotEligible.left())
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::allowGeneratingTravelCertificate)
      .isFalse()
  }

  @Test
  fun `when we get travel certificate data, return allowed to generate travel certificate`() = runTest {
    val getTravelCertificateSpecificationsUseCase = TestGetTravelCertificateSpecificationsUseCase()
    val getHomeDataUseCase = GetHomeDataUseCaseImpl(
      apolloClient.apply {
        enqueueTestResponse(
          HomeQuery(),
          HomeQuery.Data(OctopusFakeResolver),
        )
      },
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      getTravelCertificateSpecificationsUseCase,
      FakeFeatureManager2(true),
      TestClock(),
      TimeZone.UTC,
    )

    getTravelCertificateSpecificationsUseCase.turbine.add(
      TravelCertificateData(
        travelCertificateSpecification = TravelCertificateData.TravelCertificateSpecification(
          contractId = "",
          email = "",
          maxDurationDays = 0,
          dateRange = LocalDate.parse("2023-01-01")..LocalDate.parse("2023-01-01"),
          numberOfCoInsured = 0,
        ),
        infoSections = listOf(),
      ).right(),
    )
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::allowGeneratingTravelCertificate)
      .isTrue()
  }

  @Test
  fun `when reminders are present, return the MemberReminders`() = runTest {
    val testGetMemberRemindersUseCase = TestGetMemberRemindersUseCase()
    val getHomeDataUseCase = GetHomeDataUseCaseImpl(
      apolloClient.apply {
        enqueueTestResponse(
          HomeQuery(),
          HomeQuery.Data(OctopusFakeResolver),
        )
      },
      testGetMemberRemindersUseCase,
      TestGetTravelCertificateSpecificationsUseCase().apply {
        turbine.add(TravelCertificateError.NotEligible.left())
      },
      FakeFeatureManager2(true),
      TestClock(),
      TimeZone.UTC,
    )
    val testId = "test"

    testGetMemberRemindersUseCase.memberReminders.add(
      MemberReminders(
        MemberReminder.ConnectPayment(id = testId),
        listOf(MemberReminder.UpcomingRenewal("", LocalDate.parse("2023-01-01"), "", testId)),
        MemberReminder.EnableNotifications(id = testId),
      ),
    )
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::memberReminders)
      .isEqualTo(
        MemberReminders(
          MemberReminder.ConnectPayment(id = testId),
          listOf(MemberReminder.UpcomingRenewal("", LocalDate.parse("2023-01-01"), "", testId)),
          MemberReminder.EnableNotifications(id = testId),
        ),
      )
  }

  @Test
  fun `when reminders are not present, return a empty list of MemberReminders`() = runTest {
    val testGetMemberRemindersUseCase = TestGetMemberRemindersUseCase()
    val getHomeDataUseCase = GetHomeDataUseCaseImpl(
      apolloClient.apply {
        enqueueTestResponse(
          HomeQuery(),
          HomeQuery.Data(OctopusFakeResolver),
        )
      },
      testGetMemberRemindersUseCase,
      TestGetTravelCertificateSpecificationsUseCase().apply {
        turbine.add(TravelCertificateError.NotEligible.left())
      },
      FakeFeatureManager2(true),
      TestClock(),
      TimeZone.UTC,
    )

    testGetMemberRemindersUseCase.memberReminders.add(MemberReminders())
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::memberReminders)
      .isEqualTo(MemberReminders(null, null, null))
  }

  @Test
  fun `when the contract is considered active, we allow address changes if the feature flag allows it`(
    @TestParameter isMovingFlowFlagEnabled: Boolean,
  ) = runTest {
    val featureManager = FakeFeatureManager2()
    val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate(featureManager)

    apolloClient.enqueueTestResponse(
      HomeQuery(),
      HomeQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          activeContracts = listOf(buildContract { })
        }
      },
    )
    featureManager.featureTurbine.add(Feature.MOVING_FLOW to isMovingFlowFlagEnabled)
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::allowAddressChange)
      .isEqualTo(isMovingFlowFlagEnabled)
  }

  @Test
  fun `when the contract is considered inactive, we do not allow address changes regardless of feature flag status`() = runTest {
    val featureManager = FakeFeatureManager2()
    val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate(featureManager)

    apolloClient.enqueueTestResponse(
      HomeQuery(),
      HomeQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          activeContracts = emptyList()
        }
      },
    )
    featureManager.featureTurbine.add(Feature.MOVING_FLOW to false)
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::allowAddressChange)
      .isFalse()
  }

  @Test
  fun `when there's emergency data, show it`() = runTest {
    val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate()

    apolloClient.enqueueTestResponse(
      HomeQuery(),
      HomeQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          activeContracts = listOf(
            buildContract {
              currentAgreement = buildAgreement {
                productVariant = buildProductVariant {
                  commonClaimDescriptions = listOf(
                    buildCommonClaimDescription {
                      layout = buildCommonClaimLayoutEmergency {
                        title = ""
                        color = HedvigColor.Black
                        emergencyNumber = "123"
                      }
                    },
                  )
                }
              }
            },
          )
        }
      },
    )
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::emergencyData)
      .isNotNull()
      .prop(EmergencyData::emergencyNumber)
      .isEqualTo("123")
  }

  @Test
  fun `when there's no emergency data, don't show it`() = runTest {
    val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate()

    apolloClient.enqueueTestResponse(
      HomeQuery(),
      HomeQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          activeContracts = listOf(
            buildContract {
              currentAgreement = buildAgreement {
                productVariant = buildProductVariant {
                  commonClaimDescriptions = listOf(
                    buildCommonClaimDescription {
                      layout = buildCommonClaimLayoutTitleAndBulletPoints {}
                    },
                  )
                }
              }
            },
          )
        }
      },
    )
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::emergencyData)
      .isNull()
  }

  @Test
  fun `when there are very important messages, show them`() = runTest {
    val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate()

    apolloClient.enqueueTestResponse(
      HomeQuery(),
      HomeQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          importantMessages = List(3) { index ->
            buildMemberImportantMessage {
              id = "$index"
              message = "message#$index"
              link = "link#$index"
            }
          }
        }
      },
    )
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::veryImportantMessages)
      .containsExactly(
        HomeData.VeryImportantMessage("0", "message#0", "link#0"),
        HomeData.VeryImportantMessage("1", "message#1", "link#1"),
        HomeData.VeryImportantMessage("2", "message#2", "link#2"),
      )
  }

  @Test
  fun `when there are zero very important messages, don't show them`() = runTest {
    val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate()

    apolloClient.enqueueTestResponse(
      HomeQuery(),
      HomeQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          importantMessages = emptyList()
        }
      },
    )
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::veryImportantMessages)
      .isEmpty()
  }

  @Test
  fun `when there are existing claims, show them as ClaimStatusCards`() = runTest {
    val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate()

    apolloClient.enqueueTestResponse(
      HomeQuery(),
      HomeQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          claims = listOf(
            buildClaim {
              id = "claim id#1"
            },
            buildClaim {
              id = "claim id#2"
            },
          )
        }
      },
    )
    val result = getHomeDataUseCase.invoke(true).first()

    val claimStatusCardsUiState: Assert<NonEmptyList<ClaimStatusCardUiState>> = assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::claimStatusCardsData)
      .isNotNull()
      .prop(HomeData.ClaimStatusCardsData::claimStatusCardsUiState)
    claimStatusCardsUiState.hasSize(2)
    claimStatusCardsUiState.transform { list ->
      assertThat(list[0]).prop(ClaimStatusCardUiState::id).isEqualTo("claim id#1")
      assertThat(list[1]).prop(ClaimStatusCardUiState::id).isEqualTo("claim id#2")
    }
  }

  @Test
  fun `when there are no existing claims, don't show them`() = runTest {
    val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate()

    apolloClient.enqueueTestResponse(
      HomeQuery(),
      HomeQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          claims = emptyList()
        }
      },
    )
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::claimStatusCardsData)
      .isNull()
  }

  @Test
  fun `when the are only terminated contracts, the contract status is considered terminated`() = runTest {
    val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate()

    apolloClient.enqueueTestResponse(
      HomeQuery(),
      HomeQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          activeContracts = emptyList()
          pendingContracts = emptyList()
          terminatedContracts = listOf(
            buildContract {
              id = "terminatedId"
            },
          )
        }
      },
    )
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::contractStatus)
      .isEqualTo(HomeData.ContractStatus.Terminated)
  }

  @Test
  fun `when there are active contacts, just show an Active or ActiveInFuture status regardless of other upcoming or terminated contracts`(
    @TestParameter isActiveInTheFuture: Boolean,
  ) = runTest {
    val testClock = TestClock()
    val timeZone = TimeZone.UTC
    val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate(
      testClock = testClock,
      timeZone = timeZone,
    )

    val masterInceptionDate = if (isActiveInTheFuture) {
      testClock.now().plus(1.days).toLocalDateTime(timeZone).date
    } else {
      testClock.now().toLocalDateTime(timeZone).date
    }
    apolloClient.enqueueTestResponse(
      HomeQuery(),
      HomeQuery.Data(OctopusFakeResolverWithFilledLists) {
        currentMember = buildMember {
          activeContracts = listOf(
            buildContract {
              this.masterInceptionDate = masterInceptionDate
            },
          )
        }
      },
    )
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::contractStatus)
      .apply {
        if (isActiveInTheFuture) {
          isEqualTo(HomeData.ContractStatus.ActiveInFuture(masterInceptionDate))
        } else {
          isEqualTo(HomeData.ContractStatus.Active)
        }
      }
  }

  @Test
  fun `when the are only pending contracts, the contract status is considered pending or switchable`(
    @TestParameter isSwitchableByHedvig: Boolean,
  ) = runTest {
    val featureManager = FakeFeatureManager2(true)
    val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate(featureManager)

    apolloClient.enqueueTestResponse(
      HomeQuery(),
      HomeQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          activeContracts = emptyList()
          pendingContracts = listOf(
            buildPendingContract {
              id = "pendingId"
              externalInsuranceCancellationHandledByHedvig = isSwitchableByHedvig
            },
          )
          terminatedContracts = emptyList()
        }
      },
    )
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::contractStatus)
      .apply {
        if (isSwitchableByHedvig) {
          isEqualTo(HomeData.ContractStatus.Switching)
        } else {
          isEqualTo(HomeData.ContractStatus.Pending)
        }
      }
  }

  // Used as a convenience to get a use case without any enqueued apollo responses, but some sane defaults for the
  // other dependencies
  private fun testUseCaseWithoutRemindersAndNoTravelCertificate(
    faetureManager: FeatureManager = FakeFeatureManager2(true),
    testClock: TestClock = TestClock(),
    timeZone: TimeZone = TimeZone.UTC,
  ): GetHomeDataUseCase {
    return GetHomeDataUseCaseImpl(
      apolloClient,
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      TestGetTravelCertificateSpecificationsUseCase().apply {
        turbine.add(TravelCertificateError.NotEligible.left())
      },
      faetureManager,
      testClock,
      timeZone,
    )
  }
}
