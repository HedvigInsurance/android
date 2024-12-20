package com.hedvig.android.sample.design.showcase.stepper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigStepper
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.StepperDefaults
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperSize.Large
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperSize.Medium
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperSize.Small
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperStyle
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperStyle.Default
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperStyle.Labeled
import com.hedvig.android.design.system.hedvig.Surface

@Composable
fun StepperShowcase() {
  HedvigTheme {
    Surface(color = Color.White) {
      var quantity by remember { mutableIntStateOf(0) }
      var errorText: String? by remember { mutableStateOf(null) }
      val onPlusClick: () -> Unit = {
        errorText = null
        if (quantity < 6) {
          quantity += 1
        } else {
          errorText = "That would be out of bounds"
        }
      }
      val onMinusClick: () -> Unit = {
        errorText = null
        if (quantity <= 0) {
          errorText = "That would be out of bounds"
        } else {
          quantity -= 1
        }
      }
      Column(Modifier.safeContentPadding().padding(16.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(120.dp))
        StepperPreviewWithParameters(
          text = "Large",
          stepperStyle = Default,
          size = Large,
          quantity = quantity,
          errorText = errorText,
          onPlusClick = onPlusClick,
          onMinusClick = onMinusClick,
          isPlusEnabled = quantity <= 5,
          isMinusEnabled = quantity >= 1,
        )
        Spacer(Modifier.height(8.dp))
        StepperPreviewWithParameters(
          text = "Medium",
          stepperStyle = Default,
          size = Medium,
          quantity = quantity,
          errorText = errorText,
          onPlusClick = onPlusClick,
          onMinusClick = onMinusClick,
          isPlusEnabled = quantity <= 5,
          isMinusEnabled = quantity >= 1,
        )
        Spacer(Modifier.height(8.dp))
        StepperPreviewWithParameters(
          text = "Small",
          stepperStyle = Default,
          size = Small,
          quantity = quantity,
          errorText = errorText,
          onPlusClick = onPlusClick,
          onMinusClick = onMinusClick,
          isPlusEnabled = quantity <= 5,
          isMinusEnabled = quantity >= 1,
        )
        Spacer(Modifier.height(8.dp))
        StepperPreviewWithParameters(
          text = "Large",
          stepperStyle = Labeled("Label"),
          size = Large,
          quantity = quantity,
          errorText = errorText,
          onPlusClick = onPlusClick,
          isPlusEnabled = quantity <= 5,
          isMinusEnabled = quantity >= 1,
          onMinusClick = onMinusClick,
        )
        Spacer(Modifier.height(8.dp))
        StepperPreviewWithParameters(
          text = "Medium",
          stepperStyle = Labeled("Label"),
          size = Medium,
          quantity = quantity,
          errorText = errorText,
          isPlusEnabled = quantity <= 5,
          isMinusEnabled = quantity >= 1,
          onPlusClick = onPlusClick,
          onMinusClick = onMinusClick,
        )
        Spacer(Modifier.height(8.dp))
        StepperPreviewWithParameters(
          text = "Small",
          stepperStyle = Labeled("Label"),
          size = Small,
          quantity = quantity,
          errorText = errorText,
          isPlusEnabled = quantity <= 5,
          isMinusEnabled = quantity >= 1,
          onPlusClick = onPlusClick,
          onMinusClick = onMinusClick,
        )
        Spacer(Modifier.height(8.dp))
      }
    }
  }
}

@Composable
private fun StepperPreviewWithParameters(
  stepperStyle: StepperStyle,
  size: StepperDefaults.StepperSize,
  text: String,
  quantity: Int,
  onMinusClick: () -> Unit,
  onPlusClick: () -> Unit,
  errorText: String?,
  isPlusEnabled: Boolean,
  isMinusEnabled: Boolean,
) {
  HedvigStepper(
    onPlusClick = onPlusClick,
    onMinusClick = onMinusClick,
    text = "$text, quantity: $quantity",
    stepperStyle = stepperStyle,
    stepperSize = size,
    isPlusEnabled = isPlusEnabled,
    isMinusEnabled = isMinusEnabled,
    errorText = errorText,
  )
}
