package com.hedvig.android.data.chat.database

import androidx.paging.PagingSource
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import assertk.assertThat
import assertk.assertions.containsExactly
import com.benasher44.uuid.Uuid
import com.hedvig.android.data.chat.database.converter.InstantConverter
import com.hedvig.android.data.chat.database.converter.UuidConverter
import com.hedvig.android.test.clock.TestClock
import java.util.UUID
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ChatDaoTest {
  lateinit var chatMessageDatabase: TestChatMessageDatabase

  @Before
  fun openDb() {
    chatMessageDatabase = Room.inMemoryDatabaseBuilder<TestChatMessageDatabase>()
      .setDriver(BundledSQLiteDriver())
      .setQueryCoroutineContext(Dispatchers.IO)
      .build()
  }

  @After
  fun closeDb() {
    chatMessageDatabase.close()
  }

  @Test
  fun `messages currently being sent are loaded at the end of the list, regardless of the timestamp they were sent at`() =
    runTest {
      val clock = TestClock()
      val chatDao = chatMessageDatabase.chatDao()

      val conversationId = Uuid.randomUUID()
      val sentChatMessage1 = textChatMessageEntity("message1", conversationId, clock)
      clock.advanceTimeBy(1.seconds)
      val loadingChatMessage1 = textChatMessageEntity("loading1", conversationId, clock, isBeingSent = true)
      clock.advanceTimeBy(1.seconds)
      val sentChatMessage2 = textChatMessageEntity("message2", conversationId, clock)
      chatDao.insertAll(listOf(sentChatMessage1, loadingChatMessage1, sentChatMessage2))

      val pagingSource = chatDao.messages(conversationId)
      val page = pagingSource.load(PagingSource.LoadParams.Refresh(null, 3, true)) as PagingSource.LoadResult.Page

      val messages = page.data
      assertThat(messages).containsExactly(
        loadingChatMessage1,
        sentChatMessage2,
        sentChatMessage1,
      )
    }
}

private fun textChatMessageEntity(
  text: String,
  conversationId: UUID,
  clock: TestClock,
  isBeingSent: Boolean = false,
): ChatMessageEntity = ChatMessageEntity(
  id = Uuid.randomUUID(),
  conversationId = conversationId,
  sender = ChatMessageEntity.Sender.HEDVIG,
  sentAt = clock.now(),
  text = text,
  gifUrl = null,
  url = null,
  mimeType = null,
  failedToSend = null,
  isBeingSent = isBeingSent,
)

@Database(
  entities = [ChatMessageEntity::class],
  version = 1,
)
@TypeConverters(InstantConverter::class, UuidConverter::class)
abstract class TestChatMessageDatabase : RoomDatabase() {
  abstract fun chatDao(): ChatDao
}
