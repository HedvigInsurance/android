package com.hedvig.android.feature.partner.claim.details.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.plus
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBar
import com.hedvig.android.design.system.hedvig.TopAppBarActionType.BACK
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.InfoFilled
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.show
import com.hedvig.android.ui.claimstatus.ClaimDisplayItemsSection
import com.hedvig.android.ui.claimstatus.ClaimExplanationBottomSheet
import com.hedvig.android.ui.claimstatus.ClaimStatusCard
import com.hedvig.android.ui.claimstatus.ClaimTermsConditionsCard
import hedvig.resources.CLAIMS_YOUR_CLAIM
import hedvig.resources.REFERRALS_INFO_BUTTON_CONTENT_DESCRIPTION
import hedvig.resources.Res
import hedvig.resources.claim_status_being_handled_support_text
import hedvig.resources.claim_status_claim_details_title
import hedvig.resources.claim_status_detail_documents_title
import octopus.type.ClaimStatus
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PartnerClaimDetailsDestination(
  viewModel: PartnerClaimDetailsViewModel,
  navigateUp: () -> Unit,
  openUrl: (String) -> Unit,
) {
  val viewState by viewModel.uiState.collectAsStateWithLifecycle()
  PartnerClaimDetailScreen(
    uiState = viewState,
    navigateUp = navigateUp,
    openUrl = openUrl,
    retry = { viewModel.emit(PartnerClaimDetailEvent.Retry) },
  )
}

@Composable
private fun PartnerClaimDetailScreen(
  uiState: PartnerClaimDetailUiState,
  navigateUp: () -> Unit,
  openUrl: (String) -> Unit,
  retry: () -> Unit,
) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column(Modifier.fillMaxSize()) {
      TopAppBar(
        title = stringResource(Res.string.CLAIMS_YOUR_CLAIM),
        actionType = BACK,
        onActionClick = navigateUp,
      )
      when (uiState) {
        is PartnerClaimDetailUiState.Content -> {
          PartnerClaimDetailContent(
            uiState = uiState,
            openUrl = openUrl,
          )
        }

        PartnerClaimDetailUiState.Error -> {
          Spacer(Modifier.weight(1f))
          HedvigErrorSection(onButtonClick = retry)
          Spacer(Modifier.weight(1f))
        }

        PartnerClaimDetailUiState.Loading -> {
          HedvigFullScreenCenterAlignedProgressDebounced()
        }
      }
    }
  }
}

@Composable
private fun PartnerClaimDetailContent(
  uiState: PartnerClaimDetailUiState.Content,
  openUrl: (String) -> Unit,
) {
  val explanationBottomSheetState = rememberHedvigBottomSheetState<Unit>()
  ClaimExplanationBottomSheet(explanationBottomSheetState)
  Column(
    Modifier
      .padding(
        PaddingValues(horizontal = 16.dp) + WindowInsets.safeDrawing
          .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
          .asPaddingValues(),
      )
      .verticalScroll(rememberScrollState()),
  ) {
    Spacer(Modifier.height(8.dp))
    ClaimStatusCard(uiState = uiState.claimStatusCardUiState)
    if (uiState.claimStatus == ClaimStatus.IN_PROGRESS) {
      Spacer(Modifier.height(8.dp))
      HedvigCard {
        Column(modifier = Modifier.padding(16.dp)) {
          HedvigText(
            text = stringResource(Res.string.claim_status_being_handled_support_text),
            style = HedvigTheme.typography.bodySmall,
          )
        }
      }
    }
    Spacer(Modifier.height(24.dp))
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          HedvigText(
            stringResource(Res.string.claim_status_claim_details_title),
            Modifier.padding(horizontal = 2.dp),
          )
        }
      },
      endSlot = {
        Row(horizontalArrangement = Arrangement.End) {
          IconButton(
            onClick = { explanationBottomSheetState.show(Unit) },
            modifier = Modifier.size(40.dp),
          ) {
            Icon(
              imageVector = HedvigIcons.InfoFilled,
              contentDescription = stringResource(Res.string.REFERRALS_INFO_BUTTON_CONTENT_DESCRIPTION),
              modifier = Modifier.size(24.dp),
            )
          }
        }
      },
      spaceBetween = 8.dp,
    )
    Spacer(Modifier.height(8.dp))
    ClaimDisplayItemsSection(
      displayItems = uiState.displayItems,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 2.dp),
    )
    if (uiState.handlerEmail != null) {
      Spacer(Modifier.height(24.dp))
      HedvigCard {
        HorizontalItemsWithMaximumSpaceTaken(
          modifier = Modifier
            .clip(HedvigTheme.shapes.cornerXSmall)
            .clickable { openUrl("mailto:${uiState.handlerEmail}") }
            .padding(16.dp),
          startSlot = {
            HedvigText(
              text = uiState.handlerEmail,
              style = HedvigTheme.typography.bodySmall,
              modifier = Modifier.wrapContentSize(Alignment.CenterStart),
            )
          },
          endSlot = {
            Icon(
              imageVector = HedvigIcons.ArrowNorthEast,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
          },
          spaceBetween = 8.dp,
        )
      }
    }
    if (uiState.termsConditionsUrl != null) {
      Spacer(Modifier.height(24.dp))
      HedvigText(
        stringResource(Res.string.claim_status_detail_documents_title),
        Modifier.padding(horizontal = 2.dp),
      )
      Spacer(Modifier.height(8.dp))
      ClaimTermsConditionsCard(
        onClick = { openUrl(uiState.termsConditionsUrl) },
        isLoading = false,
        modifier = Modifier.padding(16.dp),
      )
    }
    Spacer(Modifier.height(16.dp))
  }
}
