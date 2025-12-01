package com.hedvig.feature.claim.com.hedvig.feature.claim.chat.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.ui.claimflow.HedvigChip
import com.hedvig.feature.claim.chat.ClaimChatEvent
import com.hedvig.feature.claim.chat.ClaimChatUiState
import com.hedvig.feature.claim.chat.ClaimChatViewModel
import com.hedvig.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.ui.BlurredGradientBackground
import com.hedvig.feature.claim.chat.ui.ContentSelectChips
import com.hedvig.feature.claim.chat.ui.rememberFilePicker
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ClaimChatDestination(developmentFlow: Boolean = true, messageId: String? = null) {
  val claimChatViewModel = koinViewModel<ClaimChatViewModel> {
    parametersOf(messageId, developmentFlow)
  }
  Box(Modifier.fillMaxSize(), propagateMinConstraints = true) {
    BlurredGradientBackground(radius = 100)
    ClaimChatScreen(claimChatViewModel)
  }
}

@Composable
internal fun ClaimChatScreen(claimChatViewModel: ClaimChatViewModel) {
  val uiState = claimChatViewModel.uiState.collectAsState().value

  Box(Modifier.fillMaxSize(), Alignment.Center) {
    when (uiState) {
      ClaimChatUiState.FailedToStart -> BasicText("FailedToStart") //todo
      ClaimChatUiState.Initializing -> BasicText("Initializing") //todo
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
    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
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

        is StepContent.ContentSelect -> ContentSelectStep(
          item = item,
          currentStep = uiState.currentStep,
          options = item.stepContent.options,
          selectedOptionId = item.stepContent.selectedOptionId,
          onOptionClick = { option ->
            onEvent(
              ClaimChatEvent.Select(
                item.id,
                option.id,
              ),
            )
          },
          modifier = Modifier.padding(horizontal = 16.dp),
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

@Composable
private fun ContentSelectStep(
  item: ClaimIntentStep,
  currentStep: ClaimIntentStep?,
  options: List<StepContent.ContentSelect.Option>,
  selectedOptionId: String?,
  onOptionClick: (StepContent.ContentSelect.Option) -> Unit,
  modifier: Modifier,
) {
  Column(modifier) {
    AnimatedContent(
      item == currentStep,
      transitionSpec = {
        (fadeIn(animationSpec = tween(220, delayMillis = 90))
          .togetherWith(fadeOut(animationSpec = tween(90))))
      },
    ) { targetState ->
      Column {
        HedvigText(
          item.text,
        )
        if (targetState) {
          Spacer(Modifier.height(8.dp))
          ContentSelectChips(
            options = options,
            selectedOption = null,
            onOptionClick = onOptionClick,
          )
        }

      }

    }
    val selected = options.firstOrNull { it.id == selectedOptionId }
    if (
      selected != null
    ) {
      Column {
        Spacer(Modifier.height(8.dp))
        Row(
          horizontalArrangement = Arrangement.End,
          modifier = Modifier
            .fillMaxWidth(),
        ) {
          val showChipAnimatable = remember {
            Animatable(0.0f)
          }
          LaunchedEffect(Unit) {
            showChipAnimatable.animateTo(
              1.0f,
              animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow,
              ),
            )
          }
          HedvigChip(
            item = item,
            showChipAnimatable = showChipAnimatable,
            itemDisplayName = {
              selected.title
            },
            isSelected = false, //should be grey according to figma
            onItemClick = {},
          )
        }
      }

    }
  }


}
