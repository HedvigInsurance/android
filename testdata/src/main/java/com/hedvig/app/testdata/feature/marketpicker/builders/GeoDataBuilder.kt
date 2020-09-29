package com.hedvig.app.testdata.feature.marketpicker.builders

import com.hedvig.android.owldroid.graphql.GeoQuery

data class GeoDataBuilder(
    val countryISOCode: String = "SE"
) {
    fun build() = GeoQuery.Data(
        geo = GeoQuery.Geo(
            countryISOCode = countryISOCode
        )
    )
}
