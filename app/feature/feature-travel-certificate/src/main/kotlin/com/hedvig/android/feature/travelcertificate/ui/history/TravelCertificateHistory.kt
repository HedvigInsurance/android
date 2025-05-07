package com.hedvig.android.feature.travelcertificate.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import arrow.core.nonEmptyListOf
import com.hedvig.android.data.addons.data.TravelAddonBannerInfo
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.INFO
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.FeatureAddonBanner
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigTooltip
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TooltipDefaults.BeakDirection.TopEnd
import com.hedvig.android.design.system.hedvig.clearFocusOnTap
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigMonthDateTimeFormatter
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.InfoOutline
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.show
import com.hedvig.android.feature.travelcertificate.data.TravelCertificate
import com.hedvig.android.feature.travelcertificate.ui.TravelCertificateInfoBottomSheet
import com.hedvig.android.feature.travelcertificate.ui.history.CertificateHistoryUiState.FailureDownloadingHistory
import com.hedvig.android.feature.travelcertificate.ui.history.CertificateHistoryUiState.Loading
import com.hedvig.android.feature.travelcertificate.ui.history.CertificateHistoryUiState.SuccessDownloadingHistory
import hedvig.resources.R
import java.io.File
import kotlin.String
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun TravelCertificateHistoryDestination(
  viewModel: CertificateHistoryViewModel,
  onStartGenerateTravelCertificateFlow: () -> Unit,
  onNavigateToChooseContract: () -> Unit,
  onNavigateToAddonPurchaseFlow: (ids: List<String>) -> Unit,
  navigateUp: () -> Unit,
  onShareTravelCertificate: (File) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  TravelCertificateHistoryScreen(
    reload = { viewModel.emit(CertificateHistoryEvent.RetryLoadData) },
    onCertificateClick = { url ->
      viewModel.emit(CertificateHistoryEvent.DownloadCertificate(url))
    },
    onDismissDownloadCertificateError = {
      viewModel.emit(CertificateHistoryEvent.DismissDownloadCertificateError)
    },
    onStartGenerateTravelCertificateFlow = onStartGenerateTravelCertificateFlow,
    onGoToChooseContract = onNavigateToChooseContract,
    navigateUp = navigateUp,
    onShareTravelCertificate = onShareTravelCertificate,
    uiState = uiState,
    launchAddonPurchaseFlow = { ids ->
      viewModel.emit(CertificateHistoryEvent.LaunchAddonPurchaseFlow(ids))
    },
    onNavigateToAddonPurchaseFlow = { ids ->
      viewModel.emit(CertificateHistoryEvent.ClearNavigation)
      onNavigateToAddonPurchaseFlow(ids)
    },
  )
}

@Composable
private fun TravelCertificateHistoryScreen(
  reload: () -> Unit,
  onCertificateClick: (String) -> Unit,
  onStartGenerateTravelCertificateFlow: () -> Unit,
  launchAddonPurchaseFlow: (ids: List<String>) -> Unit,
  onNavigateToAddonPurchaseFlow: (ids: List<String>) -> Unit,
  onGoToChooseContract: () -> Unit,
  navigateUp: () -> Unit,
  onDismissDownloadCertificateError: () -> Unit,
  onShareTravelCertificate: (File) -> Unit,
  uiState: CertificateHistoryUiState,
) {
  val explanationSheetState = rememberHedvigBottomSheetState<Unit>()
  TravelCertificateInfoBottomSheet(explanationSheetState)

  when (uiState) {
    FailureDownloadingHistory -> {
      HedvigScaffold(
        navigateUp = navigateUp,
        modifier = Modifier.clearFocusOnTap(),
        topAppBarText = stringResource(id = R.string.PROFILE_ROW_TRAVEL_CERTIFICATE),
        topAppBarActions = {
          IconButton(
            onClick = { explanationSheetState.show() },
            modifier = Modifier.size(40.dp),
          ) {
            Icon(
              imageVector = HedvigIcons.InfoOutline,
              contentDescription = stringResource(R.string.REFERRALS_INFO_BUTTON_CONTENT_DESCRIPTION),
              modifier = Modifier.size(24.dp),
            )
          }
        },
      ) {
        HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
      }
    }

    Loading -> {
      HedvigFullScreenCenterAlignedProgress()
    }

    is SuccessDownloadingHistory -> {
      if (uiState.travelCertificateUri != null) {
        LaunchedEffect(uiState.travelCertificateUri) {
          onShareTravelCertificate(uiState.travelCertificateUri)
        }
      }
      if (uiState.idsToNavigateToAddonPurchase != null) {
        LaunchedEffect(uiState.idsToNavigateToAddonPurchase) {
          onNavigateToAddonPurchaseFlow(uiState.idsToNavigateToAddonPurchase)
        }
      }
      if (uiState.isLoadingCertificate) {
        HedvigFullScreenCenterAlignedProgress()
      } else {
        TravelCertificateSuccessScreen(
          onIconClick = { explanationSheetState.show() },
          onCertificateClick = onCertificateClick,
          onStartGenerateTravelCertificateFlow = onStartGenerateTravelCertificateFlow,
          navigateUp = navigateUp,
          historyList = uiState.certificateHistoryList,
          showErrorDialog = uiState.showDownloadCertificateError,
          onDismissDownloadCertificateError = onDismissDownloadCertificateError,
          showGenerationButton = uiState.showGenerateButton,
          onGoToChooseContract = onGoToChooseContract,
          hasChooseOption = uiState.hasChooseOption,
          travelAddonBannerInfo = uiState.travelAddonBannerInfo,
          launchAddonPurchaseFlow = launchAddonPurchaseFlow,
        )
      }
    }
  }
}

@Composable
private fun TravelCertificateSuccessScreen(
  onIconClick: () -> Unit,
  onCertificateClick: (String) -> Unit,
  onStartGenerateTravelCertificateFlow: () -> Unit,
  onGoToChooseContract: () -> Unit,
  navigateUp: () -> Unit,
  historyList: List<TravelCertificate>,
  showErrorDialog: Boolean,
  onDismissDownloadCertificateError: () -> Unit,
  showGenerationButton: Boolean,
  hasChooseOption: Boolean,
  travelAddonBannerInfo: TravelAddonBannerInfo?,
  launchAddonPurchaseFlow: (ids: List<String>) -> Unit,
) {
  HedvigScaffold(
    navigateUp = navigateUp,
    modifier = Modifier.clearFocusOnTap(),
    topAppBarText = stringResource(id = R.string.PROFILE_ROW_TRAVEL_CERTIFICATE),
    topAppBarActions = {
      IconButton(
        onClick = { onIconClick() },
        modifier = Modifier.size(40.dp),
      ) {
        Icon(
          imageVector = HedvigIcons.InfoOutline,
          contentDescription = stringResource(R.string.REFERRALS_INFO_BUTTON_CONTENT_DESCRIPTION),
          modifier = Modifier.size(24.dp),
        )
      }
    },
  ) {
    Box(Modifier.weight(1f)) {
      Column(
        Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState()),
      ) {
        if (historyList.isEmpty()) {
          Spacer(modifier = Modifier.weight(1f))
          EmptyTravelCertificatesScreen(Modifier.fillMaxWidth())
        } else {
          TravelCertificatesList(
            list = historyList,
            onCertificateClick = onCertificateClick,
            showErrorDialog = showErrorDialog,
            onDismissDownloadCertificateError = onDismissDownloadCertificateError,
          )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (travelAddonBannerInfo != null) {
          TravelAddonBanner(
            travelAddonBannerInfo = travelAddonBannerInfo,
            launchAddonPurchaseFlow = launchAddonPurchaseFlow,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          )
          Spacer(Modifier.height(8.dp))
        }
        if (showGenerationButton) {
          Spacer(Modifier.height(8.dp))
          HedvigButton(
            text = stringResource(R.string.travel_certificate_get_travel_certificate_button),
            onClick = dropUnlessResumed {
              if (hasChooseOption) onGoToChooseContract() else onStartGenerateTravelCertificateFlow()
            },
            buttonStyle = Secondary,
            enabled = true,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          )
        }
        Spacer(Modifier.height(16.dp))
      }
      Column(
        Modifier.fillMaxWidth(),
      ) {
        HedvigTooltip(
          message = stringResource(R.string.TOAST_READ_MORE),
          showTooltip = true,
          beakDirection = TopEnd,
          tooltipShown = {},
          modifier = Modifier
            .padding(horizontal = 14.dp)
            .align(Alignment.End),
        )
      }
    }
  }
}

@Composable
private fun TravelAddonBanner(
  travelAddonBannerInfo: TravelAddonBannerInfo,
  launchAddonPurchaseFlow: (ids: List<String>) -> Unit,
  modifier: Modifier = Modifier,
) {
  FeatureAddonBanner(
    modifier = modifier,
    title = travelAddonBannerInfo.title,
    description = travelAddonBannerInfo.description,
    buttonText = stringResource(R.string.ADDON_FLOW_SEE_PRICE_BUTTON),
    labels = travelAddonBannerInfo.labels,
    onButtonClick = {
      launchAddonPurchaseFlow(travelAddonBannerInfo.eligibleInsurancesIds)
    },
  )
}

@Composable
private fun EmptyTravelCertificatesScreen(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    EmptyState(
      text = stringResource(R.string.travel_certificate_empty_list_message),
      description = null,
      iconStyle = INFO,
    )
  }
}

@Composable
private fun TravelCertificatesList(
  list: List<TravelCertificate>,
  onCertificateClick: (String) -> Unit,
  showErrorDialog: Boolean,
  onDismissDownloadCertificateError: () -> Unit,
) {
  if (showErrorDialog) {
    ErrorDialog(
      title = stringResource(id = R.string.general_error),
      message = stringResource(id = R.string.travel_certificate_downloading_error),
      onDismiss = {
        onDismissDownloadCertificateError()
      },
    )
  }
  Spacer(Modifier.height(16.dp))
  val dateTimeFormatter = rememberHedvigMonthDateTimeFormatter()
  val groupedHistory = list.groupBy { it.expiryDate.year }
  groupedHistory.forEach {
    val year = it.key
    val travelCertificates = it.value

    HedvigText(text = year.toString(), modifier = Modifier.padding(horizontal = 16.dp))
    Spacer(Modifier.height(4.dp))

    travelCertificates.forEachIndexed { index, certificate ->
      val isExpired = certificate.isExpiredNow
      val color = if (isExpired) HedvigTheme.colorScheme.signalRedElement else Color.Unspecified
      val endText = if (isExpired) {
        stringResource(id = R.string.travel_certificate_expired)
      } else {
        stringResource(id = R.string.travel_certificate_active)
      }

      HorizontalItemsWithMaximumSpaceTaken(
        spaceBetween = 8.dp,
        startSlot = {
          HedvigText(
            text = dateTimeFormatter.format(certificate.startDate.toJavaLocalDate()),
            color = color,
          )
        },
        endSlot = {
          HedvigText(
            text = endText,
            color = color,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
          )
        },
        modifier = Modifier
          .clickable {
            onCertificateClick(certificate.signedUrl)
          }
          .padding(16.dp),
      )

      if (index != travelCertificates.size - 1) {
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      } else {
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTravelCertificateHistoryScreenWithEmptyList(
  @PreviewParameter(TravelCertificateHistoryUiStatePreviewProvider::class) uiState: CertificateHistoryUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TravelCertificateHistoryScreen(
        reload = {},
        onCertificateClick = {},
        onStartGenerateTravelCertificateFlow = {},
        launchAddonPurchaseFlow = {},
        onNavigateToAddonPurchaseFlow = {},
        onGoToChooseContract = {},
        navigateUp = {},
        onDismissDownloadCertificateError = {},
        onShareTravelCertificate = {},
        uiState = uiState,
      )
    }
  }
}

private class TravelCertificateHistoryUiStatePreviewProvider :
  CollectionPreviewParameterProvider<CertificateHistoryUiState>(
    listOf(
      SuccessDownloadingHistory(
        listOf(),
        false,
        true,
        null,
        false,
        false,
        travelAddonBannerInfo = TravelAddonBannerInfo(
          title = "Travel Plus",
          description = "Extended travel insurance with extra coverage for your travels",
          labels = listOf("Popular"),
          eligibleInsurancesIds = nonEmptyListOf("id"),
        ),
      ),
      SuccessDownloadingHistory(
        listOf(
          TravelCertificate(
            startDate = LocalDate(2024, 6, 2),
            expiryDate = LocalDate(2024, 7, 9),
            id = "13213",
            signedUrl = "wkehdkwed",
            isExpiredNow = false,
          ),
          TravelCertificate(
            startDate = LocalDate(2024, 1, 6),
            expiryDate = LocalDate(2024, 9, 10),
            id = "13213",
            signedUrl = "wkehdkwed",
            isExpiredNow = false,
          ),
          TravelCertificate(
            startDate = LocalDate(2023, 12, 9),
            expiryDate = LocalDate(2024, 1, 31),
            id = "13213",
            signedUrl = "wkehdkwed",
            isExpiredNow = true,
          ),
          TravelCertificate(
            startDate = LocalDate(2022, 12, 9),
            expiryDate = LocalDate(2023, 1, 31),
            id = "13213",
            signedUrl = "wkehdkwed",
            isExpiredNow = true,
          ),
        ),
        false,
        false,
        null,
        false,
        false,
        travelAddonBannerInfo = null,
      ),
      SuccessDownloadingHistory(
        listOf(
          TravelCertificate(
            startDate = LocalDate(2024, 6, 2),
            expiryDate = LocalDate(2024, 7, 9),
            id = "13213",
            signedUrl = "wkehdkwed",
            isExpiredNow = false,
          ),
          TravelCertificate(
            startDate = LocalDate(2024, 1, 6),
            expiryDate = LocalDate(2024, 9, 10),
            id = "13213",
            signedUrl = "wkehdkwed",
            isExpiredNow = false,
          ),
          TravelCertificate(
            startDate = LocalDate(2023, 12, 9),
            expiryDate = LocalDate(2024, 1, 31),
            id = "13213",
            signedUrl = "wkehdkwed",
            isExpiredNow = false,
          ),
          TravelCertificate(
            startDate = LocalDate(2022, 12, 9),
            expiryDate = LocalDate(2023, 1, 31),
            id = "13213",
            signedUrl = "wkehdkwed",
            isExpiredNow = false,
          ),
        ),
        true,
        true,
        null,
        false,
        false,
        travelAddonBannerInfo = null,
      ),
      SuccessDownloadingHistory(
        listOf(
          TravelCertificate(
            startDate = LocalDate(2024, 1, 6),
            expiryDate = LocalDate(2024, 9, 10),
            id = "13213",
            signedUrl = "wkehdkwed",
            isExpiredNow = false,
          ),
          TravelCertificate(
            startDate = LocalDate(2023, 11, 25),
            expiryDate = LocalDate(
              java.time.LocalDate.now().year,
              java.time.LocalDate.now().month,
              java.time.LocalDate.now().dayOfMonth,
            ),
            id = "13213",
            signedUrl = "wkehdkwed",
            isExpiredNow = true,
          ),
        ),
        false,
        true,
        null,
        false,
        false,
        travelAddonBannerInfo = null,
      ),
      SuccessDownloadingHistory(
        listOf(
          TravelCertificate(
            startDate = LocalDate(2024, 1, 6),
            expiryDate = LocalDate(2024, 9, 10),
            id = "13213",
            signedUrl = "wkehdkwed",
            isExpiredNow = false,
          ),
          TravelCertificate(
            startDate = LocalDate(2023, 11, 25),
            expiryDate = LocalDate(
              java.time.LocalDate.now().year,
              java.time.LocalDate.now().month,
              java.time.LocalDate.now().dayOfMonth,
            ),
            id = "13213",
            signedUrl = "wkehdkwed",
            isExpiredNow = true,
          ),
        ),
        false,
        false,
        null,
        false,
        false,
        travelAddonBannerInfo = null,
      ),
      Loading,
      FailureDownloadingHistory,
      SuccessDownloadingHistory(
        listOf(
          TravelCertificate(
            startDate = LocalDate(2024, 6, 2),
            expiryDate = LocalDate(2024, 7, 9),
            id = "13213",
            signedUrl = "wkehdkwed",
            isExpiredNow = false,
          ),
          TravelCertificate(
            startDate = LocalDate(2024, 1, 6),
            expiryDate = LocalDate(2024, 9, 10),
            id = "13213",
            signedUrl = "wkehdkwed",
            isExpiredNow = false,
          ),
          TravelCertificate(
            startDate = LocalDate(2023, 12, 9),
            expiryDate = LocalDate(2024, 1, 31),
            id = "13213",
            signedUrl = "wkehdkwed",
            isExpiredNow = false,
          ),
          TravelCertificate(
            startDate = LocalDate(2022, 12, 9),
            expiryDate = LocalDate(2023, 1, 31),
            id = "13213",
            signedUrl = "wkehdkwed",
            isExpiredNow = false,
          ),
        ),
        false,
        true,
        null,
        true,
        false,
        travelAddonBannerInfo = null,
      ),
    ),
  )
