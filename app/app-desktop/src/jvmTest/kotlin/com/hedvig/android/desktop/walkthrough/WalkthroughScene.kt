package com.hedvig.android.desktop.walkthrough

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.Density
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
import java.awt.EventQueue
import java.io.File
import java.lang.reflect.InvocationTargetException
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

/** The pixel size, density and timing every capture uses, so two runs produce comparable images. */
internal data class SceneSpec(
  val widthDp: Int = 412,
  val heightDp: Int = 915,
  val density: Float = 2f,
  /** How long the scene must have no pending invalidation to count as idle. */
  val quietMillis: Long = 1_500,
  /** How long the visible semantics must stay unchanged, with no spinner, to count as settled while animating. */
  val settledMillis: Long = 3_000,
  val timeoutMillis: Long = 20_000,
  val frameMillis: Long = 16,
)

/** How waiting for a screen ended. */
internal enum class Settle {
  /** Nothing was invalidated for [SceneSpec.quietMillis]. */
  Idle,

  /** The screen kept animating, but its visible content stayed unchanged for [SceneSpec.settledMillis]. */
  Settled,

  TimedOut,
}

internal data class SettleResult(val settle: Settle, val millis: Long, val renderFailure: Throwable?)

/** A visible node of the semantics tree, flattened. */
internal data class VisibleNode(
  val text: String,
  val role: Role?,
  val bounds: Rect,
  val isIndeterminateProgress: Boolean,
  val isSelected: Boolean,
  val testTag: String?,
)

/**
 * Renders [content] off-screen into a raster image, on the AWT event thread, where `Dispatchers.Main` (Swing) also
 * runs the app's coroutines. Frames are rendered with real time, so network calls and delays behave as in the
 * running app. Provides the lifecycle, ViewModel store, saved state and navigation event owners a desktop window
 * provides, with the lifecycle RESUMED.
 */
internal class WalkthroughScene(
  private val spec: SceneSpec,
  private val content: @Composable () -> Unit,
) : AutoCloseable {
  private val owner = onEdt { WindowOwner().also { it.lifecycle.currentState = Lifecycle.State.RESUMED } }
  private val startNanos = System.nanoTime()
  private var lastImage: Image? = null

  private val scene: ImageComposeScene = onEdt {
    ImageComposeScene(
      width = (spec.widthDp * spec.density).toInt(),
      height = (spec.heightDp * spec.density).toInt(),
      density = Density(spec.density),
      coroutineContext = kotlinx.coroutines.Dispatchers.Main,
    ) {
      CompositionLocalProvider(
        LocalLifecycleOwner provides owner,
        LocalViewModelStoreOwner provides owner,
        LocalSavedStateRegistryOwner provides owner,
        LocalNavigationEventDispatcherOwner provides owner,
      ) {
        content()
      }
    }
  }

  /** Renders one frame. A failure while composing, measuring or drawing is thrown. */
  fun renderFrame(): Image = onEdt {
    val image = scene.render(System.nanoTime() - startNanos)
    lastImage?.close()
    lastImage = image
    image
  }

  /**
   * Renders frames until the scene is idle or settled, or [SceneSpec.timeoutMillis] passes. A render failure ends
   * the wait and is returned.
   */
  fun settle(minimumMillis: Long = 300): SettleResult {
    val start = System.currentTimeMillis()
    var quietSince: Long? = null
    var fingerprint: String? = null
    var fingerprintSince = start
    while (true) {
      val now = System.currentTimeMillis()
      val elapsed = now - start
      try {
        renderFrame()
      } catch (t: Throwable) {
        return SettleResult(Settle.TimedOut, elapsed, t)
      }
      val (hasInvalidations, nodes) = onEdt { scene.hasInvalidations() to visibleNodes() }
      quietSince = if (hasInvalidations) null else (quietSince ?: now)
      val newFingerprint = nodes.joinToString("|") { "${it.text}@${it.bounds}" }
      if (newFingerprint != fingerprint) {
        fingerprint = newFingerprint
        fingerprintSince = now
      }
      if (elapsed >= minimumMillis) {
        if (quietSince != null && now - quietSince >= spec.quietMillis) return SettleResult(Settle.Idle, elapsed, null)
        val hasSpinner = nodes.any { it.isIndeterminateProgress }
        if (!hasSpinner && now - fingerprintSince >= spec.settledMillis) {
          return SettleResult(Settle.Settled, elapsed, null)
        }
      }
      if (elapsed >= spec.timeoutMillis) return SettleResult(Settle.TimedOut, elapsed, null)
      Thread.sleep(spec.frameMillis)
    }
  }

  /** Writes the last rendered frame as a PNG, rendering one first when none exists. */
  fun capture(file: File) {
    val image = lastImage ?: renderFrame()
    val bytes = onEdt { image.encodeToData(EncodedImageFormat.PNG)?.bytes }
      ?: error("Could not encode a PNG for ${file.name}")
    file.parentFile.mkdirs()
    file.writeBytes(bytes)
  }

  /** Every node of the merged semantics tree that has text, a role, or is a progress indicator. */
  fun visibleNodes(): List<VisibleNode> = onEdt {
    val result = mutableListOf<VisibleNode>()

    fun visit(node: SemanticsNode) {
      val config = node.config
      val text = buildList {
        config.getOrNull(SemanticsProperties.ContentDescription)?.let { addAll(it) }
        config.getOrNull(SemanticsProperties.Text)?.let { texts -> addAll(texts.map { it.text }) }
        config.getOrNull(SemanticsProperties.EditableText)?.let { add(it.text) }
      }.joinToString(" ")
      val progress = config.getOrNull(SemanticsProperties.ProgressBarRangeInfo)
      val role = config.getOrNull(SemanticsProperties.Role)
      val testTag = config.getOrNull(SemanticsProperties.TestTag)
      if (text.isNotBlank() || role != null || progress != null || testTag != null) {
        result += VisibleNode(
          text = text,
          role = role,
          bounds = node.boundsInRoot,
          isIndeterminateProgress = progress == ProgressBarRangeInfo.Indeterminate,
          isSelected = config.getOrNull(SemanticsProperties.Selected) == true,
          testTag = testTag,
        )
      }
      node.children.forEach(::visit)
    }
    scene.semanticsOwners.forEach { visit(it.rootSemanticsNode) }
    result
  }

  /** Clicks the centre of [bounds] (pixels) with the primary mouse button. */
  fun click(bounds: Rect) {
    val position = Offset(bounds.center.x, bounds.center.y)
    onEdt {
      scene.sendPointerEvent(PointerEventType.Move, position)
      scene.sendPointerEvent(PointerEventType.Press, position)
      scene.sendPointerEvent(PointerEventType.Release, position)
    }
  }

  /** Presses the centre of [bounds] (pixels) for [millis] while rendering frames, then releases. */
  fun longPress(bounds: Rect, millis: Long = 1_000) {
    val position = Offset(bounds.center.x, bounds.center.y)
    onEdt {
      scene.sendPointerEvent(PointerEventType.Move, position)
      scene.sendPointerEvent(PointerEventType.Press, position)
    }
    val end = System.currentTimeMillis() + millis
    while (System.currentTimeMillis() < end) {
      renderFrame()
      Thread.sleep(spec.frameMillis)
    }
    onEdt { scene.sendPointerEvent(PointerEventType.Release, position) }
  }

  override fun close() {
    onEdt {
      runCatching { scene.close() }
      lastImage?.close()
      lastImage = null
      owner.lifecycle.currentState = Lifecycle.State.DESTROYED
      owner.viewModelStore.clear()
    }
  }
}

/** What a desktop window provides to its content: a lifecycle, a ViewModel store, saved state and back events. */
private class WindowOwner :
  LifecycleOwner,
  ViewModelStoreOwner,
  SavedStateRegistryOwner,
  NavigationEventDispatcherOwner {
  override val lifecycle: LifecycleRegistry = LifecycleRegistry.createUnsafe(this)
  override val viewModelStore: ViewModelStore = ViewModelStore()
  override val navigationEventDispatcher: NavigationEventDispatcher = NavigationEventDispatcher()
  private val savedStateRegistryController = SavedStateRegistryController.create(this).apply {
    performAttach()
    performRestore(null)
  }
  override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
}

/** Runs [block] on the AWT event thread and returns its result, rethrowing what it throws. */
internal fun <T> onEdt(block: () -> T): T {
  if (EventQueue.isDispatchThread()) return block()
  var result: Result<T>? = null
  try {
    EventQueue.invokeAndWait { result = runCatching(block) }
  } catch (e: InvocationTargetException) {
    throw e.targetException
  }
  return result!!.getOrThrow()
}
