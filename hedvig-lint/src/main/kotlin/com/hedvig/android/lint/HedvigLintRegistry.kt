package com.hedvig.android.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

class HedvigLintRegistry : IssueRegistry() {
  override val issues: List<Issue> = listOf(
    Material2Detector.ISSUE,
  )

  override val api: Int = CURRENT_API

  // see com.android.tools.lint.detector.api.Api / ApiKt
  override val minApi: Int = 14

  override val vendor: Vendor = Vendor(
    vendorName = "Hedvig lints",
    feedbackUrl = "https://hedviginsurance.slack.com/archives/C03HT2JRDPG",
    contact = "https://hedviginsurance.slack.com/archives/C03HT2JRDPG",
  )
}
