package com.hedvig.android.feature.chat.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.hedvig.android.data.chat.database.ChatMessageEntity
import com.hedvig.android.database.AppDatabase
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

/** [chatTransaction] on the database as Android builds it: without a driver, on the framework's SQLite. */
@RunWith(AndroidJUnit4::class)
class ChatTransactionAndroidTest {
  private val database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java)
    .setQueryCoroutineContext(Dispatchers.IO)
    .build()

  @After
  fun closeDatabase() {
    database.close()
  }

  @Test
  fun `the database runs on the framework's SQLite`() {
    assertThat(database.inCompatibilityMode()).isTrue()
  }

  @Test
  fun `a message inserted in a transaction that completes is stored`() = runBlocking {
    val conversationId = uuid4()
    val message = message(conversationId)

    database.chatTransaction { database.chatDao().insert(message) }

    assertThat(database.chatDao().latestMessage(conversationId).first()).isEqualTo(message)
  }

  @Test
  fun `a message inserted in a transaction that throws is rolled back`() = runBlocking {
    val conversationId = uuid4()

    assertFailure {
      database.chatTransaction {
        database.chatDao().insert(message(conversationId))
        error("Failing the transaction")
      }
    }.hasMessage("Failing the transaction")

    assertThat(database.chatDao().latestMessage(conversationId).first()).isNull()
  }

  @Test
  fun `an observer of the messages sees a message written in a transaction`() = runBlocking {
    val conversationId = uuid4()
    val message = message(conversationId)

    val observedMessages = withContext(Dispatchers.Default) {
      withTimeout(5.seconds) {
        val emissions = Channel<ChatMessageEntity?>(Channel.UNLIMITED)
        val observer = launch { database.chatDao().latestMessage(conversationId).collect { emissions.send(it) } }
        // The first emission comes after the observer's triggers are in place
        val beforeWrite = emissions.receive()
        database.chatTransaction { database.chatDao().insert(message) }
        val afterWrite = emissions.receive()
        observer.cancel()
        beforeWrite to afterWrite
      }
    }

    assertThat(observedMessages).isEqualTo(null to message)
  }

  @Test
  fun `a DAO call made from another dispatcher joins the transaction`() = runBlocking {
    val conversationId = uuid4()
    val message = message(conversationId)

    withTimeout(5.seconds) {
      database.chatTransaction {
        withContext(Dispatchers.Default) { database.chatDao().insert(message) }
        assertThat(database.chatDao().latestMessage(conversationId).first()).isEqualTo(message)
      }
    }

    assertThat(database.chatDao().latestMessage(conversationId).first()).isEqualTo(message)
  }

  @Test
  fun `a transaction inside a transaction is stored`() = runBlocking {
    val conversationId = uuid4()
    val message = message(conversationId)

    withTimeout(5.seconds) {
      database.chatTransaction { database.chatTransaction { database.chatDao().insert(message) } }
    }

    assertThat(database.chatDao().latestMessage(conversationId).first()).isEqualTo(message)
  }
}

private fun message(conversationId: Uuid) = ChatMessageEntity(
  id = uuid4(),
  conversationId = conversationId,
  sender = ChatMessageEntity.Sender.HEDVIG,
  sentAt = Instant.parse("2026-11-12T10:00:00Z"),
  text = "Hello",
  gifUrl = null,
  url = null,
  mimeType = null,
  failedToSend = null,
  isBeingSent = false,
  banner = null,
  action = null,
  isAiGenerationIndicator = false,
)
