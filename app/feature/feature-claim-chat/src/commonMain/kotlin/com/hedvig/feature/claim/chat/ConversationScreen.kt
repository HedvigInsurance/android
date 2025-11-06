package com.hedvig.feature.claim.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.feature.claim.chat.assistantmessage.AssistantChatMessage
import com.hedvig.feature.claim.chat.assistantmessage.AssistantLoadingState
import com.hedvig.feature.claim.chat.assistantmessage.UserChatBubble
import com.hedvig.feature.claim.chat.audiorecorder.AudioRecorder
import com.hedvig.feature.claim.chat.formmessage.FormBinary
import com.hedvig.feature.claim.chat.formmessage.FormDate
import com.hedvig.feature.claim.chat.formmessage.FormNumber
import com.hedvig.feature.claim.chat.formmessage.FormSingleSelect
import com.hedvig.feature.claim.chat.formmessage.FormText

@Composable
fun ConversationScreen(
  state: ConversationUiState,
  onAction: (UserAction) -> Unit,
) {

  Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      LazyColumn(
        modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(state.conversation) { item ->
          when (item) {
            is ConversationItem.AudioRecording ->
              AudioRecorder(
                uiState = item.uiState,
                startRecording = {
                  onAction(UserAction.TextSubmitted("I dropped my AirPods in the pool, they're completely ruined."))
                },
                stopRecording = {},
                submitAudioFile = {},
                submitAudioUrl = {},
                redo = {},
              )

            is ConversationItem.UserMessage ->
              UserChatBubble(item.text)

            is ConversationItem.AssistantMessage ->
              AssistantChatMessage(item.text, item.subText)

            is ConversationItem.AssistantLoadingState ->
              AssistantLoadingState(item.text, item.subText, item.isLoading)

            is ConversationItem.Form -> item.formFieldList.forEach { formField ->
              when (formField.type) {
                FormFieldType.TEXT -> FormText(
                  value = formField.currentValue,
                  id = formField.id,
                  title = formField.title,
                  defaultValue = formField.defaultValue,
                  onValueChange = { selectedValue ->
                    onAction(UserAction.FormSubmitted(selectedValue))
                  },
                )

                FormFieldType.DATE -> FormDate(
                  value = formField.currentValue,
                  id = formField.id,
                  title = formField.title,
                  onDateSelected = { selectedValue ->
                    onAction(UserAction.FormSubmitted(selectedValue.toString()))
                  },
                  showDatePicker = {},
                )

                FormFieldType.NUMBER -> FormNumber(
                  value = formField.currentValue,
                  id = formField.id,
                  title = formField.title,
                  defaultValue = formField.defaultValue,
                  onValueChange = { selectedValue ->
                    onAction(UserAction.FormSubmitted(selectedValue))
                  },
                  suffix = formField.suffix,
                  minValue = formField.minValue,
                  maxValue = formField.maxValue,
                )

                FormFieldType.SINGLE_SELECT -> FormSingleSelect(
                  value = formField.currentValue,
                  id = formField.id,
                  title = formField.title,
                  defaultValue = formField.defaultValue,
                  options = formField.options,
                  onOptionSelected = { selectedValue ->
                    onAction(UserAction.FormSubmitted(selectedValue))
                  },
                )

                FormFieldType.BINARY -> FormBinary(
                  value = formField.currentValue,
                  id = formField.id,
                  title = formField.title,
                  defaultValue = formField.defaultValue,
                  options = formField.options,
                  onOptionSelected = { selectedValue ->
                    selectedValue?.let {
                      onAction(UserAction.FormSubmitted(it))
                    }
                  },
                )
              }
            }
          }
        }
      }
    }
  }
}
