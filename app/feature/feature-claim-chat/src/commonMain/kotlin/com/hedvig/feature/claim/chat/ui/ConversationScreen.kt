package com.hedvig.feature.claim.chat.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.feature.claim.chat.ConversationItem
import com.hedvig.feature.claim.chat.ConversationUiState
import com.hedvig.feature.claim.chat.FormFieldType
import com.hedvig.feature.claim.chat.UserAction
import com.hedvig.feature.claim.chat.assistantmessage.AssistantChatMessage
import com.hedvig.feature.claim.chat.assistantmessage.AssistantLoadingState
import com.hedvig.feature.claim.chat.audiorecorder.AudioPrompt
import com.hedvig.feature.claim.chat.formmessage.Form
import com.hedvig.feature.claim.chat.formmessage.FormBinary
import com.hedvig.feature.claim.chat.formmessage.FormDate
import com.hedvig.feature.claim.chat.formmessage.FormNumber
import com.hedvig.feature.claim.chat.formmessage.FormSingleSelect
import com.hedvig.feature.claim.chat.formmessage.FormText
import com.hedvig.feature.claim.chat.summary.Summary
import kotlinx.coroutines.launch

@Composable
internal fun ConversationScreen(
  state: ConversationUiState,
  onAction: (UserAction) -> Unit,
) {
  val lazyListState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      LazyColumn(
        modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
        state = lazyListState,
        contentPadding = PaddingValues(top = 64.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(state.conversation) { item ->
          when (item) {
            is ConversationItem.AudioRecording -> AudioPrompt(
              item.text,
              item.hint,
              item.uploadUri,
              onStartRecording = { onAction(UserAction.AudioRecordingSubmitted(item.uploadUri)) },
            )

            is ConversationItem.AssistantMessage -> AssistantChatMessage(item.text, item.subText)
            is ConversationItem.AssistantLoadingState -> AssistantLoadingState(item.text, item.subText, item.isLoading)
            is ConversationItem.Form -> Form(item) { onAction(UserAction.FormSubmitted(item.formFieldList)) }
            is ConversationItem.Summary -> Summary(item, onSubmit = { onAction(UserAction.SummarySubmitted) })
            is ConversationItem.Outcome -> AssistantChatMessage(text = item.text, subText = item.claimId)
          }
        }
      }
    }

    LaunchedEffect(state.conversation.size) {
      if (state.conversation.isNotEmpty()) {
        coroutineScope.launch {
          lazyListState.animateScrollToItem(index = state.conversation.lastIndex)
        }
      }
    }

    state.errorMessage?.let { message ->
      ErrorDialog(
        message = message,
        onDismiss = {
          onAction(UserAction.ErrorAcknowledged)
        },
      )
    }
  }
}

@Composable
internal fun FormViews(item: ConversationItem.Form) {
  item.formFieldList.forEach { formField ->
    when (formField.type) {
      FormFieldType.TEXT -> FormText(
        value = formField.currentValue,
        id = formField.fieldId,
        title = formField.title,
        defaultValue = formField.defaultValue,
        onValueChange = { selectedValue ->

        },
      )

      FormFieldType.DATE -> FormDate(
        value = formField.currentValue,
        id = formField.fieldId,
        title = formField.title,
        onDateSelected = { selectedValue ->

        },
        showDatePicker = {},
      )

      FormFieldType.NUMBER -> FormNumber(
        value = formField.currentValue,
        id = formField.fieldId,
        title = formField.title,
        defaultValue = formField.defaultValue,
        onValueChange = { selectedValue ->

        },
        suffix = formField.suffix,
        minValue = formField.minValue,
        maxValue = formField.maxValue,
      )

      FormFieldType.SINGLE_SELECT -> FormSingleSelect(
        value = formField.currentValue,
        id = formField.fieldId,
        title = formField.title,
        defaultValue = formField.defaultValue,
        options = formField.options.map { it.second },
        onOptionSelected = { selectedValue ->

        },
      )

      FormFieldType.BINARY -> FormBinary(
        value = formField.currentValue,
        id = formField.fieldId,
        title = formField.title,
        defaultValue = formField.defaultValue,
        options = formField.options.map { it.second },
        onOptionSelected = { selectedValue ->
          selectedValue?.let {

          }
        },
      )
    }
  }
}
