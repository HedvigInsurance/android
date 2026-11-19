package com.hedvig.android.feature.product.inapp.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.feature.product.inapp.data.CrossSellProductInfo

/**
 * Shows one slide at a time, wrapping around after the last. An animated image plays once and moves on as
 * soon as it ends; a still image stays for [SLIDE_DURATION_MILLIS].
 * Tapping the left half goes back a slide and the right half forward. The timer only runs while the screen
 * is resumed, and picks up where it stopped.
 */
@Composable
internal fun CrossSellStoryCarousel(
  items: List<CrossSellProductInfo.CarouselItem>,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  var currentIndex by rememberSaveable(items) { mutableIntStateOf(0) }
  val goToNext = { currentIndex = (currentIndex + 1) % items.size }
  val goToPrevious = { currentIndex = (currentIndex - 1 + items.size) % items.size }
  val item = items[currentIndex]
  val progress = remember(currentIndex) { Animatable(0f) }
  var slideDurationMillis by remember(currentIndex) { mutableStateOf<Int?>(null) }
  val lifecycleOwner = LocalLifecycleOwner.current
  LaunchedEffect(currentIndex, slideDurationMillis, lifecycleOwner) {
    val durationMillis = slideDurationMillis ?: return@LaunchedEffect
    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
      val remainingMillis = ((1f - progress.value) * durationMillis).toInt()
      progress.animateTo(1f, tween(durationMillis = remainingMillis, easing = LinearEasing))
      goToNext()
    }
  }
  Box(
    modifier = modifier
      .clip(HedvigTheme.shapes.cornerXXLarge)
      .background(HedvigTheme.colorScheme.surfacePrimary)
      .pointerInput(items.size) {
        detectTapGestures { offset ->
          if (offset.x < size.width / 2) goToPrevious() else goToNext()
        }
      }
      .semantics {
        if (items.size > 1) {
          customActions = listOf(
            // TODO: Add "Next" / "Nästa" to Lokalise
            CustomAccessibilityAction("Next") {
              goToNext()
              true
            },
            // TODO: Add "Previous" / "Föregående" to Lokalise
            CustomAccessibilityAction("Previous") {
              goToPrevious()
              true
            },
          )
        }
      },
  ) {
    SlideImage(
      image = item.image,
      imageLoader = imageLoader,
      onLoaded = { animationMillis ->
        slideDurationMillis = animationMillis
          ?.let { it * ANIMATION_PLAY_COUNT + ANIMATION_HOLD_MILLIS }
          ?: SLIDE_DURATION_MILLIS
      },
      modifier = Modifier
        .matchParentSize()
        .padding(16.dp),
    )
    if (items.size > 1) {
      StoryProgress(
        count = items.size,
        currentIndex = currentIndex,
        currentProgress = { progress.value },
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(horizontal = 24.dp, vertical = 16.dp),
      )
    }
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(horizontal = 48.dp, vertical = 24.dp),
    ) {
      HedvigText(text = item.title, textAlign = TextAlign.Center)
      if (item.subtitle != null) {
        HedvigText(
          text = item.subtitle,
          textAlign = TextAlign.Center,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
    }
  }
}

/**
 * [onLoaded] gets the length of one play of an animated image, or null for a still image or a failed load.
 * The memory cache is skipped so every visit decodes afresh, which restarts the animation and re-reads its length.
 */
@Composable
private fun SlideImage(
  image: ImageAsset,
  imageLoader: ImageLoader,
  onLoaded: (animationMillis: Int?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalPlatformContext.current
  val durationReader = remember(image.src) { WebpAnimationDurationReader() }
  val request = remember(image.src, context) {
    ImageRequest
      .Builder(context)
      .data(image.src)
      .decoderFactory(durationReader)
      .memoryCachePolicy(CachePolicy.DISABLED)
      .playAnimation(times = ANIMATION_PLAY_COUNT)
      .build()
  }
  AsyncImage(
    model = request,
    contentDescription = image.description ?: EmptyContentDescription,
    imageLoader = imageLoader,
    contentScale = ContentScale.Fit,
    onSuccess = { onLoaded(durationReader.durationMillis) },
    onError = { onLoaded(null) },
    modifier = modifier,
  )
}

@Composable
private fun StoryProgress(count: Int, currentIndex: Int, currentProgress: () -> Float, modifier: Modifier = Modifier) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    modifier = modifier.fillMaxWidth(),
  ) {
    val trackShape = HedvigTheme.shapes.cornerFull
    repeat(count) { index ->
      Box(
        Modifier
          .weight(1f)
          .height(4.dp)
          .clip(trackShape)
          .background(HedvigTheme.colorScheme.surfaceSecondaryTransparent),
      ) {
        val fill = when {
          index < currentIndex -> 1f
          index == currentIndex -> currentProgress()
          else -> 0f
        }
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

private const val SLIDE_DURATION_MILLIS = 5_000
private const val ANIMATION_HOLD_MILLIS = 500
private const val ANIMATION_PLAY_COUNT = 2

internal expect fun ImageRequest.Builder.playAnimation(times: Int): ImageRequest.Builder

internal expect fun ImageRequest.Builder.playAnimationOnce(): ImageRequest.Builder
