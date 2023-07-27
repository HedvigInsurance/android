package com.hedvig.android.apollo.giraffe.test

import com.apollographql.apollo3.api.DefaultFakeResolver
import com.apollographql.apollo3.api.FakeResolver
import com.apollographql.apollo3.api.FakeResolverContext
import giraffe.type.__Schema

private val delegate = DefaultFakeResolver(__Schema.all)
object GiraffeFakeResolver : FakeResolver by delegate {

  override fun resolveLeaf(context: FakeResolverContext): Any {
    return when (context.mergedField.type.rawType().name) {
      "LocalDate" -> java.time.LocalDate.now()
      "Instant" -> java.time.Instant.now()
      else -> delegate.resolveLeaf(context)
    }
  }

  override fun resolveListSize(context: FakeResolverContext): Int {
    return 0
  }
}
