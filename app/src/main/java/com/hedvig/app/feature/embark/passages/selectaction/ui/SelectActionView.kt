package com.hedvig.app.feature.embark.passages.selectaction.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.selectaction.SelectActionGrid
import com.hedvig.app.feature.embark.passages.selectaction.SelectActionParameter
import com.hedvig.app.ui.compose.theme.HedvigTheme

@Composable
fun SelectActionView(
    selectActions: List<SelectActionParameter.SelectAction>,
    onActionClick: (SelectActionParameter.SelectAction, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    SelectActionGrid(
        modifier = modifier.wrapContentHeight(),
        contentPadding = PaddingValues(8.dp),
        insideGridSpace = InsideGridSpace(8.dp)
    ) {
        selectActions.forEachIndexed { index, selectAction ->
            SelectActionCard(
                text = selectAction.label,
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
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .requiredHeightIn(min = dimensionResource(R.dimen.embark_select_action_min_height))
            .fillMaxWidth()
    ) {
        Box(Modifier.padding(dimensionResource(R.dimen.base_margin))) {
            Text(
                text = text,
                style = MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }
    }
}

@Preview
@Composable
fun SelectActionViewPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            SelectActionView(
                selectActions = List(3) { index ->
                    SelectActionParameter.SelectAction(
                        link = "",
                        label = "Index: $index",
                        keys = emptyList(),
                        values = emptyList(),
                    )
                },
                onActionClick = { _, _ -> }
            )
        }
    }
}
