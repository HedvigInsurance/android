package com.hedvig.app.feature.payment

import android.view.View
import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import com.hedvig.app.feature.adyen.AdyenConnectPayinActivity
import com.hedvig.app.feature.profile.ui.payment.PaymentHistoryActivity
import com.hedvig.app.feature.referrals.ui.redeemcode.RedeemCodeDialog
import com.hedvig.app.feature.trustly.TrustlyConnectPayinActivity
import com.kaspersky.kaspresso.screens.KScreen
import org.hamcrest.Matcher

class PaymentScreen : Screen<PaymentScreen>() {
    val recycler = KRecyclerView({ withId(R.id.recycler) }, {
        itemType(::FailedPayments)
        itemType(::NextPayment)
        itemType(::Campaign)
        itemType(::ConnectPayin)
        itemType(::Charge)
        itemType(::PaymentHistoryLink)
        itemType(::TrustlyPayinDetails)
        itemType(::AdyenPayinDetails)
        itemType(::Link)
    })

    class FailedPayments(parent: Matcher<View>) : KRecyclerItem<FailedPayments>(parent) {
        val paragraph = KTextView(parent) { withId(R.id.paragraph) }
    }

    class NextPayment(parent: Matcher<View>) : KRecyclerItem<NextPayment>(parent) {
        val discount = KTextView(parent) { withId(R.id.discount) }
        val gross = KTextView(parent) { withId(R.id.gross) }
        val net = KTextView(parent) { withId(R.id.amount) }
        val paymentDate = KTextView(parent) { withId(R.id.date) }
    }

    class Campaign(parent: Matcher<View>) : KRecyclerItem<Campaign>(parent) {
        val owner = KTextView(parent) { withId(R.id.campaignInformationFieldOne) }
        val lastFreeDay = KTextView(parent) { withId(R.id.lastFreeDay) }
    }

    class ConnectPayin(parent: Matcher<View>) : KRecyclerItem<ConnectPayin>(parent) {
        val connect = KButton(parent) { withId(R.id.connect) }
    }

    class Charge(parent: Matcher<View>) : KRecyclerItem<Charge>(parent) {
        val amount = KTextView(parent) { withId(R.id.amount) }
        val date = KTextView(parent) { withId(R.id.date) }
    }

    class PaymentHistoryLink(parent: Matcher<View>) : KRecyclerItem<PaymentHistoryLink>(parent)

    class TrustlyPayinDetails(parent: Matcher<View>) : KRecyclerItem<TrustlyPayinDetails>(parent) {
        val bank = KTextView(parent) { withId(R.id.bank) }
        val accountNumber = KTextView(parent) { withId(R.id.accountNumber) }
        val pending = KTextView(parent) { withId(R.id.pending) }
    }

    class AdyenPayinDetails(parent: Matcher<View>) : KRecyclerItem<AdyenPayinDetails>(parent) {
        val cardType = KTextView(parent) { withId(R.id.cardType) }
        val maskedCardNumber = KTextView(parent) { withId(R.id.maskedCardNumber) }
    }

    class Link(parent: Matcher<View>) : KRecyclerItem<Link>(parent) {
        val button = KTextView { withMatcher(parent) }
    }

    val trustlyConnectPayin = KIntent { hasComponent(TrustlyConnectPayinActivity::class.java.name) }
    val adyenConnectPayin = KIntent { hasComponent(AdyenConnectPayinActivity::class.java.name) }
    val paymentHistory = KIntent { hasComponent(PaymentHistoryActivity::class.java.name) }
}

object RedeemCode : KScreen<RedeemCode>() {
    override val layoutId = R.layout.promotion_code_dialog
    override val viewClass = RedeemCodeDialog::class.java

    val redeem = KButton { withId(R.id.bottomSheetAddPromotionCodeButton) }
}
