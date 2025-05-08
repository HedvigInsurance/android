package com.hedvig.android.language

import androidx.annotation.StringRes
import com.hedvig.android.language.Language.EN_SE
import com.hedvig.android.language.Language.SV_SE
import hedvig.resources.R

val Language.label: Int
  @StringRes
  get() = when (this) {
    SV_SE -> R.string.swedish
    EN_SE -> R.string.english_swedish
  }
