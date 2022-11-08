package com.hedvig.app.feature.payment

import android.view.View
import com.hedvig.app.R
import com.hedvig.app.feature.adyen.payin.AdyenConnectPayinActivity
import com.hedvig.app.feature.adyen.payout.AdyenConnectPayoutActivity
import com.hedvig.app.feature.profile.ui.payment.PaymentHistoryActivity
import com.hedvig.app.feature.referrals.ui.redeemcode.RedeemCodeBottomSheet
import com.hedvig.app.feature.trustly.TrustlyConnectPayinActivity
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.intent.KIntent
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

class PaymentScreen : Screen<PaymentScreen>() {
  val recycler = KRecyclerView(
    { withId(R.id.recycler) },
    {
      itemType(::FailedPayments)
      itemType(::NextPayment)
      itemType(::Campaign)
      itemType(::ConnectPayin)
      itemType(::Charge)
      itemType(::PaymentHistoryLink)
      itemType(::TrustlyPayinDetails)
      itemType(::AdyenPayinDetails)
      itemType(::AdyenPayoutDetails)
      itemType(::AdyenPayoutParagraph)
      itemType(::Link)
    },
  )

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

  class AdyenPayoutDetails(parent: Matcher<View>) : KRecyclerItem<AdyenPayoutDetails>(parent) {
    val status = KTextView { withMatcher(parent) }
  }

  class AdyenPayoutParagraph(parent: Matcher<View>) : KRecyclerItem<AdyenPayoutParagraph>(parent) {
    val text = KTextView { withMatcher(parent) }
  }

  class Link(parent: Matcher<View>) : KRecyclerItem<Link>(parent) {
    val button = KTextView { withMatcher(parent) }
  }

  val trustlyConnectPayin = KIntent { hasComponent(TrustlyConnectPayinActivity::class.java.name) }
  val adyenConnectPayin = KIntent { hasComponent(AdyenConnectPayinActivity::class.java.name) }
  val paymentHistory = KIntent { hasComponent(PaymentHistoryActivity::class.java.name) }
  val adyenConnectPayout =
    KIntent { hasComponent(AdyenConnectPayoutActivity::class.java.name) }
}

object RedeemCode : KScreen<RedeemCode>() {
  override val layoutId = R.layout.promotion_code_dialog
  override val viewClass = RedeemCodeBottomSheet::class.java

  val redeem = KButton { withId(R.id.bottomSheetAddPromotionCodeButton) }
}
