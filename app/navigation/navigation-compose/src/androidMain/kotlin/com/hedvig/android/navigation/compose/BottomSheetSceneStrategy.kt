package com.hedvig.android.navigation.compose

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.hedvig.android.design.system.hedvig.HedvigOverlayBottomSheet
import com.hedvig.android.design.system.hedvig.api.HedvigOverlaySheetController
import com.hedvig.android.design.system.hedvig.rememberHedvigOverlaySheetController

/**
 * A [SceneStrategy] that renders any entry whose metadata carries [bottomSheet] as a Hedvig bottom
 * sheet overlay, on top of the entry beneath it. Modeled on the framework `AnimatedBottomSheetSample`
 * and the metadata-opt-in pattern of [NavSuiteSceneDecoratorStrategy].
 *
 * Must be placed before non-overlay strategies in `NavDisplay(sceneStrategies = ...)`.
 */
class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {
  override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
    val entry = entries.lastOrNull() ?: return null
    if (BottomSheetKey !in entry.metadata) return null
    return BottomSheetScene(
      key = entry.contentKey,
      entry = entry,
      previousEntries = entries.dropLast(1),
      onBack = onBack,
    )
  }

  companion object {
    /** Attach to a destination's entry metadata to render it as a bottom sheet overlay. */
    fun bottomSheet(): Map<String, Any> = metadata { put(BottomSheetKey, Unit) }
  }
}

internal object BottomSheetKey : NavMetadataKey<Unit>

private class BottomSheetScene<T : Any>(
  override val key: Any,
  private val entry: NavEntry<T>,
  override val previousEntries: List<NavEntry<T>>,
  private val onBack: () -> Unit,
) : OverlayScene<T> {
  override val entries: List<NavEntry<T>> = listOf(entry)
  override val overlaidEntries: List<NavEntry<T>> = previousEntries

  private var sheetController: HedvigOverlaySheetController? = null

  override val content: @Composable () -> Unit = {
    val controller = rememberHedvigOverlaySheetController()
    sheetController = controller
    HedvigOverlayBottomSheet(
      controller = controller,
      onDismissRequest = dropUnlessResumed { onBack() },
    ) {
      entry.Content()
    }
  }

  // Run the hide animation before the overlay leaves composition when popped.
  override suspend fun onRemove() {
    sheetController?.hide()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BottomSheetScene<*>) return false
    return key == other.key &&
      previousEntries.map { it.contentKey } == other.previousEntries.map { it.contentKey } &&
      entry.contentKey == other.entry.contentKey
  }

  override fun hashCode(): Int {
    var result = key.hashCode()
    result = 31 * result + previousEntries.map { it.contentKey }.hashCode()
    result = 31 * result + entry.contentKey.hashCode()
    return result
  }
}
