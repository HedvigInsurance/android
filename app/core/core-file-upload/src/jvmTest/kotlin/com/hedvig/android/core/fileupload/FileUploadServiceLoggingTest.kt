package com.hedvig.android.core.fileupload

import arrow.core.raise.either
import assertk.assertThat
import assertk.assertions.any
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.none
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import com.hedvig.android.logger.MemberDataLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class FileUploadServiceLoggingTest {
  private val loggedMessages = mutableListOf<String>()

  @Before
  fun setUp() {
    LogcatLogger.install(
      object : LogcatLogger {
        override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
          loggedMessages += message()
        }
      },
    )
  }

  @After
  fun tearDown() {
    LogcatLogger.uninstall()
    MemberDataLogging.isAllowed = false
  }

  @Test
  fun `a successful upload does not log the response body when member data is not allowed`() = runTest {
    val result = upload(HttpStatusCode.OK)

    assertThat(result.getOrNull()).isEqualTo(RESPONSE_BODY)
    assertThat(loggedMessages).any { it.contains("Upload successful") }
    assertThat(loggedMessages).none { it.contains(SECRET) }
  }

  @Test
  fun `a successful upload logs the response body when member data is allowed`() = runTest {
    MemberDataLogging.isAllowed = true

    upload(HttpStatusCode.OK)

    assertThat(loggedMessages).any { it.contains(RESPONSE_BODY) }
  }

  @Test
  fun `a failed upload keeps the status and drops the body when member data is not allowed`() = runTest {
    val result = upload(HttpStatusCode.InternalServerError)

    val errorMessage = result.leftOrNull()?.message
    assertThat(errorMessage).isNotNull().contains("500")
    assertThat(loggedMessages).any { it.contains("500") }
    assertThat(loggedMessages + listOfNotNull(errorMessage)).none { it.contains(SECRET) }
  }

  @Test
  fun `a failed upload logs the body when member data is allowed`() = runTest {
    MemberDataLogging.isAllowed = true

    val result = upload(HttpStatusCode.InternalServerError)

    assertThat(result.leftOrNull()?.message).isNotNull().contains(RESPONSE_BODY)
    assertThat(loggedMessages).any { it.contains(RESPONSE_BODY) }
  }

  private suspend fun upload(status: HttpStatusCode) = either<ErrorMessage, String> {
    val client = HttpClient(MockEngine { respond(RESPONSE_BODY, status) }) { install(HttpTimeout) }
    FileUploadService(client).uploadWithCustomFormData(
      url = "https://gateway.test/upload",
      formDataBuilder = {},
      deserializer = { it },
    )
  }

  private companion object {
    const val SECRET = "X-Amz-Signature=abc"
    const val RESPONSE_BODY = """{"url":"https://bucket.test/file.jpg?$SECRET"}"""
  }
}
