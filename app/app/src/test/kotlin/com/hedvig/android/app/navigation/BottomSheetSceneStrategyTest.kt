package com.hedvig.android.app.navigation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.SceneStrategyScope
import assertk.assertThat
import assertk.assertions.isNotEmpty
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.hedvig.android.navigation.compose.BottomSheetSceneStrategy
import org.junit.Test

internal class BottomSheetSceneStrategyTest {
  private fun strategy() = BottomSheetSceneStrategy<String>(
    containerColor = Color.White,
    contentColor = Color.Black,
    scrimColor = Color.Black,
    shape = RectangleShape,
    dragHandle = {},
  )

  private fun entry(key: String, metadata: Map<String, Any> = emptyMap()) = NavEntry(key = key, metadata = metadata) { }

  @Test
  fun `bottomSheet metadata is recognized as a bottom-sheet marker`() {
    assertThat(BottomSheetSceneStrategy.bottomSheet().keys).isNotEmpty()
  }

  @Test
  fun `calculateScene returns null when the top entry has no bottomSheet metadata`() {
    val entries = listOf(entry("a"), entry("b"))
    val scene = with(strategy()) { with(SceneStrategyScope<String>()) { calculateScene(entries) } }
    assertThat(scene).isNull()
  }

  @Test
  fun `calculateScene returns an overlay scene that overlays the entries below the sheet`() {
    val entries = listOf(entry("a"), entry("b", BottomSheetSceneStrategy.bottomSheet()))
    val scene = with(strategy()) { with(SceneStrategyScope<String>()) { calculateScene(entries) } }
    assertThat(scene is OverlayScene<String>).isTrue()
    val overlay = scene as OverlayScene<String>
    assertThat(overlay.overlaidEntries.map { it.contentKey } == listOf<Any>("a")).isTrue()
    assertThat(overlay.entries.map { it.contentKey } == listOf<Any>("b")).isTrue()
  }
}
