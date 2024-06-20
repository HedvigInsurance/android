package com.hedvig.android.sample.design.showcase.radio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioOptionChosenState.Chosen
import com.hedvig.android.design.system.hedvig.RadioOptionChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.RadioOptionData

@Composable
internal fun RadioGroups() {
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
  var list by remember { mutableStateOf(shortList1) }
  RadioGroup(
    data = list,
    onOptionClick = { _ ->
      list = shortList2
    },
  )
}

