package com.hedvig.android.odyssey.sdui

import com.datadog.opentracing.DDTracer.DDSpanBuilder
import com.hedvig.odyssey.datadog.DatadogLogger
import com.hedvig.odyssey.datadog.DatadogProvider
import com.hedvig.odyssey.datadog.OdysseyHTTPRequest
import com.hedvig.odyssey.datadog.OdysseyHTTPResponse
import io.opentracing.Span
import io.opentracing.Tracer
import io.opentracing.propagation.Format
import io.opentracing.propagation.TextMapInject
import io.opentracing.tag.Tags
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

internal class AndroidDatadogProvider(
  private val tracer: Tracer,
  override val logger: DatadogLogger,
) : DatadogProvider {

  private val spans = ConcurrentHashMap<String, Span>()

  override fun start(request: OdysseyHTTPRequest): Map<String, String> {
    val span = tracer.buildSpan(request.path)
      .withTag(Tags.HTTP_URL.key, request.url)
      .withTag(Tags.HTTP_METHOD.key, request.method)
      .apply {
        (this as? DDSpanBuilder)?.withResourceName(request.url.substringBefore(URL_QUERY_PARAMS_BLOCK_SEPARATOR))
      }
      .start()

    val spanId = UUID.randomUUID().toString()
    request.addAttribute(ATTRIBUTE_SPAN_ID, spanId)

    spans[spanId] = span

    val headers = mutableMapOf<String, String>()
    tracer.inject(
      span.context(),
      Format.Builtin.TEXT_MAP_INJECT,
      TextMapInject { key: String, value: String ->
        headers[key] = value
      },
    )
    return headers.toMap()
  }

  override fun end(response: OdysseyHTTPResponse) {
    val spanId: String = response.getAttribute(ATTRIBUTE_SPAN_ID) ?: return
    val span = spans[spanId] ?: return
    span.setTag(Tags.HTTP_STATUS.key, response.statusCode)
    span.finish()
    spans.remove(spanId)
  }
}

private const val URL_QUERY_PARAMS_BLOCK_SEPARATOR = '?'
private const val ATTRIBUTE_SPAN_ID = "span-id"
