package com.hedvig.android.language

import androidx.annotation.StringRes
import com.hedvig.android.language.Language.DA_DK
import com.hedvig.android.language.Language.EN_DK
import com.hedvig.android.language.Language.EN_NO
import com.hedvig.android.language.Language.EN_SE
import com.hedvig.android.language.Language.NB_NO
import com.hedvig.android.language.Language.SV_SE

val Language.label: Int
  @StringRes
  get() = when (this) {
    SV_SE -> hedvig.resources.R.string.swedish
    EN_SE -> hedvig.resources.R.string.english_swedish
    NB_NO -> hedvig.resources.R.string.norwegian
    EN_NO -> hedvig.resources.R.string.english_norwegian
    DA_DK -> hedvig.resources.R.string.danish
    EN_DK -> hedvig.resources.R.string.english_danish
  }
