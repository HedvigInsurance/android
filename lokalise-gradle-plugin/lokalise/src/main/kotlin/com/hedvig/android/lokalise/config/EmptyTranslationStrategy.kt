package com.hedvig.android.lokalise.config

enum class EmptyTranslationStrategy(val value: String) {
  REPLACE_WITH_BASE_LANGUAGE("base"),
  KEEP_EMPTY("empty"),
  SKIP("skip"),
  ;
}
