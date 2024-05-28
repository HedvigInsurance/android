package com.hedvig.android.data.claimtriaging

import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable

data class ClaimGroup(
  val id: ClaimGroupId,
  val displayName: String,
  val entryPoints: ImmutableList<EntryPoint>,
)

@Serializable
@JvmInline
value class ClaimGroupId(val id: String)
