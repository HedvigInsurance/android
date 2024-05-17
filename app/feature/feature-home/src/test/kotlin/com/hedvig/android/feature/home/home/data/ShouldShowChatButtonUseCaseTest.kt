package com.hedvig.android.feature.home.home.data

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.testing.registerTestResponse
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager2
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import octopus.NumberOfChatMessagesQuery
import octopus.type.ChatMessageSender
import octopus.type.buildChat
import octopus.type.buildChatMessageText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ApolloExperimental::class)
@RunWith(TestParameterInjector::class)
class ShouldShowChatButtonUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)
  val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

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
    val shouldShowChatButtonUseCase = testUseCase(featureManager)

    apolloClient.registerTestResponse(
      NumberOfChatMessagesQuery(),
      NumberOfChatMessagesQuery.Data(OctopusFakeResolver) {
        chat = buildChat {
          messages = if (isEligibleToSeeTheChatButton) {
            listOf(
              buildChatMessageText {
                sender = ChatMessageSender.MEMBER
              },
            )
          } else {
            emptyList()
          }
        }
      },
    )
    val result = shouldShowChatButtonUseCase.invoke().first()

    assertThat(result).apply {
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
    val shouldShowChatButtonUseCase = testUseCase(featureManager)

    apolloClient.registerTestResponse(
      NumberOfChatMessagesQuery(),
      NumberOfChatMessagesQuery.Data(OctopusFakeResolver) {
        chat = buildChat {
          messages = buildList {
            if (hasAtLeastOneMessageByMember) {
              add(
                buildChatMessageText {
                  sender = ChatMessageSender.MEMBER
                },
              )
            }
            addAll(
              List(numberOfMessagesByHedvig) {
                buildChatMessageText {
                  sender = ChatMessageSender.HEDVIG
                }
              },
            )
          }
        }
      },
    )
    val result = shouldShowChatButtonUseCase.invoke().first()

    assertThat(result).apply {
      if (hasAtLeastOneMessageByMember || numberOfMessagesByHedvig > 1) {
        isTrue()
      } else {
        isFalse()
      }
    }
  }

  private fun testUseCase(featureManager: FeatureManager = FakeFeatureManager2(true)): ShouldShowChatButtonUseCase {
    return ShouldShowChatButtonUseCaseImpl(apolloClient, featureManager)
  }
}
