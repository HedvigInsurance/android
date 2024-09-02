package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle
import com.hedvig.android.design.system.hedvig.DropdownItem.DropdownItemWithIcon
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem

@Composable
fun DropdownWithDialog(
  style: DropdownStyle,
  size: DropdownDefaults.DropdownSize,
  modifier: Modifier = Modifier,
  chosenItemIndex: Int? = null,
  hasError: Boolean = false,
  errorText: String? = null
) {

}

object DropdownDefaults {

  sealed class DropdownSize {
    protected abstract val padding: PaddingValues

    data object Large : DropdownSize() {
      override val padding: PaddingValues
        get() = TODO("Not yet implemented")
    }

    data object Medium : DropdownSize() {
      override val padding: PaddingValues
        get() = TODO("Not yet implemented")
    }

    data object Small : DropdownSize() {
      override val padding: PaddingValues
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

  data class SimpleDropdownItem(override val text: String) : DropdownItem()
  data class DropdownItemWithIcon(
    override val text: String,
    val iconResource: IconResource,
  ) : DropdownItem()
}
