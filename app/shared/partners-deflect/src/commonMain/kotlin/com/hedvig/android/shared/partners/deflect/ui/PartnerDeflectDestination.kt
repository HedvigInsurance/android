package com.hedvig.android.shared.partners.deflect.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.AccordionData
import com.hedvig.android.design.system.hedvig.AccordionList
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBar
import com.hedvig.android.design.system.hedvig.TopAppBarActionType
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.shared.partners.deflect.DeflectData
import hedvig.resources.DASHBOARD_OPEN_CHAT
import hedvig.resources.Res
import hedvig.resources.SUBMIT_CLAIM_GLOBAL_ASSISTANCE_CALL_LABEL
import hedvig.resources.SUBMIT_CLAIM_NEED_HELP_LABEL
import hedvig.resources.SUBMIT_CLAIM_NEED_HELP_TITLE
import hedvig.resources.general_back_button
import org.jetbrains.compose.resources.stringResource
import com.hedvig.android.design.system.hedvig.HedvigPreview

@Composable
fun PartnerDeflectDestination(
  deflect: DeflectData,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
  navigateUp: () -> Unit,
  openUrl: (String) -> Unit,
  tryToDialPhone: (String) -> Unit,
  onNavigateToNewConversation: () -> Unit,
) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = modifier,
  ) {
    Column {
      val topAppbarInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
      TopAppBar(
        title = deflect.title ?: "",
        actionType = TopAppBarActionType.BACK,
        onActionClick = dropUnlessResumed(block = navigateUp),
        topAppBarActions = null,
        windowInsets = topAppbarInsets,
        customTopAppBarColors = null,
      )
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .consumeWindowInsets(topAppbarInsets.only(WindowInsetsSides.Top))
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
      ) {
        when (deflect.partnersContainer) {
          is DeflectData.DeflectPartnerContainer.ExtendedPartnerContainer -> ExtendedPartnersScreen(
            infoText = deflect.infoText,
            warningText = deflect.warningText,
            partners = deflect.partnersContainer.partners,
            contentTitle = deflect.content.title,
            contentDescription = deflect.content.description,
            faq = deflect.faq,
            onNavigateToNewConversation = onNavigateToNewConversation,
            imageLoader = imageLoader,
            openUrl = openUrl,
            tryToDialPhone = tryToDialPhone,
          )

          is DeflectData.DeflectPartnerContainer.SimplePartnerContainer -> SimplePartnersScreen(
            contentTitle = deflect.content.title,
            contentDescription = deflect.content.description,
            openUrl = openUrl,
            partner = deflect.partnersContainer.partners.first(),
            infoText = deflect.infoText,
            warningText = deflect.warningText,
          )

          null -> HedvigErrorSection(
            onButtonClick = navigateUp,
            buttonText = stringResource(Res.string.general_back_button),
            modifier = Modifier
              .fillMaxSize(),
          )
        }
      }
    }
  }
}

@Composable
private fun ColumnScope.ExtendedPartnersScreen(
  infoText: String?,
  warningText: String?,
  partners: List<DeflectData.DeflectPartnerContainer.ExtendedPartner>,
  contentTitle: String,
  contentDescription: String,
  faq: List<DeflectData.InfoBlock>,
  imageLoader: ImageLoader,
  onNavigateToNewConversation: () -> Unit,
  openUrl: (String) -> Unit,
  tryToDialPhone: (String) -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Spacer(Modifier.height(8.dp))
    if (infoText != null) {
      HedvigNotificationCard(
        message = infoText,
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        style = NotificationDefaults.InfoCardStyle.Default,
        priority = NotificationDefaults.NotificationPriority.Attention,
      )
      Spacer(Modifier.height(8.dp))
    }
    if (warningText != null) {
      HedvigNotificationCard(
        message = warningText,
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        style = NotificationDefaults.InfoCardStyle.Default,
        priority = NotificationDefaults.NotificationPriority.Attention,
      )
      Spacer(Modifier.height(8.dp))
    }
    partners.forEach { partner ->
      HedvigTheme(darkTheme = true) {
        Surface(
          color = HedvigTheme.colorScheme.backgroundPrimary.copy(
            0.95f,
          ).compositeOver(HedvigTheme.colorScheme.fillWhite),
          contentColor = HedvigTheme.colorScheme.fillPrimary,
          shape = HedvigTheme.shapes.cornerXLarge,
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        ) {
          Column(Modifier.padding(16.dp)) {
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
              if (partner.imageUrl != null) {
                AsyncImage(
                  model = partner.imageUrl,
                  contentDescription = null,
                  imageLoader = imageLoader,
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                )
              }
              Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (partner.title != null) {
                  HedvigText(
                    text = partner.title,
                    textAlign = TextAlign.Center,
                    style = HedvigTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                  )
                }
                if (partner.description != null) {
                  HedvigText(
                    text = partner.description,
                    textAlign = TextAlign.Center,
                    color = HedvigTheme.colorScheme.textSecondary,
                    style = HedvigTheme.typography.bodySmall.copy(
                      lineBreak = LineBreak.Heading,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                  )
                }
              }
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (partner.url != null) {
                  HedvigButton(
                    buttonSize = ButtonDefaults.ButtonSize.Medium,
                    enabled = true,
                    text = partner.urlButtonTitle ?: partner.url,
                    onClick = {
                      openUrl(partner.url)
                    },
                    modifier = Modifier.fillMaxWidth(),
                  )
                }
                if (partner.phoneNumber != null) {
                  val style = if (partner.url != null) {
                    ButtonDefaults.ButtonStyle.Ghost
                  } else {
                    ButtonDefaults.ButtonStyle.Primary
                  }
                  HedvigButton(
                    buttonStyle = style,
                    buttonSize = ButtonDefaults.ButtonSize.Medium,
                    text = stringResource(
                      Res.string.SUBMIT_CLAIM_GLOBAL_ASSISTANCE_CALL_LABEL,
                      partner.phoneNumber,
                    ),
                    enabled = true,
                    onClick = {
                      tryToDialPhone(partner.phoneNumber)
                    },
                    modifier = Modifier.fillMaxWidth(),
                  )
                }
              }
            }
            if (partner.info != null) {
              Spacer(Modifier.height(16.dp))
              HedvigText(
                text = partner.info,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = HedvigTheme.colorScheme.textSecondary,
                style = HedvigTheme.typography.finePrint,
              )
            }
          }
        }
      }
      Spacer(Modifier.height(8.dp))
    }

    // TODO: partnersInfo section when design exists

    SelectionContainer {
      Column {
        Spacer(Modifier.height(16.dp))
        HedvigText(
          text = contentTitle,
          modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        HedvigText(
          text = contentDescription,
          modifier = Modifier.padding(horizontal = 16.dp),
          style = HedvigTheme.typography.bodySmall,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
    }

    if (faq.isNotEmpty()) {
      Spacer(Modifier.height(24.dp))
      HorizontalDivider(Modifier.padding(horizontal = 16.dp))
      Spacer(Modifier.height(24.dp))
      QuestionsAndAnswers(
        faqList = faq,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
    Spacer(Modifier.height(52.dp))
    Column(
      Modifier
        .fillMaxWidth()
        .background(color = HedvigTheme.colorScheme.surfacePrimary),
    ) {
      Spacer(Modifier.height(32.dp))
      HedvigText(
        text = stringResource(Res.string.SUBMIT_CLAIM_NEED_HELP_TITLE),
        textAlign = TextAlign.Center,
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth(),
      )
      HedvigText(
        text = stringResource(Res.string.SUBMIT_CLAIM_NEED_HELP_LABEL),
        textAlign = TextAlign.Center,
        color = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth(),
      )
      Spacer(Modifier.height(24.dp))
      HedvigButton(
        text = stringResource(Res.string.DASHBOARD_OPEN_CHAT),
        onClick = dropUnlessResumed { onNavigateToNewConversation() },
        buttonSize = ButtonDefaults.ButtonSize.Medium,
        enabled = true,
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth()
          .wrapContentWidth(Alignment.CenterHorizontally),
      )
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
  }
}

@Composable
private fun ColumnScope.SimplePartnersScreen(
  infoText: String?,
  warningText: String?,
  contentTitle: String,
  contentDescription: String,
  openUrl: (String) -> Unit,
  partner: DeflectData.DeflectPartnerContainer.SimplePartner,
) {
  if (infoText != null) {
    Spacer(Modifier.height(8.dp))
    HedvigNotificationCard(
      message = infoText,
      modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
      style = NotificationDefaults.InfoCardStyle.Default,
      priority = NotificationDefaults.NotificationPriority.Attention,
    )
    Spacer(Modifier.height(8.dp))
  }
  if (warningText != null) {
    HedvigNotificationCard(
      message = warningText,
      modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
      style = NotificationDefaults.InfoCardStyle.Default,
      priority = NotificationDefaults.NotificationPriority.Attention,
    )
    Spacer(Modifier.height(8.dp))
  }
  HedvigText(
    text = contentTitle,
    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
  )
  Spacer(Modifier.height(8.dp))
  HedvigText(
    text = contentDescription,
    modifier = Modifier.padding(horizontal = 16.dp),
    style = HedvigTheme.typography.bodySmall,
    color = HedvigTheme.colorScheme.textSecondary,
  )
  Spacer(Modifier.weight(1f))
  if (partner.url != null) {
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      enabled = true,
      modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
      onClick = dropUnlessResumed {
        openUrl(partner.url)
      },
    ) {
      partner.urlButtonTitle?.let {
        HedvigText(it)
        Spacer(Modifier.width(8.dp))
      }
      Icon(HedvigIcons.ArrowNorthEast, EmptyContentDescription)
    }
  }
  Spacer(Modifier.height(16.dp))
  Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
}

@Composable
private fun QuestionsAndAnswers(faqList: List<DeflectData.InfoBlock>, modifier: Modifier = Modifier) {
  Column(modifier) {
    AccordionList(
      faqList.map { faq ->
        AccordionData(title = faq.title, description = faq.description)
      },
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimOutcomeDeflectDestination() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PartnerDeflectDestination(
        deflect = DeflectData(
          title = "title",
          infoText = "infoText",
          warningText = "warningText",
          partnersContainer = null,
          partnersInfo = DeflectData.InfoBlock("title", "description"),
          content = DeflectData.InfoBlock("title", "description"),
          faq = emptyList(),
          buttonText = "See our partners",
        ),
        imageLoader = rememberPreviewImageLoader(),
        modifier = Modifier,
        navigateUp = {},
        openUrl = {},
        tryToDialPhone = {},
        onNavigateToNewConversation = {},
      )
    }
  }
}
