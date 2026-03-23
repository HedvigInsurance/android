package com.hedvig.feature.claim.chat.ui.step

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.hedvig.android.design.system.hedvig.BottomSheetStyle
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.DatePickerUiState
import com.hedvig.android.design.system.hedvig.DatePickerWithDialog
import com.hedvig.android.design.system.hedvig.HedvigBigCard
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.MultiSelectDialog
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.SearchField
import com.hedvig.android.design.system.hedvig.SingleSelectDialog
import com.hedvig.android.design.system.hedvig.hedvigDropShadow
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.placeholder.crossSellPainterFallback
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.feature.claim.chat.ClaimChatEvent
import com.hedvig.feature.claim.chat.data.FieldId
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.data.StepContent.Form.Field
import com.hedvig.feature.claim.chat.data.StepContent.Form.FieldError
import com.hedvig.feature.claim.chat.data.StepContent.Form.FieldOption
import com.hedvig.feature.claim.chat.data.StepContent.Form.FieldType
import com.hedvig.feature.claim.chat.data.StepId
import com.hedvig.feature.claim.chat.ui.common.EditButton
import com.hedvig.feature.claim.chat.ui.common.RoundCornersPill
import com.hedvig.feature.claim.chat.ui.common.SkippedLabel
import com.hedvig.feature.claim.chat.ui.common.YesNoBubble
import hedvig.resources.CLAIM_CHAT_FIELD_SEARCH_EMPTY_STATE_SUBTITLE
import hedvig.resources.CLAIM_CHAT_FIELD_SEARCH_EMPTY_STATE_TITLE
import hedvig.resources.CLAIM_CHAT_FIELD_SEARCH_NOTHING_FOUND
import hedvig.resources.CLAIM_CHAT_FIELD_SEARCH_PLACEHOLDER
import hedvig.resources.CLAIM_CHAT_FIELD_SEARCH_SUGGESTION
import hedvig.resources.CLAIM_CHAT_FIELD_SEARCH_TITLE
import hedvig.resources.CLAIM_CHAT_FORM_NUMBER_MAX_CHAR
import hedvig.resources.CLAIM_CHAT_FORM_NUMBER_MIN_CHAR
import hedvig.resources.CLAIM_CHAT_FORM_REQUIRED_FIELD
import hedvig.resources.GENERAL_REMOVE
import hedvig.resources.Res
import hedvig.resources.claims_skip_button
import hedvig.resources.general_cancel_button
import hedvig.resources.general_continue_button
import hedvig.resources.general_save_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FormStep(
  itemId: StepId,
  content: StepContent.Form,
  onEvent: (ClaimChatEvent) -> Unit,
  isCurrentStep: Boolean,
  canSkip: Boolean,
  canBeChanged: Boolean,
  continueButtonLoading: Boolean,
  skipButtonLoading: Boolean,
  firstFieldWithError: Field?,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  FormContent(
    content = content,
    onSkip = {
      onEvent(ClaimChatEvent.Skip(itemId))
    },
    isCurrentStep = isCurrentStep,
    canSkip = canSkip,
    canBeChanged = canBeChanged,
    onRegret = {
      onEvent(ClaimChatEvent.ShowConfirmEditDialog(itemId))
    },
    onSelectFieldAnswer = { fieldId, answer ->
      onEvent(ClaimChatEvent.UpdateFieldAnswer(itemId, fieldId, answer))
    },
    onSubmit = {
      onEvent(ClaimChatEvent.SubmitForm(itemId))
    },
    continueButtonLoading = continueButtonLoading,
    skipButtonLoading = skipButtonLoading,
    firstFieldWithError = firstFieldWithError,
    onSearchQueryChange = { query, fieldId ->
      onEvent(ClaimChatEvent.UpdateFormFieldSearchQuery(query, itemId, fieldId))
    },
    onSearchClear = { fieldId ->
      onEvent(ClaimChatEvent.ClearQuery(itemId, fieldId))
    },
    imageLoader = imageLoader,
    modifier = modifier,
  )
}

@Composable
private fun getErrorText(field: Field): String? {
  return when (field.hasError) {
    FieldError.Missing -> stringResource(Res.string.CLAIM_CHAT_FORM_REQUIRED_FIELD)
    FieldError.LessThanMinValue -> stringResource(Res.string.CLAIM_CHAT_FORM_NUMBER_MIN_CHAR)
    FieldError.BiggerThanMaxValue -> stringResource(Res.string.CLAIM_CHAT_FORM_NUMBER_MAX_CHAR)
    null -> null
  }
}

@Composable
private fun FormContent(
  content: StepContent.Form,
  isCurrentStep: Boolean,
  canSkip: Boolean,
  onSkip: () -> Unit,
  canBeChanged: Boolean,
  onRegret: () -> Unit,
  onSubmit: () -> Unit,
  continueButtonLoading: Boolean,
  skipButtonLoading: Boolean,
  onSelectFieldAnswer: (fieldId: FieldId, answer: FieldOption?) -> Unit,
  firstFieldWithError: Field?,
  onSearchQueryChange: (query: String, fieldId: FieldId) -> Unit,
  onSearchClear: (fieldId: FieldId) -> Unit,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  val errorDescription = firstFieldWithError?.let { "${getErrorText(it)}: ${it.title}" }
  Column(modifier) {
    if (isCurrentStep) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        content.fields.forEach { field ->
          val errorText = getErrorText(field)
          when (field.type) {
            FieldType.TEXT -> {
              TextInputBubble(
                questionLabel = field.title,
                text = field.selectedOptions.getOrNull(0)?.text,
                suffix = field.suffix,
                onInput = { answer ->
                  onSelectFieldAnswer(
                    field.id,
                    answer?.let { FieldOption(it, it, null) },
                  )
                },
                errorText = errorText,
              )
            }

            FieldType.DATE -> {
              LaunchedEffect(field.datePickerUiState?.datePickerState?.selectedDateMillis) {
                onSelectFieldAnswer(
                  field.id,
                  field.datePickerUiState?.datePickerState?.selectedDateMillis?.let {
                    FieldOption(it.toString(), it.toString(), null)
                  },
                )
              }
              DateSelectBubble(
                questionLabel = field.title,
                datePickerState = field.datePickerUiState!!, // todo - check "!!"
                modifier = Modifier.fillMaxWidth(),
                errorText = errorText,
              )
            }

            FieldType.NUMBER -> {
              TextInputBubble(
                questionLabel = field.title,
                text = field.selectedOptions.getOrNull(0)?.text,
                suffix = field.suffix,
                onInput = { answer ->
                  onSelectFieldAnswer(
                    field.id,
                    answer?.let { FieldOption(it, it, null) },
                  )
                },
                keyboardType = KeyboardType.Number,
                errorText = errorText,
              )
            }

            FieldType.SINGLE_SELECT -> {
              SingleSelectBubbleWithDialog(
                questionLabel = field.title,
                options = field.options.map {
                  RadioOption(
                    id = RadioOptionId(it.value),
                    text = it.text,
                    label = it.subtitle,
                    iconResource = null,
                  )
                },
                selectedOptionId = field.selectedOptions.getOrNull(0)
                  ?.let { selected ->
                    val option =
                      field.options.firstOrNull { it.value == selected.value }
                    if (option != null) {
                      RadioOptionId(option.value)
                    } else {
                      null
                    }
                  },
                onSelect = { optionId ->
                  onSelectFieldAnswer(
                    field.id,
                    field.options.firstOrNull { it.value == optionId.id },
                  )
                },
                modifier = Modifier.fillMaxWidth(),
                errorText = errorText,
              )
            }

            FieldType.MULTI_SELECT -> {
              MultiSelectBubbleWithDialog(
                questionLabel = field.title,
                options = field.options.map {
                  RadioOption(
                    id = RadioOptionId(it.value),
                    text = it.text,
                    label = it.subtitle,
                    iconResource = null,
                  )
                },
                selectedOptionIds = field.selectedOptions.mapNotNull { selected ->
                  field.options.firstOrNull { it.value == selected.value }
                    ?.let { RadioOptionId(it.value) }
                },
                onSelect = { option ->
                  onSelectFieldAnswer(
                    field.id,
                    field.options.firstOrNull {
                      it.value == option.id
                    },
                  )
                },
                modifier = Modifier.fillMaxWidth(),
                errorText = errorText,
              )
            }

            FieldType.BINARY -> {
              YesNoBubble(
                answerSelected = field.selectedOptions.firstOrNull()?.text,
                onSelect = {
                  onSelectFieldAnswer(
                    field.id,
                    FieldOption(it, it, null),
                  )
                },
                questionText = field.title,
                errorText = errorText,
              )
            }

            null -> {
              if (canSkip) {
                onSkip()
              }
            }

            FieldType.SEARCH -> {
              SearchForm(
                searchData = field.searchData,
                suggestedFixedQuery = field.suggestedFixedQuery,
                onQueryChange = { query ->
                  onSearchQueryChange(query, field.id)
                },
                queryResult = field.foundOptionsInSearch,
                selectedOption = field.selectedOptions.getOrNull(0),
                onOptionSelected = { option ->
                  onSelectFieldAnswer(field.id, option)
                },
                onClearSearch = {
                  onSearchClear(field.id)
                },
                imageLoader = imageLoader,
                fieldTitle = field.title,
              )
            }
          }
        }
      }
      Spacer(Modifier.height(16.dp))
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HedvigButton(
          text = stringResource(Res.string.general_continue_button),
          enabled = !continueButtonLoading,
          isLoading = continueButtonLoading,
          onClick = onSubmit,
          modifier = Modifier.fillMaxWidth().semantics {
            if (errorDescription != null) {
              contentDescription = errorDescription
            }
          },
        )
        if (canSkip) {
          HedvigButton(
            text = stringResource(Res.string.claims_skip_button),
            enabled = !skipButtonLoading,
            onClick = onSkip,
            isLoading = skipButtonLoading,
            modifier = Modifier.fillMaxWidth(),
            buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
          )
        }
      }
    } else {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (content.fields.flatMap { it.selectedOptions }.isNotEmpty()) {
          content.fields.forEach { field ->
            val initialTextValue = field.selectedOptions.joinToString { it.text }
            val suffix = if (initialTextValue.isNotEmpty() && field.suffix!=null) " ${field.suffix}" else ""
            val textValue = "$initialTextValue$suffix"
            Column(
              Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.End,
            ) {
              if (textValue.isNotEmpty()) {
                RoundCornersPill(
                  onClick = null,
                ) {
                  HedvigText(textValue, textAlign = TextAlign.End)
                }
              } else {
                SkippedLabel()
              }
            }
          }
        } else {
          SkippedLabel()
        }
        EditButton(canBeChanged, onRegret)
      }
    }
  }
}

@Composable
internal fun SearchForm(
  fieldTitle: String,
  queryResult: List<FieldOption>,
  searchData: StepContent.Form.SearchData?,
  suggestedFixedQuery: String?,
  onQueryChange: (String) -> Unit,
  onClearSearch: () -> Unit,
  selectedOption: FieldOption?,
  onOptionSelected: (FieldOption) -> Unit,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  val searchBottomSheetState = rememberHedvigBottomSheetState<String?>()
  val focusManager = LocalFocusManager.current
  Column(modifier) {
    HedvigBigCard(
      onClick = {
        focusManager.clearFocus()
        searchBottomSheetState.show(searchData?.suggestedQuery ?: "")
        searchData?.suggestedQuery?.let { suggestedQuery ->
          onQueryChange(suggestedQuery)
        }
      },
      labelText = fieldTitle,
      inputText = selectedOption?.text,
      modifier = Modifier.fillMaxWidth(),
      enabled = true,
    )
  }

  HedvigBottomSheet(
    modifier = Modifier
      .fillMaxHeight(1f),
    hedvigBottomSheetState = searchBottomSheetState,
    style = BottomSheetStyle(
      transparentBackground = false,
      automaticallyScrollableContent = false,
      scrimColor = null,
    ),
    content = { suggestedQuery: String? ->
      var searchQuery by remember {
        mutableStateOf(suggestedQuery)
      }
      val focusRequester = remember { FocusRequester() }
      val keyboardController = LocalSoftwareKeyboardController.current

      Column(Modifier.fillMaxHeight()) {
        HedvigText(fieldTitle, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        SearchField(
          searchQuery = searchQuery,
          focusRequester = focusRequester,
          onClearSearch = {
            searchQuery = null
            onClearSearch()
          },
          onKeyboardAction = {
            searchQuery?.let {
              focusManager.clearFocus()
            }
          },
          onSearchChange = { query ->
            if (query.isEmpty()) {
              searchQuery = null
              onClearSearch()
            } else {
              searchQuery = query
              onQueryChange(query)
            }
          },
        )
        Spacer(Modifier.height(16.dp))
        val searchState = when (queryResult.isEmpty()) {
          true -> {
            if (searchQuery.isNullOrEmpty()) {
              SearchState.SearchNotStarted
            } else {
              SearchState.NothingFound(
                query = searchQuery,
                suggestedFixedQuery = suggestedFixedQuery,
              )
            }
          }

          false -> {
            SearchState.ResultsFound(searchQuery, queryResult, suggestedFixedQuery)
          }
        }

        AnimatedContent(
          targetState = searchState,
          contentKey = { state ->
            when (state) {
              is SearchState.ResultsFound -> "results"
              is SearchState.NothingFound -> "nothing_found"
              SearchState.SearchNotStarted -> "not_started"
            }
          },
          modifier = Modifier.weight(1f),
        ) { animatedState ->
          Column {
            when (animatedState) {
              is SearchState.NothingFound -> {
                Column(
                  Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center,
                ) {
                  HedvigText(
                    stringResource(Res.string.CLAIM_CHAT_FIELD_SEARCH_NOTHING_FOUND),
                    textAlign = TextAlign.Center,
                  )
                  if (animatedState.suggestedFixedQuery != null) {
                    FixQuerySuggestion(animatedState.suggestedFixedQuery) {
                      onQueryChange(animatedState.suggestedFixedQuery)
                      searchQuery = animatedState.suggestedFixedQuery
                    }
                  }
                }
              }

              SearchState.SearchNotStarted -> {
                Column(
                  Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center,
                ) {
                  if (searchData != null) {
                    HedvigText(searchData.modalTitle, textAlign = TextAlign.Center)
                    HedvigText(
                      searchData.modalSubtitle,
                      textAlign = TextAlign.Center,
                      color = HedvigTheme.colorScheme.textSecondary,
                    )
                  }
                }
              }

              is SearchState.ResultsFound -> {
                val lazyListState = rememberLazyListState()
                LaunchedEffect(lazyListState.isScrollInProgress) {
                  if (lazyListState.isScrollInProgress) {
                    keyboardController?.hide()
                  }
                }
                LazyColumn(
                  modifier = Modifier
                    .fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(6.dp),
                  state = lazyListState,
                ) {
                  item {
                    if (animatedState.suggestedFixedQuery != null) {
                      Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(),
                      ) {
                        FixQuerySuggestion(animatedState.suggestedFixedQuery) {
                          onQueryChange(animatedState.suggestedFixedQuery)
                          searchQuery = animatedState.suggestedFixedQuery
                        }
                      }
                      Spacer(Modifier.height(26.dp))
                    }
                  }
                  items(queryResult) { item ->
                    ItemCard(
                      itemTitle = item.text,
                      itemSubtitle = item.subtitle,
                      onClick = {
                        onOptionSelected(item)
                        searchBottomSheetState.dismiss()
                      },
                      imageLoader = imageLoader,
                      itemImageUrl = item.imageUrl,
                    )
                  }
                  item {
                    Spacer(Modifier.height(8.dp)) // to allow space for shadow
                  }
                }
              }
            }
          }
        }
        Spacer(Modifier.height(16.dp))
        HedvigButton(
          text = stringResource(Res.string.general_cancel_button),
          enabled = true,
          buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
          onClick = {
            searchBottomSheetState.dismiss()
          },
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
      }
    },
  )
}

@Composable
private fun FixQuerySuggestion(suggestedFixedQuery: String, onClick: () -> Unit) {
  val annotatedString = buildAnnotatedString {
    append(stringResource(Res.string.CLAIM_CHAT_FIELD_SEARCH_SUGGESTION))
    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
      append(suggestedFixedQuery)
    }
    append("?")
  }
  HedvigText(
    annotatedString,
    textAlign = TextAlign.Center,
    color = HedvigTheme.colorScheme.textSecondary,
    modifier = Modifier.clickable {
      onClick()
    },
  )
}

private sealed interface SearchState {
  data object SearchNotStarted : SearchState

  data class NothingFound(val query: String?, val suggestedFixedQuery: String?) : SearchState

  data class ResultsFound(
    val query: String?,
    val results: List<FieldOption>,
    val suggestedFixedQuery: String?,
  ) : SearchState
}

@Composable
private fun ItemCard(
  imageLoader: ImageLoader,
  itemTitle: String,
  itemSubtitle: String?,
  itemImageUrl: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    onClick = onClick,
    modifier = modifier
      .fillMaxWidth()
      .hedvigDropShadow(HedvigTheme.shapes.cornerLarge),
    color = HedvigTheme.colorScheme.fillNegative,
    shape = HedvigTheme.shapes.cornerLarge,
    borderColor = HedvigTheme.colorScheme.borderPrimary,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 16.dp),
    ) {
      if (itemImageUrl != null) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(46.dp)
            .background(Color(0xFFFFFFFF), HedvigTheme.shapes.cornerSmall)
            .border(
              1.dp,
              HedvigTheme.colorScheme.borderPrimary,
              HedvigTheme.shapes.cornerSmall,
            )
            .clip(HedvigTheme.shapes.cornerSmall),
        ) {
          AsyncImage(
            model = itemImageUrl,
            contentDescription = itemTitle,
            placeholder = crossSellPainterFallback(),
            error = crossSellPainterFallback(),
            fallback = crossSellPainterFallback(),
            imageLoader = imageLoader,
            contentScale = ContentScale.Fit,
            modifier = Modifier.padding(2.dp),
          )
        }
      }
      Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(start = 16.dp, bottom = 14.dp, top = 12.dp),
      ) {
        HedvigText(
          text = itemTitle,
          textAlign = TextAlign.Start,
        )
        itemSubtitle?.let {
          HedvigText(
            text = itemSubtitle,
            textAlign = TextAlign.Start,
            color = HedvigTheme.colorScheme.textSecondary,
            style = HedvigTheme.typography.finePrint,
          )
        }
      }
    }
  }
}

@Composable
internal fun TextInputBubble(
  questionLabel: String,
  text: String?,
  suffix: String?,
  onInput: (String?) -> Unit,
  modifier: Modifier = Modifier,
  keyboardType: KeyboardType = KeyboardType.Unspecified,
  errorText: String? = null,
) {
  var textValue by rememberSaveable {
    mutableStateOf(
      text ?: "",
    )
  }
  val focusManager = LocalFocusManager.current
  HedvigTextField(
    text = textValue,
    trailingContent = {},
    onValueChange = onValueChange@{ newValue ->
      textValue = newValue
      onInput(newValue.ifBlank { null })
    },
    textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
    labelText = questionLabel,
    modifier = modifier,
    enabled = true,
    errorState = if (errorText != null) {
      HedvigTextFieldDefaults.ErrorState.Error.WithMessage(errorText)
    } else {
      HedvigTextFieldDefaults.ErrorState.NoError
    },
    suffix = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (suffix != null) {
          HedvigText(suffix)
        }
        AnimatedVisibility(textValue.isNotEmpty()) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(16.dp))
            IconButton(
              onClick = {
                onInput("")
                textValue = ""
              },
              Modifier.size(24.dp),
            ) {
              Icon(
                HedvigIcons.Close,
                stringResource(Res.string.GENERAL_REMOVE),
              )
            }
          }
        }
      }
    },
    keyboardOptions = KeyboardOptions(
      autoCorrectEnabled = false,
      imeAction = ImeAction.Done,
      keyboardType = keyboardType,
    ),
    keyboardActions = KeyboardActions(
      onDone = {
        focusManager.clearFocus()
      },
    ),
  )
}

@Composable
internal fun DateSelectBubble(
  datePickerState: DatePickerUiState,
  questionLabel: String?,
  modifier: Modifier = Modifier,
  errorText: String? = null,
) {
  val focusManager = LocalFocusManager.current
  Column(modifier) {
    DatePickerWithDialog(
      datePickerState,
      canInteract = true,
      startText = questionLabel ?: "",
      Modifier.fillMaxWidth(),
      onBeforeShow = {
        focusManager.clearFocus()
      },
    )
    AnimatedVisibility(
      // Adding this since datePickerState handles update internally and it's hard to clear the error state as with
      // other fields
      errorText != null && datePickerState.datePickerState.selectedDateMillis == null,
    ) {
      Column {
        Spacer(Modifier.height(4.dp))
        if (errorText != null) {
          HedvigText(
            errorText,
            style = HedvigTheme.typography.label,
            color = HedvigTheme.colorScheme.textSecondaryTranslucent,
            modifier = Modifier.padding(start = 16.dp),
          )
        }
      }
    }
  }
}

@Composable
internal fun SingleSelectBubbleWithDialog(
  questionLabel: String,
  options: List<RadioOption>,
  selectedOptionId: RadioOptionId?,
  onSelect: (RadioOptionId) -> Unit,
  modifier: Modifier = Modifier,
  errorText: String? = null,
) {
  val focusManager = LocalFocusManager.current
  var showDialog by rememberSaveable { mutableStateOf(false) }
  if (showDialog) {
    SingleSelectDialog(
      title = questionLabel,
      options = options,
      selectedOption = selectedOptionId,
      onRadioOptionSelected = onSelect,
      onDismissRequest = {
        showDialog = false
      },
    )
  }
  Column(modifier) {
    HedvigBigCard(
      onClick = {
        focusManager.clearFocus()
        showDialog = true
      },
      labelText = questionLabel,
      inputText = options.firstOrNull {
        it.id == selectedOptionId
      }?.text,
      modifier = Modifier.fillMaxWidth(),
      enabled = true,
    )
    AnimatedVisibility(errorText != null) {
      Column {
        if (errorText != null) {
          Spacer(Modifier.height(4.dp))
          HedvigText(
            errorText,
            style = HedvigTheme.typography.label,
            color = HedvigTheme.colorScheme.textSecondaryTranslucent,
            modifier = Modifier.padding(start = 16.dp),
          )
        }
      }
    }
  }
}

@Composable
internal fun MultiSelectBubbleWithDialog(
  questionLabel: String,
  options: List<RadioOption>,
  selectedOptionIds: List<RadioOptionId>,
  onSelect: (RadioOptionId) -> Unit,
  modifier: Modifier = Modifier,
  errorText: String? = null,
) {
  val focusManager = LocalFocusManager.current
  var showDialog: Boolean by rememberSaveable { mutableStateOf(false) }
  if (showDialog) {
    MultiSelectDialog(
      title = questionLabel,
      options = options,
      selectedOptions = selectedOptionIds,
      onOptionSelected = onSelect,
      onDismissRequest = { showDialog = false },
      buttonText = stringResource(Res.string.general_save_button),
    )
  }
  Column(modifier) {
    HedvigBigCard(
      onClick = {
        focusManager.clearFocus()
        showDialog = true
      },
      labelText = questionLabel,
      inputText = when {
        selectedOptionIds.isEmpty() -> null

        else -> options.filter { it.id in selectedOptionIds }
          .joinToString(transform = RadioOption::text)
      },
      modifier = Modifier.fillMaxWidth(),
      enabled = true,
    )
    AnimatedVisibility(errorText != null) {
      Column {
        Spacer(Modifier.height(4.dp))
        if (errorText != null) {
          HedvigText(
            errorText,
            style = HedvigTheme.typography.label,
            color = HedvigTheme.colorScheme.textSecondaryTranslucent,
            modifier = Modifier.padding(start = 16.dp),
          )
        }
      }
    }
  }
}
