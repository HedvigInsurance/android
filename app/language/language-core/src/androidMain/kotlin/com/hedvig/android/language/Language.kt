package com.hedvig.android.language

import com.hedvig.android.language.Language.EN_SE
import com.hedvig.android.language.Language.SV_SE
import hedvig.resources.Res
import hedvig.resources.english_swedish
import hedvig.resources.swedish
import org.jetbrains.compose.resources.StringResource

val Language.label: StringResource
  get() = when (this) {
    SV_SE -> Res.string.swedish
    EN_SE -> Res.string.english_swedish
  }
