package com.hedvig.android.sample.design.showcase.radio

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.IconResource
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupSize.Large
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupSize.Medium
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOptionData
import com.hedvig.android.design.system.hedvig.RadioOptionGroupData.RadioOptionGroupDataWithIcon
import com.hedvig.android.design.system.hedvig.RadioOptionGroupData.RadioOptionGroupDataWithLabel
import com.hedvig.android.design.system.hedvig.icon.Document
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.ID

@Composable
internal fun ShowCaseRadioGroups(modifier: Modifier = Modifier) {
  val shortList1 = listOf(
    RadioOptionData(
      id = "1",
      optionText = "Yes",
      chosenState = Chosen,
    ),
    RadioOptionData(
      id = "2",
      optionText = "No",
      chosenState = NotChosen,
    ),
  )

  val shortList2 = listOf(
    RadioOptionData(
      id = "1",
      optionText = "Yes",
      chosenState = NotChosen,
    ),
    RadioOptionData(
      id = "2",
      optionText = "No",
      chosenState = Chosen,
    ),
  )

  val shortListLabel1 = listOf(
    RadioOptionGroupDataWithLabel(shortList1[0], "Some label 1"),
    RadioOptionGroupDataWithLabel(shortList1[1], "Some label 2"),
  )
  val shortListLabel2 = listOf(
    RadioOptionGroupDataWithLabel(shortList2[0], "Some label 1"),
    RadioOptionGroupDataWithLabel(shortList2[1], "Some label 2"),
  )
  val shortListIcon1 = listOf(
    RadioOptionGroupDataWithIcon(shortList1[0], IconResource.Vector(HedvigIcons.ID)),
    RadioOptionGroupDataWithIcon(shortList1[1], IconResource.Vector(HedvigIcons.Document)),
  )
  val shortListIcon2 = listOf(
    RadioOptionGroupDataWithIcon(shortList2[0], IconResource.Vector(HedvigIcons.ID)),
    RadioOptionGroupDataWithIcon(shortList2[1], IconResource.Vector(HedvigIcons.Document)),
  )
  var list1 by remember { mutableStateOf(shortList1) }
  var list2 by remember { mutableStateOf(shortList1) }
  var listLabel by remember { mutableStateOf(shortListLabel1) }
  var listIcon by remember { mutableStateOf(shortListIcon1) }
  Column(
    modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
  ) {
    Spacer(Modifier.height(48.dp))
    RadioGroup(
      onOptionClick = { optionId ->
        list1 = if (optionId == "2") {
          shortList2
        } else {
          shortList1
        }
      },
      radioGroupSize = Medium,
      radioGroupStyle = RadioGroupStyle.Horizontal(list1),
    )
    Spacer(Modifier.height(8.dp))
    RadioGroup(
      onOptionClick = { optionId ->
        list2 = if (optionId == "2") {
          shortList2
        } else {
          shortList1
        }
      },
      radioGroupSize = Large,
      radioGroupStyle = RadioGroupStyle.HorizontalWithLabel("Your decision", list2),
    )

    Spacer(Modifier.height(8.dp))
    RadioGroup(
      onOptionClick = { optionId ->
        list2 = if (optionId == "2") {
          shortList2
        } else {
          shortList1
        }
      },
      radioGroupSize = Medium,
      radioGroupStyle = RadioGroupStyle.HorizontalWithLabel("Your decision", list2),
    )
    Spacer(Modifier.height(8.dp))
    RadioGroup(
      onOptionClick = { optionId ->
        listLabel = if (optionId == "2") {
          shortListLabel2
        } else {
          shortListLabel1
        }
      },
      radioGroupSize = Medium,
      radioGroupStyle = RadioGroupStyle.Vertical.Label(listLabel),
    )
    Spacer(Modifier.height(8.dp))
    RadioGroup(
      onOptionClick = { optionId ->
        listIcon = if (optionId == "2") {
          shortListIcon2
        } else {
          shortListIcon1
        }
      },
      radioGroupSize = Medium,
      radioGroupStyle = RadioGroupStyle.VerticalWithGroupLabel.Icon(
        "Your decision",
        listIcon,
      ),
    )
    Spacer(Modifier.height(8.dp))
  }
}
