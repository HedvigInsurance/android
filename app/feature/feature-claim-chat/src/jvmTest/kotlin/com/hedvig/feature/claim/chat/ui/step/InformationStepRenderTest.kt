package com.hedvig.feature.claim.chat.ui.step

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.use
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.feature.claim.chat.data.StepContent
import java.io.File
import kotlin.test.Test
import org.jetbrains.skia.EncodedImageFormat

/**
 * Renders [InformationStep] headlessly on the desktop target and writes reference PNGs to
 * build/renders/. AGP's screenshot-test plugin cannot do this here - it has no support for
 * the com.android.kotlin.multiplatform.library target - so this is the module's rendered
 * evidence that the step draws correctly.
 */
class InformationStepRenderTest {
  @Test
  fun `renders the information step for both severities`() {
    for (severity in StepContent.Information.Severity.entries) {
      val png = ImageComposeScene(width = 1080, height = 720, density = Density(2.75f)).use { scene ->
        scene.setContent {
          HedvigTheme {
            Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
              InformationStep(
                information = StepContent.Information(
                  notice = "Since your home is currently uninhabitable and you have nowhere to stay, please " +
                    "contact us immediately or seek temporary emergency accommodation.",
                  severity = severity,
                  buttonTitle = "I understand",
                ),
                isCurrentStep = true,
                continueButtonLoading = false,
                onAcknowledge = {},
                modifier = Modifier.padding(16.dp),
              )
            }
          }
        }
        scene.render().encodeToData(EncodedImageFormat.PNG)!!.bytes
      }
      val file = File("build/renders/InformationStep_${severity.name}.png")
      file.parentFile.mkdirs()
      file.writeBytes(png)
    }
  }
}
