package com.hedvig.android.data.chat.read.timestamp

import androidx.paging.PagingSource
import androidx.room.Room
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.enqueueTestNetworkError
import com.apollographql.apollo.testing.enqueueTestResponse
import com.benasher44.uuid.Uuid
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.data.chat.database.AppDatabase
import com.hedvig.android.data.chat.database.ChatDao
import com.hedvig.android.data.chat.database.ChatMessageEntity
import com.hedvig.android.database.test.TestAppDatabaseRule
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import octopus.ChatLatestMessageTimestampsQuery
import octopus.type.buildChat
import octopus.type.buildChatMessageText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ApolloExperimental::class)
@RunWith(TestParameterInjector::class)
class ChatLastMessageReadRepositoryImplTest {
  @get:Rule
  val testApolloClientRule = TestApolloClientRule()
  val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

//  @get:Rule
//  val appDatabaseRule = TestAppDatabaseRule(AppDatabase::class.java)
//  val appDatabase: AppDatabase
//    get() = appDatabaseRule.appDatabase as AppDatabase
  // TODO CBM: Test with in-memory JVM Room database once wew figure out how to do that

  @Test
  fun `With no timestamp stored, we get that there is a new message if any messages exist`(
    @TestParameter messagesExist: Boolean,
  ) = runTest {
    val chatMessageTimestampStorage = FakeChatMessageTimestampStorage()
    val chatLastMessageReadRepositoryImpl = ChatLastMessageReadRepositoryImpl(
      chatMessageTimestampStorage = chatMessageTimestampStorage,
      apolloClient = apolloClient,
      featureManager = FakeFeatureManager2(mapOf(Feature.ENABLE_CBM to false)),
      chatDao = NoopChatDao(),
    )
    apolloClient.enqueueTestResponse(
      ChatLatestMessageTimestampsQuery(),
      ChatLatestMessageTimestampsQuery.Data(OctopusFakeResolver) {
        chat = buildChat {
          messages = if (messagesExist) {
            listOf(
              buildChatMessageText {
                sentAt = Instant.parse("2022-01-01T00:00:00Z")
              },
            )
          } else {
            emptyList()
          }
        }
      },
    )
    val result = chatLastMessageReadRepositoryImpl.isNewestMessageNewerThanLastReadTimestamp()

    assertThat(result).isEqualTo(messagesExist)
  }

  @Test
  fun `for various message timestamps from backend, return true only on newer timestamps existing`(
    @TestParameter timeComparedToLastReadMessage: TimeComparedToLastReadMessage,
  ) = runTest {
    val chatMessageTimestampStorage = FakeChatMessageTimestampStorage()
    val chatLastMessageReadRepositoryImpl = ChatLastMessageReadRepositoryImpl(
      chatMessageTimestampStorage = chatMessageTimestampStorage,
      apolloClient = apolloClient,
      featureManager = FakeFeatureManager2(mapOf(Feature.ENABLE_CBM to false)),
      chatDao = NoopChatDao(),
    )
    val lastReadMessageTimestamp = Instant.parse("2022-01-01T00:00:01Z")
    apolloClient.enqueueTestResponse(
      ChatLatestMessageTimestampsQuery(),
      ChatLatestMessageTimestampsQuery.Data(OctopusFakeResolver) {
        chat = buildChat {
          messages = listOf(
            buildChatMessageText {
              sentAt = when (timeComparedToLastReadMessage) {
                TimeComparedToLastReadMessage.NEWER -> Instant.parse("2022-01-01T00:00:02Z")
                TimeComparedToLastReadMessage.SAME -> Instant.parse("2022-01-01T00:00:01Z")
                TimeComparedToLastReadMessage.OLDER -> Instant.parse("2022-01-01T00:00:00Z")
              }
            },
          )
        }
      },
    )
    chatMessageTimestampStorage.setLatestReadTimestamp(lastReadMessageTimestamp)
    val result = chatLastMessageReadRepositoryImpl.isNewestMessageNewerThanLastReadTimestamp()

    assertThat(result).isEqualTo(
      when (timeComparedToLastReadMessage) {
        TimeComparedToLastReadMessage.NEWER -> true
        TimeComparedToLastReadMessage.SAME -> false
        TimeComparedToLastReadMessage.OLDER -> false
      },
    )
  }

  @Test
  fun `after reporting no new unread messages, when the storage is cleared, now report correctly`() = runTest {
    val chatMessageTimestampStorage = FakeChatMessageTimestampStorage()
    val chatLastMessageReadRepositoryImpl = ChatLastMessageReadRepositoryImpl(
      chatMessageTimestampStorage = chatMessageTimestampStorage,
      apolloClient = apolloClient,
      featureManager = FakeFeatureManager2(mapOf(Feature.ENABLE_CBM to false)),
      chatDao = NoopChatDao(),
    )
    val enqueueBackendResponse = {
      apolloClient.enqueueTestResponse(
        ChatLatestMessageTimestampsQuery(),
        ChatLatestMessageTimestampsQuery.Data(OctopusFakeResolver) {
          chat = buildChat {
            messages = listOf(
              buildChatMessageText {
                sentAt = Instant.parse("2022-01-01T00:00:00Z")
              },
            )
          }
        },
      )
    }
    enqueueBackendResponse()
    chatMessageTimestampStorage.setLatestReadTimestamp(Instant.parse("2022-01-01T00:00:00Z"))
    val firstResult = chatLastMessageReadRepositoryImpl.isNewestMessageNewerThanLastReadTimestamp()
    assertThat(firstResult).isEqualTo(false)

    enqueueBackendResponse()
    chatMessageTimestampStorage.clearLatestReadTimestamp()
    val secondResult = chatLastMessageReadRepositoryImpl.isNewestMessageNewerThanLastReadTimestamp()
    assertThat(secondResult).isEqualTo(true)
  }

  @Test
  fun `if backend request fails, always default to reporting no new unread message`(
    @TestParameter hasStoredTimestamp: Boolean,
  ) = runTest {
    val chatMessageTimestampStorage = FakeChatMessageTimestampStorage()
    val chatLastMessageReadRepositoryImpl = ChatLastMessageReadRepositoryImpl(
      chatMessageTimestampStorage = chatMessageTimestampStorage,
      apolloClient = apolloClient,
      featureManager = FakeFeatureManager2(mapOf(Feature.ENABLE_CBM to false)),
      chatDao = NoopChatDao(),
    )
    apolloClient.enqueueTestNetworkError()
    if (hasStoredTimestamp) {
      chatMessageTimestampStorage.setLatestReadTimestamp(Instant.parse("2022-01-01T00:00:00Z"))
    } else {
      chatMessageTimestampStorage.clearLatestReadTimestamp()
    }
    val firstResult = chatLastMessageReadRepositoryImpl.isNewestMessageNewerThanLastReadTimestamp()
    assertThat(firstResult).isEqualTo(false)
  }

  @Test
  fun `when there are many messages, correctly check the newest one to check if there is an unread message`() =
    runTest {
      val chatMessageTimestampStorage = FakeChatMessageTimestampStorage()
      val chatLastMessageReadRepositoryImpl = ChatLastMessageReadRepositoryImpl(
        chatMessageTimestampStorage = chatMessageTimestampStorage,
        apolloClient = apolloClient,
        featureManager = FakeFeatureManager2(mapOf(Feature.ENABLE_CBM to false)),
        chatDao = NoopChatDao(),
      )
      apolloClient.enqueueTestResponse(
        ChatLatestMessageTimestampsQuery(),
        ChatLatestMessageTimestampsQuery.Data(OctopusFakeResolver) {
          chat = buildChat {
            messages = listOf(
              buildChatMessageText { sentAt = Instant.parse("2022-01-01T00:00:00Z") },
              buildChatMessageText { sentAt = Instant.parse("2022-01-01T00:00:01Z") },
              buildChatMessageText { sentAt = Instant.parse("2022-01-01T00:00:02Z") },
              buildChatMessageText { sentAt = Instant.parse("2022-01-01T00:00:03Z") },
              buildChatMessageText { sentAt = Instant.parse("2022-01-01T00:00:04Z") },
              buildChatMessageText { sentAt = Instant.parse("2022-01-01T00:00:05Z") },
              buildChatMessageText { sentAt = Instant.parse("2022-01-01T00:00:06Z") },
            )
          }
        },
      )
      chatMessageTimestampStorage.setLatestReadTimestamp(Instant.parse("2022-01-01T00:00:04Z"))
      val firstResult = chatLastMessageReadRepositoryImpl.isNewestMessageNewerThanLastReadTimestamp()
      assertThat(firstResult).isEqualTo(true)
    }
}

enum class TimeComparedToLastReadMessage { NEWER, OLDER, SAME }

private class NoopChatDao : ChatDao {
  override suspend fun insert(message: ChatMessageEntity) {
    error("noop")
  }

  override suspend fun insertAll(messages: List<ChatMessageEntity>) {
    error("noop")
  }

  override suspend fun clearRemoteMessagesAndOldUnsentMessages(
    conversationId: Uuid,
    deleteUnsentMessagesOlderThan: Instant,
  ) {
    error("noop")
  }

  override fun messages(conversationId: Uuid): PagingSource<Int, ChatMessageEntity> {
    error("noop")
  }

  override fun latestMessage(conversationId: Uuid): Flow<ChatMessageEntity?> {
    error("noop")
  }

  override fun lastDeliveredMessage(conversationId: Uuid): Flow<Uuid?> {
    error("noop")
  }

  override suspend fun deleteMessage(conversationId: Uuid, messageId: String) {
    error("noop")
  }

  override suspend fun getFailedMessage(conversationId: Uuid, messageId: String): ChatMessageEntity? {
    error("noop")
  }
}
