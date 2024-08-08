package com.hedvig.android.design.system.hedvig

import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperSize.Large
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperStyle.Default

object StepperDefaults {
  internal val stepperStyle: StepperStyle = Default
  internal val stepperSize: StepperSize = Large

  sealed class StepperStyle {
    data object Default : StepperStyle()
    data class Labeled(val labelText: String) : StepperStyle()
  }

  enum class StepperSize {
    Large,
    Medium,
    Small,
  }
}
