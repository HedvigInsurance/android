package com.hedvig.android.feature.claim.details.ui

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.fileupload.UploadSuccess
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AddFilesPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val targetUploadUrl = "https://example.com/upload?claimId=123"

  private fun createPresenter(
    uploadFiles: suspend (url: String, uriStrings: List<String>) -> Either<ErrorMessage, UploadSuccess> = { _, _ ->
      UploadSuccess(listOf("file-id-1")).right()
    },
    getMimeType: (String) -> String? = { "image/jpeg" },
    getFileName: (String) -> String? = { "test-file.jpg" },
    clearCache: suspend () -> Unit = {},
    initialFilesUri: List<String> = emptyList(),
  ) = AddFilesPresenter(
    uploadFiles = uploadFiles,
    getMimeType = getMimeType,
    getFileName = getFileName,
    targetUploadUrl = targetUploadUrl,
    clearCache = clearCache,
    initialFilesUri = initialFilesUri,
  )

  @Test
  fun `initial state has empty local files`() = runTest {
    val presenter = createPresenter()

    presenter.test(FileUploadUiState()) {
      val initialState = awaitItem()
      assertThat(initialState.localFiles).isEmpty()
      assertThat(initialState.isLoading).isFalse()
      assertThat(initialState.errorMessage).isNull()
      assertThat(initialState.uploadedFileIds).isEmpty()
    }
  }

  @Test
  fun `initial files are loaded on startup`() = runTest {
    val initialUri = "content://test/file1"
    val presenter = createPresenter(
      initialFilesUri = listOf(initialUri),
      getFileName = { "initial-file.jpg" },
      getMimeType = { "image/jpeg" },
    )

    presenter.test(FileUploadUiState()) {
      // First emission might be before LaunchedEffect completes, skip to final state
      skipItems(1)
      val state = awaitItem()
      assertThat(state.localFiles).containsExactly(
        UiFile(
          name = "initial-file.jpg",
          localPath = initialUri,
          mimeType = "image/jpeg",
          id = initialUri,
          url = null,
        ),
      )
    }
  }

  @Test
  fun `adding a local file appends to the list`() = runTest {
    val presenter = createPresenter()
    val testUri = "content://test/new-file"

    presenter.test(FileUploadUiState()) {
      awaitItem()

      sendEvent(AddFilesEvent.AddLocalFile(testUri))

      val state = awaitItem()
      assertThat(state.localFiles.size).isEqualTo(1)
      assertThat(state.localFiles[0].id).isEqualTo(testUri)
      assertThat(state.localFiles[0].name).isEqualTo("test-file.jpg")
      assertThat(state.localFiles[0].mimeType).isEqualTo("image/jpeg")
    }
  }

  @Test
  fun `adding duplicate file is ignored`() = runTest {
    val presenter = createPresenter()
    val testUri = "content://test/file"

    presenter.test(FileUploadUiState()) {
      awaitItem()

      sendEvent(AddFilesEvent.AddLocalFile(testUri))
      val stateWithOneFile = awaitItem()
      assertThat(stateWithOneFile.localFiles.size).isEqualTo(1)

      sendEvent(AddFilesEvent.AddLocalFile(testUri))
      // Should not emit a new state since duplicate was ignored
      expectNoEvents()
    }
  }

  @Test
  fun `adding file with exception shows error`() = runTest {
    val presenter = createPresenter(
      getMimeType = { throw RuntimeException("Failed to get mime type") },
    )
    val testUri = "content://test/file"

    presenter.test(FileUploadUiState()) {
      awaitItem()

      sendEvent(AddFilesEvent.AddLocalFile(testUri))

      val state = awaitItem()
      assertThat(state.errorMessage).isEqualTo("Failed to get mime type")
      assertThat(state.localFiles).isEmpty()
    }
  }

  @Test
  fun `removing a file removes it from the list`() = runTest {
    val existingFile = UiFile(
      name = "existing.jpg",
      localPath = "content://test/existing",
      mimeType = "image/jpeg",
      id = "file-1",
      url = null,
    )
    val presenter = createPresenter()
    val initialState = FileUploadUiState(localFiles = listOf(existingFile))

    presenter.test(initialState) {
      awaitItem()

      sendEvent(AddFilesEvent.RemoveFile("file-1"))

      val state = awaitItem()
      assertThat(state.localFiles).isEmpty()
    }
  }

  @Test
  fun `removing non-existent file keeps list unchanged`() = runTest {
    val existingFile = UiFile(
      name = "existing.jpg",
      localPath = "content://test/existing",
      mimeType = "image/jpeg",
      id = "file-1",
      url = null,
    )
    val presenter = createPresenter()
    val initialState = FileUploadUiState(localFiles = listOf(existingFile))

    presenter.test(initialState) {
      val initial = awaitItem()
      assertThat(initial.localFiles.size).isEqualTo(1)

      sendEvent(AddFilesEvent.RemoveFile("non-existent-id"))

      // filterNot on non-existent id produces same list, but state is still emitted
      // Actually the state emission happens with same value, which awaitItem() filters
      expectNoEvents()
    }
  }

  @Test
  fun `upload files successfully`() = runTest {
    var cacheClearCalled = false
    val presenter = createPresenter(
      uploadFiles = { _, _ -> UploadSuccess(listOf("uploaded-id-1", "uploaded-id-2")).right() },
      clearCache = { cacheClearCalled = true },
    )
    val existingFile = UiFile(
      name = "file.jpg",
      localPath = "content://test/file",
      mimeType = "image/jpeg",
      id = "file-1",
      url = null,
    )
    val initialState = FileUploadUiState(localFiles = listOf(existingFile))

    presenter.test(initialState) {
      awaitItem()

      sendEvent(AddFilesEvent.UploadFiles)

      // Skip to final state - loading and success might be emitted quickly
      val finalState = awaitItem()
      // The final state should have the uploaded file ids
      assertThat(finalState.uploadedFileIds).containsExactly("uploaded-id-1", "uploaded-id-2")
      assertThat(finalState.isLoading).isFalse()
      assertThat(cacheClearCalled).isTrue()
    }
  }

  @Test
  fun `upload files with error shows error message`() = runTest {
    val presenter = createPresenter(
      uploadFiles = { _, _ -> ErrorMessage("Upload failed").left() },
    )
    val existingFile = UiFile(
      name = "file.jpg",
      localPath = "content://test/file",
      mimeType = "image/jpeg",
      id = "file-1",
      url = null,
    )
    val initialState = FileUploadUiState(localFiles = listOf(existingFile))

    presenter.test(initialState) {
      awaitItem()

      sendEvent(AddFilesEvent.UploadFiles)

      // Skip to final state
      val errorState = awaitItem()
      assertThat(errorState.isLoading).isFalse()
      assertThat(errorState.errorMessage).isEqualTo("Upload failed")
      assertThat(errorState.uploadedFileIds).isEmpty()
    }
  }

  @Test
  fun `dismiss error clears error message`() = runTest {
    val presenter = createPresenter()
    val initialState = FileUploadUiState(errorMessage = "Some error")

    presenter.test(initialState) {
      awaitItem()

      sendEvent(AddFilesEvent.DismissError)

      val state = awaitItem()
      assertThat(state.errorMessage).isNull()
    }
  }

  @Test
  fun `upload while loading is ignored`() = runTest {
    val presenter = createPresenter()
    val initialState = FileUploadUiState(isLoading = true)

    presenter.test(initialState) {
      awaitItem()

      sendEvent(AddFilesEvent.UploadFiles)

      // Should not emit new states since already loading
      expectNoEvents()
    }
  }

  @Test
  fun `state is preserved on back navigation`() = runTest {
    val existingFile = UiFile(
      name = "preserved.jpg",
      localPath = "content://test/preserved",
      mimeType = "image/jpeg",
      id = "preserved-id",
      url = null,
    )
    val preservedState = FileUploadUiState(
      localFiles = listOf(existingFile),
      isLoading = false,
    )
    val presenter = createPresenter(initialFilesUri = emptyList())

    presenter.test(preservedState) {
      val state = awaitItem()
      // Should preserve the existing files without clearing them
      assertThat(state.localFiles.size).isEqualTo(1)
      assertThat(state.localFiles[0].id).isEqualTo("preserved-id")
    }
  }

  @Test
  fun `file without name uses uri as name`() = runTest {
    val presenter = createPresenter(
      getFileName = { null },
    )
    val testUri = "content://test/unnamed-file"

    presenter.test(FileUploadUiState()) {
      awaitItem()

      sendEvent(AddFilesEvent.AddLocalFile(testUri))

      val state = awaitItem()
      assertThat(state.localFiles[0].name).isEqualTo("content://test/unnamed-file")
    }
  }

  @Test
  fun `multiple files can be added sequentially`() = runTest {
    val presenter = createPresenter()

    presenter.test(FileUploadUiState()) {
      awaitItem()

      sendEvent(AddFilesEvent.AddLocalFile("content://test/file1"))
      val stateWithOne = awaitItem()
      assertThat(stateWithOne.localFiles.size).isEqualTo(1)

      sendEvent(AddFilesEvent.AddLocalFile("content://test/file2"))
      val stateWithTwo = awaitItem()
      assertThat(stateWithTwo.localFiles.size).isEqualTo(2)

      sendEvent(AddFilesEvent.AddLocalFile("content://test/file3"))
      val stateWithThree = awaitItem()
      assertThat(stateWithThree.localFiles.size).isEqualTo(3)
    }
  }
}
