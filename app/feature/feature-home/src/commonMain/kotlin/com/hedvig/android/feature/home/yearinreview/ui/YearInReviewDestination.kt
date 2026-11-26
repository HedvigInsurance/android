package com.hedvig.android.feature.home.yearinreview.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
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
import com.hedvig.android.design.system.hedvig.HedvigMarkdownText
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigThreeDotsProgressIndicator
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.debugBorder
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.feature.home.yearinreview.data.YearInReviewAction
import com.hedvig.android.feature.home.yearinreview.data.YearInReviewMedia
import com.hedvig.android.feature.home.yearinreview.data.YearInReviewStory
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
internal fun YearInReviewDestination(
  viewModel: YearInReviewViewModel,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  close: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  YearInReviewScreen(
    uiState = uiState,
    imageLoader = imageLoader,
    openUrl = openUrl,
    close = close,
    onEvent = viewModel::emit,
  )
}

@Composable
private fun YearInReviewScreen(
  uiState: YearInReviewUiState,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
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

      YearInReviewUiState.Unavailable -> {
        LaunchedEffect(Unit) { close() }
      }

      YearInReviewUiState.Failure -> {
        HedvigErrorSection(
          onButtonClick = { onEvent(YearInReviewEvent.Retry) },
          modifier = Modifier.fillMaxSize(),
        )
      }

      is YearInReviewUiState.Content -> {
        val imagesLoaded = rememberImagesLoaded(
          uiState.stories.mapNotNull {
            (it.media as? YearInReviewMedia.Image)?.url
          },
          imageLoader,
        )
        if (!imagesLoaded) {
          StoriesLoading()
        } else {
          LaunchedEffect(Unit) { onEvent(YearInReviewEvent.Resume) }
          PillowBackground(
            pillow = storyPillows[uiState.currentIndex % storyPillows.size],
            modifier = Modifier.fillMaxSize(),
          )
          StoriesContent(uiState, imageLoader, openUrl, close, onEvent)
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
      launch { delay(600.milliseconds) }
      imageUrls.distinct().forEach { url ->
        launch { imageLoader.execute(ImageRequest.Builder(context).data(url).build()) }
      }
    }
    value = true
  }
  return loaded
}

/** Cycled through as the stories' backgrounds. */
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
  openUrl: (String) -> Unit,
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
          isPaused = uiState.isPaused,
          openUrl = openUrl,
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
  isPaused: Boolean,
  openUrl: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val titleAndImageAlpha = remember(story.id) { Animatable(0f) }
  var showText by remember(story.id) { mutableStateOf(false) }
  var showAction by remember(story.id) { mutableStateOf(false) }

  LaunchedEffect(story.id) {
    delay(500.milliseconds)
    // The title and image fade in together, centered on the screen. After a pause the text opens up
    // beneath them, lifting them so the three stay centered as a group.
    titleAndImageAlpha.animateTo(1f, tween(durationMillis = 500))
    delay(1.seconds)
    showText = true
    delay(1.seconds)
    showAction = true
  }
  // Added only after the delay, so the image keeps its place until the button pushes it up.
//  var showThanksButton by remember(isLastStory) { mutableStateOf(isLastStory) }
//  LaunchedEffect(isLastStory) {
//    if (isLastStory) {
//      delay(4.seconds)
//      showThanksButton = true
//    }
//  }
  val textBringIntoViewRequester = remember(story.id) { BringIntoViewRequester() }
  LaunchedEffect(showText) {
    if (showText) {
      // Once the text has expanded, scrolls it into view in case it opened below the visible area.
      delay(800.milliseconds)
      textBringIntoViewRequester.bringIntoView()
    }
  }
  val title: @Composable () -> Unit = {
    HedvigText(
      text = story.title,
      color = HedvigTheme.colorScheme.textPrimary,
      style = HedvigTheme.typography.bodySmall,
      modifier = Modifier
        .fillMaxWidth()
        .alpha(titleAndImageAlpha.value),
    )
  }
  val media: (@Composable (Modifier) -> Unit)? = story.media?.let { media ->
    { mediaModifier ->
      StoryMedia(
        media = media,
        imageLoader = imageLoader,
        isPaused = isPaused,
        modifier = mediaModifier
          .clip(RoundedCornerShape(16.dp))
          .alpha(titleAndImageAlpha.value),
      )
    }
  }
  val text: @Composable () -> Unit = {
    StoryText(
      description = story.description,
      visible = showText,
      modifier = Modifier.bringIntoViewRequester(textBringIntoViewRequester),
    )
  }
  val actionButton: @Composable () -> Unit = {
    story.action?.let { action ->
      StoryActionButton(action = action, visible = showAction, openUrl = openUrl)
    }
  }
  BoxWithConstraints(modifier.padding(16.dp)) {
    val isLandscape = maxWidth > maxHeight || maxWidth >= WideLayoutMinWidth
    if (isLandscape) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
        WideStoryMediaColumn(
          title = title,
          media = media?.let { { it(Modifier.fillMaxSize()) } },
          modifier = Modifier.fillMaxHeight(),
        )
        Spacer(Modifier.width(16.dp))
        Column(
          Modifier
            .weight(1f)
            .fillMaxHeight(),
        ) {
          Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth(),
          ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
              text()
            }
          }
          actionButton()
        }
      }
    } else {
      Column(Modifier.fillMaxSize()) {
        BoxWithConstraints(
          Modifier
            .weight(1f)
            .fillMaxWidth(),
        ) {
          StoryContentLayout(
            viewportHeight = maxHeight,
            title = title,
            media = media?.let { { it(Modifier.fillMaxSize()) } },
            text = text,
            modifier = Modifier.verticalScroll(rememberScrollState()),
          )
        }
        actionButton()
      }
    }
  }
}

/**
 * The [title] above the [media], as wide as the media. The media is a square that takes the height the title
 * leaves, and at most half the available width, so the content beside it keeps the rest. Without media the
 * title takes half the width.
 */
@Composable
private fun WideStoryMediaColumn(
  title: @Composable () -> Unit,
  media: (@Composable () -> Unit)?,
  modifier: Modifier = Modifier,
) {
  Layout(
    contents = listOf(title, media ?: {}),
    modifier = modifier,
  ) { (titleMeasurables, mediaMeasurables), constraints ->
    val maxWidth = constraints.maxWidth / 2
    val hasMedia = mediaMeasurables.isNotEmpty()
    val gap = if (hasMedia) 16.dp.roundToPx() else 0

    fun titleHeight(width: Int): Int = titleMeasurables.sumOf { it.minIntrinsicHeight(width) }
    // The title wraps to the media's width, which in turn depends on the title's height. A few rounds settle it.
    var side = if (hasMedia) minOf(maxWidth, constraints.maxHeight) else maxWidth
    if (hasMedia) {
      repeat(3) {
        side = (constraints.maxHeight - titleHeight(side) - gap).coerceIn(0, maxWidth)
      }
    }
    val titlePlaceables = titleMeasurables.map { it.measure(Constraints(minWidth = side, maxWidth = side)) }
    val mediaPlaceables = mediaMeasurables.map { it.measure(Constraints.fixed(side, side)) }
    val contentHeight = titlePlaceables.sumOf { it.height } + gap + if (hasMedia) side else 0
    val height = contentHeight.coerceIn(constraints.minHeight, constraints.maxHeight)
    layout(side, height) {
      var y = (height - contentHeight) / 2
      titlePlaceables.forEach {
        it.place(0, y)
        y += it.height
      }
      y += gap
      mediaPlaceables.forEach { it.place(0, y) }
    }
  }
}

/** Wide enough to put the media beside the text even in portrait, as on a tablet. */
private val WideLayoutMinWidth = 600.dp

/** Laid out from the start so its space is reserved, fading in once [visible], and ignoring taps until then. */
@Composable
private fun StoryActionButton(action: YearInReviewAction, visible: Boolean, openUrl: (String) -> Unit) {
  val alpha by animateFloatAsState(
    targetValue = if (visible) 1f else 0f,
    animationSpec = tween(durationMillis = 500),
    label = "actionAlpha",
  )
  HedvigButton(
    text = action.title,
    onClick = { if (visible) openUrl(action.url) },
    enabled = true,
    buttonStyle = ButtonDefaults.ButtonStyle.Primary,
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 16.dp)
      .alpha(alpha),
  )
}

@Composable
private fun StoryText(description: String, visible: Boolean, modifier: Modifier = Modifier) {
  AnimatedVisibility(
    visible = visible,
    enter = expandVertically(tween(durationMillis = 800), expandFrom = Alignment.Top) +
      fadeIn(tween(durationMillis = 500, delayMillis = 300)),
    modifier = modifier,
  ) {
    HedvigMarkdownText(
      content = description,
      style = HedvigTheme.typography.bodySmall.copy(color = HedvigTheme.colorScheme.textSecondary),
      strongStyle = SpanStyle(color = HedvigTheme.colorScheme.textPrimary),
      modifier = Modifier.padding(top = 16.dp),
    )
  }
}

private val MinMediaSize = 120.dp

/**
 * Stacks [title], [media] and [text], centered in [viewportHeight]. The media is a square as wide as the
 * content when there is room, and shrinks to leave room for the title and text, down to [MinMediaSize]. Past
 * that the content grows taller than [viewportHeight], to be scrolled.
 */
@Composable
private fun StoryContentLayout(
  viewportHeight: Dp,
  title: @Composable () -> Unit,
  media: (@Composable () -> Unit)?,
  text: @Composable () -> Unit,
  modifier: Modifier = Modifier,
) {
  Layout(
    contents = listOf(title, media ?: {}, text),
    modifier = modifier,
  ) { (titleMeasurables, mediaMeasurables, textMeasurables), constraints ->
    val width = constraints.maxWidth
    val childConstraints = Constraints(maxWidth = width)
    val titlePlaceables = titleMeasurables.map { it.measure(childConstraints) }
    val textPlaceables = textMeasurables.map { it.measure(childConstraints) }
    val titleHeight = titlePlaceables.sumOf { it.height }
    val textHeight = textPlaceables.sumOf { it.height }
    val hasMedia = mediaMeasurables.isNotEmpty()
    val mediaGap = if (hasMedia) 16.dp.roundToPx() else 0
    val mediaSize = if (hasMedia) {
      val minSize = minOf(MinMediaSize.roundToPx(), width)
      (viewportHeight.roundToPx() - titleHeight - mediaGap - textHeight).coerceIn(minSize, width)
    } else {
      0
    }
    val mediaPlaceables = mediaMeasurables.map { it.measure(Constraints.fixed(mediaSize, mediaSize)) }
    val contentHeight = titleHeight + mediaGap + mediaSize + textHeight
    val height = maxOf(contentHeight, viewportHeight.roundToPx())
    layout(width, height) {
      var y = (height - contentHeight) / 2
      titlePlaceables.forEach {
        it.place(0, y)
        y += it.height
      }
      y += mediaGap
      mediaPlaceables.forEach { it.place((width - mediaSize) / 2, y) }
      y += mediaSize
      textPlaceables.forEach {
        it.place(0, y)
        y += it.height
      }
    }
  }
}

@Composable
private fun StoryMedia(
  media: YearInReviewMedia,
  imageLoader: ImageLoader,
  isPaused: Boolean,
  modifier: Modifier = Modifier,
) {
  when (media) {
    is YearInReviewMedia.Image -> AsyncImage(
      model = media.url,
      contentDescription = null,
      imageLoader = imageLoader,
      contentScale = ContentScale.Crop,
      modifier = modifier,
    )

    is YearInReviewMedia.Video -> Box(modifier.background(HedvigTheme.colorScheme.surfacePrimary)) {
      YearInReviewVideo(url = media.url, isPaused = isPaused, modifier = Modifier.fillMaxSize())
    }
  }
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
@Preview(name = "Landscape phone", device = "spec:width=891dp,height=411dp")
@Preview(name = "Tablet", device = "spec:width=800dp,height=1280dp,dpi=240")
@Composable
private fun PreviewYearInReviewScreen(
  @PreviewParameter(YearInReviewUiStatePreviewProvider::class) uiState: YearInReviewUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      YearInReviewScreen(
        uiState = uiState,
        imageLoader = rememberPreviewImageLoader(),
        openUrl = {},
        close = {},
        onEvent = {},
      )
    }
  }
}

private val previewStories = listOf(
  YearInReviewStory(
    id = "0",
    title = "Du har varit med oss i 412 dagar",
    description = "Tack för att du är **medlem hos Hedvig**.",
    media = YearInReviewMedia.Image("https://example.com/first.png"),
    action = null,
  ),
  YearInReviewStory(
    id = "1",
    title = "Antal hanterade skador",
    description = "**Vi hjälpte till med 29 125 skador under året.** Hela 2 000 fler än förra året, och de " +
      "flesta av dem fick sin ersättning samma dag som de anmälde skadan i appen.",
    media = YearInReviewMedia.Image("https://example.com/second.gif"),
    action = YearInReviewAction(title = "Läs mer", url = "https://example.com"),
  ),
  YearInReviewStory(
    id = "2",
    title = "Djur-vabb",
    description = "Under året har 71 procent fler valt **Djurförsäkring Premium**, där ersättning för djur-vabb ingår.",
    media = null,
    action = YearInReviewAction(title = "Läs mer", url = "https://example.com"),
  ),
)

private class YearInReviewUiStatePreviewProvider : CollectionPreviewParameterProvider<YearInReviewUiState>(
  listOf(
    YearInReviewUiState.Loading,
    YearInReviewUiState.Failure,
    YearInReviewUiState.Content(stories = previewStories, currentIndex = 0, progress = 0.3f, isPaused = false),
    YearInReviewUiState.Content(stories = previewStories, currentIndex = 1, progress = 0.6f, isPaused = false),
    YearInReviewUiState.Content(stories = previewStories, currentIndex = 2, progress = 1f, isPaused = false),
  ),
)
