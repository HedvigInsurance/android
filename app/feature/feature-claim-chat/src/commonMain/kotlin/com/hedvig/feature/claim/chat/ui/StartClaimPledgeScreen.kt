package com.hedvig.feature.claim.chat.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.StartClaimPledgeScreen
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBar
import com.hedvig.android.design.system.hedvig.TopAppBarActionType

@Composable
internal fun StartClaimPledgeDestination(
  navigateUp: () -> Unit,
  navigateToClaimChat: () -> Unit
) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
  ) {
    Column {
      val topAppbarInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
      TopAppBar(
        title = "TODO", //todo
        actionType = TopAppBarActionType.BACK,
        onActionClick = dropUnlessResumed(block = navigateUp),
        topAppBarActions = null,
        windowInsets = topAppbarInsets,
        customTopAppBarColors = null,
      )
      StartClaimPledgeScreen(
        navigateUp = navigateUp,
        navigateToClaimChat = navigateToClaimChat,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
    }
  }
}
