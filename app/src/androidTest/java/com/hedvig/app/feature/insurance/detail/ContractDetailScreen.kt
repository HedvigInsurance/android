package com.hedvig.app.feature.insurance.detail

import android.content.Intent
import android.view.View
import com.hedvig.app.R
import com.hedvig.app.feature.documents.DocumentRecyclerItem
import com.hedvig.app.feature.insurablelimits.InsurableLimitRecyclerItem
import com.hedvig.app.feature.perils.PerilRecyclerItem
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.intent.KIntent
import io.github.kakaocup.kakao.pager2.KViewPager2
import io.github.kakaocup.kakao.pager2.KViewPagerItem
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

class ContractDetailScreen : Screen<ContractDetailScreen>() {
    val retry = KButton { withId(R.id.retry) }

    val tabContent = KViewPager2(
        { withId(R.id.tabContent) },
        {
            itemType(::YourInfoTab)
            itemType(::CoverageTab)
            itemType(::DocumentsTab)
        }
    )

    class CoverageTab(parent: Matcher<View>) : KViewPagerItem<CoverageTab>(parent) {
        val recycler = KRecyclerView(
            parent, { withId(R.id.recycler) },
            {
                itemType(::Header)
                itemType(::PerilRecyclerItem)
                itemType(::InsurableLimitRecyclerItem)
            }
        )

        class Header(parent: Matcher<View>) : KRecyclerItem<Header>(parent) {
            val text = KTextView { withMatcher(parent) }
        }

        class PerilBottomSheetScreen : Screen<PerilBottomSheetScreen>() {
            val sheetRecycler = KRecyclerView(
                { withId(R.id.recycler) },
                {
                    itemType(::Title)
                }
            )
            val chevron = KImageView { withId(R.id.chevron) }

            class Title(parent: Matcher<View>) : KRecyclerItem<Header>(parent) {
                val title = KTextView { withId(R.id.title) }
            }
        }
    }

    class YourInfoTab(parent: Matcher<View>) : KViewPagerItem<YourInfoTab>(parent) {
        val recycler = KRecyclerView(
            parent, { withId(R.id.recycler) },
            {
                itemType(::CoInsured)
                itemType(::Home)
                itemType(::ChangeAddressButton)
            }
        )

        class CoInsured(parent: Matcher<View>) : KRecyclerItem<CoInsured>(parent) {
            val title = KTextView(parent) { withId(R.id.title) }
            val coInsuredLabel = KTextView(parent) { withId(R.id.coinsured_label) }
            val coInsured = KTextView(parent) { withId(R.id.coinsured_amount) }
        }

        class Home(parent: Matcher<View>) : KRecyclerItem<Home>(parent) {
            val title = KTextView(parent) { withId(R.id.title) }
            val addressLabel = KTextView(parent) { withId(R.id.coinsured_label) }
            val address = KTextView(parent) { withId(R.id.address_value) }
            val postCodeLabel = KTextView(parent) { withId(R.id.postcode_label) }
            val postCode = KTextView(parent) { withId(R.id.postcode_value) }
            val typeLabel = KTextView(parent) { withId(R.id.type_label) }
            val type = KTextView(parent) { withId(R.id.type_value) }
            val sizeLabel = KTextView(parent) { withId(R.id.size_label) }
            val size = KTextView(parent) { withId(R.id.size_value) }
        }

        class ChangeAddressButton(parent: Matcher<View>) : KRecyclerItem<ChangeAddressButton>(parent) {
            val button = KButton { withMatcher(parent) }
        }
    }

    class DocumentsTab(parent: Matcher<View>) : KViewPagerItem<DocumentsTab>(parent) {
        val recycler = KRecyclerView(
            parent, { withId(R.id.recycler) },
            {
                itemType(::DocumentRecyclerItem)
            }
        )

        val agreementUrl = KIntent {
            hasAction(Intent.ACTION_VIEW)
            hasData("https://www.example.com")
        }
    }
}
