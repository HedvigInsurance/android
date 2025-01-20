package com.hedvig.android.feature.movingflow.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

data class ClosedRangeSerializer<T : Comparable<T>>(
  private val dataSerializer: KSerializer<T>,
) : KSerializer<ClosedRange<T>> {
  override val descriptor: SerialDescriptor = ClosedRangeSurrogate.serializer<T>(dataSerializer).descriptor

  override fun serialize(encoder: Encoder, value: ClosedRange<T>) {
    val surrogate = ClosedRangeSurrogate(value.start, value.endInclusive)
    encoder.encodeSerializableValue(ClosedRangeSurrogate.serializer<T>(dataSerializer), surrogate)
  }

  override fun deserialize(decoder: Decoder): ClosedRange<T> {
    val surrogate = decoder.decodeSerializableValue(ClosedRangeSurrogate.serializer<T>(dataSerializer))
    return surrogate.start..surrogate.end
  }
}

// https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serializers.md#composite-serializer-via-surrogate
@Serializable
private class ClosedRangeSurrogate<T : Comparable<T>>(
  val start: T,
  val end: T,
)
