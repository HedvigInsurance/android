package com.hedvig.feature.claim.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.ui.rememberFilePicker
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ClaimChatDestination() {
  val messageId: String? = null
  val developmentFlow: Boolean = true
  val claimChatViewModel = koinViewModel<ClaimChatViewModel> {
    parametersOf(messageId, developmentFlow)
  }
  ClaimChatScreen(claimChatViewModel)
}

@Composable
internal fun ClaimChatScreen(claimChatViewModel: ClaimChatViewModel) {
  val uiState = claimChatViewModel.uiState.collectAsState().value

  Box(Modifier.fillMaxSize(), Alignment.Center) {
    when (uiState) {
      ClaimChatUiState.FailedToStart -> BasicText("FailedToStart")
      ClaimChatUiState.Initializing -> BasicText("Initializing")
      is ClaimChatUiState.ClaimChat -> ClaimChatScreen(uiState, claimChatViewModel::emit)
    }
  }
}

@Composable
private fun ClaimChatScreen(uiState: ClaimChatUiState.ClaimChat, onEvent: (ClaimChatEvent) -> Unit) {
  val lazyListState = rememberLazyListState()
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    state = lazyListState,
    contentPadding = WindowInsets.safeDrawing.asPaddingValues(),
    // + PaddingValues(bottom = 16.dp)
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    items(
      items = uiState.steps,
      key = { step -> step.id.value },
      contentType = { it.stepContent::class },
    ) { item ->
      when (item.stepContent) {
        is StepContent.AudioRecording -> {
          BasicText(
            "AudioRecording",
            Modifier.clickable {
              onEvent(
                ClaimChatEvent.AudioRecording.TextInput(
                  item.id,
                  """
Earlier this afternoon, I was walking up the steps outside my office building. I had my phone in my hand when I tripped
slightly on the last step. As I tried to catch my balance, the phone slipped out of my grip and fell onto the concrete.
The phone still works perfectly, it turns on, and the internal components are functional. However, the screen is cracked
quite badly, mainly across the top and down one side. The damage is significant and requires repair.
I purchased the phone on June 1st, 2025, and the original cost was 8999 Swedish Crowns (SEK).
              """.trimIndent(),
                ),
              )
            },
          )
        }

        is StepContent.ContentSelect -> BasicText(
          "ContentSelect",
          Modifier.clickable {
            onEvent(ClaimChatEvent.Select(item.id, item.stepContent.options.firstNotNullOf { it.id }))
          },
        )

        is StepContent.FileUpload -> {
          val filePicker = rememberFilePicker { uri ->
            onEvent(
              ClaimChatEvent.FileUpload(
                id = item.id,
                fileUri = uri,
                uploadUri = item.stepContent.uploadUri,
              ),
            )
          }
          BasicText(
            "FileUpload",
            Modifier.clickable {
              filePicker.launch()
            },
          )
        }

        is StepContent.Form -> {
          BasicText(
            "Form",
            Modifier.clickable {
              onEvent(
                ClaimChatEvent.Form(
                  item.id,
                  item.stepContent.fields.associate {
                    it.id to it.defaultValues
                  },
                ),
              )
            },
          )
        }

        is StepContent.Summary -> BasicText("Summary")
        is StepContent.Task -> {
          Column {
            BasicText("Task")
            for (description in item.stepContent.descriptions) {
              BasicText(description)
            }
          }
        }

        StepContent.Unknown -> BasicText("Unknown")
      }
    }
  }

  LaunchedEffect(uiState.steps.size) {
    if (uiState.steps.isNotEmpty()) {
      lazyListState.animateScrollToItem(index = uiState.steps.lastIndex)
    }
  }
}
