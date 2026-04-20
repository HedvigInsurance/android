package com.hedvig.android.design.system.hedvig.datepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.hedvig.android.core.locale.CommonLocale

@Composable
@ReadOnlyComposable
expect fun getLocale(): CommonLocale
