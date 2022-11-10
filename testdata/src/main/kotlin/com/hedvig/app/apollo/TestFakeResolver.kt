package com.hedvig.app.apollo

import com.apollographql.apollo3.api.DefaultFakeResolver
import com.apollographql.apollo3.api.FakeResolver
import com.apollographql.apollo3.api.FakeResolverContext
import com.hedvig.android.apollo.graphql.type.__Schema

object TestFakeResolver : FakeResolver by DefaultFakeResolver(__Schema.all) {
  override fun resolveListSize(context: FakeResolverContext): Int {
    return 0
  }
}
