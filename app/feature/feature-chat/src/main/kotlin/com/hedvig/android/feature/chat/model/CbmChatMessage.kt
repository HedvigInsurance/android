package com.hedvig.android.feature.chat.model

import android.net.Uri
import com.benasher44.uuid.Uuid
import com.hedvig.android.data.chat.database.ChatMessageEntity
import com.hedvig.android.data.chat.database.ChatMessageEntity.FailedToSendType.MEDIA
import com.hedvig.android.data.chat.database.ChatMessageEntity.FailedToSendType.PHOTO
import com.hedvig.android.data.chat.database.ChatMessageEntity.FailedToSendType.TEXT
import com.hedvig.android.data.chat.database.ChatMessageEntityBanner
import com.hedvig.android.feature.chat.CbmChatUiState.Loaded.LatestChatMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlin.time.Instant

internal sealed interface CbmChatMessage {
  val id: String
  val sender: Sender
  val sentAt: Instant
  val banner: Banner?

  data class ChatMessageText(
    override val id: String,
    override val sender: Sender,
    override val sentAt: Instant,
    override val banner: Banner?,
    val text: String,
  ) : CbmChatMessage

  data class ChatMessageGif(
    override val id: String,
    override val sender: Sender,
    override val sentAt: Instant,
    override val banner: Banner?,
    val gifUrl: String,
  ) : CbmChatMessage

  data class ChatMessageFile(
    override val id: String,
    override val sender: Sender,
    override val sentAt: Instant,
    override val banner: Banner?,
    val url: String,
    val mimeType: MimeType,
  ) : CbmChatMessage {
    enum class MimeType {
      IMAGE,
      MP4,
      PDF,
      OTHER,
    }
  }

  /**
   * A message which failed to be sent due to network errors, but should be retryable.
   */
  sealed interface FailedToBeSent : CbmChatMessage {
    /**
     * To render a picture which failed to be sent, we only got the URI representation of it on-device.
     */
    data class ChatMessagePhoto(
      override val id: String,
      override val sentAt: Instant,
      val uri: Uri,
    ) : FailedToBeSent {
      override val banner: Banner? = null
      override val sender: Sender = Sender.MEMBER
    }

    /**
     * To render a picture which failed to be sent, we only got the URI representation of it, which can not be turn into
     * a file. We must read the Uri contents instead.
     */
    data class ChatMessageMedia(
      override val id: String,
      override val sentAt: Instant,
      val uri: Uri,
    ) : FailedToBeSent {
      override val banner: Banner? = null
      override val sender: Sender = Sender.MEMBER
    }

    data class ChatMessageText(
      override val id: String,
      override val sentAt: Instant,
      val text: String,
    ) : FailedToBeSent {
      override val banner: Banner? = null
      override val sender: Sender = Sender.MEMBER
    }
  }

  data class Banner(
    val bannerInformation: DisplayInfo,
    val sheetInformation: DisplayInfo?,
    val style: Style,
  ) {
    sealed interface DisplayInfo {
      val title: String
      val subtitle: String?

      data class Both(
        override val title: String,
        override val subtitle: String,
      ) : DisplayInfo

      data class Title(override val title: String) : DisplayInfo {
        override val subtitle: String? = null
      }

      companion object Companion {
        fun fromTitleAndDescription(title: String?, description: String?): DisplayInfo? {
          return when {
            title == null && description == null -> return null
            title != null && description != null -> Both(
              title = title,
              subtitle = description,
            )
            else -> Title(title = title ?: description!!)
          }
        }
      }
    }

    enum class Style {
      INFO,
      FANCY_INFO,
    }
  }
}

internal fun CbmChatMessage.toChatMessageEntity(conversationId: Uuid): ChatMessageEntity {
  return when (this) {
    is CbmChatMessage.ChatMessageFile -> ChatMessageEntity(
      id = Uuid.fromString(id),
      conversationId = conversationId,
      sender = sender.toSender(),
      sentAt = sentAt,
      text = null,
      gifUrl = null,
      url = url,
      mimeType = mimeType.name,
      failedToSend = null,
      isBeingSent = false,
      banner = banner.toBannerEntity(),
    )

    is CbmChatMessage.ChatMessageGif -> ChatMessageEntity(
      id = Uuid.fromString(id),
      conversationId = conversationId,
      sender = sender.toSender(),
      sentAt = sentAt,
      text = null,
      gifUrl = gifUrl,
      url = null,
      mimeType = null,
      failedToSend = null,
      isBeingSent = false,
      banner = banner.toBannerEntity(),
    )

    is CbmChatMessage.ChatMessageText -> ChatMessageEntity(
      id = Uuid.fromString(id),
      conversationId = conversationId,
      sender = sender.toSender(),
      sentAt = sentAt,
      text = text,
      gifUrl = null,
      url = null,
      mimeType = null,
      failedToSend = null,
      isBeingSent = false,
      banner = banner.toBannerEntity(),
    )

    is CbmChatMessage.FailedToBeSent.ChatMessageText -> ChatMessageEntity(
      id = Uuid.fromString(id),
      conversationId = conversationId,
      sender = sender.toSender(),
      sentAt = sentAt,
      text = text,
      gifUrl = null,
      url = null,
      mimeType = null,
      failedToSend = TEXT,
      isBeingSent = false,
      banner = banner.toBannerEntity(),
    )

    is CbmChatMessage.FailedToBeSent.ChatMessagePhoto -> ChatMessageEntity(
      id = Uuid.fromString(id),
      conversationId = conversationId,
      sender = sender.toSender(),
      sentAt = sentAt,
      text = null,
      gifUrl = null,
      url = uri.toString(),
      mimeType = null,
      failedToSend = PHOTO,
      isBeingSent = false,
      banner = banner.toBannerEntity(),
    )

    is CbmChatMessage.FailedToBeSent.ChatMessageMedia -> ChatMessageEntity(
      id = Uuid.fromString(id),
      conversationId = conversationId,
      sender = sender.toSender(),
      sentAt = sentAt,
      text = null,
      gifUrl = null,
      url = uri.toString(),
      mimeType = null,
      failedToSend = MEDIA,
      isBeingSent = false,
      banner = banner.toBannerEntity(),
    )
  }
}

internal fun ChatMessageEntity.toChatMessage(): CbmChatMessage? {
  val sender = sender.toSender()
  return when {
    failedToSend != null -> {
      when {
        failedToSend == TEXT && text != null -> {
          CbmChatMessage.FailedToBeSent.ChatMessageText(id.toString(), sentAt, text!!.trim())
        }

        failedToSend == PHOTO && url != null -> {
          CbmChatMessage.FailedToBeSent.ChatMessagePhoto(id.toString(), sentAt, Uri.parse(url))
        }

        failedToSend == MEDIA && url != null -> {
          CbmChatMessage.FailedToBeSent.ChatMessageMedia(id.toString(), sentAt, Uri.parse(url))
        }

        else -> {
          logcat(LogPriority.ERROR) {
            "Tried to map a failed to be sent entity to ChatMessage which does not fit any case. Entity:$this"
          }
          null
        }
      }
    }

    text != null -> CbmChatMessage.ChatMessageText(id.toString(), sender, sentAt, banner.toBanner(), text!!.trim())
    gifUrl != null -> CbmChatMessage.ChatMessageGif(id.toString(), sender, sentAt, banner.toBanner(), gifUrl!!)
    url != null && mimeType != null -> {
      val mimeType = CbmChatMessage.ChatMessageFile.MimeType.valueOf(mimeType!!)
      CbmChatMessage.ChatMessageFile(id.toString(), sender, sentAt, banner.toBanner(), url!!, mimeType)
    }

    else -> error("Unknown ChatMessage type. Message entity:$this")
  }
}

internal fun ChatMessageEntity.toLatestChatMessage(): LatestChatMessage {
  return LatestChatMessage(id, this.sender.toSender())
}

private fun CbmChatMessage.Banner?.toBannerEntity(): ChatMessageEntityBanner? {
  if (this == null) return null
  return ChatMessageEntityBanner(
    title = bannerInformation.title,
    subtitle = bannerInformation.subtitle,
    detailsTitle = sheetInformation?.title,
    detailsDescription = sheetInformation?.subtitle,
    style = when (style) {
      CbmChatMessage.Banner.Style.INFO -> ChatMessageEntityBanner.Style.INFO
      CbmChatMessage.Banner.Style.FANCY_INFO -> ChatMessageEntityBanner.Style.FANCY_INFO
    },
  )
}

private fun ChatMessageEntityBanner?.toBanner(): CbmChatMessage.Banner? {
  if (this == null) return null
  return CbmChatMessage.Banner(
    bannerInformation = CbmChatMessage.Banner.DisplayInfo.fromTitleAndDescription(title, subtitle) ?: return null,
    sheetInformation = CbmChatMessage.Banner.DisplayInfo.fromTitleAndDescription(detailsTitle, detailsDescription),
    style = when (style) {
      ChatMessageEntityBanner.Style.INFO -> CbmChatMessage.Banner.Style.INFO
      ChatMessageEntityBanner.Style.FANCY_INFO -> CbmChatMessage.Banner.Style.FANCY_INFO
    },
  )
}
