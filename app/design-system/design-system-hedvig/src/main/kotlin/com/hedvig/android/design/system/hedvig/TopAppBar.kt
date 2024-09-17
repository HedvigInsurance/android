package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.TopAppBarDefaults.windowInsets
import com.hedvig.android.design.system.hedvig.icon.ArrowLeft
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.tokens.TopAppBarTokens

@Composable
fun TopAppBar(
  actionType: TopAppBarActionType,
  onActionClick: () -> Unit,
  modifier: Modifier = Modifier,
  windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
  content: @Composable () -> Unit,
) {
  Surface(
    color = TopAppBarTokens.ContainerColor.value,
    contentColor = TopAppBarTokens.ContentColor.value,
    modifier = modifier.fillMaxWidth(),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = modifier
        .windowInsetsPadding(windowInsets)
        .height(TopAppBarTokens.ContainerHeight),
      // .padding(horizontal = TopAppBarTokens.ContentHorizontalPadding),
      // todo removed here @Stelios
    ) {
      IconButton(
        onClick = onActionClick,
        content = {
          Icon(
            imageVector = when (actionType) {
              TopAppBarActionType.BACK -> HedvigIcons.ArrowLeft
              TopAppBarActionType.CLOSE -> HedvigIcons.Close
            },
            contentDescription = null,
            modifier = Modifier.size(24.dp),
          )
        },
      )
      Spacer(Modifier.width(8.dp))
      CompositionLocalProvider(LocalTextStyle provides TopAppBarTokens.TextStyle.value) {
        Box(
          Modifier
            .weight(1f)
            .fillMaxHeight(),
          propagateMinConstraints = true,
        ) {
          content()
        }
      }
    }
  }
}

@Composable
fun TopAppBar(
  title: String,
  actionType: TopAppBarActionType,
  onActionClick: () -> Unit,
  modifier: Modifier = Modifier,
  topAppBarActions: @Composable (RowScope.() -> Unit)? = null,
  windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) {
  Surface(
    color = TopAppBarTokens.ContainerColor.value,
    contentColor = TopAppBarTokens.ContentColor.value,
    modifier = modifier
      .windowInsetsPadding(windowInsets)
      .fillMaxWidth()
      .height(TopAppBarTokens.ContainerHeight),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .padding(horizontal = TopAppBarTokens.ContentHorizontalPadding),
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            IconButton(
              onClick = onActionClick,
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
              style = TopAppBarTokens.TextStyle.value,
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
}

@Composable
internal fun TopAppBarLayoutForActions(modifier: Modifier = Modifier, actions: @Composable RowScope.() -> Unit = {}) {
  Row(
    horizontalArrangement = Arrangement.End,
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .windowInsetsPadding(windowInsets)
      .height(TopAppBarTokens.ContainerHeight)
      .fillMaxWidth()
      .padding(horizontal = TopAppBarTokens.ContentHorizontalPadding),
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
    actionType = TopAppBarActionType.BACK,
    onActionClick = onClick,
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
