package com.hedvig.feature.claim.chat.data

data class ClaimIntent(
  val id: String,
  val step: ClaimIntentStep,
)

data class ClaimIntentStep(
  val id: String,
  val text: String,
  val stepContent: StepContent,
)

sealed interface StepContent {
  data class AudioRecording(
    val hint: String?,
    val uploadUri: String,
  ) : StepContent

  data class Task(
    val description: String,
    val isCompleted: Boolean,
  ) : StepContent

  data class Form(
    val fields: List<Field>,
  ) : StepContent {
    data class Field(
      val id: String,
      val isRequired: Boolean,
      val suffix: String?,
      val title: String,
      val defaultValue: String?,
      val maxValue: String?,
      val minValue: String?,
      val type: String?,
      val options: List<Pair<String, String>>,
    )
  }

  data class Summary(
    val items: List<Item>,
  ) : StepContent {
    data class Item(val title: String, val value: String)
  }

  data class Outcome(
    val claimId: String
  ) : StepContent

  object Unknown : StepContent
}
