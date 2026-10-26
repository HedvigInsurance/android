package com.hedvig.android.CHANGE_ME

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

/**
 * Throwaway renderer: writes one PNG per entry of [inputs], each stacking every entry of [variants].
 * Lives in the module's jvmTest so it can reach internal composables. Delete before committing.
 */
@OptIn(ExperimentalTestApi::class)
class RenderVariantsTemplate {
  /** File name to the value the variants are rendered with. */
  private val inputs = listOf(
    "two-lines" to "Jag låste cykeln med ett kedjelås vid cykelstället utanför Hemköp i Lund",
    "short" to "Stulen",
  )

  /** Label to the composable it renders. Replace with the thing being compared. */
  private val variants: List<Pair<String, @Composable (String) -> Unit>> = listOf(
    "A. Start-aligned" to { text -> HedvigText(text, textAlign = TextAlign.Start) },
    "B. End-aligned" to { text -> HedvigText(text, textAlign = TextAlign.End) },
  )

  @Test
  fun render() {
    val outDir = File("build/render").apply { mkdirs() }
    for ((name, input) in inputs) {
      // Pixels. Content below the bottom edge is clipped, so err on the tall side.
      runDesktopComposeUiTest(width = 940, height = 2000) {
        setContent {
          // Phone-like pixel density: 375dp of content becomes 940px.
          CompositionLocalProvider(LocalDensity provides Density(2.5f, 1f)) {
            HedvigTheme(darkTheme = false) {
              Box(Modifier.fillMaxSize().background(HedvigTheme.colorScheme.backgroundPrimary)) {
                Column(Modifier.width(375.dp).padding(16.dp)) {
                  for ((label, content) in variants) {
                    HedvigText(
                      label,
                      style = HedvigTheme.typography.label,
                      color = HedvigTheme.colorScheme.textSecondary,
                      modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    )
                    content(input)
                  }
                }
              }
            }
          }
        }
        waitForIdle()
        ImageIO.write(captureToImage().toAwtImage(), "png", File(outDir, "$name.png"))
      }
    }
  }
}
