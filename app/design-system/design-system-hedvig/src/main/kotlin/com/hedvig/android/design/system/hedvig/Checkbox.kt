package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.Composable
import com.hedvig.android.design.system.hedvig.CheckboxDefaults.CheckboxStyle.Default

@Composable
fun CheckBox() {

}

object CheckboxDefaults {
  internal val checkboxStyle: CheckboxStyle = Default
  internal val checkboxSize: CheckBoxSize = CheckBoxSize.Large

  sealed interface CheckboxStyle {
    data object Default : CheckboxStyle

    data class Label(val labelText: String) : CheckboxStyle

    data class Icon(val iconResource: IconResource) : CheckboxStyle

    data object LeftAligned : CheckboxStyle
  }

  enum class CheckBoxSize {
    Large,
    Medium,
    Small,
  }
}
