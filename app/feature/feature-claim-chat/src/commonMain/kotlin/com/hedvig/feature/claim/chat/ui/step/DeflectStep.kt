package com.hedvig.feature.claim.chat.ui.step

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.data.StepId

@Composable
internal fun DeflectStep(
  stepId: StepId,
  buttonText: String,
  deflect: StepContent.Deflect,
  navigateToDeflect: (StepId, StepContent.Deflect) -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigButton(
    modifier = modifier,
    text = buttonText,
    onClick = dropUnlessResumed { navigateToDeflect(stepId, deflect) },
    enabled = true,
  )
}
