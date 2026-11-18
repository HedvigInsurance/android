package com.hedvig.android.feature.claim.details.ui.rating

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize.Medium
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.CheckFilled
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Star
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal const val TRUSTPILOT_REVIEW_URL = "https://www.trustpilot.com/evaluate/hedvig.com"

/** The backend rejects longer comments. */
private const val MAX_COMMENT_LENGTH = 2000

@Composable
internal fun ClaimRatingSheet(step: RatingStep?, onEvent: (RatingEvent) -> Unit, openUrl: (String) -> Unit) {
  val sheetState = rememberHedvigBottomSheetState<RatingStep>()
  val currentOnEvent by rememberUpdatedState(onEvent)
  var isSheetReady by remember { mutableStateOf(false) }
  var comment by rememberSaveable { mutableStateOf("") }
  LaunchedEffect(sheetState) {
    snapshotFlow { sheetState.isVisible }.first { !it }
    isSheetReady = true
  }
  LaunchedEffect(step, isSheetReady) {
    if (!isSheetReady) return@LaunchedEffect
    if (step != null) {
      sheetState.show(step)
    } else {
      comment = ""
      if (sheetState.isVisible) {
        sheetState.dismiss()
      }
    }
  }
  LaunchedEffect(sheetState) {
    snapshotFlow { sheetState.isVisible }.userDismissals().collect {
      currentOnEvent(RatingEvent.SheetDismissed)
    }
  }
  HedvigBottomSheet(sheetState) { shownStep ->
    ClaimRatingSheetContent(
      step = shownStep,
      comment = comment,
      onCommentChange = { comment = it.take(MAX_COMMENT_LENGTH) },
      onEvent = onEvent,
      onOpenTrustpilot = {
        onEvent(RatingEvent.Finish)
        openUrl(TRUSTPILOT_REVIEW_URL)
      },
      modifier = Modifier.padding(bottom = 16.dp),
    )
  }
}

/**
 * Emits each time the sheet goes from shown to hidden. A sheet restored as visible after recreation is hidden by the
 * design system before anything is shown, so a leading visible run is not a dismissal by the member.
 */
internal fun Flow<Boolean>.userDismissals(): Flow<Unit> = dropWhile { it }
  .distinctUntilChanged()
  .drop(1)
  .filter { !it }
  .map { }

@Composable
private fun ClaimRatingSheetContent(
  step: RatingStep,
  comment: String,
  onCommentChange: (String) -> Unit,
  onEvent: (RatingEvent) -> Unit,
  onOpenTrustpilot: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AnimatedContent(
    targetState = step,
    contentKey = { it is RatingStep.ThankYou },
    transitionSpec = { fadeIn() togetherWith fadeOut() },
    modifier = modifier,
    label = "ClaimRatingStep",
  ) { step ->
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      when (step) {
        is RatingStep.Stars, is RatingStep.Comment -> {
          val isComment = step is RatingStep.Comment
          // TODO: Add "How happy are you with how we handled your claim?" / "Hur nöjd är du med hur vi hanterade din skada?" to Lokalise. The question wording is still unsettled with the data team, against Typeform's "Hur nöjd är du med upplevelsen av ditt skadeärende hos Hedvig?"
          Title("How happy are you with how we handled your claim?")
          Spacer(Modifier.height(24.dp))
          StarRatingRow(
            selectedRating = (step as? RatingStep.Comment)?.rating,
            onRatingClick = { onEvent(RatingEvent.StarSelected(it)) },
          )
          AnimatedVisibility(
            visible = isComment,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Spacer(Modifier.height(24.dp))
              // TODO: Add "Want to tell us more? (optional)" / "Vill du berätta mer? (valfritt)" to Lokalise
              HedvigText(
                "Want to tell us more? (optional)",
                style = HedvigTheme.typography.label,
                color = HedvigTheme.colorScheme.textSecondaryTranslucent,
                modifier = Modifier.fillMaxWidth().padding(start = 2.dp),
              )
              Spacer(Modifier.height(8.dp))
              HedvigTextField(
                text = comment,
                onValueChange = onCommentChange,
                labelText = if ((step as? RatingStep.Comment)?.rating?.let { it >= 4 } == true) {
                  // TODO: Add "What did we do best?" / "Vad var bäst med hanteringen?" to Lokalise
                  "What did we do best?"
                } else {
                  // TODO: Add "What could we have done better?" / "Vad kunde vi ha gjort bättre?" to Lokalise
                  "What could we have done better?"
                },
                textFieldSize = Medium,
                singleLine = false,
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
              )
              Spacer(Modifier.height(16.dp))
              // TODO: Add "Send" / "Skicka" to Lokalise
              HedvigButton(
                text = "Send",
                onClick = { onEvent(RatingEvent.Send(comment)) },
                enabled = true,
                modifier = Modifier.fillMaxWidth(),
              )
              Spacer(Modifier.height(8.dp))
            }
          }
          AnimatedVisibility(visible = !isComment) { Spacer(Modifier.height(24.dp)) }
          // TODO: Add "Skip" / "Hoppa över" to Lokalise
          HedvigTextButton("Skip", onClick = { onEvent(RatingEvent.Skip) }, modifier = Modifier.fillMaxWidth())
        }

        is RatingStep.ThankYou -> {
          Icon(
            imageVector = HedvigIcons.CheckFilled,
            contentDescription = null,
            tint = HedvigTheme.colorScheme.signalGreenElement,
            modifier = Modifier.size(32.dp),
          )
          Spacer(Modifier.height(16.dp))
          if (step.rating >= 4) {
            // TODO: Add "Thanks for your rating" / "Tack för ditt betyg" to Lokalise
            Title("Thanks for your rating")
            Spacer(Modifier.height(8.dp))
            // TODO: Add "Would you like to share your experience on Trustpilot too? It helps others choose insurance." / "Vill du dela din upplevelse på Trustpilot också? Det hjälper andra att välja försäkring." to Lokalise
            Body("Would you like to share your experience on Trustpilot too? It helps others choose insurance.")
            Spacer(Modifier.height(24.dp))
            // TODO: Add "Write a review on Trustpilot" / "Skriv ett omdöme på Trustpilot" to Lokalise
            HedvigButton(
              onClick = onOpenTrustpilot,
              enabled = true,
              modifier = Modifier.fillMaxWidth(),
            ) {
              HedvigText("Write a review on Trustpilot")
              Spacer(Modifier.width(8.dp))
              Icon(HedvigIcons.ArrowNorthEast, contentDescription = null)
            }
            Spacer(Modifier.height(8.dp))
            // TODO: Add "No thanks" / "Nej tack" to Lokalise
            HedvigTextButton(
              "No thanks",
              onClick = { onEvent(RatingEvent.Finish) },
              modifier = Modifier.fillMaxWidth(),
            )
          } else {
            // TODO: Add "Thanks for telling us" / "Tack för att du berättade" to Lokalise
            Title("Thanks for telling us")
            Spacer(Modifier.height(8.dp))
            // TODO: Add "We read every answer and use it to get better. If you have questions about your claim, you can write to us." / "Vi läser alla svar och använder dem för att bli bättre. Har du frågor om ditt ärende kan du skriva till oss." to Lokalise
            Body(
              "We read every answer and use it to get better. If you have questions about your claim, you can write to us.",
            )
            Spacer(Modifier.height(24.dp))
            // TODO: Add "Continue" / "Fortsätt" to Lokalise
            HedvigButton(
              text = "Continue",
              onClick = { onEvent(RatingEvent.Finish) },
              enabled = true,
              buttonStyle = Secondary,
              modifier = Modifier.fillMaxWidth(),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun Title(text: String) {
  HedvigText(text, style = HedvigTheme.typography.headlineSmall, textAlign = TextAlign.Center)
}

@Composable
private fun Body(text: String) {
  HedvigText(
    text,
    style = HedvigTheme.typography.bodySmall,
    color = HedvigTheme.colorScheme.textSecondaryTranslucent,
    textAlign = TextAlign.Center,
  )
}

@Composable
private fun StarRatingRow(selectedRating: Int?, onRatingClick: (Int) -> Unit, modifier: Modifier = Modifier) {
  Column(modifier = modifier.fillMaxWidth()) {
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val popScales = remember { List(5) { Animatable(1f) } }
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      (1..5).forEach { rating ->
        // TODO: Add "%d of 5" / "%d av 5" to Lokalise
        val ratingDescription = "$rating of 5"
        val popScale = popScales[rating - 1]
        val starColor by animateColorAsState(
          targetValue = if (selectedRating != null && rating <= selectedRating) {
            HedvigTheme.colorScheme.signalAmberElement
          } else {
            HedvigTheme.colorScheme.fillTertiary
          },
          label = "StarColor",
        )
        HedvigCard(shape = HedvigTheme.shapes.cornerLarge, modifier = Modifier.weight(1f)) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .semantics {
                contentDescription = ratingDescription
                selected = selectedRating == rating
              }
              .clickable(role = Role.RadioButton) {
                hapticFeedback.performHapticFeedback(
                  if (rating >= 4) HapticFeedbackType.Confirm else HapticFeedbackType.TextHandleMove,
                )
                onRatingClick(rating)
                coroutineScope.launch {
                  popScale.animateTo(1.25f, spring(stiffness = Spring.StiffnessHigh))
                  popScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
              },
          ) {
            Icon(
              imageVector = HedvigIcons.Star,
              contentDescription = null,
              tint = starColor,
              modifier = Modifier.size(32.dp).scale(popScale.value),
            )
          }
        }
      }
    }
    Spacer(Modifier.height(8.dp))
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        // TODO: Add "Not happy at all" / "Inte alls nöjd" to Lokalise
        HedvigText(
          "Not happy at all",
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondaryTranslucent,
        )
      },
      endSlot = {
        Row(horizontalArrangement = Arrangement.End) {
          // TODO: Add "Very happy" / "Mycket nöjd" to Lokalise
          HedvigText(
            "Very happy",
            style = HedvigTheme.typography.label,
            color = HedvigTheme.colorScheme.textSecondaryTranslucent,
          )
        }
      },
      spaceBetween = 8.dp,
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewRatingStars() {
  PreviewStep(RatingStep.Stars)
}

@HedvigPreview
@Composable
private fun PreviewRatingCommentHappy() {
  PreviewStep(RatingStep.Comment(rating = 5))
}

@HedvigPreview
@Composable
private fun PreviewRatingCommentUnhappy() {
  PreviewStep(RatingStep.Comment(rating = 2), comment = "The handling took too long.")
}

@HedvigPreview
@Composable
private fun PreviewRatingThankYouHappy() {
  PreviewStep(RatingStep.ThankYou(rating = 5))
}

@HedvigPreview
@Composable
private fun PreviewRatingThankYouUnhappy() {
  PreviewStep(RatingStep.ThankYou(rating = 2))
}

@Composable
private fun PreviewStep(step: RatingStep, comment: String = "") {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ClaimRatingSheetContent(
        step = step,
        comment = comment,
        onCommentChange = {},
        onEvent = {},
        onOpenTrustpilot = {},
        modifier = Modifier.padding(16.dp),
      )
    }
  }
}
