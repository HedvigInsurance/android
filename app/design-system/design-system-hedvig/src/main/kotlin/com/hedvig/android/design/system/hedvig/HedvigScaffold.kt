package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.TopAppBarDefaults.windowInsets
import com.hedvig.android.design.system.hedvig.icon.ArrowLeft
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
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
    modifier = modifier.fillMaxSize(),
  ) {
    val connection = remember {
      object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
          return super.onPreScroll(available, source)
        }
      }
    }
    Column {
      TopAppBar(
        title = topAppBarText ?: "",
        onClick = navigateUp,
        actionType = topAppBarActionType,
        topAppBarActions = topAppBarActions,
      )
      Column(
        horizontalAlignment = itemsColumnHorizontalAlignment,
        modifier = Modifier
          .fillMaxSize()
          .nestedScroll(connection)
          .verticalScroll(rememberScrollState())
          .windowInsetsPadding(
            WindowInsets.safeDrawing.only(
              WindowInsetsSides.Horizontal +
                WindowInsetsSides.Bottom,
            ),
          ),
      ) {
        content()
      }
    }
  }
}

@Composable
fun TopAppBarLayoutForActions(modifier: Modifier = Modifier, actions: @Composable RowScope.() -> Unit = {}) {
  Row(
    horizontalArrangement = Arrangement.End,
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .windowInsetsPadding(windowInsets)
      .height(ScaffoldTokens.TopAppBarHeight)
      .fillMaxWidth()
      .padding(horizontal = ScaffoldTokens.TopAppBarHorizontalPadding),
  ) {
    actions()
  }
}

@Composable
fun TopAppBarWithBack(
  title: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) {
  TopAppBar(
    title = title,
    onClick = onClick,
    actionType = TopAppBarActionType.BACK,
    modifier = modifier,
    windowInsets = windowInsets,
  )
}

enum class TopAppBarActionType {
  BACK,
  CLOSE,
}

internal object TopAppBarDefaults {
  val windowInsets: WindowInsets
    @Composable
    get() = WindowInsets.systemBars
      .union(WindowInsets.displayCutout)
      .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
}

@Composable
fun TopAppBar(
  title: String,
  onClick: () -> Unit,
  actionType: TopAppBarActionType,
  modifier: Modifier = Modifier,
  topAppBarActions: @Composable (RowScope.() -> Unit)? = null,
  windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier =
      modifier
        .windowInsetsPadding(windowInsets)
        .height(ScaffoldTokens.TopAppBarHeight)
        .fillMaxWidth()
        .padding(horizontal = ScaffoldTokens.TopAppBarHorizontalPadding),
  ) {
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Row(
          horizontalArrangement = Arrangement.Start,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(
            onClick = { onClick() },
            modifier = Modifier.size(24.dp),
            content = {
              Icon(
                imageVector = when (actionType) {
                  TopAppBarActionType.BACK -> HedvigIcons.ArrowLeft
                  TopAppBarActionType.CLOSE -> HedvigIcons.Close
                },
                contentDescription = null,
              )
            },
          )
          Spacer(Modifier.width(8.dp))
          HedvigText(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
            style = ScaffoldTokens.TopAppBarTextStyle.value,
          )
        }
      },
      endSlot = {
        if (topAppBarActions != null) {
          Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            topAppBarActions()
          }
        }
      },
      spaceBetween = 8.dp,
    )
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
