package com.hedvig.app.feature.offer.screen

import com.hedvig.app.R
import com.hedvig.app.feature.documents.DocumentRecyclerItem
import com.hedvig.app.feature.insurablelimits.InsurableLimitRecyclerItem
import com.hedvig.app.feature.offer.quotedetail.QuoteDetailActivity
import com.hedvig.app.feature.perils.PerilRecyclerItem
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.toolbar.KToolbar

object QuoteDetailScreen : KScreen<QuoteDetailScreen>() {
    override val layoutId = R.layout.quote_detail_activity
    override val viewClass = QuoteDetailActivity::class.java

    val toolbar = KToolbar { withId(R.id.toolbar) }

    val recycler = KRecyclerView(
        { withId(R.id.recycler) },
        {
            itemType(::PerilRecyclerItem)
            itemType(::InsurableLimitRecyclerItem)
            itemType(::DocumentRecyclerItem)
        }
    )
}
