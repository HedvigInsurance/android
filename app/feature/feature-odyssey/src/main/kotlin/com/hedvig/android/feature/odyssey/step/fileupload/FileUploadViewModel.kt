package com.hedvig.android.feature.odyssey.step.fileupload

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.either
import com.hedvig.android.core.fileupload.UploadFileUseCase
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.Upload
import com.hedvig.android.navigation.compose.typed.SerializableImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class FileUploadViewModel(
  private val claimFlowRepository: ClaimFlowRepository,
  private val uploadFileUseCase: UploadFileUseCase,
  private val targetUploadUrl: String,
  private val uploads: SerializableImmutableList<Upload>,
  private val title: String,
) : ViewModel() {
  private val _uiState = MutableStateFlow(FileUploadUiState())
  val uiState: StateFlow<FileUploadUiState> = _uiState.asStateFlow()

  fun onContinue() {
    val files = uiState.value.files
    if (files.isNullOrEmpty()) {
      continueWithoutFiles()
    } else {
      uploadFiles(emptyList()) // TODO
    }
  }

  private fun uploadFiles(fileIds: List<Uri>) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      either {
        val result = uploadFileUseCase.invoke(url = targetUploadUrl, uris = fileIds).bind()
        val nextStep = claimFlowRepository.submitFiles(result.fileIds).bind()
        _uiState.update { it.copy(nextStep = nextStep, isLoading = false) }
      }.onLeft {
        _uiState.update { it.copy(errorMessage = it.errorMessage, isLoading = false) }
      }
    }
  }

  private fun continueWithoutFiles() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      either {
        val nextStep = claimFlowRepository.submitFiles(emptyList()).bind()
        _uiState.update { it.copy(nextStep = nextStep, isLoading = false) }
      }.onLeft {
        _uiState.update { it.copy(errorMessage = it.errorMessage, isLoading = false) }
      }
    }
  }

  fun handledNextStepNavigation() {
    _uiState.update { it.copy(nextStep = null) }
  }
}

internal data class FileUploadUiState(
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val files: List<Upload>? = null,
  val nextStep: ClaimFlowStep? = null,
)
