package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import com.hedvig.android.design.system.hedvig.tokens.TableTokens

@Immutable
data class TableStyle internal constructor(
  val containerColor: Color,
  val borderColor: Color,
  val shadowColor: Color,
  val textStyle: TextStyle,
  private val cellContainerColor: Color,
  private val cellContentColor: Color,
  private val cellInactiveContentColor: Color,
  private val selectedCellContainerColor: Color,
  private val selectedCellContentColor: Color,
  private val selectedCellInactiveContentColor: Color,
  private val selectedCellTopShape: Shape,
  private val selectedCellShape: Shape,
  private val selectedCellBottomShape: Shape,
) {
  @Composable
  fun cellTextColor(isSelected: Boolean): Color {
    return if (isSelected) {
      selectedCellContentColor
    } else {
      cellContentColor
    }
  }

  fun cellContentColor(isCovered: Boolean, isSelected: Boolean): Color {
    return when (isSelected) {
      true -> if (isCovered) selectedCellContentColor else selectedCellInactiveContentColor
      false -> if (isCovered) cellContentColor else cellInactiveContentColor
    }
  }

  fun cellContainerColor(isSelected: Boolean): Color {
    return if (isSelected) {
      selectedCellContainerColor
    } else {
      cellContainerColor
    }
  }

  fun selectedCellShape(isFirst: Boolean = false, isLast: Boolean = false): Shape {
    return if (isFirst) {
      selectedCellTopShape
    } else if (isLast) {
      selectedCellBottomShape
    } else {
      selectedCellShape
    }
  }

  companion object {
    @Composable
    operator fun invoke(): TableStyle {
      return TableStyle(
        containerColor = TableTokens.ContainerColor.value,
        borderColor = TableTokens.BorderColor.value,
        shadowColor = TableTokens.ShadowColor.value,
        textStyle = TableTokens.TextFont.value,
        cellContainerColor = TableTokens.CellContainerColor.value,
        cellContentColor = TableTokens.CellContentColor.value,
        cellInactiveContentColor = TableTokens.CellInactiveContentColor.value,
        selectedCellContainerColor = TableTokens.SelectedCellContainerColor.value,
        selectedCellContentColor = TableTokens.SelectedCellContentColor.value,
        selectedCellInactiveContentColor = TableTokens.SelectedCellInactiveContentColor.value,
        selectedCellTopShape = TableTokens.SelectedCellTopShape.value,
        selectedCellShape = TableTokens.SelectedCellShape.value,
        selectedCellBottomShape = TableTokens.SelectedCellBottomShape.value,
      )
    }
  }
}
