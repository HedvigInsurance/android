package com.hedvig.android.feature.help.center.question

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
internal fun HelpCenterQuestionDestination(questionId: String, onNavigateToQuestion: (questionId: String) -> Unit) {
  Text("HelpCenterDestinations.Question:$questionId")
}
