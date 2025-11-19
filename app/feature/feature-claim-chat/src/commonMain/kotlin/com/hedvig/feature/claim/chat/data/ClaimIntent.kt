package com.hedvig.feature.claim.chat.data

import kotlin.jvm.JvmInline

@JvmInline
value class ClaimIntentId(val value: String)

internal data class ClaimIntent(
  val id: ClaimIntentId,
  val step: ClaimIntentStep,
)

@JvmInline
value class StepId(val value: String)

internal data class ClaimIntentStep(
  val id: StepId,
  val text: String,
  val stepContent: StepContent,
)

@JvmInline
value class FieldId(val value: String)

internal sealed interface StepContent {
  val isSkippable: Boolean
  val isRegrettable: Boolean

  data class AudioRecording(
    val hint: String?,
    val uploadUri: String,
    override val isSkippable: Boolean,
    override val isRegrettable: Boolean,
  ) : StepContent

  data class FileUpload(
    val uploadUri: String,
    override val isSkippable: Boolean,
    override val isRegrettable: Boolean,
  ) : StepContent

  data class Task(
    val descriptions: List<String>,
    val isCompleted: Boolean,
  ) : StepContent {
    override val isSkippable: Boolean = false
    override val isRegrettable: Boolean = false
  }

  data class Form(
    val fields: List<Field>,
    override val isSkippable: Boolean,
    override val isRegrettable: Boolean,
  ) : StepContent {

    data class Field(
      val id: FieldId,
      val isRequired: Boolean,
      val suffix: String?,
      val title: String,
      val defaultValues: List<String>,
      val maxValue: String?,
      val minValue: String?,
      val type: String?,
      val options: List<Pair<String, String>>,
    )
  }

  data class ContentSelect(
    val options: List<Option>,
    override val isSkippable: Boolean,
    override val isRegrettable: Boolean,
  ) : StepContent {
    data class Option(
      val id: String,
      val title: String,
    )
  }

  data class Summary(
    val items: List<Item>,
    val audioRecordings: List<AudioRecording>,
    val fileUploads: List<FileUpload>,
  ) : StepContent {
    override val isSkippable: Boolean = false
    override val isRegrettable: Boolean = false

    data class Item(val title: String, val value: String)
    data class AudioRecording(val url: String)
    data class FileUpload(val url: String, val contentType: String, val fileName: String)
  }

  object Unknown : StepContent {
    override val isSkippable: Boolean = false
    override val isRegrettable: Boolean = false
  }
}
