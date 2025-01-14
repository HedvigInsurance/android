package com.hedvig.android.language

import androidx.annotation.StringRes
import com.hedvig.android.language.Language.DA_DK
import com.hedvig.android.language.Language.EN_DK
import com.hedvig.android.language.Language.EN_NO
import com.hedvig.android.language.Language.EN_SE
import com.hedvig.android.language.Language.NB_NO
import com.hedvig.android.language.Language.SV_SE
import hedvig.resources.R

val Language.label: Int
  @StringRes
  get() = when (this) {
    SV_SE -> R.string.swedish
    EN_SE -> R.string.english_swedish
    NB_NO -> R.string.norwegian
    EN_NO -> R.string.english_norwegian
    DA_DK -> R.string.danish
    EN_DK -> R.string.english_danish
    Language.EN_GLOBAL -> R.string.english_swedish
    Language.SV_GLOBAL -> R.string.swedish
  }
