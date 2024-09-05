package com.hedvig.android.sample.design.showcase

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.sample.design.showcase.accordion.AccordionShowCase
import com.hedvig.android.sample.design.showcase.bottomSheet.ShowcaseBottomSheet
import com.hedvig.android.sample.design.showcase.button.ShowcaseButton
import com.hedvig.android.sample.design.showcase.datepicker.DatePickerShowcase
import com.hedvig.android.sample.design.showcase.dialog.DialogShowcase
import com.hedvig.android.sample.design.showcase.dropdown.DropdownShowcase
import com.hedvig.android.sample.design.showcase.freetext.FreeTextShowcase
import com.hedvig.android.sample.design.showcase.highlight.HighlightShowcase
import com.hedvig.android.sample.design.showcase.icons.ShowcaseIcons
import com.hedvig.android.sample.design.showcase.notifications.NotificationsSnackbarShowcase
import com.hedvig.android.sample.design.showcase.peril.PerilsShowcase
import com.hedvig.android.sample.design.showcase.progress.ProgressBarShowcase
import com.hedvig.android.sample.design.showcase.radio.ShowCaseRadioGroups
import com.hedvig.android.sample.design.showcase.stepper.StepperShowcase
import com.hedvig.android.sample.design.showcase.tabs.TabsShowcase
import com.hedvig.android.sample.design.showcase.textfield.ShowcaseTextField
import com.hedvig.android.sample.design.showcase.toggle.ToggleShowcase

@Composable
internal fun DesignShowcase(modifier: Modifier = Modifier) {
  Box(modifier) {
    if (showIcons) {
      ShowcaseIcons()
    } else if (showButton) {
      ShowcaseButton()
    } else if (showBottomSheet) {
      ShowcaseBottomSheet()
    } else if (showStepper) {
      StepperShowcase()
    } else if (showTextField) {
      ShowcaseTextField()
    } else if (showRadio) {
      ShowCaseRadioGroups()
    } else if (showToggle) {
      ToggleShowcase()
    } else if (showDialog) {
      DialogShowcase()
    } else if (showDatePicker) {
      DatePickerShowcase()
    } else if (showFreeTextOverlay) {
      FreeTextShowcase()
    } else if (showHighLight) {
      HighlightShowcase()
    } else if (showProgressBar) {
      ProgressBarShowcase()
    } else if (showSnacks) {
      NotificationsSnackbarShowcase()
    } else if (showTabs) {
      TabsShowcase()
    } else if (showPerils) {
      PerilsShowcase()
    } else if (showAccordion) {
      AccordionShowCase()
    } else if (showDropdown) {
      DropdownShowcase()
    }
  }
}

private val showStepper = false
private val showIcons = false
private val showButton = false
private val showTextField = false
private val showBottomSheet = false
private val showRadio = false
private val showToggle = false
private val showDialog = false
private val showDatePicker = false
private val showFreeTextOverlay = false
private val showHighLight = false
private val showProgressBar = false
private val showPerils = false
private val showAccordion = false
private val showSnacks = false
private val showTabs = false
private val showDropdown = true
