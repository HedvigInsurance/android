package com.hedvig.android.navigation.compose

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

/**
 * A [SceneStrategy] that renders any entry whose metadata carries [bottomSheet] inside a
 * [ModalBottomSheet], on top of the entry beneath it. Modeled on the framework
 * `AnimatedBottomSheetSample` and the metadata-opt-in pattern of [NavSuiteSceneDecoratorStrategy].
 *
 * Styling is passed in as plain values so this KMP module needs no dependency on `design-system`;
 * `:app` supplies the Hedvig tokens via `rememberHedvigBottomSheetSceneStrategy`.
 *
 * Must be placed before non-overlay strategies in `NavDisplay(sceneStrategies = ...)`.
 */
@OptIn(ExperimentalMaterial3Api::class)
class BottomSheetSceneStrategy<T : Any>(
  private val containerColor: Color,
  private val contentColor: Color,
  private val scrimColor: Color,
  private val shape: Shape,
  private val dragHandle: @Composable () -> Unit,
) : SceneStrategy<T> {
  override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
    val entry = entries.lastOrNull() ?: return null
    if (BottomSheetKey !in entry.metadata) return null
    return BottomSheetScene(
      key = entry.contentKey,
      entry = entry,
      previousEntries = entries.dropLast(1),
      onBack = onBack,
      containerColor = containerColor,
      contentColor = contentColor,
      scrimColor = scrimColor,
      shape = shape,
      dragHandle = dragHandle,
    )
  }

  companion object {
    /** Attach to a destination's entry metadata to render it as a bottom sheet overlay. */
    fun bottomSheet(): Map<String, Any> = metadata { put(BottomSheetKey, Unit) }
  }
}

internal object BottomSheetKey : NavMetadataKey<Unit>

@OptIn(ExperimentalMaterial3Api::class)
private class BottomSheetScene<T : Any>(
  override val key: Any,
  private val entry: NavEntry<T>,
  override val previousEntries: List<NavEntry<T>>,
  private val onBack: () -> Unit,
  private val containerColor: Color,
  private val contentColor: Color,
  private val scrimColor: Color,
  private val shape: Shape,
  private val dragHandle: @Composable () -> Unit,
) : OverlayScene<T> {
  override val entries: List<NavEntry<T>> = listOf(entry)
  override val overlaidEntries: List<NavEntry<T>> = previousEntries

  private lateinit var sheetState: SheetState

  override val content: @Composable () -> Unit = {
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = onBack,
      sheetState = sheetState,
      containerColor = containerColor,
      contentColor = contentColor,
      scrimColor = scrimColor,
      shape = shape,
      dragHandle = dragHandle,
    ) {
      entry.Content()
    }
  }

  // Run the hide animation before the overlay leaves composition when popped.
  override suspend fun onRemove() {
    if (::sheetState.isInitialized) sheetState.hide()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BottomSheetScene<*>) return false
    return key == other.key && previousEntries == other.previousEntries && entry == other.entry
  }

  override fun hashCode(): Int = key.hashCode() * 31 + previousEntries.hashCode() * 31 + entry.hashCode()
}
