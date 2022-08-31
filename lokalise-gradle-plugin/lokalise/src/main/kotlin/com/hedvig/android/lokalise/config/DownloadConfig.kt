package com.hedvig.android.lokalise.config

class DownloadConfig(
  val stringsOrder: StringsOrder = StringsOrder.A_Z,
  val emptyTranslationStrategy: EmptyTranslationStrategy = EmptyTranslationStrategy.REPLACE_WITH_BASE_LANGUAGE,
)
