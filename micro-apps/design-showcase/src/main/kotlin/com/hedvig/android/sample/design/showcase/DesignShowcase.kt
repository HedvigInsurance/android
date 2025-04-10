package com.hedvig.android.sample.design.showcase

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.sample.design.showcase.DesignShowcase.ShowAccordion
import com.hedvig.android.sample.design.showcase.DesignShowcase.ShowBigCard
import com.hedvig.android.sample.design.showcase.DesignShowcase.ShowBottomSheet
import com.hedvig.android.sample.design.showcase.DesignShowcase.ShowButton
import com.hedvig.android.sample.design.showcase.DesignShowcase.ShowClickableList
import com.hedvig.android.sample.design.showcase.DesignShowcase.ShowDatePicker
import com.hedvig.android.sample.design.showcase.DesignShowcase.ShowDialog
import com.hedvig.android.sample.design.showcase.DesignShowcase.ShowDropdown
import com.hedvig.android.sample.design.showcase.DesignShowcase.ShowFreeTextOverlay
import com.hedvig.android.sample.design.showcase.DesignShowcase.ShowHighLight
import com.hedvig.android.sample.design.showcase.DesignShowcase.ShowIcons
import com.hedvig.android.sample.design.showcase.DesignShowcase.ShowMaterialBottomSheet
import com.hedvig.android.sample.design.showcase.DesignShowcase.ShowPerils
import com.hedvig.android.sample.design.showcase.DesignShowcase.ShowProgressBar
import com.hedvig.android.sample.design.showcase.DesignShowcase.ShowRadio
import com.hedvig.android.sample.design.showcase.DesignShowcase.ShowSnacks
import com.hedvig.android.sample.design.showcase.DesignShowcase.ShowStepper
import com.hedvig.android.sample.design.showcase.DesignShowcase.ShowTabs
import com.hedvig.android.sample.design.showcase.DesignShowcase.ShowTextField
import com.hedvig.android.sample.design.showcase.DesignShowcase.ShowToggle
import com.hedvig.android.sample.design.showcase.DesignShowcase.ShowTopBar
import com.hedvig.android.sample.design.showcase.accordion.AccordionShowCase
import com.hedvig.android.sample.design.showcase.bigcard.BigCardShowcase
import com.hedvig.android.sample.design.showcase.bottomSheet.ShowcaseBottomSheet
import com.hedvig.android.sample.design.showcase.button.ShowcaseButton
import com.hedvig.android.sample.design.showcase.datepicker.DatePickerShowcase
import com.hedvig.android.sample.design.showcase.dialog.DialogShowcase
import com.hedvig.android.sample.design.showcase.dropdown.DropdownShowcase
import com.hedvig.android.sample.design.showcase.freetext.FreeTextShowcase
import com.hedvig.android.sample.design.showcase.highlight.HighlightShowcase
import com.hedvig.android.sample.design.showcase.icons.ShowcaseIcons
import com.hedvig.android.sample.design.showcase.list.ClickableListShowcase
import com.hedvig.android.sample.design.showcase.materialBottomSheet.MaterialExperiment
import com.hedvig.android.sample.design.showcase.notifications.NotificationsSnackbarShowcase
import com.hedvig.android.sample.design.showcase.peril.PerilsShowcase
import com.hedvig.android.sample.design.showcase.progress.ProgressBarShowcase
import com.hedvig.android.sample.design.showcase.radio.ShowCaseRadioGroups
import com.hedvig.android.sample.design.showcase.stepper.StepperShowcase
import com.hedvig.android.sample.design.showcase.tabs.TabsShowcase
import com.hedvig.android.sample.design.showcase.textfield.ShowcaseTextField
import com.hedvig.android.sample.design.showcase.toggle.ToggleShowcase
import com.hedvig.android.sample.design.showcase.topbar.TopAppBarShowcase

@Composable
internal fun DesignShowcase(modifier: Modifier = Modifier) {
  val showcase = ShowMaterialBottomSheet
  Box(modifier) {
    when (showcase) {
      ShowIcons -> ShowcaseIcons()
      ShowButton -> ShowcaseButton()
      ShowBottomSheet -> ShowcaseBottomSheet()
      ShowStepper -> StepperShowcase()
      ShowTextField -> ShowcaseTextField()
      ShowRadio -> ShowCaseRadioGroups()
      ShowToggle -> ToggleShowcase()
      ShowDialog -> DialogShowcase()
      ShowDatePicker -> DatePickerShowcase()
      ShowFreeTextOverlay -> FreeTextShowcase()
      ShowHighLight -> HighlightShowcase()
      ShowProgressBar -> ProgressBarShowcase()
      ShowSnacks -> NotificationsSnackbarShowcase()
      ShowTabs -> TabsShowcase()
      ShowPerils -> PerilsShowcase()
      ShowAccordion -> AccordionShowCase()
      ShowClickableList -> ClickableListShowcase()
      ShowDropdown -> DropdownShowcase()
      ShowTopBar -> TopAppBarShowcase()
      ShowBigCard -> BigCardShowcase()
      ShowMaterialBottomSheet -> MaterialExperiment()
    }
  }
}

enum class DesignShowcase {
  ShowStepper,
  ShowIcons,
  ShowButton,
  ShowTextField,
  ShowBottomSheet,
  ShowRadio,
  ShowToggle,
  ShowDialog,
  ShowDatePicker,
  ShowFreeTextOverlay,
  ShowHighLight,
  ShowProgressBar,
  ShowPerils,
  ShowAccordion,
  ShowSnacks,
  ShowTabs,
  ShowClickableList,
  ShowTopBar,
  ShowDropdown,
  ShowBigCard,
  ShowMaterialBottomSheet,
}
