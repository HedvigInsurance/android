package com.hedvig.audio.player.data

import io.ktor.http.encodeURLParameter
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * A class to hold the url coming from the backend, with an overridden equals in order to return true in case we're
 * dealing with the same audio file but with a different query string
 */

@Serializable(with = SignedAudioUrlSerializer::class)
class SignedAudioUrl private constructor(
  val rawUrl: String,
) {
  private val urlWithoutQueryString: String = rawUrl.substringBefore("?")

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    other as SignedAudioUrl
    return urlWithoutQueryString == other.urlWithoutQueryString
  }

  override fun hashCode(): Int {
    return urlWithoutQueryString.hashCode()
  }

  companion object {
    fun fromSignedAudioUrlString(signedAudioUrl: String): SignedAudioUrl {
      return SignedAudioUrl(signedAudioUrl)
    }

    fun fromSignedAudioUrlStringOrNull(signedAudioUrl: String?): SignedAudioUrl? {
      if (signedAudioUrl == null) return null
      return SignedAudioUrl(signedAudioUrl)
    }
  }
}

/**
 * Custom serializer for SignedAudioUrl that preserves URL encoding through navigation.
 *
 * The navigation system URL-decodes all parameters, which breaks AWS signed URLs
 * that contain encoded characters like %2B, %2F, %3D in their security tokens.
 *
 * This serializer URL-encodes the rawUrl during serialization, so after the navigation
 * system's automatic URL decoding, we end up with the original properly-encoded AWS URL.
 */
internal object SignedAudioUrlSerializer : KSerializer<SignedAudioUrl> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
    "SignedAudioUrl",
    PrimitiveKind.STRING,
  )

  override fun serialize(encoder: Encoder, value: SignedAudioUrl) {
    // URL-encode the rawUrl so it survives navigation's URL decoding
    encoder.encodeString(value.rawUrl.encodeURLParameter())
  }

  override fun deserialize(decoder: Decoder): SignedAudioUrl {
    val rawUrl = decoder.decodeString()
    return SignedAudioUrl.fromSignedAudioUrlString(rawUrl)
  }
}
