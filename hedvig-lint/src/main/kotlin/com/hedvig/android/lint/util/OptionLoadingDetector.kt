package com.hedvig.android.lint.util

import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Detector

/** A [Detector] that supports reading the given [options]. */
abstract class OptionLoadingDetector(vararg options: LintOption) : Detector() {
  private val options = options.toList()

  override fun beforeCheckRootProject(context: Context) {
    super.beforeCheckRootProject(context)
    val config = context.configuration
    options.forEach { it.load(config) }
  }
}
