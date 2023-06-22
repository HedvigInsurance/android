package com.hedvig.android.data.claimtriaging

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable

@Serializable
data class EntryPoint(
  val id: EntryPointId,
  val displayName: String,
  val entryPointOptions: ImmutableList<EntryPointOption>?,
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

internal fun List<octopus.fragment.EntryPoint.Option>.toEntryPointOptions(): ImmutableList<EntryPointOption> {
  return map { option ->
    EntryPointOption(
      EntryPointOptionId(option.id),
      option.displayName,
    )
  }.toImmutableList()
}
