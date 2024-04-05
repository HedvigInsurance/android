package com.hedvig.android.core.uidata

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias SerializableImmutableList<T> = @Serializable(ImmutableListSerializer::class) ImmutableList<T>

@Serializer(forClass = ImmutableList::class)
class ImmutableListSerializer<T>(private val dataSerializer: KSerializer<T>) : KSerializer<ImmutableList<T>> {
  private class PersistentListDescriptor : SerialDescriptor by serialDescriptor<List<String>>() {
    override val serialName: String = "kotlinx.serialization.immutable.ImmutableList"
  }

  override val descriptor: SerialDescriptor = PersistentListDescriptor()

  override fun serialize(encoder: Encoder, value: ImmutableList<T>) {
    return ListSerializer(dataSerializer).serialize(encoder, value.toList())
  }

  override fun deserialize(decoder: Decoder): ImmutableList<T> {
    return ListSerializer(dataSerializer).deserialize(decoder).toPersistentList()
  }
}
