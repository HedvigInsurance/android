package com.hedvig.android.feature.chat.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.benasher44.uuid.uuid4
import com.eygraber.uri.Uri
import kotlin.time.Instant
import org.junit.Test

/**
 * A message that failed to be sent keeps the URI of its file as a string in the database, and a retry reads it back
 * from there. These URIs are the shapes the photo picker, the file picker and the camera hand to the chat.
 */
class FailedToBeSentUriTest {
  private val uriStrings = listOf(
    "content://media/picker/0/com.android.providers.media.photopicker/media/1000000123",
    "content://com.android.providers.downloads.documents/document/msf%3A1000000456",
    "content://com.android.externalstorage.documents/document/primary%3ADownload%2Fmy%20file%20%C3%A5%C3%A4%C3%B6.pdf",
    "file:///storage/emulated/0/Android/data/com.hedvig.app/files/Pictures/JPEG_1731400000000.jpg",
  )

  @Test
  fun `parsing a URI string gives back the same string`() {
    for (uriString in uriStrings) {
      assertThat(Uri.parse(uriString).toString()).isEqualTo(uriString)
    }
  }

  @Test
  fun `two URIs parsed from the same string are equal`() {
    for (uriString in uriStrings) {
      assertThat(Uri.parse(uriString)).isEqualTo(Uri.parse(uriString))
    }
  }

  @Test
  fun `a failed photo or media message survives the round trip through its database entity`() {
    val conversationId = uuid4()
    for (uriString in uriStrings) {
      val sentAt = Instant.parse("2026-11-12T10:00:00Z")
      val photo = CbmChatMessage.FailedToBeSent.ChatMessagePhoto(uuid4().toString(), sentAt, Uri.parse(uriString))
      val media = CbmChatMessage.FailedToBeSent.ChatMessageMedia(uuid4().toString(), sentAt, Uri.parse(uriString))

      assertThat(photo.toChatMessageEntity(conversationId).url).isEqualTo(uriString)
      assertThat(photo.toChatMessageEntity(conversationId).toChatMessage()).isEqualTo(photo)
      assertThat(media.toChatMessageEntity(conversationId).url).isEqualTo(uriString)
      assertThat(media.toChatMessageEntity(conversationId).toChatMessage()).isEqualTo(media)
    }
  }

  @Test
  fun `the path of a camera photo's file URI is the absolute path of the photo`() {
    val uri = Uri.parse(
      "file:///storage/emulated/0/Android/data/com.hedvig.app/files/Pictures/JPEG_1731400000000.jpg",
    )

    assertThat(uri.path).isEqualTo(
      "/storage/emulated/0/Android/data/com.hedvig.app/files/Pictures/JPEG_1731400000000.jpg",
    )
  }
}
