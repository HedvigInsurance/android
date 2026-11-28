@file:OptIn(ExperimentalObjCEnum::class)

package com.hedvig.android.logger

import kotlin.experimental.ExperimentalObjCEnum
import kotlin.native.ObjCEnum

/**
 * An enum for log priorities that map to [android.util.Log] priority constants
 * without a direct import.
 */
@ObjCEnum
enum class LogPriority {
  VERBOSE,
  DEBUG,
  INFO,
  WARN,
  ERROR,
  ASSERT,
}

fun LogPriority.atMost(logPriority: LogPriority): LogPriority {
  return if (this.ordinal <= logPriority.ordinal) this else logPriority
}
