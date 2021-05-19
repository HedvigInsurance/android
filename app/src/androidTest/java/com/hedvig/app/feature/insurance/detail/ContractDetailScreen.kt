package com.hedvig.app.feature.insurance.detail

import android.content.Intent
import android.view.View
import com.agoda.kakao.image.KImageView
import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.pager2.KViewPager2
import com.agoda.kakao.pager2.KViewPagerItem
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
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
                itemType(::Peril)
                itemType(::Row)
            }
        )

        class Header(parent: Matcher<View>) : KRecyclerItem<Header>(parent) {
            val text = KTextView { withMatcher(parent) }
        }

        class Peril(parent: Matcher<View>) : KRecyclerItem<Peril>(parent)

        class Row(parent: Matcher<View>) : KRecyclerItem<Row>(parent) {
            val label = KTextView(parent) { withId(R.id.label) }
            val content = KTextView(parent) { withId(R.id.content) }
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
                itemType(::Button)
            }
        )

        val agreementUrl = KIntent {
            hasAction(Intent.ACTION_VIEW)
            hasData("https://www.example.com")
        }

        class Button(parent: Matcher<View>) : KRecyclerItem<Button>(parent) {
            val button = KButton { withMatcher(parent) }
        }
    }
}
