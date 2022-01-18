package com.hedvig.app.feature.claimdetail.model

/**
 * A class to hold the url coming from the backend, with an overridden equals in order to return true in case we're
 * dealing with the same audio file but with a different query string
 */
class SignedAudioUrl private constructor(
    val rawUrl: String,
) {
    private val urlWithoutQueryString: String = rawUrl.substringBefore("?")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SignedAudioUrl
        if (urlWithoutQueryString != other.urlWithoutQueryString) return false
        return true
    }

    override fun hashCode(): Int {
        return urlWithoutQueryString.hashCode()
    }

    companion object {
        fun fromSignedAudioUrlStringOrNull(signedAudioUrl: String?): SignedAudioUrl? {
            if (signedAudioUrl == null) return null
            return SignedAudioUrl(signedAudioUrl)
        }
    }
}
