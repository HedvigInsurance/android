package com.hedvig.app.feature.embark.passages.selectaction.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.selectaction.SelectActionParameter
import com.hedvig.app.ui.compose.composables.CenteredContentWithTopBadge

@Composable
fun SelectActionView(
  selectActions: List<SelectActionParameter.SelectAction>,
  onActionClick: (SelectActionParameter.SelectAction, Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  SelectActionGrid(
    modifier = modifier,
    contentPadding = PaddingValues(16.dp),
    insideGridSpace = InsideGridSpace(8.dp),
  ) {
    selectActions.forEachIndexed { index, selectAction ->
      SelectActionCard(
        text = selectAction.label,
        badge = selectAction.badge,
        onClick = {
          onActionClick(selectAction, index)
        },
      )
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SelectActionCard(
  text: String,
  badge: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    onClick = onClick,
    modifier = modifier.heightIn(min = dimensionResource(R.dimen.embark_select_action_min_height)),
  ) {
    CenteredContentWithTopBadge(
      modifier = Modifier.padding(dimensionResource(R.dimen.base_margin)),
      centeredContent = {
        Text(
          text = text,
          style = MaterialTheme.typography.subtitle2,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(vertical = 8.dp),
        )
      },
      topContent = badge?.let {
        { BadgeText(badge) }
      },
    )
  }
}

@Composable
private fun BadgeText(badge: String) {
  CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
    Text(
      text = badge,
      style = MaterialTheme.typography.caption,
      textAlign = TextAlign.Center,
    )
  }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SelectActionViewPreview(
  @PreviewParameter(SelectActionsCollection::class) selectActions: List<Pair<String, String?>>,
) {
  HedvigTheme {
    Surface(
      color = MaterialTheme.colors.background,
    ) {
      SelectActionView(
        selectActions = selectActions.map { (text, badge) ->
          SelectActionParameter.SelectAction(
            link = "",
            label = text,
            keys = emptyList(),
            values = emptyList(),
            badge = badge,
          )
        },
        onActionClick = { _, _ -> },
      )
    }
  }
}

class SelectActionsCollection : CollectionPreviewParameterProvider<List<Pair<String, String?>>>(
  List(3) { listSize ->
    List(listSize + 1) { index ->
      if (index % 2 == 0) {
        "Text#$index".repeat(10 * (index + 1)) to "badge#$index"
      } else {
        "Badgeless#$index".repeat(4 * (index + 1)) to null
      }
    }
  },
)
