package com.hedvig.android.apollo.octopus.test

import com.apollographql.apollo3.api.DefaultFakeResolver
import com.apollographql.apollo3.api.FakeResolver
import com.apollographql.apollo3.api.FakeResolverContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octopus.type.__Schema

private val delegate = DefaultFakeResolver(__Schema.all)
object OctopusFakeResolver : FakeResolver by delegate {

  override fun resolveLeaf(context: FakeResolverContext): Any {
    return when (context.mergedField.type.rawType().name) {
      "Date" -> Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
      else -> delegate.resolveLeaf(context)
    }
  }

  override fun resolveListSize(context: FakeResolverContext): Int {
    return 0
  }
}
