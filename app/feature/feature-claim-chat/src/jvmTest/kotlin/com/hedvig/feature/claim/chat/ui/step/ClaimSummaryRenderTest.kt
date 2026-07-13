package com.hedvig.feature.claim.chat.ui.step

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.feature.claim.chat.data.StepContent
import java.io.File
import kotlin.test.Test
import org.jetbrains.skia.EncodedImageFormat

class ClaimSummaryRenderTest {
  @Test
  fun renderCollapsedClaimDetailsCard() {
    renderToPng("claim-summary-collapsed-card", width = 1080, height = 1500) {
      Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
        Column(Modifier.padding(16.dp)) {
          ChatClaimSummaryTopContent(
            keyDetails = listOf(
              StepContent.Summary.Item("Type of claim", "Theft"),
              StepContent.Summary.Item("Date", "2026-07-13"),
              StepContent.Summary.Item("Location", "Stockholm"),
            ),
            answers = previewAnswers(),
            recordingUrls = listOf(""),
            fileUploads = listOf(
              UiFile("receipt.pdf", null, "https://example.com/receipt.pdf", "application/pdf", "file-1"),
            ),
            imageLoader = rememberPreviewImageLoader(),
            onNavigateToImageViewer = { _, _ -> },
          )
        }
      }
    }
  }

  @Test
  fun renderExpandedAnswersContent() {
    renderToPng("claim-summary-expanded-answers", width = 1080, height = 1200) {
      Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
        ClaimSummaryAnswersContent(
          answers = previewAnswers(),
          imageLoader = rememberPreviewImageLoader(),
          onNavigateToImageViewer = { _, _ -> },
          modifier = Modifier.padding(16.dp),
        )
      }
    }
  }

  private fun renderToPng(
    name: String,
    width: Int,
    height: Int,
    content: @Composable () -> Unit,
  ) {
    val scene = ImageComposeScene(
      width = width,
      height = height,
      density = Density(3f),
    )
    val bytes = try {
      scene.setContent {
        HedvigTheme {
          content()
        }
      }
      scene.render().encodeToData(EncodedImageFormat.PNG)!!.bytes
    } finally {
      scene.close()
    }
    val outDir = File("build/renders").apply { mkdirs() }
    val outFile = File(outDir, "$name.png")
    outFile.writeBytes(bytes)
    println("RENDERED_PNG: ${outFile.absolutePath} (${bytes.size} bytes)")
  }
}
