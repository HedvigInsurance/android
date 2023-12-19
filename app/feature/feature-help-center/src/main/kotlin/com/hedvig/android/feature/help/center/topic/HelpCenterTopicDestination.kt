package com.hedvig.android.feature.help.center.topic

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
internal fun HelpCenterTopicDestination(topicId: String, onNavigateToQuestion: (questionId: String) -> Unit) {
  Text("HelpCenterDestinations.Topic:$topicId")
}
