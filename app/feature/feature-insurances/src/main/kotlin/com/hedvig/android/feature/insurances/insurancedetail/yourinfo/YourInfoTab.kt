package com.hedvig.android.feature.insurances.insurancedetail.yourinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.common.android.time.daysUntil
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.information.HedvigPill
import com.hedvig.android.core.designsystem.material3.onWarningContainer
import com.hedvig.android.core.designsystem.material3.squircleLargeTop
import com.hedvig.android.core.designsystem.material3.warningContainer
import com.hedvig.android.core.designsystem.material3.warningElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.WarningFilled
import com.hedvig.android.core.icons.hedvig.small.hedvig.Lock
import com.hedvig.android.core.ui.infocard.InfoCardTextButton
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.infocard.VectorWarningCard
import com.hedvig.android.core.ui.rememberHedvigBirthDateDateTimeFormatter
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.feature.insurances.data.InsuranceAgreement
import hedvig.resources.R
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate

@ExperimentalMaterial3Api
@Composable
internal fun YourInfoTab(
  coverageItems: List<Pair<String, String>>,
  coInsured: List<InsuranceAgreement.CoInsured>,
  allowChangeAddress: Boolean,
  allowTerminatingInsurance: Boolean,
  allowEditCoInsured: Boolean,
  allowChangeTier: Boolean,
  onChangeTierClick: () -> Unit,
  upcomingChangesInsuranceAgreement: InsuranceAgreement?,
  onEditCoInsuredClick: () -> Unit,
  onMissingInfoClick: () -> Unit,
  onChangeAddressClick: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  openUrl: (String) -> Unit,
  onCancelInsuranceClick: () -> Unit,
  isTerminated: Boolean,
  contractHolderDisplayName: String,
  contractHolderSSN: String?,
  modifier: Modifier = Modifier,
) {
  val dateTimeFormatter = rememberHedvigDateTimeFormatter()
  val coroutineScope = rememberCoroutineScope()
  var showEditYourInfoBottomSheet by rememberSaveable { mutableStateOf(false) }
  if (showEditYourInfoBottomSheet) {
    val sheetState = rememberModalBottomSheetState(true)
    ModalBottomSheet(
      containerColor = MaterialTheme.colorScheme.background,
      onDismissRequest = {
        showEditYourInfoBottomSheet = false
      },
      shape = MaterialTheme.shapes.squircleLargeTop,
      sheetState = sheetState,
      tonalElevation = 0.dp,
      contentWindowInsets = { BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Top) },
    ) {
      EditInsuranceBottomSheetContent(
        allowChangeAddress = allowChangeAddress,
        allowEditCoInsured = allowEditCoInsured,
        allowChangeTier = allowChangeTier,
        onChangeTierClick = {
          coroutineScope.launch {
            sheetState.hide()
          }.invokeOnCompletion {
            showEditYourInfoBottomSheet = false
            onChangeTierClick()
          }
        },
        onEditCoInsuredClick = {
          coroutineScope.launch {
            sheetState.hide()
          }.invokeOnCompletion {
            showEditYourInfoBottomSheet = false
            onEditCoInsuredClick()
          }
        },
        onChangeAddressClick = {
          coroutineScope.launch {
            sheetState.hide()
          }.invokeOnCompletion {
            showEditYourInfoBottomSheet = false
            onChangeAddressClick()
          }
        },
        onDismiss = {
          coroutineScope.launch {
            sheetState.hide()
          }.invokeOnCompletion {
            showEditYourInfoBottomSheet = false
          }
        },
        modifier = Modifier
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp)
          .padding(bottom = 16.dp)
          .windowInsetsPadding(
            BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
          ),
      )
    }
  }

  var showUpcomingChangesBottomSheet by rememberSaveable { mutableStateOf(false) }
  if (showUpcomingChangesBottomSheet && upcomingChangesInsuranceAgreement != null) {
    val sheetState = rememberModalBottomSheetState(true)
    ModalBottomSheet(
      containerColor = MaterialTheme.colorScheme.background,
      onDismissRequest = {
        showUpcomingChangesBottomSheet = false
      },
      shape = MaterialTheme.shapes.squircleLargeTop,
      sheetState = sheetState,
      tonalElevation = 0.dp,
      contentWindowInsets = { BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Top) },
    ) {
      UpcomingChangesBottomSheetContent(
        infoText = stringResource(
          id = R.string.insurances_tab_your_insurance_will_be_updated_with_info,
          dateTimeFormatter.format(upcomingChangesInsuranceAgreement.activeFrom.toJavaLocalDate()),
        ),
        sections = upcomingChangesInsuranceAgreement.displayItems
          .map { it.title to it.value },
        onNavigateToNewConversation = {
          coroutineScope.launch {
            sheetState.hide()
            showUpcomingChangesBottomSheet = false
            onNavigateToNewConversation()
          }
        },
        onDismiss = {
          coroutineScope.launch {
            sheetState.hide()
            showUpcomingChangesBottomSheet = false
          }
        },
        modifier = Modifier
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp)
          .padding(bottom = 16.dp)
          .windowInsetsPadding(
            BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
          ),
      )
    }
  }

  Column(modifier) {
    Spacer(Modifier.height(16.dp))
    if (upcomingChangesInsuranceAgreement != null) {
      if (upcomingChangesInsuranceAgreement.creationCause == InsuranceAgreement.CreationCause.RENEWAL &&
        upcomingChangesInsuranceAgreement.certificateUrl != null
      ) {
        val daysUntilRenewal = remember(TimeZone.currentSystemDefault(), upcomingChangesInsuranceAgreement.activeFrom) {
          daysUntil(upcomingChangesInsuranceAgreement.activeFrom)
        }
        VectorInfoCard(
          text = stringResource(R.string.DASHBOARD_RENEWAL_PROMPTER_BODY, daysUntilRenewal),
          modifier = modifier,
        ) {
          InfoCardTextButton(
            onClick = { openUrl(upcomingChangesInsuranceAgreement.certificateUrl) },
            text = stringResource(R.string.CONTRACT_VIEW_CERTIFICATE_BUTTON),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          )
        }
      } else {
        VectorInfoCard(
          text = stringResource(
            id = R.string.CONTRACT_COINSURED_UPDATE_IN_FUTURE,
            upcomingChangesInsuranceAgreement.coInsured.size,
            dateTimeFormatter.format(upcomingChangesInsuranceAgreement.activeFrom.toJavaLocalDate()),
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        ) {
          if (upcomingChangesInsuranceAgreement.displayItems.isNotEmpty()) {
            InfoCardTextButton(
              text = stringResource(id = R.string.insurances_tab_view_details),
              onClick = { showUpcomingChangesBottomSheet = true },
              modifier = Modifier.fillMaxWidth(),
            )
          }
        }
      }
    }

    Spacer(Modifier.height(8.dp))
    CoverageRows(coverageItems, Modifier.padding(horizontal = 16.dp))

    if (allowEditCoInsured) {
      HorizontalDivider(Modifier.padding(horizontal = 16.dp))
      Spacer(Modifier.height(16.dp))
      CoInsuredSection(
        coInsuredList = coInsured,
        contractHolderDisplayName = contractHolderDisplayName,
        contractHolderSSN = contractHolderSSN,
        onMissingInfoClick = onMissingInfoClick,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
    Spacer(Modifier.height(16.dp))
    if (!isTerminated) {
      if (allowChangeAddress || allowEditCoInsured) {
        HedvigContainedButton(
          text = stringResource(R.string.CONTRACT_EDIT_INFO_LABEL),
          onClick = { showEditYourInfoBottomSheet = true },
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))
      }
      if (allowTerminatingInsurance) {
        HedvigTextButton(
          text = stringResource(R.string.TERMINATION_BUTTON),
          onClick = { onCancelInsuranceClick() },
          colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
          ),
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))
      }
      Spacer(Modifier.height(8.dp))
    }
  }
}

@Composable
internal fun CoverageRows(coverageRowItems: List<Pair<String, String>>, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    coverageRowItems.forEachIndexed { index, (firstText, secondText) ->
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 16.dp),
          ) {
            Text(firstText)
          }
        },
        endSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.padding(vertical = 16.dp),
          ) {
            Text(
              text = secondText,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.End,
            )
          }
        },
        spaceBetween = 8.dp,
      )
      if (index != coverageRowItems.size - 1) {
        HorizontalDivider()
      }
    }
  }
}

@Composable
internal fun CoInsuredSection(
  coInsuredList: List<InsuranceAgreement.CoInsured>,
  contractHolderDisplayName: String,
  contractHolderSSN: String?,
  onMissingInfoClick: () -> Unit,
  modifier: Modifier,
) {
  val dateTimeFormatter = rememberHedvigDateTimeFormatter()
  val birthDateTimeFormatter = rememberHedvigBirthDateDateTimeFormatter()
  Column(modifier = modifier) {
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(vertical = 4.dp),
        ) {
          Text(stringResource(id = R.string.CHANGE_ADDRESS_CO_INSURED_LABEL))
        }
      },
      endSlot = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.End,
          modifier = Modifier.padding(vertical = 4.dp),
        ) {
          val text = if (coInsuredList.size == 0) {
            stringResource(id = R.string.CHANGE_ADDRESS_ONLY_YOU)
          } else {
            stringResource(id = R.string.CHANGE_ADDRESS_YOU_PLUS, coInsuredList.size)
          }
          Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
          )
        }
      },
      spaceBetween = 8.dp,
    )
    Spacer(Modifier.height(16.dp))
    HorizontalDivider()
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(vertical = 12.dp),
        ) {
          Column {
            Text(contractHolderDisplayName)
            if (contractHolderSSN != null) {
              Text(
                text = contractHolderSSN,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      },
      endSlot = {
        Row(
          horizontalArrangement = Arrangement.End,
          modifier = Modifier.padding(vertical = 14.dp),
        ) {
          Icon(
            imageVector = Icons.Hedvig.Lock,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = "Locked info",
            modifier = Modifier.size(16.dp),
          )
        }
      },
      spaceBetween = 8.dp,
    )
    for (coInsured in coInsuredList) {
      HorizontalDivider()
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp),
          ) {
            Column {
              Text(
                text = coInsured.getDisplayName().ifBlank {
                  stringResource(id = R.string.CONTRACT_COINSURED)
                },
              )

              Text(
                text = coInsured.getSsnOrBirthDate(birthDateTimeFormatter)
                  ?: stringResource(id = R.string.CONTRACT_NO_INFORMATION),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )

              if (coInsured.activatesOn != null) {
                Spacer(Modifier.height(4.dp))
                HedvigPill(
                  text = stringResource(
                    id = R.string.CONTRACT_ADD_COINSURED_ACTIVE_FROM,
                    dateTimeFormatter.format(coInsured.activatesOn.toJavaLocalDate()),
                  ),
                  contentColor = MaterialTheme.colorScheme.onWarningContainer,
                  color = MaterialTheme.colorScheme.warningContainer,
                )
              }
              if (coInsured.terminatesOn != null) {
                Spacer(Modifier.height(4.dp))
                HedvigPill(
                  text = stringResource(
                    id = R.string.CONTRACT_ADD_COINSURED_ACTIVE_UNTIL,
                    dateTimeFormatter.format(coInsured.terminatesOn.toJavaLocalDate()),
                  ),
                  contentColor = MaterialTheme.colorScheme.onErrorContainer,
                  color = MaterialTheme.colorScheme.errorContainer,
                )
              }
            }
          }
        },
        endSlot = {
          if (coInsured.hasMissingInfo && coInsured.terminatesOn == null) {
            Row(
              horizontalArrangement = Arrangement.End,
              modifier = Modifier.padding(vertical = 14.dp),
            ) {
              Icon(
                imageVector = Icons.Hedvig.WarningFilled,
                tint = MaterialTheme.colorScheme.warningElement,
                contentDescription = "Needs info",
                modifier = Modifier.size(16.dp),
              )
            }
          }
        },
        spaceBetween = 8.dp,
      )
    }

    val hasMissingInfoAndIsNotTerminating = coInsuredList.any { it.hasMissingInfo && it.terminatesOn == null }
    if (hasMissingInfoAndIsNotTerminating) {
      Spacer(Modifier.height(8.dp))
      VectorWarningCard(
        text = stringResource(id = R.string.CONTRACT_COINSURED_ADD_PERSONAL_INFO),
      ) {
        InfoCardTextButton(
          text = stringResource(id = R.string.CONTRACT_COINSURED_MISSING_ADD_INFO),
          onClick = onMissingInfoClick,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
  }
}

@Composable
@HedvigPreview
private fun PreviewYourInfoTab() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      YourInfoTab(
        coverageItems = listOf(
          "Address".repeat(4) to "Bellmansgatan 19A",
          "Postal code" to "118 47".repeat(6),
          "Type" to "Homeowner",
          "Size" to "56 m2",
        ),
        coInsured = listOf(
          InsuranceAgreement.CoInsured(
            ssn = "199101131093",
            birthDate = null,
            firstName = "Hugo",
            lastName = "Linder",
            activatesOn = LocalDate.fromEpochDays(300),
            terminatesOn = LocalDate.fromEpochDays(400),
            hasMissingInfo = false,
          ),
          InsuranceAgreement.CoInsured(
            ssn = null,
            birthDate = null,
            firstName = null,
            lastName = null,
            activatesOn = null,
            terminatesOn = null,
            hasMissingInfo = true,
          ),
        ),
        allowChangeAddress = true,
        allowTerminatingInsurance = true,
        allowEditCoInsured = true,
        upcomingChangesInsuranceAgreement = InsuranceAgreement(
          activeFrom = LocalDate.fromEpochDays(200),
          activeTo = LocalDate.fromEpochDays(300),
          displayItems = listOf(
            InsuranceAgreement.DisplayItem(
              title = "test title",
              value = "test value",
            ),
          ),
          productVariant = ProductVariant(
            displayName = "Variant",
            contractGroup = ContractGroup.RENTAL,
            contractType = ContractType.SE_APARTMENT_RENT,
            partner = null,
            perils = listOf(),
            insurableLimits = listOf(),
            documents = listOf(),
            displayTierName = "Standard",
            tierDescription = "Our most standard coverage",
            termsVersion = "SE_DOG_STANDARD-20230330-HEDVIG-null",
          ),
          certificateUrl = null,
          coInsured = listOf(
            InsuranceAgreement.CoInsured(
              ssn = "199101131093",
              birthDate = null,
              firstName = "Hugo",
              lastName = "Linder",
              activatesOn = LocalDate.fromEpochDays(300),
              terminatesOn = LocalDate.fromEpochDays(300),
              hasMissingInfo = false,
            ),
            InsuranceAgreement.CoInsured(
              ssn = "1234020312",
              birthDate = null,
              firstName = "Testersson",
              lastName = "Tester",
              activatesOn = null,
              terminatesOn = null,
              hasMissingInfo = false,
            ),
          ),
          creationCause = InsuranceAgreement.CreationCause.UNKNOWN,
        ),
        onEditCoInsuredClick = {},
        onChangeAddressClick = {},
        onNavigateToNewConversation = {},
        onCancelInsuranceClick = {},
        isTerminated = false,
        contractHolderDisplayName = "Hugo Linder",
        contractHolderSSN = "19910113-1093",
        onMissingInfoClick = {},
        openUrl = {},
        allowChangeTier = true,
        onChangeTierClick = {},
      )
    }
  }
}
