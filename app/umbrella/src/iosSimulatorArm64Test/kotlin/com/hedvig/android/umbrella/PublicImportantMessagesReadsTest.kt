package com.hedvig.android.umbrella

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.android.core.tracking.RumLogger
import com.hedvig.android.data.importantmessages.ios.PublicImportantMessageSeverity
import com.hedvig.android.data.importantmessages.ios.publicImportantMessagesReads
import com.hedvig.android.iosdata.bridge.ReadFailure
import com.hedvig.android.iosdata.bridge.ReadFailureKind
import com.hedvig.android.iosdata.bridge.iosGraphHolder
import com.hedvig.android.logger.IosLogcatLogger
import com.hedvig.android.logger.LogcatLogger
import io.ktor.http.HttpHeaders
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/** The logged-out important messages read, reached through the holder as the iOS app reaches it. */
class PublicImportantMessagesReadsTest {
  @BeforeTest
  fun installLoggers() {
    IosLogcatLogger.install()
    RumLogger.install(LogcatRumLogger)
  }

  @AfterTest
  fun uninstallLoggers() {
    LogcatLogger.uninstall()
    RumLogger.uninstall()
  }

  @Test
  fun `the read returns the messages as flat items`() {
    val graph = testGraph()
    graph.baseHttpClient.answerWith(MESSAGES_JSON)

    val delivered = iosGraphHolder { graph }.publicImportantMessagesReads().messagesNative().await()

    assertThat(
      delivered.value.orEmpty().map { listOf(it.id, it.message, it.severity, it.linkUrl, it.linkButtonText) },
    ).containsExactly(
      listOf("error-1", "Logins are failing", PublicImportantMessageSeverity.ERROR, "https://example.test", "Status"),
      listOf("info-1", "New app", PublicImportantMessageSeverity.INFO, null, null),
    )
  }

  @Test
  fun `the read sends no token while the member has one`() {
    val graph = testGraph()
    val sentPublic = graph.baseHttpClient.answerWith(MESSAGES_JSON)
    val sentAuthenticated = graph.httpClient.answerWith(MESSAGES_JSON)

    iosGraphHolder { graph }.publicImportantMessagesReads().messagesNative().await()

    assertThat(sentPublic.single()[HttpHeaders.Authorization]).isNull()
    assertThat(sentAuthenticated).isEmpty()
  }

  @Test
  fun `a failed read reaches Swift as a ReadFailure in the error`() {
    val graph = testGraph()
    graph.baseHttpClient.answerWith(null)

    val delivered = iosGraphHolder { graph }.publicImportantMessagesReads().messagesNative().await()

    assertThat(delivered.value).isNull()
    val failure = delivered.error?.userInfo?.get("KotlinException") as? ReadFailure
    assertThat(failure?.kind).isEqualTo(ReadFailureKind.OTHER)
  }
}

private val MESSAGES_JSON = """
  {"data": {"publicImportantMessages": [
    {"__typename": "PublicImportantMessage", "id": "error-1", "message": "Logins are failing", "severity": "ERROR",
     "linkInfo": {"__typename": "LinkInfo", "url": "https://example.test", "buttonText": "Status"}},
    {"__typename": "PublicImportantMessage", "id": "info-1", "message": "New app", "severity": "INFO", "linkInfo": null}
  ]}}
""".trimIndent()
