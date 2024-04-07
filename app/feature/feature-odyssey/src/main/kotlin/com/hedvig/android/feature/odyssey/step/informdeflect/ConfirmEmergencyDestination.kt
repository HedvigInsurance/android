package com.hedvig.android.feature.odyssey.step.informdeflect

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.HedvigPreviewLayout
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.ui.HedvigChip
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.scaffold.ClaimFlowScaffold
import com.hedvig.android.core.ui.text.WarningTextWithIcon
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.EmergencyOption
import hedvig.resources.R
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

@Composable
internal fun SharedTransitionScope.ConfirmEmergencyDestination(
  animatedContentScope: AnimatedContentScope,
  viewModel: ConfirmEmergencyViewModel,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  windowSizeClass: WindowSizeClass,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val nextStep = uiState.nextStep
  LaunchedEffect(nextStep) {
    if (nextStep != null) {
      navigateToNextStep(nextStep)
    }
  }
  ConfirmEmergencyScreen(
    animatedContentScope = animatedContentScope,
    uiState = uiState,
    windowSizeClass = windowSizeClass,
    onSubmit = viewModel::submitIsUrgentEmergency,
    navigateUp = navigateUp,
    onSelectOption = viewModel::selectOption,
    closeClaimFlow = closeClaimFlow,
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SharedTransitionScope.ConfirmEmergencyScreen(
  animatedContentScope: AnimatedContentScope,
  uiState: ConfirmEmergencyUiState,
  windowSizeClass: WindowSizeClass,
  navigateUp: () -> Unit,
  onSelectOption: (EmergencyOption) -> Unit,
  closeClaimFlow: () -> Unit,
  onSubmit: () -> Unit,
) {
  ClaimFlowScaffold(
    animatedContentScope = animatedContentScope,
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(16.dp))
    Text(
      text = uiState.title,
      style = MaterialTheme.typography.headlineMedium,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
    Spacer(Modifier.weight(1f))
    AnimatedVisibility(
      visible = uiState.haveTriedContinuingWithoutSelection,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      Column {
        WarningTextWithIcon(
          modifier = Modifier
              .padding(horizontal = 16.dp)
              .fillMaxWidth()
              .wrapContentWidth(),
          text = stringResource(R.string.CLAIMS_SELECT_CATEGORY),
        )
        Spacer(Modifier.height(16.dp))
      }
    }
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      maxItemsInEachRow = 2,
      modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth(),
    ) {
      for (item in uiState.options) {
        key(item) {
          val isPreview = LocalInspectionMode.current
          val showChipAnimatable = remember {
            Animatable(if (isPreview) 1.0f else 0.0f)
          }
          LaunchedEffect(Unit) {
            delay(Random.nextDouble(0.3, 0.6).seconds)
            showChipAnimatable.animateTo(
              1.0f,
              animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow,
              ),
            )
          }
          HedvigChip(
            item = item,
            itemDisplayName = EmergencyOption::displayName,
            isSelected = item == uiState.selectedOption,
            onItemClick = onSelectOption,
            showChipAnimatable = showChipAnimatable,
            modifier = Modifier.weight(1f),
          )
        }
      }
    }
    Spacer(Modifier.height(8.dp))
    HedvigContainedButton(
      text = stringResource(id = R.string.general_continue_button),
      isLoading = uiState.isLoading,
      onClick = onSubmit,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@Composable
@HedvigPreview
private fun ConfirmEmergencyScreenPreview() {
  HedvigPreviewLayout { animatedContentScope ->
    ConfirmEmergencyScreen(
      animatedContentScope = animatedContentScope,
      uiState = ConfirmEmergencyUiState(
        "Är du på en resa och behöver akut vård eller assistans?",
        options = listOf(
          EmergencyOption(
            displayName = "Yes",
            value = false,
          ),
          EmergencyOption(
            displayName = "No",
            value = true,
          ),
        ),
        selectedOption = null,
        isLoading = false,
      ),
      windowSizeClass = WindowSizeClass.calculateForPreview(),
      navigateUp = {},
      closeClaimFlow = {},
      onSubmit = {},
      onSelectOption = {},
    )
  }
}
