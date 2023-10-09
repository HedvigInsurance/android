package com.hedvig.android.feature.chat.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.hedvig.android.core.designsystem.material3.infoContainer
import com.hedvig.android.feature.chat.data.ChatMessage
import java.time.format.DateTimeFormatter
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime

@Composable
fun ChatMessage.getColor() = when (sender) {
  ChatMessage.Sender.HEDVIG -> MaterialTheme.colorScheme.surface
  ChatMessage.Sender.MEMBER -> MaterialTheme.colorScheme.infoContainer
}

@Composable
fun ChatMessage.getMessageAlignment() = when (sender) {
  ChatMessage.Sender.HEDVIG -> Alignment.Start
  ChatMessage.Sender.MEMBER -> Alignment.End
}

fun ChatMessage.formattedDateTime(): String {
  val dateTime = sentAt.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
  val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
  val formatter = if (dateTime.toLocalDate().isEqual(today.toLocalDate())) {
    DateTimeFormatter.ofPattern("HH:mm:ss")
  } else {
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  }
  return formatter.format(dateTime)
}
