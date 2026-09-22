package com.hedvig.android.feature.claim.chat.ui.step

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import com.hedvig.android.feature.claim.chat.data.StepContent
import kotlin.test.Test

class AnswersToShowTest {
  @Test
  fun `the backend's own answers are shown as they arrived`() {
    val summary = summary(answers = listOf(textAnswer))

    assertThat(summary.answersToShow(RECORDING_TITLE, FILE_TITLE)).containsExactly(textAnswer)
  }

  @Test
  fun `a recording the answers do not carry becomes an answer so it still has a surface`() {
    val summary = summary(answers = emptyList(), audioRecordings = listOf(recording))

    assertThat(summary.answersToShow(RECORDING_TITLE, FILE_TITLE)).containsExactly(
      StepContent.Summary.Answer(
        title = RECORDING_TITLE,
        value = StepContent.Summary.Answer.Value.Audio(recording.url, null),
      ),
    )
  }

  @Test
  fun `a voice answer stays reachable on a claim that also has answers of its own`() {
    val summary = summary(answers = listOf(textAnswer), audioRecordings = listOf(recording))

    assertThat(summary.answersToShow(RECORDING_TITLE, FILE_TITLE).map { it.title })
      .containsExactly(textAnswer.title, RECORDING_TITLE)
  }

  @Test
  fun `a recording the answers already carry is not shown twice`() {
    val summary = summary(
      answers = listOf(audioAnswer(recording.url)),
      audioRecordings = listOf(recording),
    )

    assertThat(summary.answersToShow(RECORDING_TITLE, FILE_TITLE)).hasSize(1)
  }

  @Test
  fun `a recording is not added when the answers carry audio under a url of their own`() {
    val summary = summary(
      answers = listOf(audioAnswer("https://cdn.example.com/a-different-name.aac")),
      audioRecordings = listOf(recording),
    )

    assertThat(summary.answersToShow(RECORDING_TITLE, FILE_TITLE)).hasSize(1)
  }

  @Test
  fun `a recording the backend lists twice is shown once`() {
    val summary = summary(answers = emptyList(), audioRecordings = listOf(recording, recording))

    assertThat(summary.answersToShow(RECORDING_TITLE, FILE_TITLE)).hasSize(1)
  }

  @Test
  fun `a recording listed twice under different signing is shown once`() {
    val summary = summary(
      answers = emptyList(),
      audioRecordings = listOf(
        StepContent.Summary.AudioRecording("${recording.url}?token=first"),
        StepContent.Summary.AudioRecording("${recording.url}?token=second"),
      ),
    )

    assertThat(summary.answersToShow(RECORDING_TITLE, FILE_TITLE)).hasSize(1)
  }

  @Test
  fun `an upload the backend lists twice is shown once`() {
    val summary = summary(answers = emptyList(), fileUploads = listOf(upload, upload))

    val files = summary.answersToShow(RECORDING_TITLE, FILE_TITLE)
      .single().value as StepContent.Summary.Answer.Value.Files

    assertThat(files.files).hasSize(1)
  }

  @Test
  fun `every unanswered recording gets its own answer`() {
    val second = StepContent.Summary.AudioRecording("https://example.com/second.aac")
    val summary = summary(answers = emptyList(), audioRecordings = listOf(recording, second))

    assertThat(summary.answersToShow(RECORDING_TITLE, FILE_TITLE).map { it.title })
      .containsExactly(RECORDING_TITLE, RECORDING_TITLE)
  }

  @Test
  fun `uploads share one answer, the way a row of files is one answer`() {
    val summary = summary(answers = emptyList(), fileUploads = listOf(upload))

    assertThat(summary.answersToShow(RECORDING_TITLE, FILE_TITLE)).containsExactly(
      StepContent.Summary.Answer(
        title = FILE_TITLE,
        value = StepContent.Summary.Answer.Value.Files(listOf(upload)),
      ),
    )
  }

  @Test
  fun `uploads are not added when the answers carry files under urls of their own`() {
    val summary = summary(
      answers = listOf(
        StepContent.Summary.Answer(
          title = "Receipts",
          value = StepContent.Summary.Answer.Value.Files(
            listOf(upload.copy(url = "https://cdn.example.com/a-different-name.pdf")),
          ),
        ),
      ),
      fileUploads = listOf(upload),
    )

    assertThat(summary.answersToShow(RECORDING_TITLE, FILE_TITLE)).hasSize(1)
  }

  @Test
  fun `recordings come before uploads`() {
    val summary = summary(answers = emptyList(), audioRecordings = listOf(recording), fileUploads = listOf(upload))

    assertThat(summary.answersToShow(RECORDING_TITLE, FILE_TITLE).map { it.title })
      .containsExactly(RECORDING_TITLE, FILE_TITLE)
  }

  @Test
  fun `a summary with nothing to show shows nothing`() {
    assertThat(summary(answers = emptyList()).answersToShow(RECORDING_TITLE, FILE_TITLE)).isEmpty()
  }

  private fun summary(
    answers: List<StepContent.Summary.Answer>,
    audioRecordings: List<StepContent.Summary.AudioRecording> = emptyList(),
    fileUploads: List<StepContent.Summary.FileUpload> = emptyList(),
  ) = StepContent.Summary(
    items = emptyList(),
    audioRecordings = audioRecordings,
    fileUploads = fileUploads,
    keyDetails = emptyList(),
    answers = answers,
  )

  private fun audioAnswer(url: String) = StepContent.Summary.Answer(
    title = "Tell us what happened",
    value = StepContent.Summary.Answer.Value.Audio(url, null),
  )

  private val textAnswer = StepContent.Summary.Answer(
    title = "What happened?",
    value = StepContent.Summary.Answer.Value.Text("I dropped my phone"),
  )

  private val recording = StepContent.Summary.AudioRecording("https://example.com/recording.aac")

  private val upload = StepContent.Summary.FileUpload(
    url = "https://example.com/receipt.pdf",
    contentType = "application/pdf",
    fileName = "receipt.pdf",
  )

  companion object {
    private const val RECORDING_TITLE = "Recording"
    private const val FILE_TITLE = "File"
  }
}
