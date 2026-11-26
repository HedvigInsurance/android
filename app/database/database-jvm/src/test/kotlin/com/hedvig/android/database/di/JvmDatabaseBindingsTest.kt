package com.hedvig.android.database.di

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.benasher44.uuid.Uuid
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.DesktopDataDirectory
import com.hedvig.android.data.chat.database.ChatDao
import com.hedvig.android.data.chat.database.ChatMessageEntity
import com.hedvig.android.database.AppDatabase
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test

/**
 * Every binding comes from the JVM classpath of this module (`database-core`'s providers and this module's
 * builder); the factory supplies only the data directory, as a desktop host does.
 */
@DependencyGraph(AppScope::class)
internal interface DatabaseBindingsTestGraph {
  val appDatabase: AppDatabase
  val chatDao: ChatDao

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides @DesktopDataDirectory dataDirectory: File,
    ): DatabaseBindingsTestGraph
  }
}

/**
 * Opens the app database through the JVM bindings, in the data directory and under the Android file name, writes one
 * chat message through [ChatDao] and reads it back.
 */
class JvmDatabaseBindingsTest {
  private val dataDirectory: File = createTempDirectory("database-bindings-test").toFile()

  @After
  fun tearDown() {
    dataDirectory.deleteRecursively()
  }

  @Test
  fun `a chat message written through the JVM database is read back from the file in the data directory`() = runTest {
    val graph = createGraphFactory<DatabaseBindingsTestGraph.Factory>().create(dataDirectory)
    val conversationId = Uuid.randomUUID()
    val message = ChatMessageEntity(
      id = Uuid.randomUUID(),
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

    graph.chatDao.insert(message)
    val latestMessage = graph.chatDao.latestMessage(conversationId).first()
    graph.appDatabase.close()

    assertThat(latestMessage).isEqualTo(message)
    assertThat(File(dataDirectory, "hedvig_chat_database.db").isFile).isTrue()
  }
}
