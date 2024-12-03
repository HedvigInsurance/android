package com.hedvig.android.data.claimtriaging

import kotlinx.serialization.Serializable

@Serializable
data class EntryPoint(
  val id: EntryPointId,
  val displayName: String,
  val entryPointOptions: List<EntryPointOption>?,
)

@Serializable
@JvmInline
value class EntryPointId(val id: String)

@Serializable
data class EntryPointOption(
  val id: EntryPointOptionId,
  val displayName: String,
)

@Serializable
@JvmInline
value class EntryPointOptionId(val id: String)
