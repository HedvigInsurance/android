package com.hedvig.android.feature.travelcertificate.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.data.addons.data.TravelAddonBannerInfo
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.INFO
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun TravelCertificateHistoryDestination(
  viewModel: CertificateHistoryViewModel,
  onStartGenerateTravelCertificateFlow: () -> Unit,
  onNavigateToChooseContract: () -> Unit,
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
  )
}

@Composable
private fun TravelCertificateHistoryScreen(
  reload: () -> Unit,
  onCertificateClick: (String) -> Unit,
  onStartGenerateTravelCertificateFlow: () -> Unit,
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
    if (historyList.isEmpty()) {
      Spacer(modifier = Modifier.weight(1f))
      EmptyTravelCertificatesScreen()
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
      TravelAddonBanner(travelAddonBannerInfo)
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
}

@Composable
private fun TravelAddonBanner(
  travelAddonBannerInfo: TravelAddonBannerInfo,
  modifier: Modifier = Modifier,
) {
  val containerColor = HedvigTheme.colorScheme.fillNegative
  val borderColor = HedvigTheme.colorScheme.borderPrimary


//  Surface(
//    modifier = modifier,
//    shape = NotificationDefaults.shape,
//    color = priority.colors.containerColor,
//    border = priority.colors.borderColor,
//  ) {
//    val buttonDarkTheme = if (priority is NotificationPriority.InfoInline) isSystemInDarkTheme() else false
//    ProvideTextStyle(textStyle) {
//      Row(Modifier.padding(padding)) {
//        if (withIcon) {
//          LayoutWithoutPlacement(
//            sizeAdjustingContent = { HedvigText("H") },
//          ) {
//            Icon(
//              imageVector = priority.icon,
//              contentDescription = null,
//              tint = priority.colors.iconColor,
//              modifier = Modifier.size(18.dp),
//            )
//          }
//          Spacer(Modifier.width(6.dp))
//        }
//        Column {
//          ProvideTextStyle(LocalTextStyle.current.copy(color = priority.colors.textColor)) {
//            content()
//          }
//          when (style) {
//            is Buttons -> {
//              Spacer(Modifier.height(NotificationsTokens.SpaceBetweenTextAndButtons))
//              Row {
//                HedvigTheme(darkTheme = buttonDarkTheme) {
//                  HedvigButton(
//                    enabled = true,
//                    onClick = style.onLeftButtonClick,
//                    buttonStyle = priority.buttonStyle,
//                    buttonSize = Small,
//                    modifier = Modifier.weight(1f),
//                  ) {
//                    HedvigText(style.leftButtonText, style = textStyle)
//                  }
//                  Spacer(Modifier.width(4.dp))
//                  HedvigButton(
//                    enabled = true,
//                    onClick = style.onRightButtonClick,
//                    buttonStyle = priority.buttonStyle,
//                    buttonSize = Small,
//                    modifier = Modifier.weight(1f),
//                  ) {
//                    HedvigText(style.rightButtonText, style = textStyle)
//                  }
//                }
//              }
//            }
//
//            is Button -> {
//              Spacer(Modifier.height(NotificationsTokens.SpaceBetweenTextAndButtons))
//              HedvigTheme(darkTheme = buttonDarkTheme) {
//                HedvigButton(
//                  enabled = true,
//                  onClick = style.onButtonClick,
//                  buttonStyle = priority.buttonStyle,
//                  buttonSize = Small,
//                  modifier = Modifier.fillMaxWidth(),
//                ) {
//                  LayoutWithoutPlacement(
//                    sizeAdjustingContent = {
//                      HedvigText(style.buttonText, style = textStyle)
//                    },
//                  ) {
//                    if (!buttonLoading) {
//                      HedvigText(style.buttonText, style = textStyle)
//                    } else {
//                      Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center,
//                      ) {
//                        ThreeDotsLoading()
//                      }
//                    }
//                  }
//                }
//              }
//            }
//
//            Default -> {}
//          }
//        }
//      }
//    }
  //}
}

@Composable
private fun EmptyTravelCertificatesScreen() {
  Column(
    modifier = Modifier.fillMaxSize(),
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
private fun PreviewTravelCertificateHistoryScreenWithEmptyList() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TravelCertificateHistoryScreen(
        {},
        {},
        {},
        {},
        {},
        {},
        {},
        SuccessDownloadingHistory(
          listOf(),
          false,
          true,
          null,
          false,
          false,
          travelAddonBannerInfo = null,
        ),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTravelCertificateHistoryScreenWithExpiredEarlier() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TravelCertificateHistoryScreen(
        {},
        {},
        {},
        {},
        {},
        {},
        {},
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
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewErrorWithDownloadingCertificate() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TravelCertificateHistoryScreen(
        {},
        {},
        {},
        {},
        {},
        {},
        {},
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
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTravelCertificateHistoryScreenWithExpiredToday() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TravelCertificateHistoryScreen(
        {},
        {},
        {},
        {},
        {},
        {},
        {},
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
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTravelCertificateHistoryScreenWithExpiredTodayNoGenerateButton() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TravelCertificateHistoryScreen(
        {},
        {},
        {},
        {},
        {},
        {},
        {},
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
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewCertificateHistoryLoading() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TravelCertificateHistoryScreen(
        {},
        {},
        {},
        {},
        {},
        {},
        {},
        Loading,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewErrorWithHistory() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TravelCertificateHistoryScreen(
        {},
        {},
        {},
        {},
        {},
        {},
        {},
        FailureDownloadingHistory,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewLoadingCertificate() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TravelCertificateHistoryScreen(
        {},
        {},
        {},
        {},
        {},
        {},
        {},
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
      )
    }
  }
}
