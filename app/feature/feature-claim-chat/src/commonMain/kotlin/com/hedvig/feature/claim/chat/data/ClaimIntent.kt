package com.hedvig.feature.claim.chat.data

import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.design.system.hedvig.DatePickerUiState
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@JvmInline
value class ClaimIntentId(val value: String)

internal data class ClaimIntent(
  val id: ClaimIntentId,
  val next: Next,
  val progress: Float?,
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
  val text: String?,
  val stepContent: StepContent,
  val isRegrettable: Boolean,
  val hint: String?,
)

@Serializable
internal sealed interface ClaimIntentOutcome {
  @Serializable
  data class Claim(val claimId: String, val claimSubmissionDate: Instant) : ClaimIntentOutcome
}

@JvmInline
value class FieldId(val value: String)

internal sealed interface StepContent {
  val isSkippable: Boolean

  data class AudioRecording(
    val uploadUri: String,
    override val isSkippable: Boolean,
    val recordingState: AudioRecordingStepState,
    val freeTextMinLength: Int,
    val freeTextMaxLength: Int,
  ) : StepContent

  data class FileUpload(
    val uploadUri: String,
    override val isSkippable: Boolean,
    val localFiles: List<UiFile>,
  ) : StepContent

  data class Task(
    val descriptions: List<String>,
    val isCompleted: Boolean,
    val failedToSubmit: Boolean,
  ) : StepContent {
    override val isSkippable: Boolean = false
  }

  data class Form(
    val fields: List<Field>,
    override val isSkippable: Boolean,
  ) : StepContent {
    data class Field(
      val id: FieldId,
      val isRequired: Boolean,
      val suffix: String?,
      val title: String,
      val defaultValues: List<FieldOption>,
      val maxValue: String?,
      val minValue: String?,
      val type: FieldType?,
      val options: List<FieldOption>,
      val selectedOptions: List<FieldOption>,
      val datePickerUiState: DatePickerUiState?,

      val searchData: SearchData?,
      val hasError: FieldError? = null,
      val foundOptionsInSearch: List<FieldOption> = emptyList(),
      val suggestedFixedQuery: String? = null,
    )

    data class SearchData(
      val suggestedQuery: String?,
      val modalTitle: String,
      val modalSubtitle: String
    )

    sealed interface FieldError {
      data object BiggerThanMaxValue : FieldError

      data object LessThanMinValue : FieldError

      data object Missing : FieldError
    }

    data class FieldOption(
      val value: String,
      val text: String,
      val subtitle: String?,
      val imageUrl: String? = null
    )

    enum class FieldType {
      TEXT,
      DATE,
      NUMBER,
      SINGLE_SELECT,
      MULTI_SELECT,
      BINARY,

      SEARCH
    }
  }

  data class ContentSelect(
    val options: List<Option>,
    val selectedOptionId: String?,
    val style: ContentSelectStyle,
    override val isSkippable: Boolean,
  ) : StepContent {
    data class Option(
      val id: String,
      val title: String,
    )
  }

  enum class ContentSelectStyle {
    PILL,
    BINARY,
  }

  data class Summary(
    val items: List<Item>,
    val audioRecordings: List<AudioRecording>,
    val fileUploads: List<FileUpload>,
    val freeTexts: List<String>,
  ) : StepContent {
    override val isSkippable: Boolean = false

    data class Item(val title: String, val value: String)

    data class AudioRecording(val url: String)

    data class FileUpload(val url: String, val contentType: String, val fileName: String)
  }

  @Serializable
  data class Deflect(
    val title: String?,
    val infoText: String?,
    val warningText: String?,
    val partnersContainer: DeflectPartnerContainer?,
    val partnersInfo: InfoBlock?,
    val content: InfoBlock,
    val faq: List<InfoBlock>,
    val buttonText: String,
  ) : StepContent {
    override val isSkippable: Boolean = false

    @Serializable
    sealed interface DeflectPartnerContainer {
      @Serializable
      data class ExtendedPartnerContainer(
        val partners: List<ExtendedPartner>,
      ) : DeflectPartnerContainer

      @Serializable
      data class SimplePartnerContainer(
        val partners: List<SimplePartner>,
      ) : DeflectPartnerContainer

      @Serializable
      data class ExtendedPartner(
        val id: String,
        val imageUrl: String?,
        val phoneNumber: String?,
        val title: String?,
        val description: String?,
        val info: String?,
        val url: String?,
        val urlButtonTitle: String?,
      )

      @Serializable
      data class SimplePartner(
        val url: String?,
        val urlButtonTitle: String?,
      )
    }

    @Serializable
    data class InfoBlock(
      val title: String,
      val description: String,
    )
  }

  object Unknown : StepContent {
    override val isSkippable: Boolean = false
  }
}

sealed interface AudioRecordingStepState {
  data class FreeTextDescription(
    val errorType: FreeTextErrorType?,
    val canSubmit: Boolean,
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
      val hasError: Boolean,
    ) : AudioRecording
  }
}

sealed interface FreeTextErrorType {
  data class TooShort(val minLength: Int) : FreeTextErrorType
}
