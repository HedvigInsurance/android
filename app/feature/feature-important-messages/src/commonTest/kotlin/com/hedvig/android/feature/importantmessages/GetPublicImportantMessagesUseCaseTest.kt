package com.hedvig.android.feature.importantmessages

import arrow.core.Either
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.hedvig.android.feature.importantmessages.PublicImportantMessage.Link
import com.hedvig.android.feature.importantmessages.PublicImportantMessage.Severity
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class GetPublicImportantMessagesUseCaseTest {
  @Test
  fun `maps id, message, severity and link`() = runTest {
    val json = messagesJson(
      messageJson("error-1", "Logins are failing", "ERROR", "https://example.test/status", "Status"),
      messageJson("warning-1", "Slow claims", "WARNING"),
      messageJson("info-1", "New app", "INFO"),
    )

    val result = useCaseAnswering(json).invoke()

    assertThat(result).isEqualTo(
      Either.Right(
        listOf(
          PublicImportantMessage(
            id = "error-1",
            message = "Logins are failing",
            severity = Severity.Error,
            link = Link(url = "https://example.test/status", buttonText = "Status"),
          ),
          PublicImportantMessage("warning-1", "Slow claims", Severity.Warning, link = null),
          PublicImportantMessage("info-1", "New app", Severity.Info, link = null),
        ),
      ),
    )
  }

  @Test
  fun `an unknown severity becomes Warning`() = runTest {
    val result = useCaseAnswering(messagesJson(messageJson("new-1", severity = "CRITICAL"))).invoke()

    assertThat(result.messages().map { it.severity }).containsExactly(Severity.Warning)
  }

  @Test
  fun `a link with a blank url is dropped and the message kept`() = runTest {
    val json = messagesJson(messageJson("info-1", "Read this", linkUrl = "  ", linkButtonText = "Open"))

    val result = useCaseAnswering(json).invoke()

    assertThat(result).isEqualTo(
      Either.Right(listOf(PublicImportantMessage("info-1", "Read this", Severity.Info, link = null))),
    )
  }

  @Test
  fun `a message with blank text is left out`() = runTest {
    val json = messagesJson(messageJson("blank-1", message = " "), messageJson("kept-1", message = "Kept"))

    val result = useCaseAnswering(json).invoke()

    assertThat(result.messages().map { it.id }).containsExactly("kept-1")
  }

  @Test
  fun `a network failure is a Left`() = runTest {
    val result = useCaseAnswering(json = null).invoke()

    assertThat(result).isInstanceOf<Either.Left<*>>()
  }

  @Test
  fun `the query always goes to the network`() = runTest {
    val server = TestApolloServer { messagesJson(messageJson("info-1")) }
    val useCase = GetPublicImportantMessagesUseCase(server.apolloClient())

    useCase.invoke()
    useCase.invoke()

    assertThat(server.requests).isEqualTo(2)
  }

  private fun useCaseAnswering(json: String?) = GetPublicImportantMessagesUseCase(
    TestApolloServer {
      json
    }.apolloClient(),
  )

  private fun Either<*, List<PublicImportantMessage>>.messages(): List<PublicImportantMessage> =
    (this as Either.Right).value
}
