package com.hedvig.android.feature.chat.model

import kotlinx.datetime.LocalDateTime

internal sealed interface Conversation {
  val conversationId: String
  val lastMessageForPreview: String
  val hasNewMessages: Boolean
  val lastUpdatedTime: LocalDateTime // to order by

  data class ServiceConversation(
    override val conversationId: String,
    override val lastMessageForPreview: String,
    override val hasNewMessages: Boolean,
    override val lastUpdatedTime: LocalDateTime,
    val createdAt: LocalDateTime,
    val title: String?,
    // for this type, title will be (for now, as we're not getting anything from BE,
    // but maybe we will in the future) a placeholder - design still in process for this point
    // and label will be always "Question"
  ) : Conversation

  data class ClaimConversation(
    override val conversationId: String,
    override val lastMessageForPreview: String,
    override val hasNewMessages: Boolean,
    override val lastUpdatedTime: LocalDateTime,
    val createdAt: LocalDateTime,
    val title: String?,
    // claimType. if null, will show placeholder: stringResource(R.string.claim_casetype_insurance_case)
    val label: String,
    // "umbrella" type of insurance, similar to CrossSellTypes: "Pet", "Home" etc.
  ) : Conversation

  data class LegacyConversation(
    override val conversationId: String,
    override val lastMessageForPreview: String,
    override val hasNewMessages: Boolean,
    override val lastUpdatedTime: LocalDateTime,
    val closedAt: LocalDateTime?,
  ) : Conversation {
    val isClosed: Boolean = closedAt != null && !hasNewMessages
    // ui would depend on whether it's closed/new messages, otherwise will be in expanded state
  }
}

internal fun isLegacy(createdAt: LocalDateTime): Boolean = createdAt < legacyCheckPoint

// todo: change the date here (or remove if legacy is otherwise labeled). remove other comments above
internal val legacyCheckPoint = LocalDateTime(
  year = 2024,
  monthNumber = 4,
  dayOfMonth = 1,
  hour = 0,
  minute = 0,
)
