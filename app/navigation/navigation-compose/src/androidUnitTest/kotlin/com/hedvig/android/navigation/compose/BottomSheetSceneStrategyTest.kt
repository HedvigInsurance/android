package com.hedvig.android.navigation.compose

import androidx.compose.foundation.shape.RectangleShape
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.contains
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.SceneStrategyScope
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BottomSheetSceneStrategyTest {
  private fun strategy() = BottomSheetSceneStrategy<String>(
    containerColor = Color.White,
    contentColor = Color.Black,
    scrimColor = Color.Black,
    shape = RectangleShape,
    dragHandle = {},
  )

  private fun entry(key: String, metadata: Map<String, Any> = emptyMap()) =
    NavEntry(key = key, metadata = metadata) { }

  @Test
  fun `bottomSheet metadata is recognized as a bottom-sheet marker`() {
    assertTrue(BottomSheetSceneStrategy.bottomSheet().keys.isNotEmpty())
  }

  @Test
  fun `calculateScene returns null when the top entry has no bottomSheet metadata`() {
    val entries = listOf(entry("a"), entry("b"))
    val scene = with(strategy()) { with(SceneStrategyScope<String>()) { calculateScene(entries) } }
    assertNull(scene)
  }

  @Test
  fun `calculateScene returns an overlay scene that overlays the entries below the sheet`() {
    val entries = listOf(entry("a"), entry("b", BottomSheetSceneStrategy.bottomSheet()))
    val scene = with(strategy()) { with(SceneStrategyScope<String>()) { calculateScene(entries) } }
    val overlay = assertIs<OverlayScene<String>>(scene)
    assertTrue(overlay.overlaidEntries.map { it.contentKey } == listOf<Any>("a"))
    assertTrue(overlay.entries.map { it.contentKey } == listOf<Any>("b"))
  }
}
