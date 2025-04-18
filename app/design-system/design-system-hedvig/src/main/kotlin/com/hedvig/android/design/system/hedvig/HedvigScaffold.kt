package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.tokens.ScaffoldTokens

@Composable
fun HedvigScaffold(
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
  topAppBarText: String? = null,
  topAppBarActionType: TopAppBarActionType = TopAppBarActionType.BACK,
  itemsColumnHorizontalAlignment: Alignment.Horizontal = Alignment.Start,
  topAppBarActions: @Composable RowScope.() -> Unit = {},
  customTopAppBarColors: TopAppBarColors? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  Surface(
    color = scaffoldColors.background,
    modifier = modifier,
  ) {
    Column {
      val topAppbarInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
      TopAppBar(
        title = topAppBarText ?: "",
        actionType = topAppBarActionType,
        onActionClick = dropUnlessResumed(block = navigateUp),
        topAppBarActions = topAppBarActions,
        windowInsets = topAppbarInsets,
        customTopAppBarColors = customTopAppBarColors,
      )
      Column(
        horizontalAlignment = itemsColumnHorizontalAlignment,
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .consumeWindowInsets(topAppbarInsets.only(WindowInsetsSides.Top))
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
      ) {
        content()
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
      }
    }
  }
}

private data class ScaffoldColors(
  val background: Color,
)

private val scaffoldColors: ScaffoldColors
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      ScaffoldColors(
        background = fromToken(ScaffoldTokens.BackgroundColor),
      )
    }
  }
