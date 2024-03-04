package com.example.audio_player_data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.hedvig.audio.player.data.SignedAudioUrl
import org.junit.Test

class SignedAudioUrlTest {
  @Suppress("ktlint:standard:max-line-length")
  private val inputs = listOf(
    """https://com-hedvig-upload.s3.eu-central-1.amazonaws.com/0fbab74f-5049-485c-8b30-ab21000b570a-claim_18294177-e50d-4430-b8d9-e3466e321c242600122472964195955.aac?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAIZMW7F45HSE2X33Q%2F20220118%2Feu-central-1%2Fs3%2Faws4_request&X-Amz-Date=20220118T102653Z&X-Amz-Expires=1800&X-Amz-Signature=6c647ca9a6419020836f2534e869ab9cff6b2aff555f48c4ffd0b3d51093a560&X-Amz-SignedHeaders=host""",
    """https://com-hedvig-upload.s3.eu-central-1.amazonaws.com/0fbab74f-5049-485c-8b30-ab21000b570a-claim_18294177-e50d-4430-b8d9-e3466e321c242600122472964195955.aac?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAIZMW7F45HSE2X33Q%2F20220118%2Feu-central-1%2Fs3%2Faws4_request&X-Amz-Date=20220118T102654Z&X-Amz-Expires=1800&X-Amz-Signature=928f6ea645c6f73ec0a36aa2cf227953d55a1e4c6a5511521cae3da0a750a80f&X-Amz-SignedHeaders=host""",
  )

  @Test
  fun `a null input produces a null output`() {
    val rawInput: String? = null

    val signedAudioUrl = SignedAudioUrl.fromSignedAudioUrlStringOrNull(rawInput)

    assertThat(signedAudioUrl).isNull()
  }

  @Test
  fun `raw signed url maps to SignedAudioUrl without losing the raw value`() {
    val rawInput = inputs.first()

    val signedAudioUrl = SignedAudioUrl.fromSignedAudioUrlStringOrNull(rawInput)

    assertThat(signedAudioUrl).isNotNull()
    assertThat(signedAudioUrl!!.rawUrl).isEqualTo(rawInput)
  }

  @Test
  fun `two urls with different X-Amz-Signature still return true on their equality`() {
    val rawInput0 = inputs[0]
    val rawInput1 = inputs[1]

    val signedAudioUrl0 = SignedAudioUrl.fromSignedAudioUrlStringOrNull(rawInput0)
    val signedAudioUrl1 = SignedAudioUrl.fromSignedAudioUrlStringOrNull(rawInput1)

    assertThat(signedAudioUrl0).isEqualTo(signedAudioUrl1)
  }
}
