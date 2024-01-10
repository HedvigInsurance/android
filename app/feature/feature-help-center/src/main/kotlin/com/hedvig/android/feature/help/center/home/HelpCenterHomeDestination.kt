package com.hedvig.android.feature.help.center.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.material3.infoContainer
import com.hedvig.android.core.designsystem.material3.onInfoContainer
import com.hedvig.android.core.designsystem.material3.onTypeContainer
import com.hedvig.android.core.designsystem.material3.onYellowContainer
import com.hedvig.android.core.designsystem.material3.typeContainer
import com.hedvig.android.core.designsystem.material3.yellowContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.grid.HedvigGrid
import com.hedvig.android.core.ui.grid.InsideGridSpace
import com.hedvig.android.feature.help.center.model.Question
import com.hedvig.android.feature.help.center.model.QuickLink
import com.hedvig.android.feature.help.center.model.Topic
import com.hedvig.android.feature.help.center.model.commonQuestions
import com.hedvig.android.feature.help.center.model.commonTopics
import com.hedvig.android.feature.help.center.model.quickLinks
import com.hedvig.android.feature.help.center.ui.HelpCenterSection
import com.hedvig.android.feature.help.center.ui.HelpCenterSectionWithClickableRows
import com.hedvig.android.navigation.core.AppDestination
import hedvig.resources.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun HelpCenterHomeDestination(
  onNavigateToTopic: (topic: Topic) -> Unit,
  onNavigateToQuestion: (question: Question) -> Unit,
  onNavigateToQuickLink: (AppDestination) -> Unit,
  onNavigateUp: () -> Unit,
) {
  HelpCenterHomeScreen(
    topics = commonTopics,
    questions = commonQuestions,
    quickLinks = quickLinks,
    onNavigateToTopic = onNavigateToTopic,
    onNavigateToQuestion = onNavigateToQuestion,
    onNavigateToQuickLink = onNavigateToQuickLink,
    onNavigateUp = onNavigateUp,
  )
}

@Composable
private fun HelpCenterHomeScreen(
  topics: ImmutableList<Topic>,
  questions: ImmutableList<Question>,
  quickLinks: ImmutableList<QuickLink>,
  onNavigateToTopic: (topic: Topic) -> Unit,
  onNavigateToQuestion: (question: Question) -> Unit,
  onNavigateToQuickLink: (AppDestination) -> Unit,
  onNavigateUp: () -> Unit,
) {
  Surface(color = MaterialTheme.colorScheme.background) {
    Column(Modifier.fillMaxSize()) {
      TopAppBarWithBack(
        title = stringResource(id = R.string.HC_TITLE),
        onClick = onNavigateUp,
      )
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState()),
      ) {
        Spacer(Modifier.height(50.dp))
        Image(
          painter = painterResource(id = R.drawable.pillow_hedvig),
          contentDescription = null,
          modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .size(170.dp)
            .align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(50.dp))
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .padding(horizontal = 20.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        ) {
          Text(stringResource(id = R.string.HC_HOME_VIEW_QUESTION))
          Text(
            text = stringResource(id = R.string.HC_HOME_VIEW_ANSWER),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Spacer(Modifier.height(40.dp))
        HelpCenterSection(
          title = stringResource(id = R.string.HC_QUICK_ACTIONS_TITLE),
          chipContainerColor = MaterialTheme.colorScheme.typeContainer,
          contentColor = MaterialTheme.colorScheme.onTypeContainer,
          content = {
            HedvigGrid(
              insideGridSpace = InsideGridSpace.Companion.invoke(8.dp),
              modifier = Modifier.padding(horizontal = 16.dp),
            ) {
              for (quickLink in quickLinks)
                HedvigCard(
                  onClick = { onNavigateToQuickLink(quickLink.destination) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
                ) {
                  Text(
                    text = stringResource(quickLink.titleRes),
                    Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                  )
                }
            }
          },
        )
        Spacer(Modifier.height(56.dp))
        HelpCenterSection(
          title = "Common topics",
          chipContainerColor = MaterialTheme.colorScheme.yellowContainer,
          contentColor = MaterialTheme.colorScheme.onYellowContainer,
          content = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              for (topic in topics) {
                HedvigCard(
                  onClick = { onNavigateToTopic(topic) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
                ) {
                  Text(stringResource(topic.titleRes), Modifier.padding(16.dp))
                }
              }
            }
          },
        )
        Spacer(Modifier.height(56.dp))
        LocalConfiguration.current
        val resources = LocalContext.current.resources
        HelpCenterSectionWithClickableRows(
          title = "Common questions",
          chipContainerColor = MaterialTheme.colorScheme.infoContainer,
          contentColor = MaterialTheme.colorScheme.onInfoContainer,
          items = questions,
          itemText = { resources.getString(it.questionRes) },
          onClickItem = { onNavigateToQuestion(it) },
        )
        Spacer(Modifier.height(24.dp))
        Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHelpCenterHomeScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HelpCenterHomeScreen(
        persistentListOf(Topic.PAYMENTS, Topic.PAYMENTS),
        persistentListOf(Question.CLAIMS_Q1, Question.CLAIMS_Q1),
        persistentListOf(QuickLink.UpdateAddress, QuickLink.ChangeBank, QuickLink.ChangeBank),
        {},
        {},
        {},
        {},
      )
    }
  }
}
