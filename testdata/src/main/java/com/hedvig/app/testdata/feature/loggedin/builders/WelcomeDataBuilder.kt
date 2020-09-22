package com.hedvig.app.testdata.feature.loggedin.builders

import com.hedvig.android.owldroid.fragment.IconVariantsFragment
import com.hedvig.android.owldroid.graphql.WelcomeQuery

data class WelcomeDataBuilder(
    val pages: List<WelcomeQuery.Welcome> = listOf(
        WelcomePageBuilder().build()
    )
) {
    fun build() = WelcomeQuery.Data(pages)
}

data class WelcomePageBuilder(
    val lightUrl: String = "/app-content-service/welcome_welcome.svg",
    val darkUrl: String = "/app-content-service/welcome_welcome_dark.svg",
    val paragraph: String = "test",
    val title: String = "test"
) {
    fun build() = WelcomeQuery.Welcome(
        illustration = WelcomeQuery.Illustration(
            variants = WelcomeQuery.Variants(
                fragments = WelcomeQuery.Variants.Fragments(
                    IconVariantsFragment(
                        dark = IconVariantsFragment.Dark(
                            svgUrl = darkUrl
                        ),
                        light = IconVariantsFragment.Light(
                            svgUrl = lightUrl
                        )
                    )
                )
            )
        ),
        paragraph = paragraph,
        title = title
    )
}
