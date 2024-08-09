package com.hedvig.android.sample.design.showcase.stepper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
      var showError by remember { mutableStateOf(false) }
      val onPlusClick: () -> Unit = {
        showError = false
        if (quantity < 6) {
          quantity += 1
        } else {
          showError = true
        }
      }
      val onMinusClick: () -> Unit = {
        showError = false
        if (quantity <= 0) {
          showError = true
        } else {
          quantity -= 1
        }
      }
      Column(Modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(120.dp))
        StepperPreviewWithParameters(
          text = "Large",
          stepperStyle = Default,
          size = Large,
          quantity = quantity,
          showError = showError,
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
          showError = showError,
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
          showError = showError,
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
          showError = showError,
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
          showError = showError,
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
          showError = showError,
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
  showError: Boolean,
  isPlusEnabled: Boolean,
  isMinusEnabled: Boolean,
) {
  HedvigStepper(
    showError = showError,
    onPlusClick = onPlusClick,
    onMinusClick = onMinusClick,
    text = "$text, quantity: $quantity",
    stepperStyle = stepperStyle,
    stepperSize = size,
    isPlusEnabled = isPlusEnabled,
    isMinusEnabled = isMinusEnabled,
    errorText = "That would be out of bounds",
  )
}
