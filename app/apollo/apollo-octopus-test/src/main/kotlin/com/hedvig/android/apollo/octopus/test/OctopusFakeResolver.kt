package com.hedvig.android.apollo.octopus.test

import com.apollographql.apollo.api.FakeResolver
import com.apollographql.apollo.api.FakeResolverContext
import java.util.UUID
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octopus.builder.resolver.DefaultFakeResolver

private val delegate = DefaultFakeResolver()

object OctopusFakeResolver : FakeResolver by delegate {
  // Apollo 5 serializes data-builder leaf values through the response adapter, so a custom scalar
  // must resolve to its wire form (a String), not the mapped Kotlin type.
  override fun resolveLeaf(context: FakeResolverContext): Any {
    return when (
      context.mergedField.type
        .rawType()
        .name
    ) {
      "Date" -> {
        Clock.System
          .now()
          .toLocalDateTime(TimeZone.currentSystemDefault())
          .date
          .toString()
      }

      "UUID" -> {
        UUID.randomUUID().toString()
      }

      "Url" -> {
        """www.example.com"""
      }

      "Instant" -> {
        kotlin.time.Instant.DISTANT_FUTURE.toString()
      }

      "DateTime" -> {
        kotlin.time.Instant.DISTANT_FUTURE.toString()
      }

      "Markdown" -> {
        "test"
      }

      else -> {
        delegate.resolveLeaf(context)
      }
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
