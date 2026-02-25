package com.hedvig.android.feature.insurances.insurancedetail.yourinfo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.core.common.daysUntil
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractId
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.display.items.DisplayItem
import com.hedvig.android.data.display.items.DisplayItem.DisplayItemValue.Date
import com.hedvig.android.data.display.items.DisplayItem.DisplayItemValue.DateTime
import com.hedvig.android.data.display.items.DisplayItem.DisplayItemValue.Text
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Ghost
import com.hedvig.android.design.system.hedvig.DividerPosition
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize.Small
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Amber
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Red
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.LIGHT
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.MEDIUM
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle.Button
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle.Default
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Attention
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Info
import com.hedvig.android.design.system.hedvig.PriceInfoForBottomSheet
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.design.system.hedvig.horizontalDivider
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.InfoFilled
import com.hedvig.android.design.system.hedvig.icon.Lock
import com.hedvig.android.design.system.hedvig.icon.WarningFilled
import com.hedvig.android.design.system.hedvig.rememberHedvigBirthDateDateTimeFormatter
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.rememberHedvigDateTimeFormatter
import com.hedvig.android.design.system.hedvig.show
import com.hedvig.android.feature.insurances.data.AvailableAddon
import com.hedvig.android.feature.insurances.data.ContractAddon
import com.hedvig.android.feature.insurances.data.InsuranceAgreement
import com.hedvig.android.feature.insurances.data.InsuranceAgreement.CoInsured
import com.hedvig.android.feature.insurances.data.MonthlyCost
import hedvig.resources.ADDON_ADDED_COVERAGE
import hedvig.resources.CHANGE_ADDRESS_CO_INSURED_LABEL
import hedvig.resources.CHANGE_ADDRESS_ONLY_YOU
import hedvig.resources.CHANGE_ADDRESS_YOU_PLUS
import hedvig.resources.CONTRACT_ADD_COINSURED_ACTIVE_FROM
import hedvig.resources.CONTRACT_ADD_COINSURED_ACTIVE_UNTIL
import hedvig.resources.CONTRACT_COINSURED
import hedvig.resources.CONTRACT_COINSURED_ADD_PERSONAL_INFO
import hedvig.resources.CONTRACT_COINSURED_MISSING_ADD_INFO
import hedvig.resources.CONTRACT_EDIT_INFO_LABEL
import hedvig.resources.CONTRACT_NO_INFORMATION
import hedvig.resources.CONTRACT_OVERVIEW_ADDON_ACTIVATES_DATE
import hedvig.resources.CONTRACT_OVERVIEW_ADDON_ADD
import hedvig.resources.CONTRACT_OVERVIEW_ADDON_ENDS_DATE
import hedvig.resources.CONTRACT_VIEW_CERTIFICATE_BUTTON
import hedvig.resources.DASHBOARD_RENEWAL_PROMPTER_BODY
import hedvig.resources.DETAILS_TABLE_INSURANCE_PREMIUM
import hedvig.resources.INSURANCE_DETAILS_DECOMMISSION_INFO
import hedvig.resources.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION
import hedvig.resources.REMOVE_ADDON_BUTTON_TITLE
import hedvig.resources.REMOVE_ADDON_DESCRIPTION
import hedvig.resources.REMOVE_ADDON_OFFER_PAGE_TITLE
import hedvig.resources.Res
import hedvig.resources.TALKBACK_DOUBLE_TAP_TO_READ_DETAILS
import hedvig.resources.general_cancel_button
import hedvig.resources.insurance_details_move_button
import hedvig.resources.insurances_tab_view_details
import hedvig.resources.insurances_tab_your_insurance_will_be_updated
import hedvig.resources.insurances_tab_your_insurance_will_be_updated_with_info
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun YourInfoTab(
  contractId: String,
  coverageItems: List<DisplayItem>,
  coInsured: List<CoInsured>,
  allowChangeAddress: Boolean,
  allowTerminatingInsurance: Boolean,
  allowEditCoInsured: Boolean,
  allowChangeTier: Boolean,
  allowRemovingAddon: Boolean,
  onChangeTierClick: () -> Unit,
  isDecommissioned: Boolean,
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
  priceToShow: UiMoney,
  showPriceInfoIcon: Boolean,
  onInfoIconClick: () -> Unit,
  existingAddons: List<ContractAddon>,
  availableAddons: List<AvailableAddon>,
  navigateToRemoveAddon: (ContractId?, AddonVariant?) -> Unit,
  navigateToUpgradeAddon: (ContractId?, AddonVariant?) -> Unit,
  navigateToAddAddon: (AvailableAddon) -> Unit,
  modifier: Modifier = Modifier,
) {
  val dateTimeFormatter = rememberHedvigDateTimeFormatter()
  val editYourInfoBottomSheet = rememberHedvigBottomSheetState<Unit>()
  HedvigBottomSheet(editYourInfoBottomSheet) {
    EditInsuranceBottomSheetContent(
      allowTerminatingInsurance = allowTerminatingInsurance,
      allowEditCoInsured = allowEditCoInsured,
      allowChangeTier = allowChangeTier,
      allowRemovingAddon = allowRemovingAddon,
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
      onRemoveAddonClick = {
        editYourInfoBottomSheet.dismiss()
        navigateToRemoveAddon(ContractId(contractId), null)
      },
    )
  }

  val upcomingChangesBottomSheet = rememberHedvigBottomSheetState<Unit>()
  if (upcomingChangesInsuranceAgreement != null) {
    val upcomingPriceInfoForBottomSheet = PriceInfoForBottomSheet(
      displayItems = buildList {
        add(
          upcomingChangesInsuranceAgreement.productVariant.displayName to stringResource(
            Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
            upcomingChangesInsuranceAgreement.basePremium.toString(),
          ),
        )
        upcomingChangesInsuranceAgreement.addons?.forEach { addon ->
          add(
            addon.addonVariant.displayName
              to stringResource(
              Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
              addon.premium.toString(),
            ),
          )
        }
        upcomingChangesInsuranceAgreement.cost.discounts.forEach { discount ->
          add(discount.displayName to discount.displayValue)
        }
      },
      totalGross = upcomingChangesInsuranceAgreement.cost.monthlyGross,
      totalNet = upcomingChangesInsuranceAgreement.cost.monthlyNet,
    )
    HedvigBottomSheet(upcomingChangesBottomSheet) {
      UpcomingChangesBottomSheetContent(
        infoText = stringResource(
          Res.string.insurances_tab_your_insurance_will_be_updated_with_info,
          dateTimeFormatter.format(upcomingChangesInsuranceAgreement.activeFrom),
        ),
        sections = upcomingChangesInsuranceAgreement.displayItems,
        upcomingPriceInfo = upcomingPriceInfoForBottomSheet,
        showInfoIcon =
          upcomingChangesInsuranceAgreement.basePremium != upcomingChangesInsuranceAgreement.cost.monthlyNet ||
            upcomingChangesInsuranceAgreement.cost.monthlyNet != upcomingChangesInsuranceAgreement.cost.monthlyGross,
        onNavigateToNewConversation = {
          upcomingChangesBottomSheet.dismiss()
          onNavigateToNewConversation()
        },
        onDismiss = {
          upcomingChangesBottomSheet.dismiss()
        },
        certificateUrl = upcomingChangesInsuranceAgreement.certificateUrl,
        onOpenUrlClick = { url ->
          openUrl(url)
        },
      )
    }
  }

  Column(modifier.padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    if (upcomingChangesInsuranceAgreement != null) {
      if (upcomingChangesInsuranceAgreement.creationCause == InsuranceAgreement.CreationCause.RENEWAL &&
        upcomingChangesInsuranceAgreement.certificateUrl != null
      ) {
        val daysUntilRenewal = remember(TimeZone.currentSystemDefault(), upcomingChangesInsuranceAgreement.activeFrom) {
          daysUntil(upcomingChangesInsuranceAgreement.activeFrom)
        }
        HedvigNotificationCard(
          modifier = Modifier.padding(horizontal = 16.dp),
          message = stringResource(Res.string.DASHBOARD_RENEWAL_PROMPTER_BODY, daysUntilRenewal),
          priority = Info,
          style = Button(
            buttonText = stringResource(Res.string.CONTRACT_VIEW_CERTIFICATE_BUTTON),
            onButtonClick = { openUrl(upcomingChangesInsuranceAgreement.certificateUrl) },
          ),
        )
      } else {
        HedvigNotificationCard(
          modifier = Modifier.padding(horizontal = 16.dp),
          message = stringResource(
            Res.string.insurances_tab_your_insurance_will_be_updated,
            dateTimeFormatter.format(upcomingChangesInsuranceAgreement.activeFrom),
          ),
          priority = Info,
          style = if (upcomingChangesInsuranceAgreement.displayItems.isNotEmpty()) {
            Button(
              buttonText = stringResource(Res.string.insurances_tab_view_details),
              onButtonClick = { upcomingChangesBottomSheet.show() },
            )
          } else {
            Default
          },
        )
      }
    }
    if (isDecommissioned) {
      HedvigNotificationCard(
        modifier = Modifier.padding(horizontal = 16.dp),
        message = stringResource(Res.string.INSURANCE_DETAILS_DECOMMISSION_INFO),
        priority = Info,
      )
    }
    Surface(
      shape = HedvigTheme.shapes.cornerXLarge,
      modifier = modifier.padding(horizontal = 16.dp),
    ) {
      Column {
        CoverageRows(coverageItems, Modifier.padding(horizontal = 16.dp))
        PriceRow(
          priceToShow,
          showPriceInfoIcon,
          onInfoIconClick,
          Modifier.padding(horizontal = 16.dp),
        )
        if (allowEditCoInsured) {
          HorizontalDivider(Modifier.padding(horizontal = 16.dp))
          Spacer(Modifier.height(16.dp))
          CoInsuredSection(
            coInsuredList = coInsured,
            contractHolderDisplayName = contractHolderDisplayName,
            contractHolderSSN = contractHolderSSN,
            modifier = Modifier.padding(horizontal = 16.dp),
          )
        }
      }
    }
    val hasMissingInfoAndIsNotTerminating = coInsured.any { it.hasMissingInfo && it.terminatesOn == null }
    if (hasMissingInfoAndIsNotTerminating) {
      HedvigNotificationCard(
        message = stringResource(Res.string.CONTRACT_COINSURED_ADD_PERSONAL_INFO),
        priority = Attention,
        style = Button(
          stringResource(Res.string.CONTRACT_COINSURED_MISSING_ADD_INFO),
          onMissingInfoClick,
        ),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
    }
    AddonsSection(
      existingAddons = existingAddons,
      availableAddons = availableAddons,
      navigateToRemoveAddon = navigateToRemoveAddon,
      navigateToUpgradeAddon = navigateToUpgradeAddon,
      navigateToAddAddon = navigateToAddAddon,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    if (!isTerminated) {
      Column(Modifier.padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (allowEditCoInsured || allowChangeTier || allowTerminatingInsurance) {
          HedvigButton(
            text = stringResource(Res.string.CONTRACT_EDIT_INFO_LABEL),
            enabled = true,
            onClick = { editYourInfoBottomSheet.show() },
            buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
            modifier = Modifier
              .padding(horizontal = 16.dp)
              .fillMaxWidth(),
          )
        }
        if (allowChangeAddress) {
          HedvigButton(
            text = stringResource(Res.string.insurance_details_move_button),
            buttonStyle = Ghost,
            enabled = true,
            onClick = { onChangeAddressClick() },
            modifier = Modifier
              .padding(horizontal = 16.dp)
              .fillMaxWidth(),
          )
        }
      }
    }
  }
}

@Composable
private fun AddonsSection(
  existingAddons: List<ContractAddon>,
  availableAddons: List<AvailableAddon>,
  navigateToRemoveAddon: (ContractId?, AddonVariant?) -> Unit,
  navigateToUpgradeAddon: (ContractId?, AddonVariant?) -> Unit,
  navigateToAddAddon: (AvailableAddon) -> Unit,
  modifier: Modifier,
) {
  val removeAddonBottomSheetState = rememberHedvigBottomSheetState<ContractAddon>()
  HedvigBottomSheet(
    removeAddonBottomSheetState,
    contentPadding = PaddingValues(horizontal = 24.dp),
  ) { addon ->
    // TODO Remove `&& false` when we finalize the implementation of UpgradeOrRemoveAddonBottomSheetContent and we want
    //  to allow also upgrading coverage of an existing addon
    //  https://hedviginsurance.slack.com/archives/C0A1GAGLPAA/p1771943941276239
    @Suppress("SimplifyBooleanWithConstants", "KotlinConstantConditions")
    if (addon.isUpgradable && addon.isRemovable && false) {
      UpgradeOrRemoveAddonBottomSheetContent(
        addon = addon,
        onRemove = dropUnlessResumed {
          removeAddonBottomSheetState.dismiss {
            navigateToRemoveAddon(addon.relatedContractId, addon.addonVariant)
          }
        },
        onUpgrade = dropUnlessResumed {
          removeAddonBottomSheetState.dismiss {
            navigateToUpgradeAddon(addon.relatedContractId, addon.addonVariant)
          }
        },
        onDismiss = { removeAddonBottomSheetState.dismiss() },
      )
    } else {
      // TODO pass in upgrade-specific strings if we want the upgrade to also show in the style of a single option
      SingleButtonBottomSheetContent(
        title = addon.displayName,
        subtitle = stringResource(Res.string.REMOVE_ADDON_DESCRIPTION),
        buttonText = stringResource(Res.string.REMOVE_ADDON_BUTTON_TITLE),
        onClick = dropUnlessResumed {
          removeAddonBottomSheetState.dismiss {
            navigateToRemoveAddon(addon.relatedContractId, addon.addonVariant)
          }
        },
        onDismiss = { removeAddonBottomSheetState.dismiss() },
      )
    }
  }
  if (existingAddons.isNotEmpty() || availableAddons.isNotEmpty()) {
    val dateFormatter = HedvigDateTimeFormatterDefaults.isoLocalDateWithDashes(getLocale())
    Surface(
      shape = HedvigTheme.shapes.cornerXLarge,
      modifier = modifier,
    ) {
      Column {
        existingAddons.forEachIndexed { index, existingAddon ->
          AddonRow(
            title = existingAddon.displayName,
            description = when (val status = existingAddon.status) {
              is ContractAddon.Status.ActiveFrom -> {
                stringResource(Res.string.CONTRACT_OVERVIEW_ADDON_ACTIVATES_DATE, dateFormatter.format(status.date))
              }

              is ContractAddon.Status.EndsAt -> {
                stringResource(Res.string.CONTRACT_OVERVIEW_ADDON_ENDS_DATE, dateFormatter.format(status.date))
              }

              ContractAddon.Status.Unknown -> existingAddon.description
            },
            showTopDivider = index != 0,
            isAlreadyAdded = true,
            modifier = Modifier.clickable(
              enabled = existingAddon.isUpgradable || existingAddon.isRemovable,
            ) {
              removeAddonBottomSheetState.show(existingAddon)
            },
          )
        }
        availableAddons.forEachIndexed { index, availableAddon ->
          AddonRow(
            title = availableAddon.displayName,
            description = availableAddon.description,
            showTopDivider = index != 0 || existingAddons.isNotEmpty(),
            isAlreadyAdded = false,
            modifier = Modifier.clickable(onClick = dropUnlessResumed { navigateToAddAddon(availableAddon) }),
          )
        }
      }
    }
  }
}

@Composable
private fun SingleButtonBottomSheetContent(
  title: String,
  subtitle: String,
  buttonText: String,
  onClick: () -> Unit,
  onDismiss: () -> Unit,
) {
  Column {
    HedvigText(
      text = title,
      style = HedvigTheme.typography.headlineSmall,
    )
    HedvigText(
      text = subtitle,
      style = HedvigTheme.typography.bodySmall,
      color = HedvigTheme.colorScheme.textSecondary
    )
    Spacer(Modifier.height(32.dp))
    HedvigButton(
      text = buttonText,
      buttonStyle = ButtonDefaults.ButtonStyle.Primary,
      enabled = true,
      onClick = onClick,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigButton(
      text = stringResource(Res.string.general_cancel_button),
      buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      enabled = true,
      onClick = onDismiss,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun UpgradeOrRemoveAddonBottomSheetContent(
  addon: ContractAddon,
  onRemove: () -> Unit,
  onUpgrade: () -> Unit,
  onDismiss: () -> Unit,
) {
  Column {
    HedvigText(
      text = addon.displayName,
      style = HedvigTheme.typography.headlineSmall,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )
    val radioOptions: List<RadioOption> = buildList {
      if (addon.isRemovable) {
        add(
          RadioOption(
            RadioOptionId("remove"),
            stringResource(Res.string.REMOVE_ADDON_OFFER_PAGE_TITLE),
            "todo change or edit an existing addon",
          ),
        )
      }
      if (addon.isUpgradable) {
        add(
          RadioOption(
            RadioOptionId("upgrade"),
            "todo upgrade",
            "todo upgrade",
          ),
        )
      }
    }
    Spacer(Modifier.height(32.dp))
    var selectedRadioOption: RadioOptionId? by remember { mutableStateOf(null) }
    RadioGroup(
      radioOptions,
      selectedRadioOption,
      onRadioOptionSelected = { selectedRadioOption = it },
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.REMOVE_ADDON_OFFER_PAGE_TITLE),
      enabled = selectedRadioOption != null,
      onClick = {
        when (selectedRadioOption?.id) {
          "remove" -> onRemove()
          "upgrade" -> onUpgrade()
          null -> {}
          else -> error("RemoveAddonBottomSheetContent wrong radio option selected")
        }
      },
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigButton(
      text = stringResource(Res.string.general_cancel_button),
      buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      enabled = true,
      onClick = onDismiss,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun AddonRow(
  title: String,
  description: String,
  showTopDivider: Boolean,
  isAlreadyAdded: Boolean,
  modifier: Modifier = Modifier,
) {
  HorizontalItemsWithMaximumSpaceTaken(
    {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        HedvigText(title)
        HedvigText(
          text = description,
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondaryTranslucent,
        )
      }
    },
    {
      HighlightLabel(
        labelText = if (isAlreadyAdded) {
          stringResource(Res.string.ADDON_ADDED_COVERAGE)
        } else {
          stringResource(Res.string.CONTRACT_OVERVIEW_ADDON_ADD)
        },
        size = HighlightLabelDefaults.HighLightSize.Medium,
        color = if (isAlreadyAdded) {
          HighlightLabelDefaults.HighlightColor.Grey(MEDIUM)
        } else {
          HighlightLabelDefaults.HighlightColor.Green(MEDIUM)
        },
        Modifier.wrapContentSize(Alignment.TopEnd),
      )
    },
    4.dp,
    modifier
      .horizontalDivider(DividerPosition.Top, showTopDivider)
      .padding(16.dp),
  )
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
                is Date -> formatter.format(item.date)
                is DateTime -> formatter.format(item.localDateTime)
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
internal fun PriceRow(
  priceToShow: UiMoney,
  showInfoIcon: Boolean,
  onInfoIconClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HorizontalItemsWithMaximumSpaceTaken(
    modifier = modifier.horizontalDivider(DividerPosition.Top),
    startSlot = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 16.dp),
      ) {
        HedvigText(stringResource(Res.string.DETAILS_TABLE_INSURANCE_PREMIUM))
      }
    },
    endSlot = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.padding(vertical = 16.dp),
      ) {
        HedvigText(
          text = stringResource(
            Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
            priceToShow.toString(),
          ),
          color = HedvigTheme.colorScheme.textSecondary,
          textAlign = TextAlign.End,
        )
        if (showInfoIcon) {
          Spacer(Modifier.width(8.dp))
          IconButton(
            onInfoIconClick,
            modifier = Modifier.size(24.dp),
          ) {
            Icon(
              HedvigIcons.InfoFilled,
              stringResource(Res.string.TALKBACK_DOUBLE_TAP_TO_READ_DETAILS),
              tint = HedvigTheme.colorScheme.fillSecondary,
            )
          }
        }
      }
    },
    spaceBetween = 8.dp,
  )
}

@Composable
internal fun CoInsuredSection(
  coInsuredList: List<CoInsured>,
  contractHolderDisplayName: String,
  contractHolderSSN: String?,
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
          HedvigText(stringResource(Res.string.CHANGE_ADDRESS_CO_INSURED_LABEL))
        }
      },
      endSlot = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.End,
          modifier = Modifier.padding(vertical = 4.dp),
        ) {
          val text = if (coInsuredList.isEmpty()) {
            stringResource(Res.string.CHANGE_ADDRESS_ONLY_YOU)
          } else {
            stringResource(Res.string.CHANGE_ADDRESS_YOU_PLUS, coInsuredList.size)
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
                  stringResource(Res.string.CONTRACT_COINSURED)
                },
              )

              HedvigText(
                text = coInsured.getSsnOrBirthDate(birthDateTimeFormatter)
                  ?: stringResource(Res.string.CONTRACT_NO_INFORMATION),
                color = HedvigTheme.colorScheme.textSecondary,
              )

              if (coInsured.activatesOn != null) {
                Spacer(Modifier.height(4.dp))
                HighlightLabel(
                  labelText = stringResource(
                    Res.string.CONTRACT_ADD_COINSURED_ACTIVE_FROM,
                    dateTimeFormatter.format(coInsured.activatesOn),
                  ),
                  size = Small,
                  color = Amber(MEDIUM),
                )
              }
              if (coInsured.terminatesOn != null) {
                Spacer(Modifier.height(4.dp))
                HighlightLabel(
                  labelText = stringResource(
                    Res.string.CONTRACT_ADD_COINSURED_ACTIVE_UNTIL,
                    dateTimeFormatter.format(coInsured.terminatesOn),
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
  }
}

@Composable
@HedvigPreview
@Preview(name = "long", device = "spec:width=1080px,height=5000px,dpi=440")
private fun PreviewYourInfoTab() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      YourInfoTab(
        contractId = "",
        coverageItems = listOf(
          DisplayItem("Address".repeat(4), Text("Bellmansgatan 19A")),
          DisplayItem("Postal code", Text("118 47".repeat(6))),
          DisplayItem("Type", Text("Homeowner")),
          DisplayItem("Size", Text("56 m2")),
        ),
        coInsured = listOf(
          CoInsured(
            ssn = "199101131093",
            birthDate = null,
            firstName = "Hugo",
            lastName = "Linder",
            activatesOn = LocalDate.fromEpochDays(300),
            terminatesOn = LocalDate.fromEpochDays(400),
            hasMissingInfo = false,
          ),
          CoInsured(
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
        allowChangeTier = true,
        allowRemovingAddon = true,
        onChangeTierClick = {},
        isDecommissioned = true,
        upcomingChangesInsuranceAgreement = InsuranceAgreement(
          activeFrom = LocalDate.fromEpochDays(200),
          activeTo = LocalDate.fromEpochDays(300),
          displayItems = listOf(
            DisplayItem(
              title = "test title",
              value = Text("test value"),
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
            CoInsured(
              ssn = "199101131093",
              birthDate = null,
              firstName = "Hugo",
              lastName = "Linder",
              activatesOn = LocalDate.fromEpochDays(300),
              terminatesOn = LocalDate.fromEpochDays(300),
              hasMissingInfo = false,
            ),
            CoInsured(
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
            discounts = emptyList(),
          ),
        ),
        onEditCoInsuredClick = {},
        onMissingInfoClick = {},
        onChangeAddressClick = {},
        onNavigateToNewConversation = {},
        openUrl = {},
        onCancelInsuranceClick = {},
        isTerminated = false,
        contractHolderDisplayName = "Hugo Linder",
        contractHolderSSN = "19910113-1093",
        priceToShow = UiMoney(89.0, UiCurrencyCode.SEK),
        showPriceInfoIcon = true,
        onInfoIconClick = {},
        existingAddons = listOf(
          ContractAddon(
            relatedContractId = ContractId(""),
            addonVariant = AddonVariant(
              termsVersion = "1",
              displayName = "displayName",
              product = "product",
              documents = emptyList(),
              perils = emptyList(),
            ),
            displayName = "DisplayName",
            description = "Description",
            status = ContractAddon.Status.ActiveFrom(LocalDate.fromEpochDays(100)),
            isUpgradable = false,
            isRemovable = false,
          ),
        ),
        availableAddons = listOf(
          AvailableAddon(ContractId("id"), "Available addon", "Description"),
        ),
        navigateToRemoveAddon = { _, _ -> },
        navigateToUpgradeAddon = { _, _ -> },
        navigateToAddAddon = {},
      )
    }
  }
}
