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
import com.hedvig.android.design.system.hedvig.OptionData
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupSize.Large
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupSize.Medium
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupStyle

@Composable
internal fun ShowCaseRadioGroups(modifier: Modifier = Modifier) {
  val shortList1 = listOf(
    OptionData(
      id = "1",
      optionText = "Yes",
      chosenState = Chosen,
    ),
    OptionData(
      id = "2",
      optionText = "No",
      chosenState = NotChosen,
    ),
  )

  val shortList2 = listOf(
    OptionData(
      id = "1",
      optionText = "Yes",
      chosenState = NotChosen,
    ),
    OptionData(
      id = "2",
      optionText = "No",
      chosenState = Chosen,
    ),
  )
  var list1 by remember { mutableStateOf(shortList1) }
  var list2 by remember { mutableStateOf(shortList1) }
  var list3 by remember { mutableStateOf(shortList1) }
  var list4 by remember { mutableStateOf(shortList1) }
  Column(
    modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
  ) {
    Spacer(Modifier.height(48.dp))
    RadioGroup(
      data = list1,
      onOptionClick = { optionId ->
        list1 = if (optionId == "2") {
          shortList2
        } else {
          shortList1
        }
      },
      radioGroupSize = Medium,
      radioGroupStyle = RadioGroupStyle.Horizontal,
    )
    Spacer(Modifier.height(8.dp))
    RadioGroup(
      data = list2,
      onOptionClick = { optionId ->
        list2 = if (optionId == "2") {
          shortList2
        } else {
          shortList1
        }
      },
      radioGroupSize = Large,
      radioGroupStyle = RadioGroupStyle.HorizontalWithLabel("Your decision"),
    )

    Spacer(Modifier.height(8.dp))
    RadioGroup(
      data = list2,
      onOptionClick = { optionId ->
        list2 = if (optionId == "2") {
          shortList2
        } else {
          shortList1
        }
      },
      radioGroupSize = Medium,
      radioGroupStyle = RadioGroupStyle.HorizontalWithLabel("Your decision"),
    )
    Spacer(Modifier.height(8.dp))
    RadioGroup(
      data = list3,
      onOptionClick = { optionId ->
        list3 = if (optionId == "2") {
          shortList2
        } else {
          shortList1
        }
      },
      radioGroupSize = Medium,
      radioGroupStyle = RadioGroupStyle.Vertical.Label,
    )
    Spacer(Modifier.height(8.dp))
    RadioGroup(
      data = list4,
      onOptionClick = { _ ->
        list4 = if (list4 == shortList1) {
          shortList2
        } else {
          shortList1
        }
      },
      radioGroupSize = Medium,
      radioGroupStyle = RadioGroupStyle.VerticalWithGroupLabel.Default(
        "Your decision",
      ),
    )
    Spacer(Modifier.height(8.dp))
  }
}
