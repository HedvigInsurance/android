package com.hedvig.android.feature.insurances.insurancedetail.yourinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.common.daysUntil
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.display.items.DisplayItem
import com.hedvig.android.data.display.items.DisplayItem.DisplayItemValue
import com.hedvig.android.data.display.items.DisplayItem.DisplayItemValue.Date
import com.hedvig.android.data.display.items.DisplayItem.DisplayItemValue.DateTime
import com.hedvig.android.data.display.items.DisplayItem.DisplayItemValue.Text
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Ghost
import com.hedvig.android.design.system.hedvig.DividerPosition
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize.Small
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Amber
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Red
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.LIGHT
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.MEDIUM
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle.Button
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle.Default
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Attention
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Info
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigBirthDateDateTimeFormatter
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigDateTimeFormatter
import com.hedvig.android.design.system.hedvig.horizontalDivider
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Lock
import com.hedvig.android.design.system.hedvig.icon.WarningFilled
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.show
import com.hedvig.android.feature.insurances.data.InsuranceAgreement
import com.hedvig.android.feature.insurances.data.InsuranceAgreement.CoInsured
import com.hedvig.android.feature.insurances.data.MonthlyCost
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime

@Composable
internal fun YourInfoTab(
  coverageItems: List<DisplayItem>,
  coInsured: List<CoInsured>,
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
  val editYourInfoBottomSheet = rememberHedvigBottomSheetState<Unit>()
  HedvigBottomSheet(editYourInfoBottomSheet) {
    EditInsuranceBottomSheetContent(
      allowTerminatingInsurance = allowTerminatingInsurance,
      allowEditCoInsured = allowEditCoInsured,
      allowChangeTier = allowChangeTier,
      onChangeTierClick = {
        editYourInfoBottomSheet.dismiss()
        onChangeTierClick()
      },
      onEditCoInsuredClick = {
        editYourInfoBottomSheet.dismiss()
        onEditCoInsuredClick()
      },
      onDismiss = {
        editYourInfoBottomSheet.dismiss()
      },
      onCancelInsuranceClick = {
        editYourInfoBottomSheet.dismiss()
        onCancelInsuranceClick()
      },
    )
  }

  val upcomingChangesBottomSheet = rememberHedvigBottomSheetState<Unit>()
  if (upcomingChangesInsuranceAgreement != null) {
    HedvigBottomSheet(upcomingChangesBottomSheet) {
      UpcomingChangesBottomSheetContent(
        infoText = stringResource(
          id = R.string.insurances_tab_your_insurance_will_be_updated_with_info,
          dateTimeFormatter.format(upcomingChangesInsuranceAgreement.activeFrom.toJavaLocalDate()),
        ),
        sections = upcomingChangesInsuranceAgreement.displayItems,
        onNavigateToNewConversation = {
          upcomingChangesBottomSheet.dismiss()
          onNavigateToNewConversation()
        },
        onDismiss = {
          upcomingChangesBottomSheet.dismiss()
        },
      )
    }
  }

  Column(modifier) {
    if (upcomingChangesInsuranceAgreement != null) {
      if (upcomingChangesInsuranceAgreement.creationCause == InsuranceAgreement.CreationCause.RENEWAL &&
        upcomingChangesInsuranceAgreement.certificateUrl != null
      ) {
        val daysUntilRenewal = remember(TimeZone.currentSystemDefault(), upcomingChangesInsuranceAgreement.activeFrom) {
          daysUntil(upcomingChangesInsuranceAgreement.activeFrom)
        }
        HedvigNotificationCard(
          modifier = Modifier.padding(horizontal = 16.dp),
          message = stringResource(R.string.DASHBOARD_RENEWAL_PROMPTER_BODY, daysUntilRenewal),
          priority = Info,
          style = Button(
            stringResource(R.string.CONTRACT_VIEW_CERTIFICATE_BUTTON),
            { openUrl(upcomingChangesInsuranceAgreement.certificateUrl) },
          ),
        )
      } else {
        HedvigNotificationCard(
          modifier = Modifier.padding(horizontal = 16.dp),
          message = stringResource(
            R.string.insurances_tab_your_insurance_will_be_updated,
            dateTimeFormatter.format(upcomingChangesInsuranceAgreement.activeFrom.toJavaLocalDate()),
          ),
          priority = Info,
          style = if (upcomingChangesInsuranceAgreement.displayItems.isNotEmpty()) {
            Button(
              stringResource(id = R.string.insurances_tab_view_details),
              { upcomingChangesBottomSheet.show() },
            )
          } else {
            Default
          },
        )
      }
    }
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
      if (allowEditCoInsured || allowChangeTier || allowTerminatingInsurance) {
        HedvigButton(
          text = stringResource(R.string.CONTRACT_EDIT_INFO_LABEL),
          enabled = true,
          onClick = { editYourInfoBottomSheet.show() },
          buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
      }
      if (allowChangeAddress) {
        HedvigButton(
          text = stringResource(R.string.insurance_details_move_button),
          buttonStyle = Ghost,
          enabled = true,
          onClick = { onChangeAddressClick() },
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
      }
      Spacer(Modifier.height(8.dp))
    }
  }
}

@Composable
internal fun CoverageRows(coverageRowItems: List<DisplayItem>, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    coverageRowItems.forEachIndexed { index, displayItem ->
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 16.dp),
          ) {
            HedvigText(displayItem.title)
          }
        },
        endSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.padding(vertical = 16.dp),
          ) {
            val formatter = rememberHedvigDateTimeFormatter()
            HedvigText(
              text = when (val item = displayItem.value) {
                is Date -> formatter.format(item.date.toJavaLocalDate())
                is DateTime -> formatter.format(item.localDateTime.toJavaLocalDateTime())
                is Text -> item.text
              },
              color = HedvigTheme.colorScheme.textSecondary,
              textAlign = TextAlign.End,
            )
          }
        },
        spaceBetween = 8.dp,
        modifier = Modifier.horizontalDivider(DividerPosition.Top, show = index != 0),
      )
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
          HedvigText(stringResource(id = R.string.CHANGE_ADDRESS_CO_INSURED_LABEL))
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
          HedvigText(
            text = text,
            color = HedvigTheme.colorScheme.textSecondary,
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
            HedvigText(contractHolderDisplayName)
            if (contractHolderSSN != null) {
              HedvigText(
                text = contractHolderSSN,
                color = HedvigTheme.colorScheme.textSecondary,
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
            imageVector = HedvigIcons.Lock,
            tint = HedvigTheme.colorScheme.fillSecondary,
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
              HedvigText(
                text = coInsured.getDisplayName().ifBlank {
                  stringResource(id = R.string.CONTRACT_COINSURED)
                },
              )

              HedvigText(
                text = coInsured.getSsnOrBirthDate(birthDateTimeFormatter)
                  ?: stringResource(id = R.string.CONTRACT_NO_INFORMATION),
                color = HedvigTheme.colorScheme.textSecondary,
              )

              if (coInsured.activatesOn != null) {
                Spacer(Modifier.height(4.dp))
                HighlightLabel(
                  labelText = stringResource(
                    id = R.string.CONTRACT_ADD_COINSURED_ACTIVE_FROM,
                    dateTimeFormatter.format(coInsured.activatesOn.toJavaLocalDate()),
                  ),
                  size = Small,
                  color = Amber(MEDIUM),
                )
              }
              if (coInsured.terminatesOn != null) {
                Spacer(Modifier.height(4.dp))
                HighlightLabel(
                  labelText = stringResource(
                    id = R.string.CONTRACT_ADD_COINSURED_ACTIVE_UNTIL,
                    dateTimeFormatter.format(coInsured.terminatesOn.toJavaLocalDate()),
                  ),
                  size = Small,
                  color = Red(LIGHT),
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
                imageVector = HedvigIcons.WarningFilled,
                tint = HedvigTheme.colorScheme.signalAmberElement,
                contentDescription = null,
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
      HedvigNotificationCard(
        message = stringResource(R.string.CONTRACT_COINSURED_ADD_PERSONAL_INFO),
        priority = Attention,
        style = Button(
          stringResource(R.string.CONTRACT_COINSURED_MISSING_ADD_INFO),
          onMissingInfoClick,
        ),
      )
    }
  }
}

@Composable
@HedvigPreview
private fun PreviewYourInfoTab() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      YourInfoTab(
        coverageItems = listOf(
          DisplayItem("Address".repeat(4), DisplayItem.DisplayItemValue.Text("Bellmansgatan 19A")),
          DisplayItem("Postal code", DisplayItem.DisplayItemValue.Text("118 47".repeat(6))),
          DisplayItem("Type", DisplayItem.DisplayItemValue.Text("Homeowner")),
          DisplayItem("Size", DisplayItem.DisplayItemValue.Text("56 m2")),
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
            DisplayItem(
              title = "test title",
              value = DisplayItemValue.Text("test value"),
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
          certificateUrl = "adq",
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
          creationCause = InsuranceAgreement.CreationCause.RENEWAL,
          addons = null,
          basePremium = UiMoney(89.0, UiCurrencyCode.SEK),
          cost = MonthlyCost(
            UiMoney(89.0, UiCurrencyCode.SEK),
            UiMoney(89.0, UiCurrencyCode.SEK),
            discounts = emptyList()
          )
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
