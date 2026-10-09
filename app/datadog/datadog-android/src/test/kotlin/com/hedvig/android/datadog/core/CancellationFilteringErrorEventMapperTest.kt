package com.hedvig.android.datadog.core

import assertk.assertThat
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.datadog.android.rum.model.ErrorEvent
import com.hedvig.android.datadog.core.network.REQUEST_CANCELLED_RUM_ATTRIBUTE
import org.junit.Test

internal class CancellationFilteringErrorEventMapperTest {
  @Test
  fun `drops a network error tagged as a cancelled request`() {
    val event = errorEvent(
      type = "su1",
      stack = "su1: Child of the scoped flow was cancelled",
      attributes = mapOf(REQUEST_CANCELLED_RUM_ATTRIBUTE to true),
    )

    assertThat(cancellationFilteringErrorEventMapper.map(event)).isNull()
  }

  @Test
  fun `keeps a network error that was not cancelled`() {
    val event = errorEvent(
      type = "java.net.UnknownHostException",
      stack = "java.net.UnknownHostException: Unable to resolve host",
      attributes = emptyMap(),
    )

    assertThat(cancellationFilteringErrorEventMapper.map(event)).isNotNull()
  }

  @Test
  fun `drops a cancelled OkHttp request`() {
    val event = errorEvent(
      type = "java.io.IOException",
      stack = "java.io.IOException: Canceled",
      attributes = emptyMap(),
    )

    assertThat(cancellationFilteringErrorEventMapper.map(event)).isNull()
  }

  private fun errorEvent(type: String, stack: String, attributes: Map<String, Any?>): ErrorEvent = ErrorEvent(
    date = 0L,
    application = ErrorEvent.Application(id = "application"),
    session = ErrorEvent.ErrorEventSession(id = "session", type = ErrorEvent.ErrorEventSessionType.USER),
    view = ErrorEvent.ErrorEventView(id = "view", url = "view"),
    dd = ErrorEvent.Dd(),
    context = ErrorEvent.Context(additionalProperties = attributes.toMutableMap()),
    error = ErrorEvent.Error(
      message = "Ktor request error POST https://apollo-router.prod.hedvigit.com",
      source = ErrorEvent.ErrorSource.NETWORK,
      stack = stack,
      isCrash = false,
      type = type,
      category = ErrorEvent.Category.EXCEPTION,
    ),
  )
}
