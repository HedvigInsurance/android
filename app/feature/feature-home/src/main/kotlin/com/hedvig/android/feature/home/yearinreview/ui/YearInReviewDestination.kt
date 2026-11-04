package com.hedvig.android.feature.home.yearinreview.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.hedvig.android.data.contract.PillowType
import com.hedvig.android.data.contract.pillowResource
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigThreeDotsProgressIndicator
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.feature.home.yearinreview.data.YearInReviewStory
import hedvig.resources.PROFILE_ABOUT_APP_TITLE
import hedvig.resources.Res
import hedvig.resources.general_close_button
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun YearInReviewDestination(viewModel: YearInReviewViewModel, imageLoader: ImageLoader, close: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  YearInReviewScreen(
    uiState = uiState,
    imageLoader = imageLoader,
    close = close,
    onEvent = viewModel::emit,
  )
}

@Composable
private fun YearInReviewScreen(
  uiState: YearInReviewUiState,
  imageLoader: ImageLoader,
  close: () -> Unit,
  onEvent: (YearInReviewEvent) -> Unit,
) {
  Box(
    Modifier
      .fillMaxSize()
      .background(HedvigTheme.colorScheme.backgroundPrimary),
  ) {
    when (uiState) {
      YearInReviewUiState.Loading -> {
        StoriesLoading()
      }

      YearInReviewUiState.Failure -> {
        HedvigErrorSection(
          onButtonClick = { onEvent(YearInReviewEvent.Retry) },
          modifier = Modifier.fillMaxSize(),
        )
      }

      is YearInReviewUiState.Content -> {
        // Previews cannot load images, so they skip the preload.
        val imagesLoaded = LocalInspectionMode.current ||
          rememberImagesLoaded(uiState.stories.mapNotNull { it.imageUrl }, imageLoader)
        if (!imagesLoaded) {
          StoriesLoading()
        } else {
          LaunchedEffect(Unit) { onEvent(YearInReviewEvent.Resume) }
          PillowBackground(
            pillow = uiState.currentStory.backgroundPillow
              ?: storyPillows[uiState.currentIndex % storyPillows.size],
            modifier = Modifier.fillMaxSize(),
          )
          StoriesContent(uiState, imageLoader, close, onEvent)
        }
      }
    }
  }
}

@Composable
private fun StoriesLoading() {
  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier.fillMaxSize(),
  ) {
    HedvigThreeDotsProgressIndicator()
  }
}

/**
 * Loads every image up front and returns whether all of them are done, so each one is shown from cache
 * the moment its story opens, without a loading gap.
 */
@Composable
private fun rememberImagesLoaded(imageUrls: List<String>, imageLoader: ImageLoader): Boolean {
  val context = LocalPlatformContext.current
  val loaded by produceState(false, imageUrls, imageLoader) {
    coroutineScope {
      // Keeps the loading indicator up long enough to read as loading rather than a flash.
      launch { delay(800.milliseconds) }
      imageUrls.distinct().forEach { url ->
        launch { imageLoader.execute(ImageRequest.Builder(context).data(url).build()) }
      }
    }
    value = true
  }
  return loaded
}

/** Cycled through as the background of stories that do not name their own pillow. */
private val storyPillows = listOf(
  PillowType.HOME,
  PillowType.RENTAL,
  PillowType.VILLA,
  PillowType.HOMEOWNER,
  PillowType.CAR,
  PillowType.DOG,
  PillowType.CAT,
  PillowType.ACCIDENT,
  PillowType.STUDENT,
  PillowType.VACATION,
)

@Composable
private fun PillowBackground(pillow: PillowType, modifier: Modifier = Modifier) {
  Crossfade(
    targetState = pillow,
    animationSpec = tween(durationMillis = 1500),
    label = "pillowBackground",
    modifier = modifier,
  ) { currentPillow ->
    Image(
      painter = painterResource(currentPillow.pillowResource()),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxSize()
        .blur(40.dp)
        .alpha(0.2f),
    )
  }
}

@Composable
private fun StoriesContent(
  uiState: YearInReviewUiState.Content,
  imageLoader: ImageLoader,
  close: () -> Unit,
  onEvent: (YearInReviewEvent) -> Unit,
) {
  Column(
    Modifier
      .fillMaxSize()
      .safeDrawingPadding(),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(start = 16.dp, end = 4.dp),
    ) {
      StoryProgressBar(
        storyCount = uiState.stories.size,
        currentIndex = uiState.currentIndex,
        progress = uiState.progress,
        modifier = Modifier.weight(1f),
      )
      IconButton(onClick = close) {
        Icon(
          imageVector = HedvigIcons.Close,
          contentDescription = stringResource(Res.string.general_close_button),
          modifier = Modifier.size(24.dp),
        )
      }
    }
    Box(
      Modifier
        .weight(1f)
        .fillMaxWidth()
        .pointerInput(Unit) {
          detectTapGestures(
            onPress = {
              onEvent(YearInReviewEvent.Pause)
              tryAwaitRelease()
              onEvent(YearInReviewEvent.Resume)
            },
            // A long press only pauses; without this handler releasing it would also count as a tap.
            onLongPress = {},
            onTap = { offset ->
              if (offset.x < size.width / 2) {
                onEvent(YearInReviewEvent.Previous)
              } else {
                onEvent(YearInReviewEvent.Next)
              }
            },
          )
        },
    ) {
      val story = uiState.currentStory
      // A fresh page per story, so the previous story's revealed parts are not animated out showing the next
      // story's content.
      key(story.id) {
        StoryPage(
          story = story,
          imageLoader = imageLoader,
          isLastStory = uiState.isLastStory,
          close = close,
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }
}

@Composable
private fun StoryProgressBar(storyCount: Int, currentIndex: Int, progress: Float, modifier: Modifier = Modifier) {
  // The presenter ticks in steps; easing linearly across each step keeps the bar moving smoothly. Each story
  // gets its own animation, so a new segment starts from empty instead of the previous story's progress.
  val animatedProgress by key(currentIndex) {
    animateFloatAsState(
      targetValue = progress,
      animationSpec = if (progress == 0f) tween(0) else tween(durationMillis = 100, easing = LinearEasing),
      label = "storyProgress",
    )
  }
  Row(
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    modifier = modifier,
  ) {
    repeat(storyCount) { index ->
      val fill = when {
        index < currentIndex -> 1f
        index == currentIndex -> animatedProgress
        else -> 0f
      }
      Box(
        Modifier
          .weight(1f)
          .height(4.dp)
          .clip(CircleShape)
          .background(HedvigTheme.colorScheme.fillTertiary),
      ) {
        Box(
          Modifier
            .fillMaxHeight()
            .fillMaxWidth(fill)
            .background(HedvigTheme.colorScheme.fillPrimary),
        )
      }
    }
  }
}

/** One story, fading in its image, then its title, then its text. */
@Composable
private fun StoryPage(
  story: YearInReviewStory,
  imageLoader: ImageLoader,
  isLastStory: Boolean,
  close: () -> Unit,
  modifier: Modifier = Modifier,
) {
  // Previews render a single frame, so they start where the animations end.
  val isPreview = LocalInspectionMode.current
  val initialAlpha = if (isPreview) 1f else 0f
  val titleAndImageAlpha = remember(story.id) { Animatable(initialAlpha) }
  var showText by remember(story.id) { mutableStateOf(isPreview) }
  LaunchedEffect(story.id) {
    delay(500.milliseconds)
    // The title and image fade in together, centered on the screen. After a pause the text opens up
    // beneath them, lifting them so the three stay centered as a group.
    titleAndImageAlpha.animateTo(1f, tween(durationMillis = 500))
    delay(1.seconds)
    showText = true
  }
  // Added only after the delay, so the image keeps its place until the button pushes it up.
  var showThanksButton by remember(isLastStory) { mutableStateOf(isPreview && isLastStory) }
  LaunchedEffect(isLastStory) {
    if (isLastStory) {
      delay(4.seconds)
      showThanksButton = true
    }
  }
  Column(modifier.padding(16.dp)) {
    Spacer(Modifier.weight(1f))
    story.title?.let {
      HedvigText(
        text = story.title,
        textAlign = TextAlign.Center,
        color = HedvigTheme.colorScheme.textPrimary,
        style = HedvigTheme.typography.bodyMedium,
        modifier = Modifier
          .fillMaxWidth()
          .alpha(titleAndImageAlpha.value),
      )
    }
    if (story.imageUrl != null) {
      Spacer(Modifier.height(16.dp))
      StoryImage(
        story = story,
        imageLoader = imageLoader,
        modifier = Modifier
          .fillMaxWidth()
          // Every image is shown as a square, cropped to fill it.
          .aspectRatio(1f)
          .clip(RoundedCornerShape(16.dp))
          .alpha(titleAndImageAlpha.value),
      )
    }
    AnimatedVisibility(
      visible = showText,
      enter = expandVertically(tween(durationMillis = 600), expandFrom = Alignment.Top) +
        fadeIn(tween(durationMillis = 500, delayMillis = 300)),
    ) {
      StoryText(story, Modifier.padding(top = 16.dp))
    }
    Spacer(Modifier.weight(1f))
    AnimatedVisibility(
      visible = showThanksButton,
      enter = expandVertically() + fadeIn(),
      exit = shrinkVertically() + fadeOut(),
    ) {
      ThanksButton(onClick = close)
    }
  }
}

@Composable
private fun StoryImage(story: YearInReviewStory, imageLoader: ImageLoader, modifier: Modifier = Modifier) {
  if (LocalInspectionMode.current && !story.showImageOnCard) {
    Box(modifier.background(HedvigTheme.colorScheme.surfacePrimary))
  } else if (story.showImageOnCard) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = modifier.background(Color.White),
    ) {
      AsyncImage(
        model = story.imageUrl,
        contentDescription = null,
        imageLoader = imageLoader,
        contentScale = ContentScale.Fit,
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
      )
    }
  } else {
    AsyncImage(
      model = story.imageUrl,
      contentDescription = null,
      imageLoader = imageLoader,
      contentScale = ContentScale.Crop,
      modifier = modifier,
    )
  }
}

/** The story's text, with its lead emphasized ahead of the rest. */
@Composable
private fun StoryText(story: YearInReviewStory, modifier: Modifier = Modifier) {
  val textPrimary = HedvigTheme.colorScheme.textPrimary
  val text = remember(story, textPrimary) {
    buildAnnotatedString {
      withStyle(SpanStyle(color = textPrimary)) { append(story.leadText) }
      append(" ")
      append(story.text)
    }
  }
  HedvigText(
    text = text,
    color = HedvigTheme.colorScheme.textSecondary,
    style = HedvigTheme.typography.bodyMedium,
    modifier = modifier,
  )
}

@Composable
private fun ThanksButton(onClick: () -> Unit) {
  HedvigButton(
    // TODO: Add "Thanks!" / "Tack!" to Lokalise
    text = "Tack!",
    onClick = onClick,
    enabled = true,
    buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 16.dp),
  )
}

@HedvigPreview
@Composable
private fun PreviewYearInReviewScreen(
  @PreviewParameter(YearInReviewUiStatePreviewProvider::class) uiState: YearInReviewUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      YearInReviewScreen(
        uiState = uiState,
        imageLoader = rememberPreviewImageLoader(),
        close = {},
        onEvent = {},
      )
    }
  }
}

private val previewStories = listOf(
  YearInReviewStory(
    id = "first",
    title = "Höjdpunkter från året som gått – Hedvig 2024",
    leadText = "Året börjar lida mot sitt slut.",
    text = "För att runda av 2024 har vi på Hedvig sammanfattat några höjdpunkter och kuriosa från året som gått.",
    imageUrl = "https://example.com/first.png",
    showImageOnCard = true,
  ),
  YearInReviewStory(
    id = "second",
    title = "Antal hanterade skador",
    leadText = "Vi hjälpte till med 29 125 skador under 2024.",
    text = "Hela 2 000 fler än förra året.",
    imageUrl = "https://example.com/second.gif",
  ),
  YearInReviewStory(
    id = "third",
    title = "Djur-vabb",
    leadText = "Under året har 71 procent fler valt Djurförsäkring Premium,",
    text = "där ersättning för djur-vabb ingår.",
    imageUrl = "https://example.com/third.png",
  ),
)

private class YearInReviewUiStatePreviewProvider : CollectionPreviewParameterProvider<YearInReviewUiState>(
  listOf(
    YearInReviewUiState.Loading,
    YearInReviewUiState.Failure,
    YearInReviewUiState.Content(stories = previewStories, currentIndex = 0, progress = 0.3f),
    YearInReviewUiState.Content(stories = previewStories, currentIndex = 1, progress = 0.6f),
    YearInReviewUiState.Content(stories = previewStories, currentIndex = 2, progress = 1f),
  ),
)
