package com.hedvig.android.feature.home.home.data

import arrow.core.NonEmptyList
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
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestResponse
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolverWithFilledLists
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.feature.home.home.data.HomeData.VeryImportantMessage.LinkInfo
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager2
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
import octopus.CbmNumberOfChatMessagesQuery
import octopus.HomeQuery
import octopus.type.ChatMessageSender
import octopus.type.buildChatMessagePage
import octopus.type.buildChatMessageText
import octopus.type.buildClaim
import octopus.type.buildContract
import octopus.type.buildConversation
import octopus.type.buildLinkInfo
import octopus.type.buildMember
import octopus.type.buildMemberImportantMessage
import octopus.type.buildPendingContract
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ApolloExperimental::class)
@RunWith(TestParameterInjector::class)
internal class GetHomeUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)
  val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  @Test
  fun `when reminders are present, return the MemberReminders`() = runTest {
    val testGetMemberRemindersUseCase = TestGetMemberRemindersUseCase()
    val getHomeDataUseCase = GetHomeDataUseCaseImpl(
      apolloClient.apply {
        registerTestResponse(
          HomeQuery(),
          HomeQuery.Data(OctopusFakeResolver),
        )
        apolloClient.registerTestResponse(
          CbmNumberOfChatMessagesQuery(),
          CbmNumberOfChatMessagesQuery.Data(OctopusFakeResolver),
        )
      },
      testGetMemberRemindersUseCase,
      FakeFeatureManager2(true),
      TestClock(),
      TimeZone.UTC,
    )
    val testId = "test"

    testGetMemberRemindersUseCase.memberReminders.add(
      MemberReminders(
        MemberReminder.PaymentReminder.ConnectPayment(id = testId),
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
          MemberReminder.PaymentReminder.ConnectPayment(id = testId),
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
        registerTestResponse(
          HomeQuery(),
          HomeQuery.Data(OctopusFakeResolver),
        )
        apolloClient.registerTestResponse(
          CbmNumberOfChatMessagesQuery(),
          CbmNumberOfChatMessagesQuery.Data(OctopusFakeResolver),
        )
      },
      testGetMemberRemindersUseCase,
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
  fun `when there are very important messages, show them`() = runTest {
    val getHomeDataUseCase = testUseCaseWithoutReminders()

    apolloClient.registerTestResponse(
      HomeQuery(),
      HomeQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          importantMessages = List(3) { index ->
            buildMemberImportantMessage {
              id = "$index"
              message = "message#$index"
              linkInfo = buildLinkInfo {
                this.buttonText = "buttonText#$index"
                this.url = "link#$index"
              }
            }
          }
        }
      },
    )
    apolloClient.registerTestResponse(
      CbmNumberOfChatMessagesQuery(),
      CbmNumberOfChatMessagesQuery.Data(OctopusFakeResolver),
    )
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::veryImportantMessages)
      .containsExactly(
        HomeData.VeryImportantMessage("0", "message#0", LinkInfo("buttonText#0", "link#0")),
        HomeData.VeryImportantMessage("1", "message#1", LinkInfo("buttonText#1", "link#1")),
        HomeData.VeryImportantMessage("2", "message#2", LinkInfo("buttonText#2", "link#2")),
      )
  }

  @Test
  fun `when there are zero very important messages, don't show them`() = runTest {
    val getHomeDataUseCase = testUseCaseWithoutReminders()

    apolloClient.registerTestResponse(
      HomeQuery(),
      HomeQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          importantMessages = emptyList()
        }
      },
    )
    apolloClient.registerTestResponse(
      CbmNumberOfChatMessagesQuery(),
      CbmNumberOfChatMessagesQuery.Data(OctopusFakeResolver),
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
    val getHomeDataUseCase = testUseCaseWithoutReminders()

    apolloClient.registerTestResponse(
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
    apolloClient.registerTestResponse(
      CbmNumberOfChatMessagesQuery(),
      CbmNumberOfChatMessagesQuery.Data(OctopusFakeResolver),
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
    val getHomeDataUseCase = testUseCaseWithoutReminders()

    apolloClient.registerTestResponse(
      HomeQuery(),
      HomeQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          claims = emptyList()
        }
      },
    )
    apolloClient.registerTestResponse(
      CbmNumberOfChatMessagesQuery(),
      CbmNumberOfChatMessagesQuery.Data(OctopusFakeResolver),
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
    val getHomeDataUseCase = testUseCaseWithoutReminders()

    apolloClient.registerTestResponse(
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
    apolloClient.registerTestResponse(
      CbmNumberOfChatMessagesQuery(),
      CbmNumberOfChatMessagesQuery.Data(OctopusFakeResolver),
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
    val getHomeDataUseCase = testUseCaseWithoutReminders(
      testClock = testClock,
      timeZone = timeZone,
    )

    val masterInceptionDate = if (isActiveInTheFuture) {
      testClock.now().plus(1.days).toLocalDateTime(timeZone).date
    } else {
      testClock.now().toLocalDateTime(timeZone).date
    }
    apolloClient.registerTestResponse(
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
    apolloClient.registerTestResponse(
      CbmNumberOfChatMessagesQuery(),
      CbmNumberOfChatMessagesQuery.Data(OctopusFakeResolver),
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
    val getHomeDataUseCase = testUseCaseWithoutReminders(featureManager)

    apolloClient.registerTestResponse(
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
    apolloClient.registerTestResponse(
      CbmNumberOfChatMessagesQuery(),
      CbmNumberOfChatMessagesQuery.Data(OctopusFakeResolver),
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

  @Test
  fun `showing the chat button is true when there are any messages by the member, or more than 1 by Hedvig`(
    @TestParameter hasAtLeastOneMessageByMember: Boolean,
    @TestParameter("0", "1", "2") numberOfMessagesByHedvig: Int,
  ) = runTest {
    val featureManager = FakeFeatureManager2(
      mapOf(
        Feature.DISABLE_CHAT to false,
        Feature.HELP_CENTER to true,
      ),
    )
    val getHomeDataUseCase = testUseCaseWithoutReminders(featureManager)

    apolloClient.registerTestResponse(
      HomeQuery(),
      HomeQuery.Data(OctopusFakeResolver),
    )
    apolloClient.registerTestResponse(
      CbmNumberOfChatMessagesQuery(),
      CbmNumberOfChatMessagesQuery.Data(OctopusFakeResolver) {
        this.currentMember = buildMember {
          this.legacyConversation = buildConversation {
            this.messagePage = buildChatMessagePage {
              this.messages = buildList {
                if (hasAtLeastOneMessageByMember) {
                  add(
                    buildChatMessageText {
                      this.sender = ChatMessageSender.MEMBER
                    },
                  )
                }
                addAll(
                  List(numberOfMessagesByHedvig) {
                    buildChatMessageText {
                      this.sender = ChatMessageSender.HEDVIG
                    }
                  },
                )
              }
            }
          }
        }
      },
    )
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::showChatIcon)
      .apply {
        if (hasAtLeastOneMessageByMember || numberOfMessagesByHedvig > 1) {
          isTrue()
        } else {
          isFalse()
        }
      }
  }

  @Test
  fun `showChatIcon depends on if there are existing messages, and on the help center and chat feature flags`(
    @TestParameter chatIsKillSwitched: Boolean,
    @TestParameter helpCenterIsEnabled: Boolean,
    @TestParameter isEligibleToSeeTheChatButton: Boolean,
  ) = runTest {
    val featureManager = FakeFeatureManager2(
      mapOf(
        Feature.DISABLE_CHAT to chatIsKillSwitched,
        Feature.HELP_CENTER to helpCenterIsEnabled,
      ),
    )
    val getHomeDataUseCase = testUseCaseWithoutReminders(featureManager)

    apolloClient.registerTestResponse(
      HomeQuery(),
      HomeQuery.Data(OctopusFakeResolver),
    )
    apolloClient.registerTestResponse(
      CbmNumberOfChatMessagesQuery(),
      CbmNumberOfChatMessagesQuery.Data(OctopusFakeResolver) {
        this.currentMember = buildMember {
          this.legacyConversation = buildConversation {
            this.messagePage = buildChatMessagePage {
              this.messages = if (isEligibleToSeeTheChatButton) {
                listOf(
                  buildChatMessageText {
                    sender = ChatMessageSender.MEMBER
                  },
                )
              } else {
                emptyList()
              }
            }
          }
        }
      },
    )
    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::showChatIcon)
      .apply {
        if (chatIsKillSwitched) {
          // No matter what, if the chat is disabled, we do not show the chat button
          isFalse()
        } else {
          if (isEligibleToSeeTheChatButton) {
            // If they are eligible to see the chat button, we just show it
            isTrue()
          } else {
            if (helpCenterIsEnabled) {
              isFalse()
            } else {
              // Despite not being eligible to see the chat button, if the help center is disabled, we must show the
              // chat button to allow them to still get to the chat in some way
              isTrue()
            }
          }
        }
      }
  }

  @Test
  fun `the disable help center feature flag determines if we show it or not`(
    @TestParameter helpCenterIsEnabled: Boolean,
  ) = runTest {
    val featureManager = FakeFeatureManager2(
      mapOf(
        Feature.DISABLE_CHAT to true,
        Feature.HELP_CENTER to helpCenterIsEnabled,
      ),
    )
    val getHomeDataUseCase = testUseCaseWithoutReminders(featureManager)

    apolloClient.registerTestResponse(
      HomeQuery(),
      HomeQuery.Data(OctopusFakeResolver),
    )
    apolloClient.registerTestResponse(
      CbmNumberOfChatMessagesQuery(),
      CbmNumberOfChatMessagesQuery.Data(OctopusFakeResolver),
    )

    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::showHelpCenter)
      .apply {
        if (helpCenterIsEnabled) {
          isTrue()
        } else {
          isFalse()
        }
      }
  }

  @Test
  fun `without legacy conversations, show the chat icon depending on the other conversations status`(
    @TestParameter hasAtLeastOneOpenConversation: Boolean,
    @TestParameter closedConversationHasAtLeastOneMessage: Boolean,
  ) = runTest {
    val featureManager = FakeFeatureManager2(
      mapOf(
        Feature.DISABLE_CHAT to false,
        Feature.HELP_CENTER to true,
        Feature.ENABLE_CBM to true,
      ),
    )
    val getHomeDataUseCase = testUseCaseWithoutReminders(featureManager)

    apolloClient.registerTestResponse(
      HomeQuery(),
      HomeQuery.Data(OctopusFakeResolver),
    )
    apolloClient.registerTestResponse(
      CbmNumberOfChatMessagesQuery(),
      CbmNumberOfChatMessagesQuery.Data(OctopusFakeResolver) {
        this.currentMember = buildMember {
          val openConversation = this.buildConversation {
            this.isOpen = true
          }
          val closedConversation = this.buildConversation {
            this.isOpen = false
            this.newestMessage = buildChatMessageText {}.takeIf { closedConversationHasAtLeastOneMessage }
          }
          this.conversations = buildList {
            if (hasAtLeastOneOpenConversation) {
              add(openConversation)
            }
            add(closedConversation)
          }
          this.legacyConversation = null
        }
      },
    )

    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::showChatIcon)
      .apply {
        when {
          hasAtLeastOneOpenConversation -> isTrue()
          closedConversationHasAtLeastOneMessage -> isTrue()
          else -> isFalse()
        }
      }
  }

  // Used as a convenience to get a use case without any enqueued apollo responses, but some sane defaults for the
  // other dependencies
  private fun testUseCaseWithoutReminders(
    faetureManager: FeatureManager = FakeFeatureManager2(true),
    testClock: TestClock = TestClock(),
    timeZone: TimeZone = TimeZone.UTC,
  ): GetHomeDataUseCase {
    return GetHomeDataUseCaseImpl(
      apolloClient,
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      faetureManager,
      testClock,
      timeZone,
    )
  }
}
