package com.hedvig.android.core.designsystem.theme

import android.content.res.Resources
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Colors
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.google.accompanist.themeadapter.material.createMdcTheme
import java.lang.reflect.Method

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HedvigTheme(
  colorOverrides: ((Colors) -> Colors)? = null,
  content: @Composable () -> Unit,
) {
  val context = LocalContext.current
  val key = context.theme.key ?: context.theme
  val layoutDirection = LocalLayoutDirection.current
  val themeParameters = remember(key) {
    createMdcTheme(
      context = context,
      layoutDirection = layoutDirection,
      setDefaultFontFamily = true,
    )
  }
  val colors = themeParameters.colors ?: MaterialTheme.colors
  Box(
    modifier = Modifier.semantics {
      testTagsAsResourceId = true
    },
  ) {
    MaterialTheme(
      colors = colorOverrides?.invoke(colors) ?: colors,
      typography = themeParameters.typography ?: MaterialTheme.typography,
      shapes = themeParameters.shapes ?: MaterialTheme.shapes,
    ) {
      CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colors.onBackground,
        content = content,
      )
    }
  }
}

/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This is gross, but we need a way to check for theme equality. Theme does not implement
 * `equals()` or `hashCode()`, but it does have a hidden method called `getKey()`.
 *
 * The cost of this reflective invoke is a lot cheaper than the full theme read which can
 * happen on each re-composition.
 */
private inline val Resources.Theme.key: Any?
  get() {
    if (!sThemeGetKeyMethodFetched) {
      try {
        @Suppress("SoonBlockedPrivateApi")
        sThemeGetKeyMethod = Resources.Theme::class.java.getDeclaredMethod("getKey")
          .apply { isAccessible = true }
      } catch (e: ReflectiveOperationException) {
        // Failed to retrieve Theme.getKey method
      }
      sThemeGetKeyMethodFetched = true
    }
    if (sThemeGetKeyMethod != null) {
      return try {
        sThemeGetKeyMethod?.invoke(this)
      } catch (e: ReflectiveOperationException) {
        // Failed to invoke Theme.getKey()
      }
    }
    return null
  }

private var sThemeGetKeyMethodFetched = false
private var sThemeGetKeyMethod: Method? = null
