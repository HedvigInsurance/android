package com.hedvig.android.app.navigation

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import androidx.navigation3.scene.SceneStrategyScope
import androidx.navigation3.scene.SinglePaneSceneStrategy
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.hedvig.android.navigation.compose.NavSuiteSceneDecoratorStrategy
import com.hedvig.android.navigation.compose.navUpBarSceneDecoratorStrategy
import org.junit.Test

/**
 * Regression test for b/516312097: NavUpBarScene.equals must compare by key, not by the wrapped
 * Scene's full equality (which in turn compares NavEntry.content lambda identity).
 *
 * Root cause: the old `data class NavUpBarScene` delegated equals to its constructor parameters.
 * The wrapped `SinglePaneScene` compares `entry == other.entry`, and NavEntry equality is identity
 * on the `content: @Composable () -> Unit` lambda. Between recompositions Nav3 creates fresh
 * NavEntry instances for the same logical destination: same `contentKey`, different `content`
 * lambda. That made two logically-identical NavUpBarScene wrappers unequal, causing the
 * NavDisplay's AnimatedContent to treat them as different scenes mid-transition and triggering
 * the SaveableStateProvider "Key used multiple times" crash.
 *
 * NavUpBarScene is `internal` to :navigation-compose; this test reaches it via the public
 * [navUpBarSceneDecoratorStrategy] factory, which exercises the same code path that
 * [NavSuiteSceneDecoratorStrategy] uses for the ShowUpBar mode.
 */
internal class NavUpBarSceneEqualityTest {
  private fun singlePaneStrategy() = SinglePaneSceneStrategy<String>()

  // Non-capturing `{ }` lambdas are folded by the compiler into a singleton, so entries built
  // that way would share one content reference and would NOT reproduce the bug. Capturing entries
  // guarantee distinct content lambda instances, which is the actual production scenario Nav3
  // creates between recompositions.
  private fun capturingEntry(key: String, capture: String) =
    NavEntry(key = key, metadata = NavSuiteSceneDecoratorStrategy.showNavBar()) { capture.length }

  @Test
  fun `NavUpBarScene wrappers for same-key entries with distinct content lambdas are equal`() {
    fun buildWrapped(capture: String) = with(navUpBarSceneDecoratorStrategy<String>()) {
      with(SceneDecoratorStrategyScope<String>()) {
        val singlePane = with(singlePaneStrategy()) {
          with(SceneStrategyScope<String>()) {
            calculateScene(listOf(capturingEntry(key = "dest", capture = capture)))
          }
        }!!
        decorateScene(singlePane)
      }
    }

    // Pre-condition: two SinglePaneScenes with the same contentKey but different content lambdas
    // are NOT equal to each other. This documents the root cause: any scene wrapper that delegated
    // equals to the wrapped scene would inherit this inequality and produce a spurious "new scene"
    // mid-transition.
    val rawFirst = with(singlePaneStrategy()) {
      with(SceneStrategyScope<String>()) {
        calculateScene(listOf(capturingEntry(key = "dest", capture = "alpha")))
      }
    }!!
    val rawSecond = with(singlePaneStrategy()) {
      with(SceneStrategyScope<String>()) {
        calculateScene(listOf(capturingEntry(key = "dest", capture = "beta")))
      }
    }!!
    assertThat(rawFirst == rawSecond).isFalse()

    // After the fix: two NavUpBarScene wrappers for the same contentKey must be equal, regardless
    // of the content lambda identity of the underlying entries.
    val first = buildWrapped("alpha")
    val second = buildWrapped("beta")
    assertThat(first == second).isTrue()
    assertThat(first.hashCode() == second.hashCode()).isTrue()
  }
}
