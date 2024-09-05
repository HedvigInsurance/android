package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun ClickableList(
  items: List<ClickableItem>,
  size: ClickableListDefaults.Size,
  style: ClickableListDefaults.Style,
  modifier: Modifier = Modifier,
) {
  val verticalArrangement = when (style) {
    ClickableListDefaults.Style.Default -> Arrangement.Top
    ClickableListDefaults.Style.Filled -> Arrangement.spacedBy(4.dp)
  }
  Column(
    modifier = modifier,
    verticalArrangement = verticalArrangement,
  ) {
    when (style) {
      ClickableListDefaults.Style.Default -> TODO()
      ClickableListDefaults.Style.Filled -> TODO()
    }
  }
}

object ClickableListDefaults {
  sealed class Size {
    internal abstract val shape: Shape
    internal abstract val padding: PaddingValues

    data object Small : Size() {
      override val shape: Shape
        get() = TODO("Not yet implemented")
      override val padding: PaddingValues
        get() = TODO("Not yet implemented")
    }

    data object Medium : Size() {
      override val shape: Shape
        get() = TODO("Not yet implemented")
      override val padding: PaddingValues
        get() = TODO("Not yet implemented")
    }

    data object Large : Size() {
      override val shape: Shape
        get() = TODO("Not yet implemented")
      override val padding: PaddingValues
        get() = TODO("Not yet implemented")
    }
  }

  sealed class Style {
    data object Default : Style()

    data object Filled : Style()
  }
}

data class ClickableItem(
  val text: String,
  val onClick: () -> Unit,
)

private data class ClickableListColor(
  private val defaultContainerColor: Color,
  private val filledContainerColor: Color,
  val dividerColor: Color,
  val textColor: Color,
  val iconColor: Color,
) {
  fun containerColor(style: ClickableListDefaults.Style): Color {
    return when (style) {
      ClickableListDefaults.Style.Default -> defaultContainerColor
      ClickableListDefaults.Style.Filled -> filledContainerColor
    }
  }
}

private val listColors: ClickableListColor
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      ClickableListColor(
        defaultContainerColor = TODO(),
        filledContainerColor = TODO(),
        dividerColor = TODO(),
        textColor = TODO(),
        iconColor = TODO(),
      )
    }
  }
