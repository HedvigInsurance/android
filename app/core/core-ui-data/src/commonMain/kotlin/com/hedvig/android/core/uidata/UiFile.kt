package com.hedvig.android.core.uidata

import io.ktor.http.encodeURLParameter
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

@Serializable(with = UiFileSerializer::class)
data class UiFile(
  val name: String,
  val localPath: String?,
  val url: String?,
  val mimeType: String,
  val id: String,
)

/**
 * Custom serializer for UiFile that preserves URL encoding through navigation.
 *
 * The navigation system URL-decodes all parameters, which breaks AWS signed URLs
 * in the url field. This serializer URL-encodes the url field during serialization,
 * so after navigation's automatic URL decoding, we end up with the original
 * properly-encoded AWS signed URL.
 */
internal object UiFileSerializer : KSerializer<UiFile> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("UiFile") {
    element<String>("name")
    element<String?>("localPath")
    element<String?>("url")
    element<String>("mimeType")
    element<String>("id")
  }

  override fun serialize(encoder: Encoder, value: UiFile) {
    encoder.encodeStructure(descriptor) {
      encodeStringElement(descriptor, 0, value.name)
      encodeNullableSerializableElement(descriptor, 1, kotlinx.serialization.serializer(), value.localPath)
      // URL-encode the url field so it survives navigation's URL decoding
      val encodedUrl = value.url?.encodeURLParameter()
      encodeNullableSerializableElement(descriptor, 2, kotlinx.serialization.serializer(), encodedUrl)
      encodeStringElement(descriptor, 3, value.mimeType)
      encodeStringElement(descriptor, 4, value.id)
    }
  }

  override fun deserialize(decoder: Decoder): UiFile {
    return decoder.decodeStructure(descriptor) {
      var name = ""
      var localPath: String? = null
      var url: String? = null
      var mimeType = ""
      var id = ""

      while (true) {
        when (val index = decodeElementIndex(descriptor)) {
          0 -> name = decodeStringElement(descriptor, 0)
          1 -> localPath = decodeNullableSerializableElement(descriptor, 1, kotlinx.serialization.serializer())
          2 -> url = decodeNullableSerializableElement(descriptor, 2, kotlinx.serialization.serializer())
          3 -> mimeType = decodeStringElement(descriptor, 3)
          4 -> id = decodeStringElement(descriptor, 4)
          CompositeDecoder.DECODE_DONE -> break
          else -> error("Unexpected index: $index")
        }
      }
      UiFile(name, localPath, url, mimeType, id)
    }
  }
}
