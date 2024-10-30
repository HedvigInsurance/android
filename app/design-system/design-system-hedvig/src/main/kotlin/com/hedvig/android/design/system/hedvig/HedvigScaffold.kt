package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
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
  content: @Composable ColumnScope.() -> Unit,
) {
  Surface(
    color = scaffoldColors.background,
    modifier = modifier,
  ) {
    Column {
      TopAppBar(
        title = topAppBarText ?: "",
        actionType = topAppBarActionType,
        onActionClick = dropUnlessResumed(block = navigateUp),
        topAppBarActions = topAppBarActions,
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
      )
      Column(
        horizontalAlignment = itemsColumnHorizontalAlignment,
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .windowInsetsPadding(
            // todo remove this bottom insets padding from Scaffold, as it forces the callers to clip the bottom bar
            //  insets if they happen to want to have their own scrollable state
            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
          ),
      ) {
        content()
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
