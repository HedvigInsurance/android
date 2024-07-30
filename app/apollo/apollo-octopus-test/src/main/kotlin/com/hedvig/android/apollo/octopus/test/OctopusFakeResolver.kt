package com.hedvig.android.apollo.octopus.test

import com.apollographql.apollo.api.DefaultFakeResolver
import com.apollographql.apollo.api.FakeResolver
import com.apollographql.apollo.api.FakeResolverContext
import com.hedvig.android.core.markdown.MarkdownString
import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octopus.schema.__Schema

private val delegate = DefaultFakeResolver(__Schema.all)

object OctopusFakeResolver : FakeResolver by delegate {
  override fun resolveLeaf(context: FakeResolverContext): Any {
    return when (
      context.mergedField.type
        .rawType()
        .name
    ) {
      "Date" ->
        Clock.System
          .now()
          .toLocalDateTime(TimeZone.currentSystemDefault())
          .date
      "UUID" -> UUID.randomUUID().toString()
      "Url" -> """www.example.com"""
      "Instant" -> kotlinx.datetime.Instant.DISTANT_FUTURE
      "DateTime" -> kotlinx.datetime.Instant.DISTANT_FUTURE
      "Markdown" -> MarkdownString("test")
      else -> delegate.resolveLeaf(context)
    }
  }

  override fun resolveListSize(context: FakeResolverContext): Int {
    return 0
  }
}

object OctopusFakeResolverWithFilledLists : FakeResolver by OctopusFakeResolver {
  override fun resolveListSize(context: FakeResolverContext): Int {
    return 3
  }
}
