package com.hedvig.feature.claim.chat.audiorecorder.audioplayer

import kotlinx.serialization.Serializable

/**
 * A class to hold the url coming from the backend, with an overridden equals in order to return true in case we're
 * dealing with the same audio file but with a different query string
 */

@Serializable
class SignedAudioUrl private constructor(
  val rawUrl: String,
) {
  private val urlWithoutQueryString: String = rawUrl.substringBefore("?")

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    // TODO if (javaClass != other?.javaClass) return false
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
