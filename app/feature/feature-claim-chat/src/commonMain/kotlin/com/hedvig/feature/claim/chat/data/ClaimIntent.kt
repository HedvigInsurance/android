package com.hedvig.feature.claim.chat.data

import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@JvmInline
value class ClaimIntentId(val value: String)

internal data class ClaimIntent(
  val id: ClaimIntentId,
  val next: Next,
) {
  sealed interface Next {
    val step: Step?
      get() = this as? Step

    data class Step(val claimIntentStep: ClaimIntentStep) : Next

    data class Outcome(val claimIntentOutcome: ClaimIntentOutcome) : Next
  }
}

@JvmInline
value class StepId(val value: String)

internal data class ClaimIntentStep(
  val id: StepId,
  val text: String,
  val stepContent: StepContent,
)

internal sealed interface ClaimIntentOutcome {
  data class Deflect(
    val title: String?,
    val infoText: String?,
    val warningText: String?,
    val partners: List<Partner>,
    val partnersInfo: InfoBlock?,
    val content: InfoBlock,
    val faq: List<InfoBlock>,
  ) : ClaimIntentOutcome {
    data class Partner(
      val id: String,
      val imageUrl: String?,
      val phoneNumber: String?,
      val title: String?,
      val description: String?,
      val info: String?,
      val url: String?,
      val urlButtonTitle: String?,
    )

    data class InfoBlock(
      val title: String,
      val description: String,
    )
  }

  data class Claim(val claimId: String, val claimSubmissionDate: Instant) : ClaimIntentOutcome

  data object Unknown : ClaimIntentOutcome
}

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
    val recordingState: AudioRecordingStepState
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

    fun canContinue(): Boolean {
      return fields.filter { it.isRequired }.all { it.selectedOptions.isNotEmpty() }
    }

    data class Field(
      val id: FieldId,
      val isRequired: Boolean,
      val suffix: String?,
      val title: String,
      val defaultValues: List<String>,
      val maxValue: String?,
      val minValue: String?,
      val type: FieldType?,
      val options: List<Pair<String, String>>,
      val selectedOptions: List<String>,
    )

    enum class FieldType {
      TEXT,
      DATE,
      NUMBER,
      SINGLE_SELECT,
      MULTI_SELECT,
      BINARY,
    }
  }

  data class ContentSelect(
    val options: List<Option>,
    val selectedOptionId: String?, //todo: check
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

sealed interface AudioRecordingStepState {
  data class FreeTextDescription(
    val showOverlay: Boolean,
    val errorType: FreeTextErrorType?,
    val hasError: Boolean = false,
  ) : AudioRecordingStepState

  sealed interface AudioRecording : AudioRecordingStepState {
    data object NotRecording : AudioRecording

    data class Recording(
      val amplitudes: List<Int>,
      val startedAt: Instant,
      val filePath: String,
    ) : AudioRecording

    data class Playback(
      val filePath: String,
      val isPlaying: Boolean,
      val isPrepared: Boolean,
      val amplitudes: List<Int>,
      val isLoading: Boolean,
      val hasError: Boolean,
      val canSubmit: Boolean,
    ) : AudioRecording
  }
}

sealed interface FreeTextErrorType {
  data class TooShort(val minLength: Int) : FreeTextErrorType
}

@Serializable
@JvmInline
value class AudioUrl(val value: String)

@Immutable
@Serializable
data class AudioContent(
  /**
   * The url to be used to play back the audio file
   */
  val signedUrl: AudioUrl,
  /**
   * The url that the backend expects when trying to go to the next step of the flow
   */
  val audioUrl: AudioUrl,
)
