package com.hedvig.app.testdata.feature.marketpicker.builders

import giraffe.GeoQuery

data class GeoDataBuilder(
  val countryISOCode: String = "SE",
) {
  fun build() = GeoQuery.Data(
    geo = GeoQuery.Geo(
      countryISOCode = countryISOCode,
    ),
  )
}
