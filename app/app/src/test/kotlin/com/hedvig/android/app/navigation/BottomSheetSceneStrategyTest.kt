package com.hedvig.android.app.navigation

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
  private fun strategy() = BottomSheetSceneStrategy<String>()

  private fun entry(key: String, metadata: Map<String, Any> = emptyMap()) = NavEntry(key = key, metadata = metadata) { }

  private fun capturingEntry(key: String, capture: String, metadata: Map<String, Any> = emptyMap()) =
    NavEntry(key = key, metadata = metadata) { capture.length }

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
    val below = entry("a")
    val sheet = entry("b", BottomSheetSceneStrategy.bottomSheet())
    val entries = listOf(below, sheet)
    val scene = with(strategy()) { with(SceneStrategyScope<String>()) { calculateScene(entries) } }
    assertThat(scene is OverlayScene<String>).isTrue()
    val overlay = scene as OverlayScene<String>
    // Compare against the entries this test built rather than a literal, so the assertion stays about
    // which entry lands in which bucket and not about how navigation3 happens to derive contentKey.
    assertThat(overlay.overlaidEntries.map { it.contentKey } == listOf(below.contentKey)).isTrue()
    assertThat(overlay.entries.map { it.contentKey } == listOf(sheet.contentKey)).isTrue()
  }

  @Test
  fun `bottom-sheet scenes for the same content keys are equal despite distinct content lambdas`() {
    fun buildScene(capture: String) = with(strategy()) {
      with(SceneStrategyScope<String>()) {
        calculateScene(
          listOf(
            capturingEntry("a", capture),
            capturingEntry("b", capture, BottomSheetSceneStrategy.bottomSheet()),
          ),
        )
      }
    }
    val first = buildScene("first")!!
    val second = buildScene("second")!!
    assertThat(first == second).isTrue()
    assertThat(first.hashCode() == second.hashCode()).isTrue()
  }
}
