package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle
import com.hedvig.android.design.system.hedvig.DropdownItem.DropdownItemWithIcon
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem

@Composable
fun DropdownWithDialog(
  style: DropdownStyle,
  size: DropdownDefaults.DropdownSize,
  hintText: String,
  modifier: Modifier = Modifier,
  chosenItemIndex: Int? = null,
  isEnabled: Boolean = true,
  hasError: Boolean = false,
  errorText: String? = null
) {

}

object DropdownDefaults {

  sealed class DropdownSize {
    protected abstract val padding: PaddingValues
    protected abstract val shape: Shape
    protected abstract val textStyle: TextStyle

    data object Large : DropdownSize() {
      override val padding: PaddingValues
        get() = TODO("Not yet implemented")
      override val shape: Shape
        get() = TODO("Not yet implemented")
      override val textStyle: TextStyle
        get() = TODO("Not yet implemented")
    }

    data object Medium : DropdownSize() {
      override val padding: PaddingValues
        get() = TODO("Not yet implemented")
      override val shape: Shape
        get() = TODO("Not yet implemented")
      override val textStyle: TextStyle
        get() = TODO("Not yet implemented")
    }

    data object Small : DropdownSize() {
      override val padding: PaddingValues
        get() = TODO("Not yet implemented")
      override val shape: Shape
        get() = TODO("Not yet implemented")
      override val textStyle: TextStyle
        get() = TODO("Not yet implemented")
    }
  }

  sealed class DropdownStyle {
    data class Default(val items: List<SimpleDropdownItem>): DropdownStyle()
    data class Icon(val items: List<DropdownItemWithIcon>, val defaultIcon: IconResource): DropdownStyle()
    data class Label(val items: List<SimpleDropdownItem>, val label: String): DropdownStyle()
  }
}

sealed class DropdownItem {
  abstract val text: String
  abstract val enabled: Boolean

  data class SimpleDropdownItem(
    override val text: String,
    override val enabled: Boolean = true) : DropdownItem()

  data class DropdownItemWithIcon(
    override val text: String,
    val iconResource: IconResource,
    override val enabled: Boolean = true
  ) : DropdownItem()
}
