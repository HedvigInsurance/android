package com.hedvig.feature.claim.chat.data

import arrow.core.raise.either
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.core.locale.previewCommonLocale
import kotlin.test.Test
import octopus.fragment.ClaimIntentFragment
import octopus.fragment.InformationFragment
import octopus.type.ClaimIntentStepContentInformationSeverity

class ClaimIntentInformationMappingTest {
  @Test
  fun `an information step content maps to the information step content`() {
    val result = either {
      informationClaimIntentFragment(ClaimIntentStepContentInformationSeverity.CRITICAL)
        .toClaimIntent(previewCommonLocale)
    }

    val stepContent = result.getOrNull()?.next?.step?.claimIntentStep?.stepContent
    assertThat(stepContent).isEqualTo(
      StepContent.Information(
        notice = "Seek emergency accommodation.",
        severity = StepContent.Information.Severity.CRITICAL,
        buttonTitle = "I understand",
      ),
    )
  }

  @Test
  fun `an unknown information severity degrades to INFO instead of failing the flow`() {
    val result = either {
      informationClaimIntentFragment(ClaimIntentStepContentInformationSeverity.UNKNOWN__)
        .toClaimIntent(previewCommonLocale)
    }

    val stepContent = result.getOrNull()?.next?.step?.claimIntentStep?.stepContent
    assertThat((stepContent as StepContent.Information).severity)
      .isEqualTo(StepContent.Information.Severity.INFO)
  }

  private fun informationClaimIntentFragment(
    informationSeverity: ClaimIntentStepContentInformationSeverity,
  ): ClaimIntentFragment = object : ClaimIntentFragment {
    override val id = "intent-id"
    override val progress = 0.8
    override val createdClaim = null
    override val currentStep = object : ClaimIntentFragment.CurrentStep {
      override val id = "step-id"
      override val text = null
      override val hint = null
      override val isRegrettable = false
      override val content = object : ClaimIntentFragment.CurrentStep.Content, InformationFragment {
        override val __typename = "ClaimIntentStepContentInformation"
        override val notice = "Seek emergency accommodation."
        override val severity = informationSeverity
        override val buttonTitle = "I understand"
      }
    }
  }
}
