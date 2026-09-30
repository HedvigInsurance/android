package com.hedvig.android.feature.claim.chat

import androidx.compose.runtime.mutableStateListOf
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.android.feature.claim.chat.data.CommonFileId
import com.hedvig.android.feature.claim.chat.data.StepContent
import com.hedvig.android.feature.claim.chat.data.StepId
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class MarkFileAsUploadedTest {
  @BeforeTest
  fun installLogger() = LogcatLogger.install(PrintingLogcatLogger)

  @AfterTest
  fun uninstallLogger() = LogcatLogger.uninstall()

  @Test
  fun `an uploaded file moves to the remote list under its backend id`() {
    val steps = mutableStateListOf(fileUploadStep(localFiles = listOf(localFile("content://a"))))

    steps.markFileAsUploaded(STEP_ID, "content://a", CommonFileId("remote-a"))

    val content = steps.single().stepContent as StepContent.FileUpload
    assertThat(content.localFiles).isEmpty()
    assertThat(content.remoteFiles.map { it.id }).containsExactly("remote-a")
  }

  @Test
  fun `the local path survives the move so the thumbnail keeps rendering`() {
    val steps = mutableStateListOf(fileUploadStep(localFiles = listOf(localFile("content://a"))))

    steps.markFileAsUploaded(STEP_ID, "content://a", CommonFileId("remote-a"))

    val content = steps.single().stepContent as StepContent.FileUpload
    assertThat(content.remoteFiles.single().localPath).isEqualTo("content://a")
  }

  @Test
  fun `the files that have not uploaded yet are left pending`() {
    val steps = mutableStateListOf(
      fileUploadStep(localFiles = listOf(localFile("content://a"), localFile("content://b"))),
    )

    steps.markFileAsUploaded(STEP_ID, "content://a", CommonFileId("remote-a"))

    val content = steps.single().stepContent as StepContent.FileUpload
    assertThat(content.localFiles.map { it.localPath }).containsExactly("content://b")
    assertThat(content.remoteFiles.map { it.id }).containsExactly("remote-a")
  }

  @Test
  fun `files already on the backend are kept alongside the newly uploaded one`() {
    val steps = mutableStateListOf(
      fileUploadStep(
        localFiles = listOf(localFile("content://a")),
        remoteFiles = listOf(localFile("content://earlier").copy(id = "remote-earlier")),
      ),
    )

    steps.markFileAsUploaded(STEP_ID, "content://a", CommonFileId("remote-a"))

    val content = steps.single().stepContent as StepContent.FileUpload
    assertThat(content.remoteFiles.map { it.id }).containsExactly("remote-earlier", "remote-a")
  }

  @Test
  fun `a path that is not pending leaves the step untouched`() {
    val step = fileUploadStep(localFiles = listOf(localFile("content://a")))
    val steps = mutableStateListOf(step)

    steps.markFileAsUploaded(STEP_ID, "content://missing", CommonFileId("remote-x"))

    assertThat(steps.single()).isEqualTo(step)
  }
}

private object PrintingLogcatLogger : LogcatLogger {
  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    println("[${priority.name}] ${message()}")
  }
}

private val STEP_ID = StepId("step")

private fun localFile(path: String) = UiFile(
  name = path.substringAfterLast('/'),
  localPath = path,
  url = null,
  mimeType = "image/jpeg",
  id = path,
)

private fun fileUploadStep(localFiles: List<UiFile> = emptyList(), remoteFiles: List<UiFile> = emptyList()) =
  ClaimIntentStep(
    id = STEP_ID,
    text = null,
    stepContent = StepContent.FileUpload(
      uploadUri = "upload",
      isSkippable = false,
      localFiles = localFiles,
      remoteFiles = remoteFiles,
    ),
    isRegrettable = false,
    hint = null,
  )
