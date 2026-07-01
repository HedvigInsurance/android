package com.hedvig.android.navigation.compose

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import androidx.navigation3.ui.LocalNavAnimatedContentScope

/**
 * Which chrome the scene decorator should render for a chrome-bearing scene. Driven by a single
 * runtime probe supplied by `:app`. A lone deep link must never show the rail (it would expose the
 * broken index-0 runs invariant — see spec D7), yet a bare tab root has no Up affordance of its own.
 */
enum class LoneDeepLinkChrome {
  /** Normal: render the rail/bar. */
  ShowSuite,

  /** Lone tab root (no own Up): the decorator supplies a top-app-bar Up. */
  ShowUpBar,

  /** Lone deep "bar-keeper" that already renders its own Up: render nothing extra. */
  ShowNothing,
}

/**
 * Renders the global navigation bar/rail as part of the scene itself, via a
 * [SceneDecoratorStrategy], rather than wrapping `NavDisplay` in an outer
 * `Row/Column { chrome ; content }`. This is the purpose-built Navigation 3 way to add persistent
 * chrome — the decorator hands every chrome-bearing [Scene] back wrapped so the bar rides *inside*
 * the top-level `AnimatedContent`.
 *
 * The chrome is opt-in: only scenes whose metadata carries [showNavBar] (the top-level tab roots
 * plus the handful of deeper destinations that keep the bar) get wrapped; everything else stays
 * full-screen.
 *
 * The naive version of this re-composes the chrome twice during a scene transition (both the
 * outgoing and incoming scene compose at once) and lets it slide/reflow with the content. Two
 * cooperating pieces keep it visually static while transitioning between two chrome-bearing scenes:
 * 1. [movableContentOf] — one chrome instance, *moved* (not re-created) between scenes. Each scene
 *    decides locally whether it is the caller via `transition.targetState == Visible`, so at any
 *    moment exactly one scene calls the movable content. That single-caller invariant is what lets
 *    Compose perform a clean move that preserves the chrome's live nodes — including the navigation
 *    item's press ripple (Bug #2). A previous design coordinated the host through shared state
 *    written in a `SideEffect`, which produced frames with zero or two callers; the movable content
 *    was then torn down and re-created instead of moved, killing the ripple.
 * 2. `sharedElement` + the local [cacheSize] modifier — pins the chrome in place across the two
 *    scenes and holds the non-caller scene's empty slot at the chrome's last measured size so the
 *    content pane doesn't jump while the chrome lives in the other scene.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
class NavSuiteSceneDecoratorStrategy<T : Any> internal constructor(
  private val sharedTransitionScope: SharedTransitionScope,
  private val navigationSuiteType: () -> NavigationSuiteType,
  private val chromeContent: @Composable () -> Unit,
  private val upBarContent: @Composable () -> Unit,
  private val loneDeepLinkChrome: () -> LoneDeepLinkChrome,
) : SceneDecoratorStrategy<T> {
  override fun SceneDecoratorStrategyScope<T>.decorateScene(scene: Scene<T>): Scene<T> {
    if (!scene.metadata.showsNavBar()) return scene
    return when (loneDeepLinkChrome()) {
      LoneDeepLinkChrome.ShowSuite -> {
        NavSuiteScene(
          scene = scene,
          sharedTransitionScope = sharedTransitionScope,
          navigationSuiteType = navigationSuiteType,
          chromeContent = chromeContent,
        )
      }

      LoneDeepLinkChrome.ShowUpBar -> {
        NavUpBarScene(scene = scene, upBarContent = upBarContent)
      }

      LoneDeepLinkChrome.ShowNothing -> {
        scene
      }
    }
  }

  companion object {
    /**
     * Metadata marker that opts a destination into the global navigation bar/rail. Attach it to a
     * scene's entry metadata (`entry<Key>(metadata = NavSuiteSceneDecoratorStrategy.showNavBar())`)
     * to have [NavSuiteSceneDecoratorStrategy] wrap it with the chrome.
     */
    fun showNavBar(): Map<String, Any> = metadata { put(ShowNavBarKey, Unit) }
  }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun <T : Any> rememberNavSuiteSceneDecoratorStrategy(
  sharedTransitionScope: SharedTransitionScope,
  navigationSuiteType: () -> NavigationSuiteType,
  chromeContent: @Composable () -> Unit,
  upBarContent: @Composable () -> Unit,
  loneDeepLinkChrome: () -> LoneDeepLinkChrome,
): NavSuiteSceneDecoratorStrategy<T> {
  // movableContentOf wants a single stable lambda; rememberUpdatedState lets the captured chrome
  // composable change (new callbacks each recomposition) without rebuilding the movable wrapper.
  val currentChrome by rememberUpdatedState(chromeContent)
  val currentUpBar by rememberUpdatedState(upBarContent)
  val currentType by rememberUpdatedState(navigationSuiteType)
  val currentLoneChrome by rememberUpdatedState(loneDeepLinkChrome)
  val movableChrome = remember { movableContentOf { currentChrome() } }
  val movableUpBar = remember { movableContentOf { currentUpBar() } }
  return remember(sharedTransitionScope) {
    NavSuiteSceneDecoratorStrategy(
      sharedTransitionScope = sharedTransitionScope,
      navigationSuiteType = { currentType() },
      chromeContent = movableChrome,
      upBarContent = movableUpBar,
      loneDeepLinkChrome = { currentLoneChrome() },
    )
  }
}

@OptIn(ExperimentalSharedTransitionApi::class)
// Key-based equality: two NavSuiteScene wrappers are the same scene if they wrap a scene of the
// same class and the same key, regardless of NavEntry.content lambda identity. A data class default
// would delegate to the wrapped scene's equals, which in turn compares NavEntry references. Because
// NavEntry.content is a lambda created fresh each recomposition, two logically-identical entries
// can differ by identity, making the wrapping scene look "new" mid-transition and triggering the
// SaveableStateProvider "Key used multiple times" crash (b/516312097). NavSuiteScene uses the
// same equals/hashCode logic as NavUpBarScene below.
internal class NavSuiteScene<T : Any>(
  val scene: Scene<T>,
  val sharedTransitionScope: SharedTransitionScope,
  val navigationSuiteType: () -> NavigationSuiteType,
  val chromeContent: @Composable () -> Unit,
) : Scene<T> by scene {
  override val key: Any = scene::class to scene.key

  override val content: @Composable () -> Unit = {
    val animatedContentScope = LocalNavAnimatedContentScope.current
    // Exactly one active scene has targetState == Visible (the incoming/settled destination); it
    // hosts the single movable chrome. The leaving scene leaves its slot empty at the cached size.
    val isChromeCaller = animatedContentScope.transition.targetState == EnterExitState.Visible
    with(sharedTransitionScope) {
      val chromeSlot = @Composable {
        Box(
          Modifier
            .cacheSize(useCachedSize = !isChromeCaller)
            .sharedElement(rememberSharedContentState(NavSuiteSharedKey), animatedContentScope),
        ) {
          if (isChromeCaller) chromeContent()
        }
      }
      when (navigationSuiteType()) {
        NavigationSuiteType.NavigationBar -> Column(Modifier.fillMaxSize()) {
          Box(
            Modifier
              .weight(1f)
              .fillMaxWidth()
              .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
          ) {
            scene.content()
          }
          chromeSlot()
        }

        else -> Row(Modifier.fillMaxSize()) {
          chromeSlot()
          Box(
            Modifier
              .weight(1f)
              .fillMaxHeight()
              .consumeWindowInsets(
                WindowInsets.systemBars
                  .union(WindowInsets.displayCutout)
                  .only(WindowInsetsSides.Left),
              ),
          ) {
            scene.content()
          }
        }
      }
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is NavSuiteScene<*>) return false
    return key == other.key
  }

  override fun hashCode(): Int = key.hashCode()
}

// Key-based equality: two NavUpBarScene wrappers are the same scene if they wrap a scene of the
// same class and the same key, regardless of NavEntry.content lambda identity. See the comment on
// NavSuiteScene above for the full rationale (b/516312097). NavSuiteScene uses identical
// equals/hashCode logic; the only reason it is not tested directly is that its constructor requires
// a SharedTransitionScope which is hard to build outside a Compose host. Coverage by the
// identical-logic argument is provided there; direct test coverage lives in
// NavUpBarSceneEqualityTest.
internal class NavUpBarScene<T : Any>(
  val scene: Scene<T>,
  val upBarContent: @Composable () -> Unit,
) : Scene<T> by scene {
  override val key: Any = scene::class to scene.key

  override val content: @Composable () -> Unit = {
    Column(Modifier.fillMaxSize()) {
      upBarContent()
      Box(
        Modifier
          .weight(1f)
          .fillMaxWidth()
          .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
      ) {
        scene.content()
      }
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is NavUpBarScene<*>) return false
    return key == other.key
  }

  override fun hashCode(): Int = key.hashCode()
}

/**
 * A standalone [SceneDecoratorStrategy] that wraps nav-bar-opted-in scenes with an up bar, without
 * needing a [SharedTransitionScope]. Equivalent to the [LoneDeepLinkChrome.ShowUpBar] path inside
 * [NavSuiteSceneDecoratorStrategy], extracted so tests and callers that only need the up-bar
 * decoration can use it without standing up the full suite chrome.
 */
fun <T : Any> navUpBarSceneDecoratorStrategy(upBarContent: @Composable () -> Unit = {}): SceneDecoratorStrategy<T> =
  SceneDecoratorStrategy { scene ->
    if (!scene.metadata.showsNavBar()) scene else NavUpBarScene(scene = scene, upBarContent = upBarContent)
  }

private const val NavSuiteSharedKey = "nav-suite-chrome"

private object ShowNavBarKey : NavMetadataKey<Unit>

private fun Map<String, Any>.showsNavBar(): Boolean = ShowNavBarKey in this

/**
 * Caches the measured size of the chrome slot so the non-hosting scene reserves it. The hosting
 * scene (the one that actually calls the movable chrome) measures the real chrome and caches that
 * size; the leaving scene leaves the slot empty (the movable content lives in the other scene) and
 * reuses its last cached size, so the content pane doesn't jump while the chrome is "absent" from it.
 */
private fun Modifier.cacheSize(useCachedSize: Boolean): Modifier = this then CacheSizeElement(useCachedSize)

private data class CacheSizeElement(
  val useCachedSize: Boolean,
) : ModifierNodeElement<CacheSizeNode>() {
  override fun create(): CacheSizeNode = CacheSizeNode(useCachedSize)

  override fun update(node: CacheSizeNode) {
    node.useCachedSize = useCachedSize
  }
}

private class CacheSizeNode(
  useCachedSize: Boolean,
) : Modifier.Node(), LayoutModifierNode {
  var useCachedSize: Boolean = useCachedSize
    set(value) {
      if (field != value) {
        field = value
        invalidateMeasurement()
      }
    }

  private var cachedSize: IntSize? = null

  override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
    val placeable = measurable.measure(constraints)
    val measured = IntSize(placeable.width, placeable.height)
    val size = if (useCachedSize) cachedSize ?: measured else measured
    cachedSize = size
    return layout(size.width, size.height) { placeable.place(0, 0) }
  }
}
