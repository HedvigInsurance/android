package com.hedvig.app.testdata.feature.loggedin.builders

import com.hedvig.android.owldroid.fragment.IconVariantsFragment
import com.hedvig.android.owldroid.graphql.WhatsNewQuery

data class WhatsNewDataBuilder(
    val pages: List<WhatsNewQuery.New> = listOf(WhatsNewPageBuilder().build())
) {
    fun build() = WhatsNewQuery.Data(pages)
}

data class WhatsNewPageBuilder(
    val lightUrl: String = "/app-content-service/welcome_welcome.svg",
    val darkUrl: String = "/app-content-service/welcome_welcome_dark.svg",
    val paragraph: String = "test",
    val title: String = "test"
) {
    fun build() = WhatsNewQuery.New(
        illustration = WhatsNewQuery.Illustration(
            variants = WhatsNewQuery.Variants(
                fragments = WhatsNewQuery.Variants.Fragments(
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
