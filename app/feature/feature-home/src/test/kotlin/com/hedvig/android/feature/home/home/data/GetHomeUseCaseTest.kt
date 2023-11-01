package com.hedvig.android.feature.home.home.data

import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.first
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.api.BuilderScope
import com.apollographql.apollo3.testing.enqueueTestResponse
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.hedvig.android.apollo.giraffe.test.GiraffeFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.data.travelcertificate.TestGetTravelCertificateSpecificationsUseCase
import com.hedvig.android.data.travelcertificate.TravelCertificateData
import com.hedvig.android.data.travelcertificate.TravelCertificateError
import com.hedvig.android.feature.home.claims.commonclaim.BulletPoint
import com.hedvig.android.feature.home.claims.commonclaim.CommonClaimsData
import com.hedvig.android.feature.home.claims.commonclaim.EmergencyData
import com.hedvig.android.feature.home.claimstatus.claimprogress.ClaimProgressUiState
import com.hedvig.android.feature.home.claimstatus.data.ClaimStatusCardUiState
import com.hedvig.android.feature.home.claimstatus.data.PillUiState
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager2
import com.hedvig.android.language.test.FakeLanguageService
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.memberreminders.MemberReminder
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.memberreminders.UpcomingRenewal
import com.hedvig.android.memberreminders.test.TestGetMemberRemindersUseCase
import giraffe.HomeQuery
import giraffe.type.ClaimStatusCardPillType
import giraffe.type.ClaimStatusProgressType
import giraffe.type.CommonClaimLayoutsMap
import giraffe.type.ContractStatusMap
import giraffe.type.HedvigColor
import giraffe.type.Locale
import giraffe.type.buildActiveInFutureAndTerminatedInFutureStatus
import giraffe.type.buildActiveInFutureStatus
import giraffe.type.buildActiveStatus
import giraffe.type.buildBulletPoints
import giraffe.type.buildClaimStatusCard
import giraffe.type.buildClaimStatusCardPill
import giraffe.type.buildClaimStatusProgressSegment
import giraffe.type.buildCommonClaim
import giraffe.type.buildContract
import giraffe.type.buildDeletedStatus
import giraffe.type.buildEmergency
import giraffe.type.buildImportantMessage
import giraffe.type.buildInsuranceProvider
import giraffe.type.buildOtherCommonClaimLayouts
import giraffe.type.buildOtherContractStatus
import giraffe.type.buildPendingStatus
import giraffe.type.buildTerminatedInFutureStatus
import giraffe.type.buildTerminatedStatus
import giraffe.type.buildTerminatedTodayStatus
import giraffe.type.buildTitleAndBulletPoints
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
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
  fun `a complete response returns all fields correctly`() = runTest {
    val getMemberRemindersUseCase = TestGetMemberRemindersUseCase()
    val getTravelCertificateSpecificationsUseCase = TestGetTravelCertificateSpecificationsUseCase()
    val getHomeDataUseCase = GetHomeDataUseCaseImpl(
      apolloClient,
      FakeLanguageService(),
      getMemberRemindersUseCase,
      getTravelCertificateSpecificationsUseCase,
      FakeFeatureManager2(true),
    )

    getMemberRemindersUseCase.memberReminders.add(
      MemberReminders(
        MemberReminder.ConnectPayment,
        MemberReminder.UpcomingRenewals(
          nonEmptyListOf(
            UpcomingRenewal("renewal #1", LocalDate.parse("2023-01-01"), "url#1"),
            UpcomingRenewal("renewal #2", LocalDate.parse("2023-01-02"), "url#2"),
          ),
        ),
      ),
    )
    getTravelCertificateSpecificationsUseCase.turbine.add(
      TravelCertificateData(
        TravelCertificateData.TravelCertificateSpecification(
          "",
          "",
          1,
          LocalDate.parse("2023-01-01")..LocalDate.parse("2023-01-02"),
          0,
        ),
        emptyList(),
      ).right(),
    )
    apolloClient.enqueueTestResponse(
      HomeQuery(Locale.en_SE, ""),
      HomeQuery.Data(GiraffeFakeResolver) {
        contracts = listOf(
          buildContract {
            status = buildActiveStatus { }
          },
        )
        // claims_statusCards aliased https://www.apollographql.com/docs/kotlin/testing/data-builders#aliases
        this["claimStatusCards"] = listOf(
          buildClaimStatusCard {
            id = "card id"
            title = "card title"
            subtitle = "card subtitle"
          },
        )
        importantMessages = listOf(
          buildImportantMessage {
            message = "important"
            link = "message url"
          },
        )
        commonClaims = listOf(
          buildCommonClaim {
            layout = buildEmergency {
              isEligibleToCreateClaim = true
              emergencyNumber = "112"
            }
          },
          buildCommonClaim {
            id = "bullet point id"
            layout = buildTitleAndBulletPoints {
              title = "layout title"
              color = HedvigColor.DarkPurple
              bulletPoints = listOf(
                buildBulletPoints {
                  title = "bullet point title"
                  description = "bullet point description"
                },
              )
            }
          },
        )
      },
    )
    getTravelCertificateSpecificationsUseCase.turbine.add(TravelCertificateError.NotEligible.left())
    val result = getHomeDataUseCase.invoke(true).first()

    val rightResult = assertThat(result).isNotNull().isRight()
    assertAll {
      rightResult.apply {
        prop(HomeData::contractStatus).isEqualTo(HomeData.ContractStatus.Active)
        prop(HomeData::claimStatusCardsData)
          .isNotNull()
          .prop(HomeData.ClaimStatusCardsData::claimStatusCardsUiState)
          .containsExactly(
            ClaimStatusCardUiState(
              "card id",
              emptyList(),
              "card title",
              "card subtitle",
              emptyList(),
            ),
          )
        prop(HomeData::veryImportantMessages).containsExactly(
          HomeData.VeryImportantMessage("important", "message url"),
        )
        prop(HomeData::memberReminders).isEqualTo(
          MemberReminders(
            MemberReminder.ConnectPayment,
            MemberReminder.UpcomingRenewals(
              nonEmptyListOf(
                UpcomingRenewal("renewal #1", LocalDate.parse("2023-01-01"), "url#1"),
                UpcomingRenewal("renewal #2", LocalDate.parse("2023-01-02"), "url#2"),
              ),
            ),
          ),
        )
        prop(HomeData::allowAddressChange).isTrue()
        prop(HomeData::allowGeneratingTravelCertificate).isTrue()
        prop(HomeData::emergencyData).isNotNull().apply {
          prop(EmergencyData::eligibleToClaim).isTrue()
          prop(EmergencyData::emergencyNumber).isEqualTo("112")
        }
        prop(HomeData::commonClaimsData).first().apply {
          prop(CommonClaimsData::id).isEqualTo("bullet point id")
          prop(CommonClaimsData::layoutTitle).isEqualTo("layout title")
          prop(CommonClaimsData::bulletPoints).first().apply {
            prop(BulletPoint::title).isEqualTo("bullet point title")
            prop(BulletPoint::description).isEqualTo("bullet point description")
          }
        }
      }
    }
  }

  @Test
  fun `when we get no travel certificate data, return not allowed to generate travel certificate`() = runTest {
    val getTravelCertificateSpecificationsUseCase = TestGetTravelCertificateSpecificationsUseCase()
    val getHomeDataUseCase = GetHomeDataUseCaseImpl(
      apolloClient.apply {
        enqueueTestResponse(
          HomeQuery(Locale.en_SE, ""),
          HomeQuery.Data(GiraffeFakeResolver),
        )
      },
      FakeLanguageService(),
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      getTravelCertificateSpecificationsUseCase,
      FakeFeatureManager2(true),
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
          HomeQuery(Locale.en_SE, ""),
          HomeQuery.Data(GiraffeFakeResolver),
        )
      },
      FakeLanguageService(),
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      getTravelCertificateSpecificationsUseCase,
      FakeFeatureManager2(true),
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
          HomeQuery(Locale.en_SE, ""),
          HomeQuery.Data(GiraffeFakeResolver),
        )
      },
      FakeLanguageService(),
      testGetMemberRemindersUseCase,
      TestGetTravelCertificateSpecificationsUseCase().apply {
        turbine.add(TravelCertificateError.NotEligible.left())
      },
      FakeFeatureManager2(true),
    )

    testGetMemberRemindersUseCase.memberReminders.add(
      MemberReminders(
        MemberReminder.ConnectPayment,
        MemberReminder.UpcomingRenewals(nonEmptyListOf(UpcomingRenewal("", LocalDate.parse("2023-01-01"), ""))),
        MemberReminder.EnableNotifications,
      ),
    )
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::memberReminders)
      .isEqualTo(
        MemberReminders(
          MemberReminder.ConnectPayment,
          MemberReminder.UpcomingRenewals(nonEmptyListOf(UpcomingRenewal("", LocalDate.parse("2023-01-01"), ""))),
          MemberReminder.EnableNotifications,
        ),
      )
  }

  @Test
  fun `when reminders are not present, return a empty list of MemberReminders`() = runTest {
    val testGetMemberRemindersUseCase = TestGetMemberRemindersUseCase()
    val getHomeDataUseCase = GetHomeDataUseCaseImpl(
      apolloClient.apply {
        enqueueTestResponse(
          HomeQuery(Locale.en_SE, ""),
          HomeQuery.Data(GiraffeFakeResolver),
        )
      },
      FakeLanguageService(),
      testGetMemberRemindersUseCase,
      TestGetTravelCertificateSpecificationsUseCase().apply {
        turbine.add(TravelCertificateError.NotEligible.left())
      },
      FakeFeatureManager2(true),
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
    val activeStatusList = listOf<BuilderScope.() -> ContractStatusMap>(
      { buildActiveStatus { } },
      { buildTerminatedTodayStatus { } },
      { buildTerminatedInFutureStatus { } },
    )
    for (activeStatus in activeStatusList) {
      val featureManager = FakeFeatureManager2()
      val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate(featureManager)

      apolloClient.enqueueTestResponse(
        HomeQuery(Locale.en_SE, ""),
        HomeQuery.Data(GiraffeFakeResolver) { contracts = listOf(buildContract { status = activeStatus() }) },
      )
      featureManager.featureTurbine.add(Feature.NEW_MOVING_FLOW to isMovingFlowFlagEnabled)
      val result = getHomeDataUseCase.invoke(true).first()

      assertThat(result)
        .isNotNull()
        .isRight()
        .prop(HomeData::allowAddressChange)
        .isEqualTo(isMovingFlowFlagEnabled)
    }
  }

  @Test
  fun `when the contract is considered inactive, we do not allow address changes regardless of feature flag status`(
    @TestParameter isMovingFlowFlagEnabled: Boolean,
  ) = runTest {
    val nonActiveStatusList = listOf<BuilderScope.() -> ContractStatusMap>(
      { buildActiveInFutureAndTerminatedInFutureStatus { } },
      { buildActiveInFutureStatus { } },
      { buildDeletedStatus { } },
      { buildPendingStatus { } },
      { buildTerminatedStatus { } },
      { buildOtherContractStatus("typename") { } },
    )
    for (nonActiveStatus in nonActiveStatusList) {
      val featureManager = FakeFeatureManager2()
      val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate(featureManager)

      apolloClient.enqueueTestResponse(
        HomeQuery(Locale.en_SE, ""),
        HomeQuery.Data(GiraffeFakeResolver) { contracts = listOf(buildContract { status = nonActiveStatus() }) },
      )
      featureManager.featureTurbine.add(Feature.NEW_MOVING_FLOW to isMovingFlowFlagEnabled)
      val result = getHomeDataUseCase.invoke(true).first()

      assertThat(result)
        .isNotNull()
        .isRight()
        .prop(HomeData::allowAddressChange)
        .isFalse()
    }
  }

  @Test
  fun `when there's emergency data, show it`() = runTest {
    val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate()

    apolloClient.enqueueTestResponse(
      HomeQuery(Locale.en_SE, ""),
      HomeQuery.Data(GiraffeFakeResolver) {
        commonClaims = listOf(
          buildCommonClaim {
            layout = buildEmergency { emergencyNumber = "123" }
          },
        )
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
    val nonEmergencyCommonClaims = listOf<(BuilderScope.() -> CommonClaimLayoutsMap)?>(
      { buildOtherCommonClaimLayouts("") {} },
      { buildTitleAndBulletPoints {} },
      null,
    )
    for (nonEmergencyCommonClaim in nonEmergencyCommonClaims) {
      val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate()

      apolloClient.enqueueTestResponse(
        HomeQuery(Locale.en_SE, ""),
        HomeQuery.Data(GiraffeFakeResolver) {
          commonClaims = nonEmergencyCommonClaim?.let {
            listOf(buildCommonClaim { layout = nonEmergencyCommonClaim() })
          } ?: emptyList()
        },
      )
      val result = getHomeDataUseCase.invoke(true).first()

      assertThat(result)
        .isNotNull()
        .isRight()
        .prop(HomeData::emergencyData)
        .isNull()
    }
  }

  @Test
  fun `when there are very important messages, show them`() = runTest {
    val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate()

    apolloClient.enqueueTestResponse(
      HomeQuery(Locale.en_SE, ""),
      HomeQuery.Data(GiraffeFakeResolver) {
        importantMessages = listOf(
          buildImportantMessage {
            message = "important message"
            link = "important link"
          },
          buildImportantMessage {
            message = "important message#2"
            link = "important link#2"
          },
        )
      },
    )
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::veryImportantMessages)
      .containsExactly(
        HomeData.VeryImportantMessage("important message", "important link"),
        HomeData.VeryImportantMessage("important message#2", "important link#2"),
      )
  }

  @Test
  fun `when there are zero very important messages, don't show them`() = runTest {
    val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate()

    apolloClient.enqueueTestResponse(
      HomeQuery(Locale.en_SE, ""),
      HomeQuery.Data(GiraffeFakeResolver),
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
      HomeQuery(Locale.en_SE, ""),
      HomeQuery.Data(GiraffeFakeResolver) {
        this["claimStatusCards"] = listOf(
          buildClaimStatusCard {
            id = "status id#1"
            title = "status title#1"
            subtitle = "status subtitle#1"
            pills = listOf(
              buildClaimStatusCardPill {
                text = "pill"
                type = ClaimStatusCardPillType.PAYMENT
              },
            )
            progressSegments = listOf(
              buildClaimStatusProgressSegment {
                type = ClaimStatusProgressType.CURRENTLY_ACTIVE
                text = "Ongoing"
              },
            )
          },
          buildClaimStatusCard {
            id = "status id#2"
            title = "status title#2"
            subtitle = "status subtitle#2"
          },
        )
      },
    )
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::claimStatusCardsData)
      .isNotNull()
      .prop(HomeData.ClaimStatusCardsData::claimStatusCardsUiState)
      .containsExactly(
        ClaimStatusCardUiState(
          id = "status id#1",
          title = "status title#1",
          subtitle = "status subtitle#1",
          claimProgressItemsUiState = listOf(
            ClaimProgressUiState(
              text = "Ongoing",
              type = ClaimProgressUiState.ClaimProgressType.CURRENTLY_ACTIVE,
            ),
          ),
          pillsUiState = listOf(
            PillUiState(
              text = "pill",
              type = PillUiState.PillType.PAYMENT,
            ),
          ),
        ),
        ClaimStatusCardUiState(
          id = "status id#2",
          title = "status title#2",
          subtitle = "status subtitle#2",
          claimProgressItemsUiState = emptyList(),
          pillsUiState = emptyList(),
        ),
      )
  }

  @Test
  fun `when there are no existing claims, don't show them`() = runTest {
    val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate()

    apolloClient.enqueueTestResponse(
      HomeQuery(Locale.en_SE, ""),
      HomeQuery.Data(GiraffeFakeResolver),
    )
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::claimStatusCardsData)
      .isNull()
  }

  @Test
  fun `contracts list map properly to the correct decision about the contract status`() = runTest {
    val contractListToExpectedMap = listOf<Pair<List<BuilderScope.() -> ContractStatusMap>, HomeData.ContractStatus>>(
      Pair(
        listOf({ buildActiveStatus {} }),
        HomeData.ContractStatus.Active,
      ),
      Pair(
        listOf(
          { buildActiveInFutureStatus { futureInception = LocalDate.parse("2023-01-01").toJavaLocalDate() } },
        ),
        HomeData.ContractStatus.ActiveInFuture(LocalDate.parse("2023-01-01")),
      ),
      Pair(
        listOf({ buildTerminatedStatus { } }),
        HomeData.ContractStatus.Terminated,
      ),
      Pair(
        listOf({ buildOtherContractStatus("") {} }),
        HomeData.ContractStatus.Unknown,
      ),
      Pair(
        listOf({ buildPendingStatus { } }),
        HomeData.ContractStatus.Pending,
      ),
      Pair(
        listOf(
          { buildActiveStatus { } },
          { buildOtherContractStatus("") {} },
        ),
        HomeData.ContractStatus.Active,
      ),
      Pair(
        // terminated + pending need all of them to be that in order to show the terminated/pending state.
        listOf(
          { buildTerminatedStatus { } },
          { buildPendingStatus { } },
          { buildActiveInFutureStatus { futureInception = LocalDate.parse("2023-01-01").toJavaLocalDate() } },
        ),
        HomeData.ContractStatus.ActiveInFuture(LocalDate.parse("2023-01-01")),
      ),
    )
    for (contractListToExpected in contractListToExpectedMap) {
      val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate()

      apolloClient.enqueueTestResponse(
        HomeQuery(Locale.en_SE, ""),
        HomeQuery.Data(GiraffeFakeResolver) {
          contracts = contractListToExpected.first.map {
            buildContract {
              status = it.invoke(this)
            }
          }
        },
      )
      val result = getHomeDataUseCase.invoke(true).first()

      assertThat(result)
        .isNotNull()
        .isRight()
        .prop(HomeData::contractStatus)
        .isEqualTo(contractListToExpected.second)
    }
  }

  // ktlint-disable max-line-length
  @Test
  fun `a combination of contract statuses where not all of them are terminated or pending, does not result in Terminated or Pending contract status`() =
    runTest {
      val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate()

      apolloClient.enqueueTestResponse(
        HomeQuery(Locale.en_SE, ""),
        HomeQuery.Data(GiraffeFakeResolver) {
          contracts = listOf(
            buildContract { status = buildTerminatedStatus { } },
            buildContract { status = buildPendingStatus { } },
            buildContract { status = buildActiveInFutureStatus { } },
          )
        },
      )
      val result = getHomeDataUseCase.invoke(true).first()

      assertThat(result)
        .isNotNull()
        .isRight()
        .prop(HomeData::contractStatus)
        .isInstanceOf<HomeData.ContractStatus.ActiveInFuture>()
    }

  @Test
  fun `with a switchable insurance, and only pending contracts, result in a switchable contract result`() = runTest {
    val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate()

    apolloClient.enqueueTestResponse(
      HomeQuery(Locale.en_SE, ""),
      HomeQuery.Data(GiraffeFakeResolver) {
        insuranceProviders = listOf(
          buildInsuranceProvider {
            id = "switchable insurer id"
            switchable = true
          },
        )
        contracts = listOf(
          buildContract { status = buildPendingStatus { } },
          buildContract { status = buildPendingStatus { } },
          buildContract {
            status = buildPendingStatus { }
            switchedFromInsuranceProvider = "switchable insurer id"
          },
        )
      },
    )
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::contractStatus)
      .isEqualTo(HomeData.ContractStatus.Switching)
  }

  @Test
  fun `with a switchable insurance, and only future contracts, result in a switchable contract result`() = runTest {
    val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate()

    apolloClient.enqueueTestResponse(
      HomeQuery(Locale.en_SE, ""),
      HomeQuery.Data(GiraffeFakeResolver) {
        insuranceProviders = listOf(
          buildInsuranceProvider {
            id = "switchable insurer id"
            switchable = true
          },
        )
        contracts = listOf(
          buildContract { status = buildActiveInFutureStatus { } },
          buildContract { status = buildActiveInFutureStatus { } },
          buildContract {
            status = buildActiveInFutureStatus { }
            switchedFromInsuranceProvider = "switchable insurer id"
          },
        )
      },
    )
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::contractStatus)
      .isEqualTo(HomeData.ContractStatus.Switching)
  }

  @Test
  fun `with a switchable insurance, and an active contract, do not return a switchable contract result`() = runTest {
    val getHomeDataUseCase = testUseCaseWithoutRemindersAndNoTravelCertificate()

    apolloClient.enqueueTestResponse(
      HomeQuery(Locale.en_SE, ""),
      HomeQuery.Data(GiraffeFakeResolver) {
        insuranceProviders = listOf(
          buildInsuranceProvider {
            id = "switchable insurer id"
            switchable = true
          },
        )
        contracts = listOf(
          buildContract { status = buildPendingStatus { } },
          buildContract { status = buildActiveStatus { } },
          buildContract {
            status = buildPendingStatus { }
            switchedFromInsuranceProvider = "switchable insurer id"
          },
        )
      },
    )
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::contractStatus)
      .isEqualTo(HomeData.ContractStatus.Active)
  }

  // Used as a convenience to get a use case without any enqueued apollo responses, but some sane defaults for the
  // other dependencies
  private fun testUseCaseWithoutRemindersAndNoTravelCertificate(
    faetureManager: FeatureManager = FakeFeatureManager2(true),
  ): GetHomeDataUseCase {
    return GetHomeDataUseCaseImpl(
      apolloClient,
      FakeLanguageService(),
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      TestGetTravelCertificateSpecificationsUseCase().apply {
        turbine.add(TravelCertificateError.NotEligible.left())
      },
      faetureManager,
    )
  }
}
