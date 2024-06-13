package com.hedvig.android.data.claimtriaging

import kotlinx.serialization.Serializable

data class ClaimGroup(
  val id: ClaimGroupId,
  val displayName: String,
  val entryPoints: List<EntryPoint>,
)

@Serializable
@JvmInline
value class ClaimGroupId(val id: String)
